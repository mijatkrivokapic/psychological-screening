package com.ftn.sbnz.model;

public class SubscaleAverage {

    private String familyId;
    private String subscale;
    private double average;

    public SubscaleAverage() {}

    public SubscaleAverage(String familyId, String subscale, double average) {
        this.familyId = familyId;
        this.subscale = subscale;
        this.average = average;
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getSubscale() { return subscale; }
    public void setSubscale(String subscale) { this.subscale = subscale; }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }

    @Override
    public String toString() {
        return "SubscaleAverage{familyId=" + familyId +
                ", subscale='" + subscale + '\'' +
                ", average=" + average + '}';
    }
}