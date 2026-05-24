package com.ftn.sbnz.model;

public class Answer {
    private String familyId;
    private int itemNumber;
    private int value;

    public Answer() {}

    public Answer(String familyId, int itemNumber, int value) {
        this.familyId = familyId;
        this.itemNumber = itemNumber;
        this.value = value;
    }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public int getItemNumber() { return itemNumber; }
    public void setItemNumber(int itemNumber) { this.itemNumber = itemNumber; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    @Override
    public String toString() {
        return "Answer{familyId='" + familyId + "', item=" + itemNumber + ", value=" + value + "}";
    }
}
