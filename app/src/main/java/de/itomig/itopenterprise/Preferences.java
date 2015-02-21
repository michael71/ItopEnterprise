// Copyright (C) 2011-2013 ITOMIG GmbH
//
//   This file is part of iTopMobile.
//
//   iTopMobile is free software; you can redistribute it and/or modify	
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   iTopMobile is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with iTopMobile. If not, see <http://www.gnu.org/licenses/>

package de.itomig.itopenterprise;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import static de.itomig.itopenterprise.ItopConfig.DEMO_LOGIN;
import static de.itomig.itopenterprise.ItopConfig.DEMO_SSL;
import static de.itomig.itopenterprise.ItopConfig.DEMO_URL;
import static de.itomig.itopenterprise.ItopConfig.KEY_LOGIN;
import static de.itomig.itopenterprise.ItopConfig.KEY_NOTIFY;
import static de.itomig.itopenterprise.ItopConfig.KEY_SSL;
import static de.itomig.itopenterprise.ItopConfig.KEY_URL;
import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.itopAppContext;

/**
 * itopMobile Settings
 *
 * @author mblank
 */
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private EditTextPreference urlPref, loginPref;
    private ListPreference notifyPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (debug) Log.i(TAG, "Preferences - onCreate");
        addPreferencesFromResource(R.xml.preferences);

        urlPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_URL);
        loginPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_LOGIN);
        notifyPref = (ListPreference) getPreferenceScreen().findPreference(KEY_NOTIFY);

        initSummaryTexts();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug) Log.i(TAG, "Preferences - onResume");
        initSummaryTexts();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Log.i(TAG, "Preferences - onResume");
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initSummaryTexts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String login = prefs.getString(KEY_LOGIN, DEMO_LOGIN);
        loginPref.setSummary(login);

        int notifySetting = Integer.valueOf(prefs.getString(KEY_NOTIFY, "0"));
        if (debug) Log.i(TAG, "Pref: notify=" + notifySetting);
        CharSequence[] notifyEntries = notifyPref.getEntries();
        notifyPref.setSummary(notifyEntries[notifySetting]);

        String url = (prefs.getString(KEY_URL, DEMO_URL)).trim();
        boolean ssl_enabled = prefs.getBoolean(KEY_SSL, DEMO_SSL);

        if (ssl_enabled) {
            urlPref.setSummary("https://" + url);
        } else {
            urlPref.setSummary("http://" + url);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs,
                                          String key) {
        if (debug) Log.i(TAG, "Preferences - sharedPrefChanged - " + key);
        if (key.equals(KEY_LOGIN)) {
            String login = prefs.getString(KEY_LOGIN, DEMO_LOGIN);
            loginPref.setSummary(login);
        } else if (key.equals(KEY_NOTIFY)) {
            int notifySetting = Integer.valueOf(prefs.getString(KEY_NOTIFY, "0"));
            if (debug) Log.i(TAG, "Pref: notify=" + notifySetting);
            CharSequence[] notifyEntries = notifyPref.getEntries();
            notifyPref.setSummary(notifyEntries[notifySetting]);
        } else if (key.equals(KEY_SSL)) {
            String url = (prefs.getString(KEY_URL, DEMO_URL)).trim();
            boolean ssl_enabled = prefs.getBoolean(KEY_SSL, DEMO_SSL);
            if (ssl_enabled) {
                urlPref.setSummary("https://" + url);
            } else {
                urlPref.setSummary("http://" + url);
            }
        } else if (key.equals(KEY_URL)) {
            String url = (prefs.getString(KEY_URL, DEMO_URL)).trim();
            boolean ssl_enabled = prefs.getBoolean(KEY_SSL, DEMO_SSL);
            if (ssl_enabled) {
                urlPref.setSummary("https://" + url);
            } else {
                urlPref.setSummary("http://" + url);
            }
            // cache makes no sense when server has changed.
            de.itomig.itopenterprise.Cache.clearPersonsCache(itopAppContext);
            de.itomig.itopenterprise.Cache.clearOrgsCache(itopAppContext);
        }
    }
}