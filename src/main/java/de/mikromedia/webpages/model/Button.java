package de.mikromedia.webpages.model;

import static de.mikromedia.webpages.WebpageService.BUTTON;
import static de.mikromedia.webpages.WebpageService.BUTTON_STYLE;
import static de.mikromedia.webpages.WebpageService.BUTTON_TITLE;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static de.mikromedia.webpages.WebpageService.LINK;
import systems.dmx.core.JSONEnabled;
import systems.dmx.core.Topic;
import systems.dmx.core.service.CoreService;

public class Button implements JSONEnabled {

    public Topic button;

    public Button(Topic button) {
        this.button = button;
        if (!isWebpageButton()) {
            throw new IllegalArgumentException("Given topic is not of type Webpage Button");
        }
        this.button.loadChildTopics();
    }

    public Button(long topicId, CoreService dms) {
        this.button = dms.getTopic(topicId);
        if (!isWebpageButton()) {
            throw new IllegalArgumentException("Given topic is not of type Webpage Button");
        }
        this.button.loadChildTopics();
    }

    public long getId() {
        return this.button.getId();
    }

    public Topic getTopic() {
        return this.button;
    }

    // -- Custom Accessor

    public String getLabel() {
        return this.button.getChildTopics().getString(BUTTON_TITLE, null);
    }

    public String getLink() {
        return this.button.getChildTopics().getString(LINK, null);
    }

    public String getStyle() {
        return this.button.getChildTopics().getString(BUTTON_STYLE, null);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("label", getLabel())
                .put("href", getLink())
                .put("style", getStyle());
        } catch (JSONException ex) {
            Logger.getLogger(MenuItem.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isWebpageButton() {
        if (this.button == null) return false;
        return (button.getTypeUri().equals(BUTTON));
    }

}
