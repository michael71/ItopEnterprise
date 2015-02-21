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

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;

/**
 * AboutActivity for displaying some info about this application
 *
 * @author mblank
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String vinfo = "";
        TextView versTv;

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.about);
        versTv = (TextView) findViewById(R.id.version);
        int version = -1;
        String vName = "";

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            version = pInfo.versionCode;
            vName = pInfo.versionName;
            vinfo = "Version: " + vName + " (" + version + ")";

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (debug) Log.i(TAG, "About screen - version " + vinfo);
        versTv.setText(vinfo.toString());


    }

}
