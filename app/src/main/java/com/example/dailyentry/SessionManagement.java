package com.example.dailyentry;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String SESSION_KEY = "session_";

    public SessionManagement(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(UserObject user){
        String id = user.getId();
        String FirstName = user.getFirstName();
        String LastName = user.getLastName();
        String Email = user.getEmail();
        String UserName = user.getUsername();
        String Mobile = user.getMobile();
        String img = user.getImg();

        editor.putString(SESSION_KEY + "id", id);
        editor.putString(SESSION_KEY + "FirstName", FirstName);
        editor.putString(SESSION_KEY + "LastName", LastName);
        editor.putString(SESSION_KEY + "email", Email);
        editor.putString(SESSION_KEY + "username", UserName);
        editor.putString(SESSION_KEY + "Mobile", Mobile);
        editor.putString(SESSION_KEY + "Img", img);
        editor.commit();
    }

    public void addNewSession(String Field, String value) {
        editor.putString(SESSION_KEY + Field, value);
        editor.commit();
    }

    public String getSession(String Field){
        return sharedPreferences.getString(SESSION_KEY + Field, "");
    }

    public void ClearSession() {
        editor.clear();
        editor.commit();
    }
}
