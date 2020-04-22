package de.mikromedia.webpages.model;

import de.mikromedia.webpages.WebpageService;
import static systems.dmx.core.Constants.*;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_ALIAS;
import static de.mikromedia.webpages.WebpageService.WEBSITE;
import static de.mikromedia.webpages.WebpageService.WEBSITE_PREFIX;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import systems.dmx.core.JSONEnabled;
import systems.dmx.core.Topic;

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
                Topic site = getPageSite(topic);
                String href = getSitePrefix(site) + "/" + getPageAlias(topic);
                result.put("site", (site == null) ? "undefined" : site.toJSON().toString());
                if (site != null && !site.getUri().equals("de.mikromedia.standard_site")) {
                    result.put("zusatz", "in <em>" + site.getSimpleValue().toString() + "</em>");
                }
                result.put("link", href);
            } else if (topic.getTypeUri().equals(WebpageService.WEBSITE)) {
                result.put("link", getSitePrefix(topic));
            }
        } catch (Exception ex) {
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
        return webpage.getChildTopics().getString(WEBPAGE_ALIAS);
    }

    private Topic getPageSite(Topic webpage) {
        return webpage.getRelatedTopic(ASSOCIATION, DEFAULT, DEFAULT, WEBSITE);
    }

    private String getSitePrefix(Topic website) {
        String sitePrefix = website.getChildTopics().getStringOrNull(WEBSITE_PREFIX);
        if (sitePrefix != null && sitePrefix.equals("standard")) return "";
        return (sitePrefix == null) ? "" :  "/" + sitePrefix;
    }

    @Override
    public JSONObject toJSON() {
        return result;
    }

}
