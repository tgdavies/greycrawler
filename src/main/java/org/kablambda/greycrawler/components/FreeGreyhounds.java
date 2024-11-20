package org.kablambda.greycrawler.components;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.kablambda.greycrawler.model.FreeGreyhound;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeGreyhounds {
    public Map<String, FreeGreyhound> load() {
        try (Reader r = new FileReader(new File("config/free-greyhounds.csv"))) {
            CsvToBean<FreeGreyhound> toBean = new CsvToBeanBuilder<FreeGreyhound>(r).withType(FreeGreyhound.class).build();
            return toBean.stream().collect(Collectors.toMap(FreeGreyhound::getMicrochipNumber, g -> g, (g1, g2) -> {
                System.out.printf("Duplicate microchip '%s' (%s, %s)%n", g1.getMicrochipNumber(), g1.getGumtreeUrl(), g2.getGumtreeUrl());
                return g1;
            }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
