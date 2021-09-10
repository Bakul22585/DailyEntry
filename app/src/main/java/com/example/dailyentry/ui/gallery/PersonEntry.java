package com.example.dailyentry.ui.gallery;

import org.json.JSONArray;

public class PersonEntry {
    String id, name, perPersonBudget, pl, personBudget, status, sr;
    Boolean expandable;
    JSONArray data;

    public PersonEntry() {}
    public PersonEntry(String id, String name, String personBudget, String perPersonBudget, String pl, String status, String sr) {
        this.id = id;
        this.name = name;
        this.personBudget = personBudget;
        this.perPersonBudget = perPersonBudget;
        this.pl = pl;
        this.status = status;
        this.sr = sr;
        this.expandable = false;
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

    public Boolean getExpandable() {
        return expandable;
    }

    public void setExpandable(Boolean expandable) {
        this.expandable = expandable;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    public String getPerPersonBudget() {
        return perPersonBudget;
    }

    public void setPerPersonBudget(String perPersonBudget) {
        this.perPersonBudget = perPersonBudget;
    }

    public String getPl() {
        return pl;
    }

    public void setPl(String pl) {
        this.pl = pl;
    }

    public String getPersonBudget() {
        return personBudget;
    }

    public void setPersonBudget(String personBudget) {
        this.personBudget = personBudget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSr() {
        return sr;
    }

    public void setSr(String sr) {
        this.sr = sr;
    }
}
