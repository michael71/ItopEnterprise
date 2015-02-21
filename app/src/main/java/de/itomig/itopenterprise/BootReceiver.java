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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static de.itomig.itopenterprise.ItopConfig.BACKGROUND_INTERVAL_MIN;
import static de.itomig.itopenterprise.ItopConfig.NOTIFY_NO;

/**
 * starts backgroundCheck after boot
 *
 * @author Michael Blank
 * @version 1.1
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // start only when notification is set in Preferences
        int notifySetting = ItopConfig.getItopNotifySetting();
        Log.i("ITOP", "BG: BootReceiver - notifySetting=" + notifySetting);

        if (notifySetting != NOTIFY_NO) {
            startAlarmMgr(context);
        }
    }

    public void startAlarmMgr(Context context) {
        Log.i("ITOP", "BG: startAlarmMgr - setting alarm");
        AlarmManager mgr = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 1);
        mgr.setRepeating(AlarmManager.RTC_WAKEUP,
                now.getTimeInMillis(), BACKGROUND_INTERVAL_MIN * 60 * 1000, sender);
    }
}