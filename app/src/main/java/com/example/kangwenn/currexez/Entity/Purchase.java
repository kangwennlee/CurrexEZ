package com.example.kangwenn.currexez.Entity;

/**
 * Created by Kangwenn on 1/3/2018.
 */

public class Purchase {
    private String currency;
    private Long amount;
    private Long amountInRM;
    private String payMethod;

    public Purchase(String currency, Long amount, Long amountInRM, String payMethod) {
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getAmountInRM() {
        return amountInRM;
    }

    public void setAmountInRM(Long amountInRM) {
        this.amountInRM = amountInRM;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
}
