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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.util.Log;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

/**
 * receives the recurring alarm which does the BackgroundCheck
 * every X minutes, checks for working network before actually
 * requesting http data
 *
 * @author Michael Blank
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOCK_TAG = "de.itomig.itopenterprise";
    private static PowerManager.WakeLock wakeLock = null;

    public static synchronized void acquireLock(Context context) {
        if (wakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
            wakeLock.setReferenceCounted(true);
        }
        wakeLock.acquire();
        if (debug) Log.i(TAG, "BG AlarmReceiver: aquired wakeLock");
    }

    public static void releaseLock() {
        if ((wakeLock != null) && (wakeLock.isHeld())) {
            wakeLock.release();
            if (debug) Log.i(TAG, "BG AlarmReceiver: released wakeLock");
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkConnected(context)) {
            if (debug) Log.i(TAG, "BG AlarmReceiver: onReceive, starting BackgroundCheck service");
            acquireLock(context);
            Intent serviceIntent = new Intent(context, de.itomig.itopenterprise.BackgroundCheck.class);
            context.startService(serviceIntent);

            // update person and org info
            de.itomig.itopenterprise.PersonAndOrgsLookup pl = new de.itomig.itopenterprise.PersonAndOrgsLookup();
            pl.update();
        } else {
            // do nothing
            Log.e(TAG, "BG: no network connection.");
        }

    }


    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.e(TAG, "BG: no network connection.");
            return false;
        }
    }

}
