package com.example.dailyentry.ui.gallery;

public class TourEntry {
    private String id;
    private String name;
    private String description;
    private String date;
    private String status;
    private String budget;

    public TourEntry() {}
    public TourEntry(String id, String name, String description, String date, String status, String budget) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.status = status;
        this.budget = budget;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }
}
