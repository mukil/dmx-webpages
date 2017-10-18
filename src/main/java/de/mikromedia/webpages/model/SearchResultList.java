package de.mikromedia.webpages.model;

import de.deepamehta.core.JSONEnabled;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malted
 */
public class SearchResultList implements JSONEnabled {

    JSONObject list;
    String status = "OK";

    public SearchResultList() {
        try {
            this.list = new JSONObject();
            JSONObject results = new JSONObject();
            JSONObject pages = new JSONObject();
                pages.put("name", "Pages");
                pages.put("results", new JSONArray());
            JSONObject sites = new JSONObject();
                sites.put("name", "Websites");
                sites.put("results", new JSONArray());
            results.put("cat1", pages);
            results.put("cat2", sites);
            list.put("results", results);
            // list.put("action", new JSONObject().put("url", "javascript:do_fulltext_search()").put("text", "Volltextsuche starten"));
        } catch (JSONException ex) {
            throw new RuntimeException("Constructing a ResultList failed", ex);
        }
    }

    public void putPageResult(SearchResult result) throws JSONException {
        list.getJSONObject("results").getJSONObject("cat1")
            .getJSONArray("results").put(result.toJSON());
    }

    public void putWebsiteResult(SearchResult result) throws JSONException {
        list.getJSONObject("results").getJSONObject("cat2")
            .getJSONArray("results").put(result.toJSON());
    }

    @Override
    public JSONObject toJSON() {
        try {
            list.put("status", this.status);
        } catch (JSONException ex) {
            Logger.getLogger(SearchResultList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

}
