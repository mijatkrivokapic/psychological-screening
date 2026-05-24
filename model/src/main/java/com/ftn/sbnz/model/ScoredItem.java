package com.ftn.sbnz.model;

public class ScoredItem {
    private String familyId;
    private int itemNumber;
    private String subscale;
    private int value;

    public ScoredItem() {}

    public ScoredItem(String familyId, int itemNumber, String subscale, int value) {
        this.familyId = familyId;
        this.itemNumber = itemNumber;
        this.subscale = subscale;
        this.value = value;
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public int getItemNumber() { return itemNumber; }
    public void setItemNumber(int itemNumber) { this.itemNumber = itemNumber; }

    public String getSubscale() { return subscale; }
    public void setSubscale(String subscale) { this.subscale = subscale; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    @Override
    public String toString() {
        return "ScoredItem{familyId='" + familyId + "', item=" + itemNumber
                + ", subscale='" + subscale + "', value=" + value + "}";
    }
}
