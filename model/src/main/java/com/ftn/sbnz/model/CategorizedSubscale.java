package com.ftn.sbnz.model;

public class CategorizedSubscale {

    private String familyId;
    private String subscale;
    private String category; // "low" | "moderate" | "high"

    public CategorizedSubscale() {}

    public CategorizedSubscale(String familyId, String subscale, String category) {
        this.familyId = familyId;
        this.subscale = subscale;
        this.category = category;
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getSubscale() { return subscale; }
    public void setSubscale(String subscale) { this.subscale = subscale; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "CategorizedSubscale{familyId='" + familyId +
                "', subscale='" + subscale +
                "', category='" + category + "'}";
    }
}