package de.mikromedia.webpages.models;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.service.CoreService;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WebpageViewModel implements JSONEnabled {

	public Topic page;

	public WebpageViewModel(Topic pageAliasTopic) {
		this.page = pageAliasTopic.getRelatedTopic("dm4.core.composition",
		    "dm4.core.child", "dm4.core.parent", "de.mikromedia.page");
		if (!isWebpageTopic(this.page)) {
			throw new IllegalArgumentException("Given topic is not of type Webpage");
		}
		this.page.loadChildTopics();
	}

	public WebpageViewModel(long topicId, CoreService dms) {
		this.page = dms.getTopic(topicId);
		if (!isWebpageTopic(this.page)) {
			throw new IllegalArgumentException("Given topic is not of type Webpage");
		}
		this.page.loadChildTopics();
	}

	public String getPageTitle() {
		return page.getSimpleValue().toString();
	}

	public String getPageHtmlText() {
		return page.getChildTopics().getString("de.mikromedia.page.header");
	}

	public String getPageWebAlias() {
		return page.getChildTopics().getString("de.mikromedia.page.web_alias");
	}

	public Date getPageModificationDate() {
		Object modified = page.getProperty("dm4.time.modified");
		Date modificationDate = new Date();
		modificationDate.setTime((Long) modified);
		// DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY);
		// df.format(modificationDate);
		return modificationDate;
	}

	public Date getPageCreationDate() {
		Object created = page.getProperty("dm4.time.created");
		Date creationDate = new Date();
		creationDate.setTime((Long) created);
		// DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY);
		return creationDate;
	}

	public boolean isDraft() {
		return page.getChildTopics().getBoolean("de.mikromedia.page.is_draft");
	}

	public String getAuthorNames() {
		String nameOfAuthors = "";
                List<RelatedTopic> authorNames = page.getChildTopics().getTopicsOrNull("de.mikromedia.page.author_name");
		if (authorNames != null) {
                    Iterator<RelatedTopic> nameIterator = authorNames.iterator();
                    while (nameIterator.hasNext()) {
                            RelatedTopic authorName = nameIterator.next();
                            nameOfAuthors += authorName.getSimpleValue();
                            if (nameIterator.hasNext()) nameOfAuthors += ", ";
                    }
                }
		return nameOfAuthors;
	}
	
	public JSONObject toJSON() {
		try {
			return new JSONObject()
			    .put("title", getPageTitle())
			    .put("header", getPageHtmlText())
			    .put("modification_date", getPageModificationDate())
			    .put("creation_date", getPageCreationDate())
				.put("author_names", getAuthorNames())
				.put("web_alias", getPageWebAlias());
			 /** .put("web_description", webDescription) **/
		} catch (JSONException ex) {
			Logger.getLogger(WebpageViewModel.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	private boolean isWebpageTopic(Topic topic) {
		return (topic.getTypeUri().equals("de.mikromedia.page"));
	}

}
