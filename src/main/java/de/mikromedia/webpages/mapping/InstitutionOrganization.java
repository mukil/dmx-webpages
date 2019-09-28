package de.mikromedia.webpages.mapping;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;

/**
 *
 * @author malt
 */
public class InstitutionOrganization {
    
    private Topic institution;

    public InstitutionOrganization(Topic institution) {
        this.institution = institution;
    }

    private List<RelatedTopic> getEntries(String typeUri) {
        return this.institution.getChildTopics().getTopicsOrNull(typeUri);
    }

    private String getEmailAddress() {
        List<RelatedTopic> emails = getEntries("dmx.contacts.email_address");
        if (emails != null && emails.size() > 0) {
            return emails.get(0).getSimpleValue().toString();
        }
        return null;
    }

    private String getWebsiteURL() {
        List<RelatedTopic> websites = getEntries("dmx.base.url");
        if (websites != null && websites.size() > 0) {
            return websites.get(0).getSimpleValue().toString();
        }
        return null;
    }

    private String getTelephoneEntry() {
        List<RelatedTopic> phoneEntries = getEntries("dmx.contacts.phone_number#dmx.contacts.phone_entry");
        if (phoneEntries != null && phoneEntries.size() > 0) {
            return phoneEntries.get(0).getSimpleValue().toString();
        }
        return null;
    }

    private String getAddressEntry() {
        List<RelatedTopic> addressEntry = getEntries("dmx.contacts.address#dmx.contacts.address_entry");
        if (addressEntry != null && addressEntry.size() > 0) {
            return addressEntry.get(0).getSimpleValue().toString();
        }
        return null;
    }

    public String toJSONLD() {
        try {
            JSONObject organization = new JSONObject();
            organization.put("@context", "http://schema.org");
            organization.put("@type", "Organization");
            organization.put("name", this.institution.getSimpleValue());
            organization.put("url", getWebsiteURL());
            organization.put("email", getEmailAddress());
            organization.put("telephone", getTelephoneEntry());
            organization.put("address", getAddressEntry());
            return organization.toString().replace("\\","");
        } catch (JSONException ex) {
            Logger.getLogger(InstitutionOrganization.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
