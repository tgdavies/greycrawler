package org.kablambda.greycrawler.components;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kablambda.greycrawler.model.Greyhound;
import org.kablambda.greycrawler.model.Sex;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class GapNSWCrawler {

    public static void main(String[] args) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        new GapNSWCrawler().crawl();
    }

    public void crawl() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        List<Greyhound> dogs = StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Document>() {
            private int page = 1;
            private Document nextDoc = null;
            private boolean end = false;
            @Override
            public boolean hasNext() {
                if (!end && nextDoc == null) {
                    try {
                        delay();
                        nextDoc = Jsoup.connect("https://www.gapnsw.com.au/our-greyhounds?page=" + page).get();
                        page++;
                    } catch (HttpStatusException s) {
                        if (s.getStatusCode() == 404) {
                            nextDoc = null;
                            end = true;
                        } else {
                            throw new RuntimeException("Error fetching page " + page, s);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error fetching page " + page, e);
                    }
                }
                return !end;
            }

            @Override
            public Document next() {
                if (end) {
                    throw new NoSuchElementException();
                }
                Document d = nextDoc;
                nextDoc = null;
                return d;
            }
        }, Spliterator.IMMUTABLE), false).flatMap(
                doc -> {
                    return doc.getElementsByClass("dog-profile-card").stream();
                }
        ).map(e -> {
            String urlName = StringUtils.substringBetween(e.attr("href"), "/our-greyhounds/", "?");
            try {
                delay();
                Document dogDoc = Jsoup.connect("https://www.gapnsw.com.au/our-greyhounds/" + urlName).get();
                return createGreyHound(urlName, dogDoc);
            } catch (IOException ex) {
                System.out.println("Error fetching page " + urlName);
                ex.printStackTrace(System.out);
                return null;
            }
        }).filter(Objects::nonNull).toList();
        persist(dogs);
    }

    private void delay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void persist(List<Greyhound> dogs) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        String file = "gap-" + LocalDate.now().toString() + ".csv";
        try (Writer writer  = new FileWriter("data/" + file)) {

            StatefulBeanToCsv<Greyhound> sbc = new StatefulBeanToCsvBuilder<Greyhound>(writer)
                    .build();

            sbc.write(dogs);
        }
        System.out.println("Wrote " + dogs.size() + " greyhounds to " + file);
    }

    private Greyhound createGreyHound(String urlName, Document d) {
        String microChip = getProfileContent(d, "Microchip:").get();
        return new Greyhound(
                microChip,
                urlName,
                getText(d, "profile-content-header-title").orElse(null),
                Sex.valueOf(getText(d, "profile-content-details-gender-type").orElse("UNKNOWN").toUpperCase()),
                getProfileContent(d, "Location:").orElse(null),
                getText(d, "trix-content").orElse(null),
                "",
                "",
                calculateDob(getText(d, "profile-content-details-age-count")),
                LocalDate.now()
        );
    }

    private LocalDate calculateDob(Optional<String> text) {
        return text.map(s -> {
            String years = StringUtils.substringBefore(s, " year");
            int yearsAge = Integer.parseInt(years);
            String months = StringUtils.substringBetween(s, yearsAge == 1 ? "year " : "years ", " month");
            int monthsAge = (months != null ? Integer.parseInt(months) : 0) + yearsAge * 12;

            return LocalDate.now().minusMonths(monthsAge).withDayOfMonth(1);
        }).orElse(null);
    }

    private Optional<String> getText(Document d, String uniqueClass) {
        return d.getElementsByClass(uniqueClass).stream().map(e -> e.text()).findFirst();
    }

    private Optional<String> getProfileContent(Document d, String name) {
        return d.getElementsByClass("profile-content-field")
                .stream()
                .filter(e -> e.child(0).text().equals(name))
                .map(e -> e.child(1).text())
                .findFirst();
    }
}
