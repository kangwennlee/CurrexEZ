package com.example.kangwenn.currexez.Entity;

/**
 * Created by JinXian on 25/02/2018.
 */

public class User {
    private String userID;
    private String name;
    private String nation;
    private String birthday;
    private String phoneNumber;
    private String address;
    private String icAndPassport;

    public User(String userID, String name, String nation, String birthday, String phoneNumber, String address, String icAndPassport) {
        this.userID = userID;
        this.name = name;
        this.nation = nation;
        this.birthday = birthday;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.icAndPassport = icAndPassport;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIcAndPassport() {
        return icAndPassport;
    }

    public void setIcAndPassport(String icAndPassport) {
        this.icAndPassport = icAndPassport;
    }
}
