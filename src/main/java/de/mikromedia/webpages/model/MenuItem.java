package de.mikromedia.webpages.model;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class MenuItem implements JSONEnabled {

    public Topic menuItem;
    private List<RelatedTopic> relatedItems;
    private List<MenuItem> childItems = new ArrayList<MenuItem>();
    public boolean hasChildItems = false;

    public MenuItem(Topic menuItem) {
        this.menuItem = menuItem;
        if (!isWebpageMenuItemTopic(this.menuItem)) {
            throw new IllegalArgumentException("Given topic is not of type Webpage Menu Item");
        }
        this.menuItem.loadChildTopics();
        loadRelatedMenuItems();
    }

    public MenuItem(long topicId, CoreService dms) {
        this.menuItem = dms.getTopic(topicId);
        if (!isWebpageMenuItemTopic(this.menuItem)) {
            throw new IllegalArgumentException("Given topic is not of type Webpage Menu Item");
        }
        this.menuItem.loadChildTopics();
        loadRelatedMenuItems();
    }

    public long getId() {
        return menuItem.getId();
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

    public List<MenuItem> getChildMenuItems() {
        if (relatedItems == null) loadRelatedMenuItems();
        for (Topic relatedItem : relatedItems) {
            childItems.add(new MenuItem(relatedItem));
        }
        return childItems;
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("label", getLabel())
                .put("href", getHref())
                .put("items", getChildMenuItems());
        } catch (JSONException ex) {
            Logger.getLogger(MenuItem.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void loadRelatedMenuItems() {
        relatedItems = menuItem.getRelatedTopics("dm4.core.association", "dm4.core.parent",
                "dm4.core.child", "de.mikromedia.menu.item");
        hasChildItems = (relatedItems.size() > 0);
    }

    private boolean isWebpageMenuItemTopic(Topic topic) {
        return (topic.getTypeUri().equals("de.mikromedia.menu.item"));
    }

}
