// Copyright (C) 2011-2013 ITOMIG GmbH
//
//   This file is part of iTopEnterprise.
//
//   iTopEnterprise is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   iTopEnterprise is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with iTopEnterprise. If not, see <http://www.gnu.org/licenses/>
package de.itomig.itopenterprise;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.itomig.itopenterprise.cmdb.InternalTask;
import de.itomig.itopenterprise.cmdb.ItopTicket;
import de.itomig.itopenterprise.cmdb.Organization;
import de.itomig.itopenterprise.cmdb.Person;


@SuppressLint("UseSparseArrays")
public class ItopConfig extends Application {
    // DEBUG must be false for all versions released to android market
    // also remove    android:debuggable="true"  in the AndroidManifest.xml
    public static boolean debug = true;

    // corporate version with URL check and update from itomig.de
    // or Android market version with update from market
    public static final String KEY_URL = "urlPref";
    public static final String KEY_LOGIN = "loginPref";
    public static final String KEY_PASSWORD = "passwordPref";
    public static final String KEY_SSL = "sslPref";
    public static final String KEY_ITIL_TICKETS = "itilTicketsPref";
    public static final String KEY_TASKS = "taskPref";
    public static final String KEY_NOTIFY = "notifyPref";
    public static final String KEY_SORTBYPRIO = "sortByPrioPref";
    public static final String KEY_QUERY_NOTIFY = "queryNotifyPref";
    public static final String STATUS_ASSIGNED = "assigned";   // must match to iTop installation
    public static final String ERROR = "error";
    public static final String TAG = "ITOP";
    public static final int INVALID_ID = 0; // used for invalid people or ticket id.
    public static final int NOTIFY_NO = 0;
    public static final int NOTIFY_NEW = 1;
    public static final int NOTIFY_UPDATE = 2;
    public static final int NOTIFICATION_ID_INCIDENT = 1;
    public static final int BACKGROUND_INTERVAL_MIN = 10;
    public static final int DISPLAY_REFRESH_SECS = 120;


    public static Context itopAppContext;
    // default values point to DEMO server
    public static String DEMO_URL = "www.itomig.de/itop-demo20";
    public static String DEMO_LOGIN = "admin";
    public static String DEMO_PASSWORD = "1234admi";
    public static boolean DEMO_SSL = true;
    @SuppressWarnings("rawtypes")
    public static Class itopmobileactivity = null;

    // used in TicketActivity
    public static ArrayList<ItopTicket> tickets = new ArrayList<ItopTicket>();

    public static List<InternalTask> tasks = new ArrayList<InternalTask>();

    public static ConcurrentHashMap<Integer, Person> personLookup = new ConcurrentHashMap<Integer, Person>();
    public static ConcurrentHashMap<Integer, String> organizationLookup = new ConcurrentHashMap<Integer, String>();

    public static long personLookupTime = 0L;
    public static long organizationLookupTime = 0L;

    public static String[] prioStrings;  // language dependent, read from arrays.xml

    public static void init(Context context, Class itopmob) {

        Log.i(TAG, "ItopLib initialized - debug=" + debug);

        itopmobileactivity = itopmob;  // used for menu in SearchActivity

        itopAppContext = context; // Application Context for later use in static functions.

        boolean result1, result2;
        result1 = de.itomig.itopenterprise.Cache.getCachePersons(itopAppContext);
        result2 = de.itomig.itopenterprise.Cache.getCacheOrgs(itopAppContext);

        if (!result1 || !result2) {
            //TODO make static and separate persons and orgs
            de.itomig.itopenterprise.PersonAndOrgsLookup pl = new de.itomig.itopenterprise.PersonAndOrgsLookup();
            pl.update();
        }
    }

    public static int getRefreshRateSeconds() {
        return DISPLAY_REFRESH_SECS;
    }

    public static String getItopUrl() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        String url = (prefs.getString(KEY_URL, DEMO_URL)).trim();
        if ((url.length() > 3) && (url.charAt(url.length() - 1) == '/')) {  // remove trailing slash in url, if there is one
            url = url.substring(0, url.length() - 1);
        }
        boolean ssl_enabled = prefs.getBoolean(KEY_SSL, DEMO_SSL);

        if (ssl_enabled) {
            return "https://" + url;
        } else {
            return "http://" + url;
        }

    }

    public static String getItopCredentials() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        String login = prefs.getString(KEY_LOGIN, DEMO_LOGIN).trim();
        String password = null;
        try {
            password = URLEncoder.encode(prefs.getString(KEY_PASSWORD, DEMO_PASSWORD).trim(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return "auth_user=" + login + "&auth_pwd=" + password;

    }

    public static BasicNameValuePair getItopUserNameValuePair() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        String login = prefs.getString(KEY_LOGIN, DEMO_LOGIN).trim();
        return new BasicNameValuePair("auth_user", login);
    }

    public static BasicNameValuePair getItopPwdNameValuePair() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        String password = null;
        try {
            password = URLEncoder.encode(prefs.getString(KEY_PASSWORD, DEMO_PASSWORD).trim(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return new BasicNameValuePair("auth_pwd", password);
    }

    public static int getItopNotifySetting() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        return Integer.valueOf(prefs.getString(KEY_NOTIFY, "0"));

    }

    public static boolean isEnabledlITILTicket() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itopAppContext);
        return  prefs.getBoolean(KEY_ITIL_TICKETS, true);

    }


    public static int getNumericalPriority(String prio) {
        // the prio string is language dependent!!

        int numPrio = INVALID_ID;
        for (int i = 0; i < prioStrings.length; i++) {
            if (prio.equals(prioStrings[i])) {
                numPrio = i + 1;
                break;
            }
        }
        return numPrio;
    }
}
