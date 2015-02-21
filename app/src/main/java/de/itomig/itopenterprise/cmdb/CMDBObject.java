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

package de.itomig.itopenterprise.cmdb;

import java.io.Serializable;

import static de.itomig.itopenterprise.ItopConfig.INVALID_ID;

public class CMDBObject implements Serializable {
    // Serializable is needed to "hook it up" on an intent with
    private static final long serialVersionUID = -5998434779602343501L;

    public int id = INVALID_ID;

    public CMDBObject(int id) {
        super();
        this.id = id;
    }


}
