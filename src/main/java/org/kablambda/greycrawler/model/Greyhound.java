package org.kablambda.greycrawler.model;

import com.opencsv.bean.CsvDate;

import java.io.Serializable;
import java.time.LocalDate;

public class Greyhound implements Comparable<Greyhound>, Serializable {
    private String microchipNumber;
    private String urlName;
    private String name;
    private Sex sex;
    private String location;
    private String text;
    private String whatWeHaveNoticed;
    private String specificNeeds;
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate recordedDate;

    public Greyhound(String microchipNumber, String urlName, String name, Sex sex, String location, String text,
                     String whatWeHaveNoticed, String specificNeeds, LocalDate dateOfBirth, LocalDate recordedDate) {
        this.microchipNumber = microchipNumber;
        this.urlName = urlName;
        this.name = name;
        this.sex = sex;
        this.location = location;
        this.text = text;
        this.whatWeHaveNoticed = whatWeHaveNoticed;
        this.specificNeeds = specificNeeds;
        this.dateOfBirth = dateOfBirth;
        this.recordedDate = recordedDate;
    }

    public Greyhound() {
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getWhatWeHaveNoticed() {
        return whatWeHaveNoticed;
    }

    public void setWhatWeHaveNoticed(String whatWeHaveNoticed) {
        this.whatWeHaveNoticed = whatWeHaveNoticed;
    }

    public String getSpecificNeeds() {
        return specificNeeds;
    }

    public void setSpecificNeeds(String specificNeeds) {
        this.specificNeeds = specificNeeds;
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String toString() {
        return String.format("%s (%s)", name, microchipNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Greyhound greyhound = (Greyhound) o;
        return microchipNumber.equals(greyhound.microchipNumber);
    }

    @Override
    public int hashCode() {
        return microchipNumber.hashCode();
    }

    @Override
    public int compareTo(Greyhound o) {
        return this.microchipNumber.compareTo(o.microchipNumber);
    }
}
