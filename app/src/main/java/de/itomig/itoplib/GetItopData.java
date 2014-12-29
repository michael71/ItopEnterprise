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


import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.itomig.itoplib.cmdb.ItopTicket;
import de.itomig.itoplib.cmdb.Organization;
import de.itomig.itoplib.cmdb.Person;
import de.itomig.itoplib.cmdb.XmlResult;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import static de.itomig.itoplib.ItopConfig.*;

public class GetItopData {

	private static ArrayList<ItopTicket> tickets = new ArrayList<ItopTicket>();
	private static ArrayList<Person> persons = new ArrayList<Person>();
	private static ArrayList<Organization> organizations = new ArrayList<Organization>();

	// called from AsyncTask - therefor no UI context, no Toast possible from
	// here.
	public static ArrayList<ItopTicket> getTicketsFromItopServer(
			String selectExpression) throws Exception {
		
		XmlResult result = getDataFromItopServer(selectExpression);

		if (result.error == "") {
			// Parse the data
			tickets = parseTicketDoc(result.doc);
			return tickets;
		} else {
			tickets.clear();
			tickets.add(new ItopTicket(
					ERROR,
					result.error,
					"", "3", ""));

			return null;
		}

	
	}
	

	private static String createUrlWithCredentials(String expr) {
		String url = ItopConfig.getItopUrl();
		String cred = ItopConfig.getItopCredentials();
		return url + "/webservices/export.php?" + cred + "&expression=" + expr
				+ "&format=xml&no_localize=1";
	}

	private static ArrayList<ItopTicket> parseTicketDoc(Document doc) {
		// assemble new ArrayList of tickets.
		tickets.clear();
		Element root = doc.getDocumentElement();

		// look for UserRequest
		NodeList items = root.getElementsByTagName("UserRequest");
		for (int i = 0; i < items.getLength(); i++) {
			tickets.add(parseTicketNode("UserRequest", items.item(i)));
		}

		// look for Incident
		items = root.getElementsByTagName("Incident");
		for (int i = 0; i < items.getLength(); i++) {
			tickets.add(parseTicketNode("Incident", items.item(i)));
		}

		return tickets; // UserRequests and Incidents
	}

	private static ItopTicket parseTicketNode(String type, Node item) {
		// ticket node can be Incident oder UserRequest
		ItopTicket ticket = new ItopTicket(type);
		NodeList properties = item.getChildNodes();
		for (int j = 0; j < properties.getLength(); j++) {
			Node property = properties.item(j);
			String name = property.getNodeName();
			if (name.equalsIgnoreCase("ref")) {
				ticket.setRef(property.getFirstChild().getNodeValue().trim());
			} else if (name.equalsIgnoreCase("title")) {
				ticket.setTitle(getConcatNodeValues(property));
			} else if (name.equalsIgnoreCase("priority")) {
				ticket.setPriority(property.getFirstChild().getNodeValue()
						.trim());
			} else if (name.equalsIgnoreCase("description")) {
				ticket.setDescription(getConcatNodeValues(property));
			} else if (name.equalsIgnoreCase("start_date")) {
				ticket.setStartDate(property.getFirstChild().getNodeValue()
						.trim());
			} else if (name.equalsIgnoreCase("tto_escalation_deadline")) {
				ticket.setTtoEscalationDate(getConcatNodeValues(property));
			} else if (name.equalsIgnoreCase("caller_id")) {
				ticket.setCallerID(property.getFirstChild().getNodeValue()
						.trim());
			} else if (name.equalsIgnoreCase("agent_id")) {
				ticket.setAgentID(property.getFirstChild().getNodeValue()
						.trim());
			} else if (name.equalsIgnoreCase("status")) {
				ticket.setStatus(property.getFirstChild().getNodeValue().trim());
			} else if (name.equalsIgnoreCase("last_update")) {
				ticket.setLastUpdate(getConcatNodeValues(property));
			} else if (name.equalsIgnoreCase("ticket_log")) { // itop 1.2
				ticket.setTicketLog(getConcatNodeValues(property));
			} else if (name.equalsIgnoreCase("public_log")) { // itop 2.0
				ticket.setTicketLog(getConcatNodeValues(property));
			}
		}
		return ticket;

	}

	private static String getConcatNodeValues(Node prop) {
		// behaves well for non-existing nodes and for node values which are
		// broken into several values because of special characters like '"'
		// needed for the android code - this problem only exists in
		// Android xml library and not on the PC !!
		if (prop.hasChildNodes()) { // false for optional attributes
			StringBuilder text = new StringBuilder();
			NodeList chars = prop.getChildNodes();
			for (int k = 0; k < chars.getLength(); k++) {
				text.append(chars.item(k).getNodeValue());
			}
			return text.toString().trim();
		} else {
			return (""); // return empty string if empty
		}
	}

