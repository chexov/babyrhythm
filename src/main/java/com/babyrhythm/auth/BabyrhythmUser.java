package com.babyrhythm.auth;

import java.util.Date;

public class BabyrhythmUser {

    public String facebookId;
    public boolean confirmed;
    public String facebookToken;
    public int facebookTokenExpiry;
    public Date dateCreated;
    private Date lastModified;
    public String name;
    
    public BabyrhythmUser(String facebookId, String string, String name) {
        this.facebookId = facebookId;
    }

    public BabyrhythmUser(String facebookId, String password, String name, String id) {
        this(facebookId, password, name);
    }

    public void setCreationDate(Date date) {
        dateCreated = date;
    }

    public void setLastModified(Date date) {
        this.lastModified = date;
    }

    public Date getCreationDate() {
        return dateCreated;
    }


}
