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

package de.itomig.itopmobile;

import static de.itomig.itoplib.ItopConfig.*;
import static de.itomig.itopmobile.ItopApplication.DEBUG;
import de.itomig.itoplib.AboutActivity;
import de.itomig.itoplib.AlarmReceiver;
import de.itomig.itoplib.HelpdeskActivity;
import de.itomig.itoplib.IncidentActivity;
import de.itomig.itoplib.InternalTaskActivity;
import de.itomig.itoplib.MyActivity;
import de.itomig.itoplib.Preferences;
import de.itomig.itoplib.SearchActivity;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import de.itomig.itoplib.R;

/**
 * this is the itopMobile MAIN activity, with tabs for Helpdesk, (Incident)
 * MyTickets and (Tasks)
 * also starts background alarm to perform http requests to itop server in 
 * the Background (when itopMobile Activity is not visible)
 * 
 * @author Michael Blank
 * @version 2.01
 */
public class ItopMobileActivity extends TabActivity {

	private static final String TAG = "ITOP";

	private static boolean enableITILTickets;
	private static boolean enableTasks;
	

	@SuppressWarnings("unused")
	private NotificationManager notificationManager;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// switch statement not possible for android sdk>14 and using a library
		// !!!
		if (item.getItemId() == R.id.menu_settings) { // call preferences
														// activity
			startActivity(new Intent(this, Preferences.class));

			return (true);

/*	TODO	} else if (item.getItemId() == R.id.menu_add) {
			startActivity(new Intent(this, AddTaskActivity.class));
			return (true);  */
		}
		else if (item.getItemId() == R.id.menu_search) {
			startActivity(new Intent(this, SearchActivity.class));
			return (true);

		} else if (item.getItemId() == R.id.menu_about) {
			startActivity(new Intent(this, AboutActivity.class));
			return (true);
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// needs to be called before anything else - these features are only
		// used
		// in the tab-activities but must be requested here!
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.main);
		if (DEBUG)
			Log.i(TAG, "ItopMobileActivity - onCreate");
		int whoCalled = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			whoCalled = extras.getInt("type");
			if (DEBUG)
				Log.i(TAG, "..extra type=" + whoCalled);
		} else {
			if (DEBUG)
				Log.i(TAG, "no extras");
		}
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, HelpdeskActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("helpdesk")
				.setIndicator("Helpdesk",
						res.getDrawable(R.drawable.user_request))
				.setContent(intent);
		tabHost.addTab(spec);

		// Display Incident TAB only when using "ITIL" ticketing
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		enableITILTickets = prefs.getBoolean(KEY_ITIL_TICKETS, false);
		if (enableITILTickets) {
			
			intent = new Intent().setClass(this, IncidentActivity.class);
			spec = tabHost
					.newTabSpec("incident")
					.setIndicator("Incidents",
							res.getDrawable(R.drawable.incident32))
					.setContent(intent);
			tabHost.addTab(spec);
		}
		
	

		// display myTickets, i.e. where agent_id=my-own-id
		intent = new Intent().setClass(this, MyActivity.class);
		spec = tabHost.newTabSpec("myTickets")
				.setIndicator("MyTickets", res.getDrawable(R.drawable.person))
				.setContent(intent);
		tabHost.addTab(spec);

		
		enableTasks = prefs.getBoolean(KEY_TASKS, false);
		if (enableTasks) {	
			intent = new Intent().setClass(this, InternalTaskActivity.class);
			spec = tabHost
					.newTabSpec("mytasks")
					.setIndicator("MyTasks",
							res.getDrawable(R.drawable.incident32))
					.setContent(intent);
			tabHost.addTab(spec);
		}

		tabHost.setCurrentTab(0);
		
		/* start notification service */
		String svcName = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) getSystemService(svcName);
		
		/* start also background check service */

		startRepeatingAlarm();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.i(TAG, "ItopMobileActivity - onDestroy");

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG)
			Log.i(TAG, "ItopMobileActivity - onResume");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean newEnableITILTickets = prefs
				.getBoolean(KEY_ITIL_TICKETS, false);
		boolean newEnableTasks = prefs
				.getBoolean(KEY_TASKS, false);

		// restart needed when TABs should be changed.
		if ( (enableITILTickets != newEnableITILTickets) ||
			 (enableTasks != newEnableTasks) )
			restartActivity();

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG)
			Log.i(TAG, "ItopMobileActivity - onPause");
	}


	
	private void startRepeatingAlarm() {
		Log.i("ITOP", "BG: startRepeatingAlarm - setting alarm");
		AlarmManager mgr = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, 1); // first alarm in 1 Minute from now
		mgr.setRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(),
				BACKGROUND_INTERVAL_MIN * 60 * 1000, sender);
	}

	private void restartActivity() {
		// if (Build.VERSION.SDK_INT >= 11) {
		// recreate();
		// } else {
		Log.d(TAG, "Restart activity " + this);
		Intent intent = getIntent();
		finish();
		startActivity(intent);
		// }
	}

}