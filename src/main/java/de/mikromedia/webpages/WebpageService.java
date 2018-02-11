package de.mikromedia.webpages;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.mikromedia.webpages.model.MenuItem;
import de.mikromedia.webpages.model.Webpage;
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
    public static final String WEBSITE_CAPTION = "de.mikromedia.site.caption";
    public static final String WEBSITE_STYLESHEET = "de.mikromedia.site.stylesheet";
    public static final String WEBSITE_FOOTER = "de.mikromedia.site.footer_html";
    public static final String WEBSITE_PREFIX = "de.mikromedia.site.prefix";

    public static final String WEBPAGE = "de.mikromedia.page";
    public static final String WEBPAGE_TITLE = "de.mikromedia.page.headline";
    public static final String WEBPAGE_CONTENT = "de.mikromedia.page.main";

    public static final String HEADER = "de.mikromedia.header";
    public static final String HEADER_TITLE = "de.mikromedia.header.title";
    public static final String HEADER_CONTENT = "de.mikromedia.header.content";
    public static final String HEADER_BG_COLOR = "de.mikromedia.header.color_bg";
    public static final String HEADER_COLOR = "de.mikromedia.header.color_font";
    public static final String HEADER_SCRIPT = "de.mikromedia.header.script";

    public static final String DESKTOP_IMAGE_ASSOC = "de.mikromedia.header.desktop_image";
    public static final String MOBILE_IMAGE_ASSOC = "de.mikromedia.header.mobile_image";

    public static final String BUTTON = "de.mikromedia.button";
    public static final String BUTTON_TITLE = "de.mikromedia.button.title";
    public static final String BUTTON_HREF = "de.mikromedia.button.href";
    public static final String BUTTON_STYLE = "de.mikromedia.button.style";

    public static final String STANDARD_STYLESHEET_URI = "de.mikromedia.standard_site_style";
    public static final String STANDARD_WEBSITE_URI = "de.mikromedia.standard_site";

    public static final String REDIRECT_STATUS_CODE = "de.mikromedia.redirect.status_code";
    public static final String REDIRECT_TARGET_URL = "de.mikromedia.redirect.target_url";
    public static final String REDIRECT_WEB_ALIAS = "de.mikromedia.redirect.web_alias";

    public static final String WEBPAGES_WS_URI = "de.mikromedia.webpages_ws";
    public static final String WEBPAGES_WS_NAME = "Webpages";
    public static final SharingMode WEBPAGES_SHARING_MODE = SharingMode.PUBLIC;

    public static final String STANDARD_WEBSITE_PREFIX = "standard";

    /** Standard Distribution URIs (DM4) **/
    public static final String ROLE_DEFAULT = "dm4.core.default";
    public static final String ROLE_CHILD = "dm4.core.child";
    public static final String ROLE_PARENT = "dm4.core.parent";
    public static final String ASSOCIATION = "dm4.core.association";
    public static final String COMPOSITION = "dm4.core.composition";
    public static final String AGGREGATION = "dm4.core.aggregation";
    public static final String FILE = "dm4.files.file";
    public static final String FILE_PATH = "dm4.files.path";

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

    List<Webpage> getWebpagesSortedByTimestamp(List<Webpage> all, final boolean lastModified);
    
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