	public static ArrayList<Organization> getOrganizationsFromItopServer(
			String selectExpression)  {
		XmlResult result = getDataFromItopServer(selectExpression);

		if (result.error == "") {
			// Parse the data
			organizations = parseOrganizationDoc(result.doc);
			return organizations;
		} else {
			return null;
		}

	}

	public static ArrayList<Person> getPersonsFromItopServer(
			String selectExpression) {

		XmlResult result = getDataFromItopServer(selectExpression);

		if (result.error == "") {
			// Parse the data
			persons = parsePersonDoc(result.doc);
			return persons;
		} else {
			return null;
		}

	}

	private static ArrayList<Person> parsePersonDoc(Document doc) {
		// get the root elememt
		Element root = doc.getDocumentElement();
		NodeList items = root.getElementsByTagName("Person");

		persons.clear();
		for (int i = 0; i < items.getLength(); i++) {
			Node item = items.item(i);
			String person_id = ((Element) items.item(i)).getAttribute("id");
			NodeList properties = item.getChildNodes();

			String person_first = "";
			String person_name = "";
			String phone = "";
			String org="0";
			for (int j = 0; j < properties.getLength(); j++) {
				Node property = properties.item(j);
				String name = property.getNodeName();
				if (name.equalsIgnoreCase("name")) {
					person_name = getConcatNodeValues(property);
				} else if (name.equalsIgnoreCase("first_name")) {
					person_first = getConcatNodeValues(property);
				} else if (name.equalsIgnoreCase("phone")) {
					phone = getConcatNodeValues(property);
				} else if (name.equalsIgnoreCase("org_id")) {
					org = getConcatNodeValues(property);
				}
			}
			// there should ALLWAYS be an id.

			int id, org_id;
			try {
				id = Integer.parseInt(person_id);
				org_id = Integer.parseInt(org);
				persons.add(new Person(id, person_first + " " + person_name,
						phone,org_id));
			} catch (NumberFormatException e) {
				// should never happen
				Log.e("TAG", "id of person not parseable");
			}
		}
		return persons;
	}

	private static ArrayList<Organization> parseOrganizationDoc(Document doc) {
		// get the root elememt
		Element root = doc.getDocumentElement();
		NodeList items = root.getElementsByTagName("Organization");

		organizations.clear();
		for (int i = 0; i < items.getLength(); i++) {
			Node item = items.item(i);
			String org_id = ((Element) items.item(i)).getAttribute("id");
			NodeList properties = item.getChildNodes();
            String org_name="";
			for (int j = 0; j < properties.getLength(); j++) {
				Node property = properties.item(j);
				String name = property.getNodeName();
				if (name.equalsIgnoreCase("name")) {
					org_name = getConcatNodeValues(property);
				} 
			}
			// there should ALLWAYS be an id.

			int id;
			try {
				if (org_name !="") {
					id = Integer.parseInt(org_id);
					organizations.add(new Organization(id, org_name));
				} else {
					Log.e(TAG,"no name for org with id="+org_id);
				}
			} catch (NumberFormatException e) {
				Log.e("TAG", "id of person not parseable");
			}
		}
		return organizations;
	}

	public static XmlResult getDataFromItopServer(String selectExpression) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		XmlResult x = new XmlResult();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			x.error = "error: " + e1.getMessage();
			return x;
		}

		AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

		try {
			HttpPost request = new HttpPost();
			String req = createUrlWithCredentials(selectExpression);
			// do not log password, log only search expression
			if (debug)
				Log.i(TAG, "expr.=" + selectExpression);
			request.setURI(new URI(req));

			HttpResponse response = client.execute(request);
			String status = response.getStatusLine().toString();
			if (debug)
				Log.i(TAG, "status: " + status);

			if (status.contains("200") && status.contains("OK")) {

				// request worked fine, retrieved some data
				InputStream instream = response.getEntity().getContent();
				x.doc = builder.parse(instream);
				x.error = "";
			} else // some error in http response
			{
				Log.e(TAG, "Get data - http-ERROR: " + status);
				x.error = "http-error, status " + status;
			}

		} catch (Exception e) {
			// Toast does not work in background task
			Log.e(TAG, "Get data -  " + e.toString());
			x.error = "Get data -  " + e.toString();
		} finally {
			client.close(); // needs to be done for androidhttpclient
			if (debug)
				Log.i(TAG, "...finally.. get data finished");
		}

		return x;
	}
}
