package de.mikromedia.webpages.model;

import static systems.dmx.core.Constants.*;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.INSTITUTION;
import static de.mikromedia.webpages.WebpageService.MENU_ITEM;
import static de.mikromedia.webpages.WebpageService.REDIRECT;
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
import static de.mikromedia.webpages.WebpageService.LOGO_IMAGE;
import de.mikromedia.webpages.mapping.InstitutionOrganization;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.service.CoreService;
import static de.mikromedia.webpages.WebpageService.DMX_FILE;

public class Website {
    
    private Logger log = Logger.getLogger(getClass().getName());

    Topic topic = null;
    CoreService dmx = null;

    public Website(Topic website, CoreService dmx) {
        this.topic = website;
        this.dmx = dmx;
    }

    public boolean isWebsiteTopic() {
        if (this.topic == null) return false;
        return (this.topic.getTypeUri().equals(WEBSITE));
    }

    public Topic getRelatedUsername() {
        return this.topic.getRelatedTopic(ASSOCIATION, DEFAULT,
            DEFAULT, USERNAME);
    }

    public String getLogoPath() {
        Topic imageFile = this.topic.getRelatedTopic(LOGO_IMAGE, DEFAULT,
                DEFAULT, DMX_FILE);
        if (imageFile != null) {
            return imageFile.getChildTopics().getString(FILE_PATH, null);
        }
        return null;
    }

    public List<RelatedTopic> getRelatedWebpages() {
        return this.topic.getRelatedTopics(ASSOCIATION, DEFAULT,
            DEFAULT, WEBPAGE);
    }

    public String getInstitutionLD() {
        Topic inst = this.topic.getRelatedTopic(ASSOCIATION, DEFAULT,
            DEFAULT, INSTITUTION);
        if (inst != null) {
            InstitutionOrganization institution = new InstitutionOrganization(inst);
            return institution.toJSONLD();
        }
        return null;
    }

    public List<Webpage> getRelatedWebpagesPublished() {
        ArrayList<Webpage> result = new ArrayList();
        List<RelatedTopic> pages = getRelatedWebpages();
        Iterator<RelatedTopic> iterator = pages.iterator();
        while (iterator.hasNext()) {
            Webpage page = new Webpage(iterator.next().getId(), dmx);
            if (!page.isDraft()) result.add(page);
        }
        return result;
    }

    public List<MenuItem> getActiveMenuItems() {
        List<RelatedTopic> menuItems = this.topic.getRelatedTopics(ASSOCIATION, DEFAULT,
                DEFAULT, MENU_ITEM);
        sortMenuItems(menuItems);
        ArrayList<MenuItem> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItem menuItem = new MenuItem(iterator.next().getId(), this, dmx);
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
                    int value1 = Integer.parseInt(item1.getRelatingAssoc().getSimpleValue().toString());
                    int value2 = Integer.parseInt(item2.getRelatingAssoc().getSimpleValue().toString());
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
        return this.topic.getRelatedTopics(ASSOCIATION, DEFAULT, DEFAULT, REDIRECT);
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
            Topic webpageTopic = dmx.getTopic(webpage.getModel().getId()).loadChildTopics();
            String webpageAlias = webpageTopic.getChildTopics().getString(WEBPAGE_ALIAS);
            if (webpageAlias.equals(webAlias)) {
                log.fine("Loaded webpage by web alias \"" + webAlias + "\" Title: " + webpageTopic.getSimpleValue());
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
        return this.topic.getChildTopics().getString(WEBSITE_CAPTION, null);
    }

    public String getAboutHTML() {
        return this.topic.getChildTopics().getString(WEBSITE_ABOUT, null);
    }

    public String getName() {
        return this.topic.getChildTopics().getString(WEBSITE_NAME);
    }

    public String getStylesheetPath() {
        return this.topic.getChildTopics().getString(WEBSITE_CSS);
    }

}
