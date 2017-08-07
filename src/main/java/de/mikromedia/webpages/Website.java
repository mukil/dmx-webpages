package de.mikromedia.webpages;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Malte Reißig
 */
public class Website {
    
    private Logger log = Logger.getLogger(getClass().getName());

    Topic topic = null;
    CoreService dm4 = null;

    public Website(Topic website, CoreService dm4) {
        this.topic = website;
        this.dm4 = dm4;
    }

    public boolean isWebsiteTopic() {
        if (this.topic == null) return false;
        return (this.topic.getTypeUri().equals("de.mikromedia.site"));
    }

    public Topic getRelatedUsername() {
        return this.topic.getRelatedTopic("dm4.core.association", "dm4.core.default",
            "dm4.core.default", "dm4.accesscontrol.username");
    }

    public List<RelatedTopic> getRelatedWebpages() {
        return this.topic.getRelatedTopics("dm4.core.association", "dm4.core.default",
            "dm4.core.default", "de.mikromedia.page");
    }

    public List<Webpage> getRelatedWebpagesPublished() {
        ArrayList<Webpage> result = new ArrayList();
        List<RelatedTopic> pages = getRelatedWebpages();
        Iterator<RelatedTopic> iterator = pages.iterator();
        while (iterator.hasNext()) {
            Webpage page = new Webpage(iterator.next().getId(), dm4);
            if (!page.isDraft()) result.add(page);
        }
        return result;
    }

    public List<MenuItem> getActiveMenuItems() {
        List<RelatedTopic> menuItems = this.topic.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.menu.item");
        ArrayList<MenuItem> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItem menuItem = new MenuItem(iterator.next().getId(), dm4);
            if (menuItem.isActive()) result.add(menuItem);
        }
        return result;
    }

    public List<RelatedTopic> getConfiguredRedirects() {
        return this.topic.getRelatedTopics("dm4.core.association",
            "dm4.core.default","dm4.core.default", "de.mikromedia.redirect");
    }

    /**
     * Returns a topic of type <code>de.mikromedia.page</code> if its associated with the given `Website` topic.
     * @param webAlias
     * @return Topic    A topic representing the Webpage.
     */
    public Topic getWebpageByAlias(String webAlias) {
        List<RelatedTopic> relatedWebpages = getRelatedWebpages();
        if (relatedWebpages == null) return null;
        for (RelatedTopic webpage : relatedWebpages) {
            Topic webpageTopic = dm4.getTopic(webpage.getModel().getId()).loadChildTopics();
            String webpageAlias = webpageTopic.getChildTopics().getString("de.mikromedia.page.web_alias");
            if (webpageAlias.equals(webAlias)) {
                log.fine("Loaded webpage with web alias \"" + webAlias + "\" Title: " + webpageTopic.getSimpleValue());
                return webpageTopic.getChildTopics().getTopic("de.mikromedia.page.web_alias");
            }
        }
        return null;
    }
    
    public String getFooter() {
        return this.topic.getChildTopics().getString("de.mikromedia.site.footer_html");
    }

    public String getCaption() {
        return this.topic.getChildTopics().getStringOrNull("de.mikromedia.site.caption");
    }

    public String getAboutHTML() {
        return this.topic.getChildTopics().getStringOrNull("de.mikromedia.site.about_html");
    }

    public String getName() {
        return this.topic.getChildTopics().getString("de.mikromedia.site.name");
    }

    public String getStylesheetPath() {
        return this.topic.getChildTopics().getString("de.mikromedia.site.stylesheet");
    }

}
