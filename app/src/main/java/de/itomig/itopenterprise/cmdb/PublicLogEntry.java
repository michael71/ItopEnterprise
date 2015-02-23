package de.itomig.itopenterprise.cmdb;

import java.io.Serializable;

/**
 * Created by mblank on 23.02.15.
 */
public class PublicLogEntry implements Serializable {
    // Serializable is needed to "hook it up" on an intent with
    private static final long serialVersionUID = -5998434779602343991L;
    String date ="";
    String user_login="";
    int user_id=0;
    String message="";

    public PublicLogEntry() {
    }

    public PublicLogEntry(String s) {
        date="";
        user_login="";
        user_id = 0;
        message = s;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser_login() {
        return user_login;
    }

    public void setUser_login(String user_login) {
        this.user_login = user_login;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
