// Copyright (C) 2011-2015 ITOMIG GmbH
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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.itomig.itopenterprise.cmdb.InternalTask;
import de.itomig.itopenterprise.cmdb.Person;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.personLookup;
import static de.itomig.itopenterprise.ItopConfig.prioStrings;

public class TaskDetailActivity extends Activity {
    private TextView tvTitle, tvDesc, tvStatus, tvRemark;
    private TextView tvCaller;
    private ImageView priorityIcon, callCaller;
    private InternalTask t;
    private String callerPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn;

        if (debug)
            Log.i(TAG, "TaskDetailActivity - onCreate");

        setContentView(R.layout.task_details);

        btn = (Button) findViewById(R.id.button1);

        t = (InternalTask) getIntent().getSerializableExtra("task");

        priorityIcon = (ImageView) findViewById(R.id.image);

        tvTitle = (TextView) findViewById(R.id.titleText); // name

        tvDesc = (TextView) findViewById(R.id.descText);
        tvRemark = (TextView) findViewById(R.id.remarkText);

        tvStatus = (TextView) findViewById(R.id.statusText);
        tvCaller = (TextView) findViewById(R.id.callerText); // responsible

        callCaller = (ImageView) findViewById(R.id.callCallerImage);

        callCaller.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click: call "caller"
                if (callerPhone != null) {
                    call(callerPhone);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                toast("edit not yet implemented.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug)
            Log.i(TAG, "TaskDetailActivity - onResume");
        display();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug)
            Log.i(TAG, "TaskTicketActivity - onPause");
        startService(new Intent(BackgroundCheck.class.getName()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            String result = data.toURI();
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
    }

    private void display() {
        if (debug)
            Log.i(TAG, "TaskTicketActivity - display()");
        priorityIcon.setImageResource(t.prioImageResource(prioStrings));
        tvTitle.setText(t.name);
        tvStatus.setText("Status: " + t.status);

        // reset phone numbers and call icons.
        callerPhone = null;
        callCaller.setImageResource(R.drawable.nothing32);

        tvCaller.setText("resp.: " + t.person_id_friendlyname);

        Person p = personLookup.get(t.person_id);
        if (p != null) {
            if (p.getPhonenumber().length() > 7) {
                callCaller.setImageResource(R.drawable.call_contact);
                callerPhone = p.getPhonenumber();
            } else {
                callerPhone = null;
                callCaller.setImageResource(R.drawable.call_contact_off);
            }
        }

        tvDesc.setText(t.description);
        tvRemark.setText(t.remarks);

    }

    @SuppressWarnings("SameParameterValue")
    private void toast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    private void call(String num) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + num));
            startActivity(callIntent);

        } catch (ActivityNotFoundException activityException) {
            Log.e(TAG, "dialing - Call to " + num + " failed. ",
                    activityException);
        }
    }

}
