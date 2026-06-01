package com.ftn.sbnz.model;

public class DependsOn {

    private Object consequence;
    private Object cause;

    public DependsOn() {}

    public DependsOn(Object consequence, Object cause) {
        this.consequence = consequence;
        this.cause = cause;
    }

    public Object getConsequence() { return consequence; }
    public void setConsequence(Object consequence) { this.consequence = consequence; }

    public Object getCause() { return cause; }
    public void setCause(Object cause) { this.cause = cause; }

    @Override
    public String toString() {
        return "DependsOn{consequence=" + consequence + ", cause=" + cause + "}";
    }
}