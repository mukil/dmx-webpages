/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mikromedia.webpages.model;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.mikromedia.webpages.WebpageService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malted
 */
public class SearchResult implements JSONEnabled {

    JSONObject result = new JSONObject();

    public SearchResult(Topic topic) {
        try {
            result.put("name", topic.getSimpleValue());
            if (topic.getTypeUri().equals(WebpageService.WEBPAGE)) {
                String href = getSiteLocation(getPageSite(topic)) + "/" + getPageAlias(topic);
                result.put("link", href);
            } else if (topic.getTypeUri().equals(WebpageService.WEBSITE)) {
                result.put("link", getSiteLocation(topic));
            }
        } catch (JSONException ex) {
            throw new RuntimeException("Constructing a SearchResult failed", ex);
        }
    }

    public SearchResult(Topic topic, String zusatzInfo) {
        try {
            SearchResult searchResult = new SearchResult(topic);
            searchResult.result.put("zusatz", zusatzInfo);
        } catch (JSONException ex) {
            throw new RuntimeException("Constructing a SearchResult failed", ex);
        }
    }

    // --- Private Utility Methods
   
    private String getPageAlias(Topic webpage) {
        return webpage.getChildTopics().getString("de.mikromedia.page.web_alias");
    }

    private Topic getPageSite(Topic webpage) {
        return webpage.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
    }

    private String getSiteLocation(Topic website) {
        Topic username = website.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "dm4.accesscontrol.username");
        return (username == null) ? "/" :  "/" + username.getSimpleValue().toString();
    }

    @Override
    public JSONObject toJSON() {
        return result;
    }

}
