package edu.ncku.application.model;

import java.io.Serializable;

/**
 * Created by NCKU on 2015/12/1.
 */
public class ContactInfo implements Serializable {

    private String division;
    private String phone;
    private String email;

    public ContactInfo(String division, String phone, String email) {
        this.division = division;
        this.phone = phone;
        this.email = email;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
