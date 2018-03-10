package com.example.kangwenn.currexez.Entity;

/**
 * Created by Kangwenn on 1/3/2018.
 */

public class Purchase {
    private String currency;
    private Double amount;
    private Double amountInRM;
    private String payMethod;

    public Purchase() {
    }

    public Purchase(String currency, Double amount, Double amountInRM, String payMethod) {
        this.currency = currency;
        this.amount = amount;
        this.amountInRM = amountInRM;
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

    public Double getAmountInRM() {
        return amountInRM;
    }

    public void setAmountInRM(Double amountInRM) {
        this.amountInRM = amountInRM;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
}
