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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.itomig.itopenterprise.cmdb.Organization;
import de.itomig.itopenterprise.cmdb.Person;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.itopAppContext;
import static de.itomig.itopenterprise.ItopConfig.organizationLookup;
import static de.itomig.itopenterprise.ItopConfig.organizationLookupTime;
import static de.itomig.itopenterprise.ItopConfig.personLookup;
import static de.itomig.itopenterprise.ItopConfig.personLookupTime;

public class PersonAndOrgsLookup {

    private static final long MIN = 60 * 1000L;
    private boolean reqRunningFlag = false;

    void update() {
        //if (debug)
        //	Log.d(TAG, "PersonAndOrgsLookup");

        if (!reqRunningFlag) {
            if ((System.currentTimeMillis() - personLookupTime) > 150 * MIN) {
                reqRunningFlag = true;
                RequestPersonsFromServerTask reqServer = new RequestPersonsFromServerTask();
                if (debug) Log.d(TAG, "PersonAndOrgsLookup, retrieving Persons from server");
                reqServer.execute();
            } else if ((System.currentTimeMillis() - organizationLookupTime) > 630 * MIN) {
                reqRunningFlag = true;
                RequestOrgsFromServerTask reqServer = new RequestOrgsFromServerTask();
                if (debug) Log.d(TAG, "PersonAndOrgsLookup, retrieving Organizations from server");
                reqServer.execute();
            }
        } else {
            Log.e(TAG, "could not start PersonAndOrgsLookup");
        }
    }

    class RequestPersonsFromServerTask extends
            AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String resp="";
            try {
                resp = GetItopJSON.postJsonToItopServer("core/get", "Person", "SELECT Person",
                        "name, friendlyname, phone, org_id");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }

        @SuppressLint("UseSparseArrays") // sparse arrays cannot be iterated.
        @Override
        protected synchronized void onPostExecute(String resp) {

            reqRunningFlag = false;

            if (!ItopUtils.getItopError(resp).isEmpty()) return;

            ArrayList<Person> persons;
            Type type = new TypeToken<Person>() {
            }.getType();
            persons = GetItopJSON.getArrayFromJson(resp, type, null);
            if (debug)
                Log.i(TAG,
                        "onPostExecute - RefreshPersonsFromServer");
            if (persons == null) {
                Log.e(TAG, "empty response when req. Person List. - RefreshPersonsFromServer");
                return;
            }


            for (Person p : persons) {
                personLookup.put(p.getId(), p);
                if (debug)
                    Log.d(TAG, "PersonAndOrgsLookup - setting person id=" + p.getId() + " to name=" + p.getFriendlyname() + "org_id=" + p.getOrg_id());
            }

            personLookupTime = System.currentTimeMillis();
            Cache.cachePersons(itopAppContext);
        }
    }

    class RequestOrgsFromServerTask extends
            AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... expr) {
            String resp="";
            try {
                resp = GetItopJSON.postJsonToItopServer("core/get", "Organization",
                        "SELECT Organization", "name, id");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }

        @SuppressLint("UseSparseArrays")  // sparse arrays cannot be iterated.
        @Override
        protected void onPostExecute(String resp) {

            reqRunningFlag = false;
            if (!ItopUtils.getItopError(resp).isEmpty()) return;

            ArrayList<Organization> organizations;
            Type type = new TypeToken<Organization>() {
            }.getType();
            organizations = GetItopJSON.getArrayFromJson(resp, type, null);

            if (debug)
                Log.i(TAG,
                        "onPostExecute - PersonAndOrgsLookup - Organizations - reqRunningFlag cleared");

            if (organizations == null) {
                Log.e(TAG, "empty response when req. Person List.");
                return;
            }

            for (Organization o : organizations) {
                organizationLookup.put(o.getId(), o.getName());
                if (debug) Log.d(TAG,"PersonAndOrgsLookup - setting org id="+o.getId()+" to name="+o.getName());
            }
            organizationLookupTime = System.currentTimeMillis();
            Cache.cacheOrgs(itopAppContext);
        }
    }
}
