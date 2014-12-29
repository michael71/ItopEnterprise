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

package de.itomig.itoplib;

import static de.itomig.itoplib.ItopConfig.*;
import java.util.ArrayList;
import java.util.HashMap;
import de.itomig.itoplib.cmdb.Organization;
import de.itomig.itoplib.cmdb.Person;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

public class PersonAndOrgsLookup {

	private boolean reqRunningFlag = false;
	private static final long MIN = 60 * 1000L;
	
	void update() {
		//if (debug)
		//	Log.d(TAG, "PersonAndOrgsLookup");

		if (reqRunningFlag == false) {
			if ((System.currentTimeMillis() - personLookupTime) > 150*MIN) {
				reqRunningFlag = true;
				RequestPersonsFromServerTask reqServer = new RequestPersonsFromServerTask();
				String expr = "SELECT+Person";
				if (debug) Log.d(TAG, "PersonAndOrgsLookup, retrieving Persons from server");
				reqServer.execute(expr);
			} else if ((System.currentTimeMillis() - organizationLookupTime) > 630*MIN) {
				reqRunningFlag = true;
				RequestOrgsFromServerTask reqServer = new RequestOrgsFromServerTask();
				String expr = "SELECT+Organization";
				if (debug) Log.d(TAG, "PersonAndOrgsLookup, retrieving Organizations from server");
				reqServer.execute(expr);
			}
		} else {
			Log.e(TAG, "could not start PersonAndOrgsLookup");
		}
	}

	class RequestPersonsFromServerTask extends
			AsyncTask<String, Void, ArrayList<Person>> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected ArrayList<Person> doInBackground(String... expr) {
			ArrayList<Person> persons = new ArrayList<Person>();
			try {
				persons = GetItopData.getPersonsFromItopServer(expr[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return persons;
		}

		@SuppressLint("UseSparseArrays") // sparse arrays cannot be iterated.
		@Override
		protected void onPostExecute(ArrayList<Person> persons) {
			
			reqRunningFlag = false;
			if (debug)
				Log.i(TAG,
						"onPostExecute - PersonAndOrgsLookup - Persons - reqRunningFlag cleared");
			
			if (persons == null) {
				Log.e(TAG,"empty response when req. Person List.");
				return;
			}
			

			HashMap<Integer,Person> plNew = new HashMap<Integer,Person>();
			for (Person p : persons) {			
				plNew.put(p.getId(), p);				
				if (debug) Log.d(TAG,"PersonAndOrgsLookup - setting person id="+p.getId()+" to name="+p.getFriendlyname()+ "org_id="+p.getOrg_id());
			}
			personLookup=plNew;
			personLookupTime = System.currentTimeMillis();
			Cache.cachePersons(itopAppContext);
		}
	}

	class RequestOrgsFromServerTask extends
			AsyncTask<String, Void, ArrayList<Organization>> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected ArrayList<Organization> doInBackground(String... expr) {
			ArrayList<Organization> organizations = new ArrayList<Organization>();
			try {
				organizations = GetItopData
						.getOrganizationsFromItopServer(expr[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return organizations;
		}

		@SuppressLint("UseSparseArrays")  // sparse arrays cannot be iterated.
		@Override
		protected void onPostExecute(ArrayList<Organization> organizations) {

			reqRunningFlag = false;
			organizationLookupTime = System.currentTimeMillis();
			if (debug)
				Log.i(TAG,
						"onPostExecute - PersonAndOrgsLookup - Organizations - reqRunningFlag cleared");
			
			if (organizations == null) {
				Log.e(TAG,"empty response when req. Person List.");
				return;
			}
			
			HashMap<Integer,String> orgNew = new HashMap<Integer,String>();
			for (Organization o : organizations) {
				orgNew.put(o.getId(), o.getName());
				//if (debug) Log.d(TAG,"PersonAndOrgsLookup - setting org id="+o.getId()+" to name="+o.getName());
			}
			organizationLookup=orgNew;  // replace old lookup table
			organizationLookupTime = System.currentTimeMillis();
			Cache.cacheOrgs(itopAppContext);
		}
	}
}
