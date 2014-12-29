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

//   Google-GSON: Copyright 2008-2011 Google Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0

package de.itomig.itoplib;

import static de.itomig.itoplib.ItopConfig.TAG;
import static de.itomig.itoplib.ItopConfig.debug;
import static de.itomig.itoplib.ItopConfig.organizationLookup;
import static de.itomig.itoplib.ItopConfig.organizationLookupTime;
import static de.itomig.itoplib.ItopConfig.personLookup;
import static de.itomig.itoplib.ItopConfig.personLookupTime;
import java.lang.reflect.Type;
import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.itomig.itoplib.cmdb.Person;

public class Cache {
	
	public static final long MAX_CACHE_LIFETIME = 24*3600*1000L;  // 24h
	
	public static void cachePersons(Context context) {
		
		Gson gson = new Gson();
		
		// must be used with TypeToken, gson/getClass does not work for parametrized class
		Type listType = new TypeToken<HashMap<Integer,Person>>() {}.getType();
		String json = gson.toJson(personLookup, listType);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("CACHE-PERSONS", json).commit();	
		if (debug) Log.d(TAG, "CACHE - writing persons. datetime=" +
		               DateFormat.format("dd.MM - hh:mm:ss", personLookupTime));
		prefs.edit().putLong("CACHETIME-PERSONS", personLookupTime).commit();

	}
	
	public static void cacheOrgs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Gson gson = new Gson();
		// must be used with TypeToken, gson/getClass does not work for parametrized class
		Type listType = new TypeToken<HashMap<Integer,String>>() {}.getType();
		String json = gson.toJson(organizationLookup, listType);
			
		prefs.edit().putString("CACHE-ORGS", json).commit();				
		if (debug) Log.d(TAG, "CACHE - writing organizations. datetime="+
		                DateFormat.format("dd.MM - hh:mm:ss",organizationLookupTime));
		prefs.edit().putLong("CACHETIME-ORGS", organizationLookupTime).commit();

	}
	
	public static boolean getCachePersons(Context context) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String cachedPersonsJson = prefs.getString("CACHE-PERSONS", "");
		Long ctPersons = prefs.getLong("CACHETIME-PERSONS", 0L);
		
		if ( (System.currentTimeMillis() - ctPersons) < MAX_CACHE_LIFETIME) {
			Gson gson = new Gson();
			// must be used with TypeToken, gson/getClass does not work for parametrized class
			Type listType = new TypeToken<HashMap<Integer,Person>>() {}.getType();
			personLookup = gson.fromJson(
					cachedPersonsJson,listType);
			personLookupTime = ctPersons;
			if (debug) Log.d(TAG, "CACHE - reading persons.  " + DateFormat.format("dd.MM - HH:mm:ss",personLookupTime));
			return true;
		} else {
			if (debug) Log.d(TAG, "CACHETIME-PERSONS too old: " + DateFormat.format("dd.MM - HH:mm:ss",personLookupTime));
			return false;
		}
		
	
	}
	
	public static boolean getCacheOrgs(Context context) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String cachedOrgsJson = prefs.getString("CACHE-ORGS", "");
		Long ctOrgs = prefs.getLong("CACHETIME-ORGS", 0L);
		
		if ( (System.currentTimeMillis() - ctOrgs) < MAX_CACHE_LIFETIME) {
			Gson gson = new Gson();
			
			// must be used with TypeToken, gson/getClass does not work for parametrized class
			Type listType = new TypeToken<HashMap<Integer,String>>() {}.getType();
			organizationLookup = gson.fromJson(
					cachedOrgsJson, listType);
			organizationLookupTime = ctOrgs;
			if (debug) Log.d(TAG, "reading orgs cache. datetime=" + DateFormat.format("dd.MM - HH:mm:ss",organizationLookupTime));
		    return true;
		} else {
			if (debug) Log.d(TAG, "CACHETIME-ORGS too old: " + DateFormat.format("dd.MM - HH:mm:ss",organizationLookupTime));
		    return false;
		}

	}
	
	public static void clearPersonsCache(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
		prefs.edit().putString("CACHE-PERSONS", "{}").commit();	
		prefs.edit().putLong("CACHETIME-PERSONS", 0L).commit();
		personLookup.clear();
		personLookupTime = 0L;
		if (debug) Log.d(TAG,"persons cache cleared.");
	}

	public static void clearOrgsCache(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("CACHE-ORGS", "{}").commit();	
		prefs.edit().putLong("CACHETIME-ORGS", 0L).commit();
		organizationLookup.clear();  
		organizationLookupTime = 0L;
	}
	public static void write(Context context,  long timemillis, String name, Object obj, Type listType) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Gson gson = new Gson();
	
		String json = gson.toJson(obj, listType);
		if (debug) Log.d(TAG, "CACHE - "+name+" writing object:" + json);		
		prefs.edit().putString("CACHE-"+name, json).commit();
				
		if (debug) Log.d(TAG, "CACHETIME-"+name+": " + DateFormat.format("dd.MM - hh:mm:ss",timemillis));
		prefs.edit().putLong("CACHETIME-"+name, timemillis).commit();

	}

	public static Object read(Context context, String name, Object obj, Type listType) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String json = prefs.getString("CACHE-"+name, "");
		Long timemillis = prefs.getLong("CACHETIME-"+name, 0L);
		
		if ( (System.currentTimeMillis() - timemillis) < MAX_CACHE_LIFETIME) {
			if (debug) Log.d(TAG, "cache can be used. "+name+" json="+json);
			Gson gson = new Gson();
			obj = gson.fromJson(
					json, listType);
			if (debug) Log.d(TAG, "CACHETIME- was: " + DateFormat.format("dd.MM - hh:mm:ss",timemillis));
		} else {
			obj = null;
			if (debug) Log.d(TAG, "CACHETIME- was too old: " + DateFormat.format("dd.MM - hh:mm:ss",timemillis));
		}
		
		return obj;
	}
}
