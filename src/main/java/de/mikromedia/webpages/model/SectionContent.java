package de.mikromedia.webpages.model;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import static de.mikromedia.webpages.WebpageService.ASSOCIATION;
import static de.mikromedia.webpages.WebpageService.DEEPAMEHTA_FILE;
import static de.mikromedia.webpages.WebpageService.DESKTOP_IMAGE_ASSOC;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.MOBILE_IMAGE_ASSOC;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import static de.mikromedia.webpages.WebpageService.SECTION_BG_COLOR;
import static de.mikromedia.webpages.WebpageService.SECTION_COLOR;
import static de.mikromedia.webpages.WebpageService.SECTION_CONTENT;
import static de.mikromedia.webpages.WebpageService.SECTION_HEADLINE;
import static de.mikromedia.webpages.WebpageService.SECTION_HTML;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malt
 */
public class SectionContent {
    
    private Topic content;
    private Topic relatedTopic;

    public SectionContent(Topic sectionContent) {
        this.content = sectionContent;
        if (!isSectionContentTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Section Content");
        }
        this.content.loadChildTopics();
    }

    public SectionContent(long topicId, CoreService dms) {
        this.content = dms.getTopic(topicId);
        if (!isSectionContentTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Section Content");
        }
        this.content.loadChildTopics();
    }

    public long getId() {
        return this.content.getId();
    }

    public Topic getTopic() {
        return this.content;
    }

    // --- Custom Section Data Accessors
    
    public String getTitle() {
        return this.content.getChildTopics().getStringOrNull(SECTION_HEADLINE);
    }

    public String getHtml() {
        return this.content.getChildTopics().getStringOrNull(SECTION_HTML);
    }

    /**
     * If some DM standard topic is directly associated with a "Section Content" topic
     * that topic should be given priority in the templates and become the actual section content.
     * @return  Topic   A standard topic replacing the (title, html) section content.
     **/
    public Topic getRelatedTopic() {
        if (this.relatedTopic != null) {
            this.relatedTopic = this.content.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT,
                ROLE_DEFAULT, null);
        }
        return this.relatedTopic;
    }

    public String getMobileImage() {
        Topic imageFile = this.content.getRelatedTopic(MOBILE_IMAGE_ASSOC, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getDesktopImage() {
        Topic imageFile = this.content.getRelatedTopic(DESKTOP_IMAGE_ASSOC, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getBackgroundColor() {
        return this.content.getChildTopics().getStringOrNull(SECTION_BG_COLOR);
    }

    public String getFontColor() {
        return this.content.getChildTopics().getStringOrNull(SECTION_COLOR);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("titl", getTitle())
                .put("html", getHtml())
                .put("related_topic", getRelatedTopic())
                .put("font_color", getFontColor())
                .put("bg_color", getBackgroundColor());
        } catch (JSONException ex) {
            Logger.getLogger(Webpage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isSectionContentTopic() {
        if (this.content == null) return false;
        return (this.content.getTypeUri().equals(SECTION_CONTENT));
    }
    
}
