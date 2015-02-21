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

import android.app.Application;
import android.util.Log;

/**
 * starts backgroundCheck after boot
 *
 * @author Michael Blank
 * @version 2.01 (34)
 */
public class ItopApplication extends Application {
    // version 34: - released to public domain

    // version 32: - background check service only started when network connected

    public static final boolean DEBUG = true;
    // DEBUG must be false for all versions released to android market
    // also remove    android:debuggable="true"  in the AndroidManifest.xml

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) Log.i("ITOP", "ItopApplication - onCreate");
        ItopConfig.init(getApplicationContext(), ItopEnterpriseActivity.class);

    }


}
