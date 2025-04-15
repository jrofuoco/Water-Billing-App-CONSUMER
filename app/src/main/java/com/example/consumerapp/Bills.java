package com.example.consumerapp;

public class Bills {
    private String readingLastMonthDate;
    private double amountPayable;

    public Bills(String readingLastMonthDate, double amountPayable) {
        this.readingLastMonthDate = readingLastMonthDate;
        this.amountPayable = amountPayable;
    }

    public String getReadingLastMonthDate() {
        return readingLastMonthDate;
    }

    public void setReadingLastMonthDate(String readingLastMonthDate) {
        this.readingLastMonthDate = readingLastMonthDate;
    }

    public double getAmountPayable() {
        return amountPayable;
    }

    public void setAmountPayable(double amountPayable) {
        this.amountPayable = amountPayable;
    }
}
