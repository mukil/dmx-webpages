package de.mikromedia.pages;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PageModel implements JSONEnabled {

	public Topic page;

	public PageModel(Topic pageAliasTopic) {
		this.page = pageAliasTopic.getRelatedTopic("dm4.core.composition",
		    "dm4.core.child", "dm4.core.parent", "de.mikromedia.page");
		this.page.loadChildTopics();
	}

	public String getPageTitle() {
		return page.getSimpleValue().toString();
	}

	public String getPageText() {
		return page.getChildTopics().getString("de.mikromedia.page.header_paragraph");
	}

	public String getPageModificationDate() {
		return page.getProperty("dm4.time.modified").toString();
	}

	public String getPageCreationDate() {
		return page.getProperty("dm4.time.created").toString();
	}

	public boolean isPublished() {
		return page.getChildTopics().getBoolean("de.mikromedia.page.is_published");
	}

	public String getAuthors() {
		String nameOfAuthors = "";
		return nameOfAuthors;
	}
	
	public JSONObject toJSON() {
		try {
			return new JSONObject()
			    .put("title", getPageTitle())
			    .put("text", getPageText())
			    .put("date", getPageModificationDate());
			/* .put("web_alias", webAlias)
			 .put("web_description", webDescription)
			 .put("author_name", authorName); */
		} catch (JSONException ex) {
			Logger.getLogger(PageModel.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

}
