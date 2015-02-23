// Copyright (C) 2011-2013 ITOMIG GmbH
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

package de.itomig.itopenterprise.cmdb;

import java.io.Serializable;

public class Person extends CMDBObject implements Serializable {
    private static final long serialVersionUID = -5998434777702343501L;
    private int org_id=1;
    private String friendlyname="";
    private String phone="";

    public Person(int id) {
        super(id);
    }
    public Person(int id, String friendlyname, String phone, int org_id) {
        super(id);

        this.org_id = org_id;
        this.friendlyname = friendlyname;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrg_id() {
        return org_id;
    }

    public void setOrg_id(int org_id) {
        this.org_id = org_id;
    }

    public String getFriendlyname() {

        if (friendlyname !=null) {
            return friendlyname;
        } else {
            return "?";
        }
    }

    public void setFriendlyname(String friendlyname) {
        this.friendlyname = friendlyname;
    }

    public String getPhone() {

        if (phone != null) {
            return phone;
        } else {
            return "?";
        }
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
