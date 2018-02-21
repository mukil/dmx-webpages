package de.mikromedia.webpages.model;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import static de.mikromedia.webpages.WebpageService.BUTTON;
import static de.mikromedia.webpages.WebpageService.HEADER_TITLE;
import static de.mikromedia.webpages.WebpageService.DEEPAMEHTA_FILE;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.HEADER;
import static de.mikromedia.webpages.WebpageService.HEADER_CONTENT;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static de.mikromedia.webpages.WebpageService.BACKGROUND_COLOR;
import static de.mikromedia.webpages.WebpageService.FONT_COLOR;
import static de.mikromedia.webpages.WebpageService.IMAGE_LARGE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SMALL;

public class Header {
    
    private Topic pageHeader;

    public Header(Topic pageHeader) {
        this.pageHeader = pageHeader;
        if (!isHeaderTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Header");
        }
        this.pageHeader.loadChildTopics();
    }

    public Header(long topicId, CoreService dms) {
        this.pageHeader = dms.getTopic(topicId);
        if (!isHeaderTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Header");
        }
        this.pageHeader.loadChildTopics();
    }

    public long getId() {
        return this.pageHeader.getId();
    }

    public Topic getTopic() {
        return this.pageHeader;
    }

    // --- Custom Section Data Accessors
    
    public String getTitle() {
        return this.pageHeader.getChildTopics().getStringOrNull(HEADER_TITLE);
    }

    public String getContent() {
        return this.pageHeader.getChildTopics().getStringOrNull(HEADER_CONTENT);
    }

    public List<Button> getButtons() {
        List<Button> headerButtons = new ArrayList();
        List<RelatedTopic> buttons = this.pageHeader.getChildTopics().getTopics(BUTTON);
        for (RelatedTopic topic : buttons) {
            Button button = new Button(topic);
            headerButtons.add(button);
        }
        return headerButtons;
    }

    public String getSmallImage() {
        Topic imageFile = this.pageHeader.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getLargeImage() {
        Topic imageFile = this.pageHeader.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getBackgroundColor() {
        return this.pageHeader.getChildTopics().getStringOrNull(BACKGROUND_COLOR);
    }

    public String getFontColor() {
        return this.pageHeader.getChildTopics().getStringOrNull(FONT_COLOR);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("title", getTitle())
                .put("buttons", getButtons())
                .put("font_color", getFontColor())
                .put("bg_color", getBackgroundColor());
        } catch (JSONException ex) {
            Logger.getLogger(Webpage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isHeaderTopic() {
        if (this.pageHeader == null) return false;
        return (this.pageHeader.getTypeUri().equals(HEADER));
    }
    
}
