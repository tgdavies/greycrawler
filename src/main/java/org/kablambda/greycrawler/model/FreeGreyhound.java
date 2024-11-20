package org.kablambda.greycrawler.model;

public class FreeGreyhound {
    private String microchipNumber;
    private String gumtreeUrl;

    public FreeGreyhound() {

    }

    public FreeGreyhound(String gumtreeUrl, String microchipNumber) {
        this.microchipNumber = microchipNumber;
        this.gumtreeUrl = gumtreeUrl;
    }

    public String getGumtreeUrl() {
        return gumtreeUrl;
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchip) {
        this.microchipNumber = microchip;
    }

    public void setGumtreeUrl(String gumtreeUrl) {
        this.gumtreeUrl = gumtreeUrl;
    }
}
