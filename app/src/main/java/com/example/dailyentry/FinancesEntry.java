package com.example.dailyentry;

public class FinancesEntry {
    private String id;
    private String user;
    private String description;
    private String amount;
    private String paymentType;
    private String financeType;
    private String date;
    private String chequeNumber;
    private String chequeDate;
    private String isComplete;

    public FinancesEntry() {}
    public FinancesEntry(String id, String user, String description, String amount, String paymentType, String financeType, String date, String chequeNumber, String chequeDate, String isComplete) {
        this.id = id;
        this.user = user;
        this.description = description;
        this.amount = amount;
        this.paymentType = paymentType;
        this.financeType = financeType;
        this.date = date;
        this.chequeNumber = chequeNumber;
        this.chequeDate = chequeDate;
        this.isComplete = isComplete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getFinanceType() {
        return financeType;
    }

    public void setFinanceType(String financeType) {
        this.financeType = financeType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(String chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(String isComplete) {
        this.isComplete = isComplete;
    }
}
