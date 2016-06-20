package de.mikromedia.webpages.models;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class MenuItemViewModel implements JSONEnabled {

    public Topic menuItem;

    public MenuItemViewModel(long topicId, CoreService dms) {
        this.menuItem = dms.getTopic(topicId);
        if (!isWebpageMenuItemTopic(this.menuItem)) {
            throw new IllegalArgumentException("Given topic is not of type Webpage Menu Item");
        }
        this.menuItem.loadChildTopics();
    }

    public String getLabel() {
        return menuItem.getChildTopics().getStringOrNull("de.mikromedia.menu.item_name");
    }

    public String getHref() {
        return menuItem.getChildTopics().getStringOrNull("de.mikromedia.menu.item_href");
    }

    public boolean isActive() {
        return menuItem.getChildTopics().getBooleanOrNull("de.mikromedia.menu.item_active");
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("label", getLabel())
                .put("href", getHref());
        } catch (JSONException ex) {
            Logger.getLogger(MenuItemViewModel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isWebpageMenuItemTopic(Topic topic) {
        return (topic.getTypeUri().equals("de.mikromedia.menu.item"));
    }

}
