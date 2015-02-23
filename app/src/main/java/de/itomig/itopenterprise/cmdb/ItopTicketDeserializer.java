package de.itomig.itopenterprise.cmdb;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializer;
import java.lang.reflect.Type;

import static de.itomig.itopenterprise.ItopConfig.TAG;


/**
 * Created by mblank on 23.02.15.
 */
public class ItopTicketDeserializer implements JsonDeserializer<ItopTicket> {

    @Override
    public ItopTicket deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final ItopTicket ticket = new ItopTicket("UserRequest");
        //"ref, title, priority, start_date, tto_escalation_deadline, caller_id, agent_id, status, last_update, description, public_log"
        final String ref= jsonObject.get("ref").getAsString();
        ticket.setRef(ref);

        final JsonElement jsonTitle = jsonObject.get("title");
        final String title = jsonTitle.getAsString();
        ticket.setTitle(title);

        final String priority = jsonObject.get("priority").getAsString();
        ticket.setPriority(priority);

        final String status = jsonObject.get("status").getAsString();
        ticket.setStatus(status);

        final String last_update = jsonObject.get("last_update").getAsString();
        ticket.setLast_update(last_update);

        /* the following are just WANT and might not exist in response - just ignore */
        try {
            final String start_date = jsonObject.get("start_date").getAsString();
            ticket.setStart_date(start_date);

            final String tto_escalation_deadline = jsonObject.get("tto_escalation_deadline").getAsString();
            ticket.setTto_escalation_deadline(tto_escalation_deadline);

            final String caller_id = jsonObject.get("caller_id").getAsString();
            ticket.setCaller_id(caller_id);

            final String agent_id = jsonObject.get("agent_id").getAsString();
            ticket.setAgent_id(agent_id);

            final String description = jsonObject.get("description").getAsString();
            ticket.setDescription(description);

            final JsonElement jsonPublicLogObject = jsonObject.get("public_log");

            final JsonArray jsonPublicLogArray = jsonPublicLogObject.getAsJsonObject().get("entries").getAsJsonArray();
            for (int i = 0; i < jsonPublicLogArray.size(); i++) {
                final JsonElement jsonPLEntry = jsonPublicLogArray.get(i);
                PublicLogEntry ple = new PublicLogEntry();
                ple.setDate(jsonPLEntry.getAsJsonObject().get("date").getAsString());
                ple.setMessage(jsonPLEntry.getAsJsonObject().get("message").getAsString());
                //ple.setUser_id(jsonPLEntry.getAsJsonObject().get("user_id").getAsInt()); is always =null
                ple.setUser_login(jsonPLEntry.getAsJsonObject().get("user_login").getAsString());
                ticket.addPublicLogEntry(ple);

            }
            ticket.sortPublic_log();
        } catch (Exception e) {
            Log.i(TAG,"in json deserialize:"+e.getMessage());
        }
        return ticket;
    }
}
