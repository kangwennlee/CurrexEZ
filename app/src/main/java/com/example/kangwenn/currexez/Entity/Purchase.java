package com.example.kangwenn.currexez.Entity;

/**
 * Created by Kangwenn on 1/3/2018.
 */

public class Purchase {
    private String currency;
    private Double amount;
    private String payMethod;

    public Purchase(String currency, Double amount, String payMethod) {
        this.currency = currency;
        this.amount = amount;
        this.payMethod = payMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
}
