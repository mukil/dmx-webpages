package de.mikromedia.webpages;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import java.util.HashMap;
import java.util.List;
import org.osgi.framework.Bundle;

/**
 * Collaborative, multi-site standard HTML webpages with DeepaMehta 4.
 *
 * @author Malte Rei&szlig;ig
 * @version 0.4.2 - compatible with DeepaMehta 4.8
 */
public interface WebpageService {

    public static final String WEBSITE = "de.mikromedia.site";
    public static final String WEBSITE_NAME = "de.mikromedia.site.name";
    public static final String WEBSITE_STYLESHEET = "de.mikromedia.site.stylesheet";
    public static final String WEBSITE_FOOTER = "de.mikromedia.site.footer_html";

    public static final String STANDARD_STYLESHEET_URI = "de.mikromedia.standard_site_style";
    public static final String STANDARD_WEBSITE_URI = "de.mikromedia.standard_site";

    public static final String REDIRECT_STATUS_CODE = "de.mikromedia.redirect.status_code";
    public static final String REDIRECT_TARGET_URL = "de.mikromedia.redirect.target_url";
    public static final String REDIRECT_WEB_ALIAS = "de.mikromedia.redirect.web_alias";

    public static final String WEBPAGES_WS_URI = "de.mikromedia.webpages_ws";
    public static final String WEBPAGES_WS_NAME = "Webpages";
    public static final SharingMode WEBPAGES_SHARING_MODE = SharingMode.PUBLIC;

    public static final String STANDARD_WEBSITE_PREFIX = "standard";

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
    List<Webpage> getPublishedWebpages(Topic website);

    /**
     * Fetches all published webpages related to the website associated with the username.
     * @param username
     * @return All webpage topics associated with the website for the given username and not marked as \"Drafts\".
     */
    List<Webpage> getPublishedWebpages(String username);

    /**
     * Fetches all menut items related to the given website.
     * @param site
     * @return All \"active\" menu items associated with the website for the given username..
     */
    List<MenuItem> getActiveMenuItems(Topic site);
    
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
