package com.ftn.sbnz.model;

public class CategorizedComposite {

    private String familyId;
    private String composite;
    private String category; // "low" | "moderate" | "high"

    public CategorizedComposite() {}

    public CategorizedComposite(String familyId, String composite, String category) {
        this.familyId = familyId;
        this.composite = composite;
        this.category = category;
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getComposite() { return composite; }
    public void setComposite(String composite) { this.composite = composite; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "CategorizedComposite{familyId='" + familyId +
                "', composite='" + composite +
                "', category='" + category + "'}";
    }
}