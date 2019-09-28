package de.mikromedia.webpages.model;

import static de.mikromedia.webpages.WebpageService.AUTHOR_NAME;
import static de.mikromedia.webpages.WebpageService.COMPOSITION;
import static de.mikromedia.webpages.WebpageService.ROLE_CHILD;
import static de.mikromedia.webpages.WebpageService.ROLE_PARENT;
import static de.mikromedia.webpages.WebpageService.TIME_CREATED;
import static de.mikromedia.webpages.WebpageService.TIME_MODIFIED;
import static de.mikromedia.webpages.WebpageService.WEBPAGE;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_ABOUT;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_ALIAS;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_CONTENT;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_CSS;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_IS_DRAFT;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import systems.dmx.core.JSONEnabled;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.service.CoreService;

public class Webpage implements JSONEnabled {

    public Topic page;

    public Webpage(Topic pageAliasTopic) {
        this.page = pageAliasTopic.getRelatedTopic(COMPOSITION,
            ROLE_CHILD, ROLE_PARENT, WEBPAGE);
        if (!isWebpageTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Webpage");
        }
        this.page.loadChildTopics();
    }

    public Webpage(long topicId, CoreService dms) {
        this.page = dms.getTopic(topicId);
        if (!isWebpageTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Webpage");
        }
        this.page.loadChildTopics();
    }

    public long getId() {
        return page.getId();
    }

    public String getTitle() {
        return page.getSimpleValue().toString();
    }

    public String getDescription() {
        return page.getChildTopics().getStringOrNull(WEBPAGE_ABOUT);
    }

    public String getMainHTML() {
        return page.getChildTopics().getStringOrNull(WEBPAGE_CONTENT);
    }

    public Topic getTopic() {
        return this.page;
    }

    public String getStylesheet() {
        return page.getChildTopics().getStringOrNull(WEBPAGE_CSS);
    }

    public String getWebAlias() {
        return page.getChildTopics().getStringOrNull(WEBPAGE_ALIAS);
    }

    public Date getModificationDate() {
        Object modified = page.getProperty(TIME_MODIFIED);
        Date modificationDate = new Date();
        modificationDate.setTime((Long) modified);
        // DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY);
        // df.format(modificationDate);
        return modificationDate;
    }

    public Date getCreationDate() {
        Object created = page.getProperty(TIME_CREATED);
        Date creationDate = new Date();
        creationDate.setTime((Long) created);
        // DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMANY);
        return creationDate;
    }

    public boolean isDraft() {
        boolean isDraft = false;
        try {
            isDraft = page.getChildTopics().getBoolean(WEBPAGE_IS_DRAFT);
        } catch (Exception ex) {
            return isDraft;
        }
        return isDraft;
    }

    public String getAuthorNames() {
        String nameOfAuthors = "";
        List<RelatedTopic> authorNames = page.getChildTopics().getTopicsOrNull(AUTHOR_NAME);
        if (authorNames != null) {
            Iterator<RelatedTopic> nameIterator = authorNames.iterator();
            while (nameIterator.hasNext()) {
                RelatedTopic authorName = nameIterator.next();
                nameOfAuthors += authorName.getSimpleValue();
                if (nameIterator.hasNext()) {
                    nameOfAuthors += ", ";
                }
            }
        }
        return nameOfAuthors;
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("title", getTitle())
                .put("description", getDescription())
                .put("main", getMainHTML())
                .put("modified", getModificationDate())
                .put("created", getCreationDate())
                .put("author_names", getAuthorNames())
                .put("web_alias", getWebAlias());
            /**
             * .put("web_description", webDescription) *
             */
        } catch (JSONException ex) {
            Logger.getLogger(Webpage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isWebpageTopic() {
        if (this.page == null) return false;
        return (this.page.getTypeUri().equals("de.mikromedia.page"));
    }

}
