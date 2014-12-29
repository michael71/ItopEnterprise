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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import android.os.Bundle;
import android.util.Log;

public class MyActivity extends MainActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String getMyRequests = "SELECT UserRequest AS i WHERE i.agent_id=:current_contact_id "+
				"AND i.status!='Closed' AND i.status!='Resolved'";
		try {
			expression = URLEncoder.encode(getMyRequests, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,"MyActivity: "+e.toString());
		}
	}
}