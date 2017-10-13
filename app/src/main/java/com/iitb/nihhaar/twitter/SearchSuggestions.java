package com.iitb.nihhaar.twitter;

import java.io.Serializable;

/**
 * Created by Nihhaar on 10/12/2017.
 */

public class SearchSuggestions implements Serializable{

    private String uid;
    private String name;
    private String email;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
