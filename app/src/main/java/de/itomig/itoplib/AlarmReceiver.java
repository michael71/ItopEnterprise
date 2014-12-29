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

import android.content.BroadcastReceiver;
import static de.itomig.itoplib.ItopConfig.TAG;
import static de.itomig.itoplib.ItopConfig.debug;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.util.Log;

/**
 * receives the recurring alarm which does the BackgroundCheck
 * every X minutes, checks for working network before actually
 * requesting http data
 * 
 * @author Michael Blank
 * @copyriht
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	private static PowerManager.WakeLock wakeLock = null;
	private static final String LOCK_TAG = "de.itomig.itoplib";
	
	public static synchronized void acquireLock(Context context) {
		if (wakeLock == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
			wakeLock.setReferenceCounted(true);
		}
		wakeLock.acquire();
		if (debug) Log.i(TAG,"BG AlarmReceiver: aquired wakeLock" );
	}
	
	public static void releaseLock() {
		if ((wakeLock != null) && (wakeLock.isHeld())) {
				wakeLock.release();
				Log.i(TAG,"BG AlarmReceiver: released wakeLock" );
		}
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
        if (isNetworkConnected(context)) {
        	Log.i(TAG,"BG AlarmReceiver: onReceive, starting BackgroundCheck service");
        	acquireLock(context);
        	Intent serviceIntent = new Intent(context, BackgroundCheck.class);
        	context.startService(serviceIntent); 
        	
        	// update person and org info
        	PersonAndOrgsLookup pl = new PersonAndOrgsLookup();
        	pl.update();
        } else {
        	// do nothing
        	Log.i(TAG,"BG: no network connection.");
        }
        	
	}
 
	
	private boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			if (debug) Log.i(TAG,"BG: no network connection.");
			return false;
		}
	}

}
