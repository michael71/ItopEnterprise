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
//

package de.itomig.itoplib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import static de.itomig.itoplib.ItopConfig.*;
import de.itomig.itoplib.cmdb.Person;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/** !! TODO implementation not yet finished !!
 *  
 * @author mblank
 *
 */
public class AddTaskActivity extends Activity {
	private Spinner spPerson, spOrg, spStatus, spPrio;
	private Button btnCancel, btnSave;
	private EditText title, desc, remark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		spPerson = (Spinner) findViewById(R.id.spPerson);
		spOrg = (Spinner) findViewById(R.id.spOrg);
		spStatus = (Spinner) findViewById(R.id.spStatus);
		spPrio = (Spinner) findViewById(R.id.spPriority);
		title = (EditText) findViewById(R.id.titleInput);
		desc = (EditText) findViewById(R.id.descInput);
		remark = (EditText) findViewById(R.id.remarkInput);

		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnSave = (Button) findViewById(R.id.btnSave);

		addItemsOnSpinners();

		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String p = (String) spPerson.getSelectedItem();
				String o = (String) spOrg.getSelectedItem();
				String stat = (String) spStatus.getSelectedItem();
				String prio = (String) spPrio.getSelectedItem();

				toast("not yet implemented \ntitle" + title.getText() + "\np="
						+ p + "\no=" + o + "\nprio=" + prio + "\nstat=" + stat);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.add, menu);
		return true;
	}

	private void addItemsOnSpinners() {
		List<String> list = new ArrayList<String>();

		// organization spinner
		List<String> orglist = new ArrayList<String>();
		int idItomig = 0;

		Iterator<Entry<Integer, String>> it2 = organizationLookup.entrySet()
				.iterator();
		while (it2.hasNext()) {
			Entry<Integer, String> pairs = it2.next();
			if (debug)
				Log.d(TAG,
						"orgLookup " + pairs.getKey() + " = "
								+ (pairs.getValue()));
			orglist.add(pairs.getValue());
			if (pairs.getValue().toUpperCase().contains("ITOMIG"))
				idItomig = pairs.getKey();
		}

		ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(this,
				R.layout.spinner_item, orglist);
		dataAdapter3
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spOrg.setAdapter(dataAdapter3);
		// make ITOMIG default
		int i = orglist.indexOf("ITOMIG");
		spOrg.setSelection(i);

		// select only ITOMIG people for Tasklist
		Iterator<Entry<Integer, Person>> it = personLookup.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<Integer, Person> pairs = it.next();
			// if (pairs.getValue().)
			if (idItomig != 0) {
				// get only ITOMIG employees
				if (pairs.getValue().getOrg_id() == idItomig) {
					if (debug)
						Log.d(TAG, "personLookup " + pairs.getKey() + " = "
								+ ((Person) pairs.getValue()).getFriendlyname());
					list.add(((Person) pairs.getValue()).getFriendlyname());
				}
			} else {
				// get all people if ITOMIG not found
				if (debug)
					Log.d(TAG, "personLookup " + pairs.getKey() + " = "
							+ ((Person) pairs.getValue()).getFriendlyname());
				list.add(((Person) pairs.getValue()).getFriendlyname());
			}
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPerson.setAdapter(dataAdapter);

		Resources res = getResources();
		String[] prios = res.getStringArray(R.array.priority);
		ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
				R.layout.spinner_item, prios);
		dataAdapter2
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPrio.setAdapter(dataAdapter2);
		spPrio.setSelection(1);

		String[] status = res.getStringArray(R.array.status);
		ArrayAdapter<String> dataAdapter4 = new ArrayAdapter<String>(this,
				R.layout.spinner_item, status);
		dataAdapter4
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spStatus.setAdapter(dataAdapter4);

	}

	public void toast(String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

}
