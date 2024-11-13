package org.kablambda.greycrawler.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.io.FileUtils;
import org.kablambda.greycrawler.model.Greyhound;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
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
        Map<String, LocalDate> firstSeen = new HashMap<>();
        Set<Greyhound> distinct = new HashSet<>();
        LocalDate firstDate = days.get(0).values().stream().findFirst().map(g -> g.getRecordedDate()).get();
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < days.size()-1; i++) {
            int count = days.get(i+1).entrySet().size();
            max = Math.max(max, count);
            min = Math.min(min, count);
            System.out.println(days.get(i+1).entrySet().stream().findFirst().get().getValue().getRecordedDate() + " (" + count + ")");
            days.get(i).values().stream().forEach(g -> seen.put(g.getMicrochipNumber(), g.getRecordedDate()));
            days.get(i).values().stream().forEach(g -> firstSeen.putIfAbsent(g.getMicrochipNumber(), g.getRecordedDate()));
            distinct.addAll(days.get(i).values());
            compare(firstDate, seen, firstSeen, days.get(i), days.get(i+1));
        }
        System.out.println("Distinct: " + distinct.size() + " Min: " + min + " Max: " + max);
    }

    private void compare(
            LocalDate firstDate,
            Map<String,LocalDate> seen,
            Map<String,LocalDate> firstSeen,
            Map<String, Greyhound> base,
            Map<String, Greyhound> changed) {
        Set<Greyhound> baseNames = new HashSet<>(base.values());
        Set<Greyhound> changedNames = new HashSet<>(changed.values());

        // missing
        Set<Greyhound> missing = new HashSet<>(baseNames);
        missing.removeAll(changedNames);
        if (missing.size() > 0) {
            System.out.println("Missing: (" + missing.size() + ")");
            missing.stream().sorted().forEach(dog -> {
                String residencyInDays = firstSeen.containsKey(dog.getMicrochipNumber())
                        ? residency(firstDate, firstSeen.get(dog.getMicrochipNumber()), dog.getRecordedDate())
                        : "";

                System.out.println(dog + residencyInDays);
            });
        }

        // new
        Set<Greyhound> newDogs = new HashSet<>(changedNames);
        newDogs.removeAll(baseNames);
        if (newDogs.size() > 0) {
            System.out.println("New: (" + newDogs.size() + ")");
            newDogs.stream().sorted().forEach(dog -> {
                String s = seen.containsKey(dog.getMicrochipNumber()) ? " seen on " + seen.get(dog.getMicrochipNumber()) : "";
                System.out.println(dog + s);
            });
        }
    }

    private String residency(LocalDate firstDate, LocalDate firstSeen, LocalDate lastSeen) {
        if (firstDate.equals(firstSeen)) {
            return " since start";
        } else {
           return " here for " + Duration.between(firstSeen.atStartOfDay(), lastSeen.atStartOfDay()).toDays();
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
