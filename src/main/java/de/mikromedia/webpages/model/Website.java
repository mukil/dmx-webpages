package de.mikromedia.webpages.model;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import static de.mikromedia.webpages.WebpageService.ASSOCIATION;
import static de.mikromedia.webpages.WebpageService.DEEPAMEHTA_FILE;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.MENU_ITEM;
import static de.mikromedia.webpages.WebpageService.REDIRECT;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import static de.mikromedia.webpages.WebpageService.USERNAME;
import static de.mikromedia.webpages.WebpageService.WEBPAGE;
import static de.mikromedia.webpages.WebpageService.WEBPAGE_ALIAS;
import static de.mikromedia.webpages.WebpageService.WEBSITE;
import static de.mikromedia.webpages.WebpageService.WEBSITE_ABOUT;
import static de.mikromedia.webpages.WebpageService.WEBSITE_CAPTION;
import static de.mikromedia.webpages.WebpageService.WEBSITE_CSS;
import static de.mikromedia.webpages.WebpageService.WEBSITE_FOOTER;
import static de.mikromedia.webpages.WebpageService.WEBSITE_NAME;
import static de.mikromedia.webpages.WebpageService.WEBSITE_PREFIX;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import static de.mikromedia.webpages.WebpageService.IMAGE_LARGE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SMALL;
import static de.mikromedia.webpages.WebpageService.LOGO_IMAGE;

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
        return (this.topic.getTypeUri().equals(WEBSITE));
    }

    public Topic getRelatedUsername() {
        return this.topic.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT,
            ROLE_DEFAULT, USERNAME);
    }

    public String getLogoPath() {
        Topic imageFile = this.topic.getRelatedTopic(LOGO_IMAGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        if (imageFile != null) {
            return imageFile.getChildTopics().getStringOrNull(FILE_PATH);
        }
        return null;
    }

    public List<RelatedTopic> getRelatedWebpages() {
        return this.topic.getRelatedTopics(ASSOCIATION, ROLE_DEFAULT,
            ROLE_DEFAULT, WEBPAGE);
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
        List<RelatedTopic> menuItems = this.topic.getRelatedTopics(ASSOCIATION, ROLE_DEFAULT,
                ROLE_DEFAULT, MENU_ITEM);
        sortMenuItems(menuItems);
        ArrayList<MenuItem> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItem menuItem = new MenuItem(iterator.next().getId(), this, dm4);
            if (menuItem.isActive()) {
                menuItem.getChildMenuItems();
                result.add(menuItem);
            }
        }
        return result;
    }

    private void sortMenuItems(List<RelatedTopic> items) {
        Collections.sort(items, new Comparator<RelatedTopic>() {
            @Override
            public int compare(RelatedTopic item1, RelatedTopic item2) {
                try {
                    int value1 = Integer.parseInt(item1.getRelatingAssociation().getSimpleValue().toString());
                    int value2 = Integer.parseInt(item2.getRelatingAssociation().getSimpleValue().toString());
                    if (value1 > value2) {
                        return 1;
                    } else if (value1 == value2) {
                        return 0;
                    } else {
                        return -1;
                    }
                } catch (NumberFormatException nex) {
                    // Supresses sorting of ordinal number for one of the two items with a NFException
                    log.fine("Sorting Menu Items by Ordinal Number encountered an error: "
                            + nex.getLocalizedMessage());
                    return 0; // ### Depending which item has a bad number, continue sorting
                }
            }
        });
    }

    public List<RelatedTopic> getConfiguredRedirects() {
        return this.topic.getRelatedTopics(ASSOCIATION,
            ROLE_DEFAULT, ROLE_DEFAULT, REDIRECT);
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
            String webpageAlias = webpageTopic.getChildTopics().getString(WEBPAGE_ALIAS);
            if (webpageAlias.equals(webAlias)) {
                log.fine("Loaded webpage with web alias \"" + webAlias + "\" Title: " + webpageTopic.getSimpleValue());
                return webpageTopic.getChildTopics().getTopic(WEBPAGE_ALIAS);
            }
        }
        return null;
    }
    
    public String getFooter() {
        return this.topic.getChildTopics().getString(WEBSITE_FOOTER);
    }

    public String getSitePrefix() {
        return this.topic.getChildTopics().getString(WEBSITE_PREFIX);
    }

    public String getCaption() {
        return this.topic.getChildTopics().getStringOrNull(WEBSITE_CAPTION);
    }

    public String getAboutHTML() {
        return this.topic.getChildTopics().getStringOrNull(WEBSITE_ABOUT);
    }

    public String getName() {
        return this.topic.getChildTopics().getString(WEBSITE_NAME);
    }

    public String getStylesheetPath() {
        return this.topic.getChildTopics().getString(WEBSITE_CSS);
    }

}
