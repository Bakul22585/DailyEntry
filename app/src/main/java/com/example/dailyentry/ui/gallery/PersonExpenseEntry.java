package com.example.dailyentry.ui.gallery;

public class PersonExpenseEntry {
    String title, description, amount, date, finance_type, type;

    public PersonExpenseEntry() {}
    public PersonExpenseEntry(String title, String description, String amount, String date, String finance_type, String type) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.finance_type = finance_type;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFinance_type() {
        return finance_type;
    }

    public void setFinance_type(String finance_type) {
        this.finance_type = finance_type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
