package com.example.gpslocationtracker.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public PreferenceManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(VariableBag.PREF_NAME, context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public String getRegistredUSerID() {
        return sharedPreferences.getString(VariableBag.USER_ID, "");
    }

    public void setRegistredUSerID(String uSerID) {
        editor.putString(VariableBag.USER_ID, uSerID);
        editor.commit();
    }

    public void clearPrefrence() {
        editor.clear();
        editor.commit();
    }

    public void setLoginSession() {
        editor.putBoolean(VariableBag.LOGIN_FLAG, true);
        editor.commit();
    }
    public void setLoginSessionFalse() {
        editor.putBoolean(VariableBag.LOGIN_FLAG, false);
        editor.commit();
    }

    public boolean getLoginSession() {
        boolean login = sharedPreferences.getBoolean(VariableBag.LOGIN_FLAG, false);
        return login;
    }

    public void SetName(String UserName) {
        editor.putString(VariableBag.USER_NAME, UserName);
        editor.commit();
    }

    public String GetName() {
        return sharedPreferences.getString(VariableBag.USER_NAME, "");
    }

    public void SetProfilePic(String ProfilePic) {
        editor.putString(VariableBag.PROFILE_PIC, ProfilePic);
        editor.commit();
    }

    public String GetProfilePic() {
        return sharedPreferences.getString(VariableBag.PROFILE_PIC, "");
    }

    public void SetRoleID(String RoleId) {
        editor.putString(VariableBag.ROLE_ID, RoleId);
        editor.commit();
    }

    public String GetRoleID() {
        return sharedPreferences.getString(VariableBag.ROLE_ID, "");
    }

    public void SetDesignation(String Dessignation) {
        editor.putString(VariableBag.USER_DESIGNATION, Dessignation);
        editor.commit();
    }

    public String GetDesignation() {
        return sharedPreferences.getString(VariableBag.USER_DESIGNATION, "");
    }

    public void SetEmail(String Email) {
        editor.putString(VariableBag.USER_EMAIL, Email);
        editor.commit();
    }

    public String GetEmail() {
        return sharedPreferences.getString(VariableBag.USER_EMAIL, "");
    }

    public void SetPhone(String Phone) {
        editor.putString(VariableBag.USER_PHONE, Phone);
        editor.commit();
    }

    public String GetPhone() {
        return sharedPreferences.getString(VariableBag.USER_PHONE, "");
    }

    public String getKeyValueString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void setKeyValueString(String key, String value) {
        editor.putString(key, value).commit();
    }



    public void SetAutostart(Boolean aBoolean) {
        editor.putBoolean("autostart", aBoolean);
        editor.commit();
    }

    public Boolean GetAutostart() {
        boolean inout = sharedPreferences.getBoolean("autostart", false);
        return inout;

    }
}
