package de.mikromedia.webpages;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.mikromedia.webpages.models.MenuItemViewModel;
import de.mikromedia.webpages.models.WebpageViewModel;
import java.util.HashMap;
import java.util.List;
import org.osgi.framework.Bundle;

/**
 * Collaborative, multi-site standard HTML webpages with DeepaMehta 4.
 *
 * @author Malte Rei&szlig;ig (<a href="mailto:malte@mikromedia.de">Mail</a>)
 * @version 0.4 - compatible with DeepaMehta 4.8
 */
public interface WebpageService {

    /**
     * Fetches an existing or creates a new website topic (if non exists and the requesting user equals given username).
     * @param username
     * @return The website for the given username.
     */
    Topic getWebsiteByUsername(String username);

    /**
     * Fetches the standard website topic.
     * @return A topic representing the global website.
     */
    Topic getStandardWebsite();

    /**
     * Fetches all published webpages related to the given website topic.
     * @return A list of webpages associated with the given website.
     */
    List<WebpageViewModel> getPublishedWebpages(Topic website);

    /**
     * Fetches all published webpages related to the website associated with the username.
     * @param username
     * @return All webpage topics associated with the website for the given username and not marked as \"Drafts\".
     */
    List<WebpageViewModel> getPublishedWebpages(String username);

    /**
     * Fetches all menut items related to the given website.
     * @param site
     * @return All \"active\" menu items associated with the website for the given username..
     */
    List<MenuItemViewModel> getActiveMenuItems(Topic site);
    
    /**
     * IMPORTANT: If you register your own bundle as a resource for thymeleaf templates you must call
     * reinitTemplateEngine afterwards.
     */
    void addTemplateResolverBundle(Bundle bundle);

    void removeTemplateResolverBundle(Bundle bundle);

    void reinitTemplateEngine();

    void overrideFrontpageTemplate(String templateName);

    void setFrontpageAliases(HashMap<String, String[]> frontpageAliases);

    void viewData(String key, Object value);

    Viewable view(String templateName);

}
