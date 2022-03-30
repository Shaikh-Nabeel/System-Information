package com.snabeel.systeminformation;

public class BelowThirtyCount {
    long date;
    String charge;

    public BelowThirtyCount(){ }

    public BelowThirtyCount(long date, String charge) {
        this.date = date;
        this.charge = charge;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
