package org.kablambda.greycrawler.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.io.FileUtils;
import org.kablambda.greycrawler.model.Greyhound;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class ChangeDetector {
    public static void main(String[] args) {
        new ChangeDetector().changes();
    }

    private void changes() {
        List<Map<String, Greyhound>> days = FileUtils.listFiles(new File("data"), new String[] {"csv"}, false).stream().sorted()
                .map(f -> read(f)).collect(Collectors.toList());
        Map<String, LocalDate> seen = new HashMap<>();
        for (int i = 0; i < days.size()-1; i++) {
            System.out.println(days.get(i+1).entrySet().stream().findFirst().get().getValue().getRecordedDate());
            days.get(i).values().stream().forEach(g -> seen.put(g.getMicrochipNumber(), g.getRecordedDate()));
            compare(seen, days.get(i), days.get(i+1));
        }
    }

    private void compare(Map<String,LocalDate> seen, Map<String, Greyhound> base, Map<String, Greyhound> changed) {
        Set<Greyhound> baseNames = new HashSet<>(base.values());
        Set<Greyhound> changedNames = new HashSet<>(changed.values());

        // missing
        Set<Greyhound> missing = new HashSet<>(baseNames);
        missing.removeAll(changedNames);
        if (missing.size() > 0) {
            System.out.println("Missing:");
            missing.stream().sorted().forEach(dog -> System.out.println(dog));
        }

        // new
        Set<Greyhound> newDogs = new HashSet<>(changedNames);
        newDogs.removeAll(baseNames);
        if (newDogs.size() > 0) {
            System.out.println("New:");
            newDogs.stream().sorted().forEach(dog -> {
                String s = seen.containsKey(dog.getMicrochipNumber()) ? " seen on " + seen.get(dog.getMicrochipNumber()) : "";
                System.out.println(dog + s);
            });
        }
    }

    private Map<String, Greyhound> read(File f) {
        try (Reader r = new FileReader(f)) {
            CsvToBean<Greyhound> toBean = new CsvToBeanBuilder<Greyhound>(r).withType(Greyhound.class).build();
            return toBean.stream().collect(Collectors.toMap(Greyhound::getUrlName, g -> g, (g1, g2) -> {
                System.out.printf("Duplicate name '%s' (%s, %s) in %s%n", g1.getUrlName(), g1.getMicrochipNumber(), g2.getMicrochipNumber(), f.getName());
                return g1;
            }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
