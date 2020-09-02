package de.mikromedia.webpages;

import com.sun.jersey.api.view.Viewable;
import de.mikromedia.webpages.model.MenuItem;
import de.mikromedia.webpages.model.Webpage;
import java.util.HashMap;
import java.util.List;
import org.osgi.framework.Bundle;
import systems.dmx.core.Topic;

/**
 * Collaborative, multi-site standard HTML webpages with DMX.
 *
 * @author Malte Rei&szlig;ig <a href="mailto:malte@mikromedia.de">malte@mikromedia.de></a>
 * @version 0.8.0 - compatible with DMX 5.0
 */
public interface WebpageService {

    public static final String WEBSITE = "de.mikromedia.site";
    public static final String WEBSITE_NAME = "de.mikromedia.site.name";
    public static final String WEBSITE_CAPTION = "de.mikromedia.site.caption";
    public static final String WEBSITE_ABOUT = "de.mikromedia.site.about_html";
    public static final String WEBSITE_STYLESHEET = "de.mikromedia.site.stylesheet";
    public static final String WEBSITE_FOOTER = "de.mikromedia.site.footer_html";
    public static final String WEBSITE_PREFIX = "de.mikromedia.site.prefix";
    public static final String WEBSITE_CSS = "de.mikromedia.site.stylesheet";

    public static final String WEBPAGE = "de.mikromedia.page";
    public static final String WEBPAGE_TITLE = "de.mikromedia.page.headline";
    public static final String WEBPAGE_CONTENT = "de.mikromedia.page.main";
    public static final String WEBPAGE_ABOUT = "de.mikromedia.page.about";
    public static final String WEBPAGE_CSS = "de.mikromedia.page.stylesheet";
    public static final String CUSTOM_SCRIPT_PATH = "de.mikromedia.javascript_path";
    public static final String WEBPAGE_ALIAS = "de.mikromedia.page.web_alias";
    public static final String WEBPAGE_IS_DRAFT = "de.mikromedia.page.is_draft";

    // ### TODO: Switch to related "Person" topics for authorship
    public static final String AUTHOR_NAME = "de.mikromedia.page.author_name";

    public static final String BACKGROUND_COLOR_ASSOC = "de.mikromedia.background.color";
    public static final String FONT_COLOR_ASSOC = "de.mikromedia.font.color";

    public static final String HEADER = "de.mikromedia.header";
    public static final String HEADER_TITLE = "de.mikromedia.header.title";
    public static final String HEADER_CONTENT = "de.mikromedia.header.html";
    public static final String HEADER_SCRIPT = "de.mikromedia.header.script";

    public static final String IMAGE_LARGE = "de.mikromedia.image.large";
    public static final String IMAGE_SMALL = "de.mikromedia.image.small";
    public static final String LOGO_IMAGE = "de.mikromedia.image.logo";
    public static final String IMAGE_SIZE_STYLE = "de.mikromedia.image.size_style";
    public static final String IMAGE_ATTACHMENT_STYLE = "de.mikromedia.image.attachment_style";

    public static final String SECTION = "de.mikromedia.section";
    public static final String SECTION_TITLE = "de.mikromedia.section.title";
    public static final String SECTION_LAYOUT = "de.mikromedia.section.layout";
    public static final String SECTION_PLACEMENT = "de.mikromedia.section.placement";
    public static final String SECTION_CSS_CLASS = "de.mikromedia.section.css_class";

    public static final String TILE = "de.mikromedia.tile";
    public static final String TILE_HEADLINE = "de.mikromedia.tile.headline";
    public static final String TILE_HTML = "de.mikromedia.tile.html";

    public static final String PLACEMENT_ABOVE = "de.mikromedia.placement.above";
    public static final String PLACEMENT_BELOW = "de.mikromedia.placement.below";
    public static final String PLACEMENT_ASIDE_RIGHT = "de.mikromedia.placement.aside_right";
    public static final String PLACEMENT_ASIDE_LEFT = "de.mikromedia.placement.aside_left";

    public static final String BUTTON = "de.mikromedia.button";
    public static final String BUTTON_TITLE = "de.mikromedia.button.title";
    public static final String BUTTON_STYLE = "de.mikromedia.button.style";

    public static final String LINK = "de.mikromedia.link";

    public static final String MENU_ITEM = "de.mikromedia.menu.item";
    public static final String MENU_ITEM_ACTIVE = "de.mikromedia.menu.item_active";
    public static final String MENU_ITEM_HREF = "de.mikromedia.menu.item_href"; // ### Migrate > "de.mikromedia.link";
    public static final String MENU_ITEM_NAME = "de.mikromedia.menu.item_name";

    public static final String STANDARD_STYLESHEET_URI = "de.mikromedia.standard_site_style";
    public static final String STANDARD_WEBSITE_URI = "de.mikromedia.standard_site";
    public static final String STANDARD_WEBSITE_PREFIX = "standard";

    public static final String REDIRECT = "de.mikromedia.redirect";
    public static final String REDIRECT_STATUS_CODE = "de.mikromedia.redirect.status_code";
    public static final String REDIRECT_TARGET_URL = "de.mikromedia.redirect.target_url";
    public static final String REDIRECT_WEB_ALIAS = "de.mikromedia.redirect.web_alias";

    public static final String WEBPAGES_WS_URI = "de.mikromedia.webpages_ws";
    public static final String WEBPAGES_WS_NAME = "Webpages";
    public static final String FAVICON_NAME = "favicon.ico";

    public static final String DEFAULT_ATTACHMENT = "fixed";
    public static final String DEFAULT_SIZE = "contain";

    /** Standard Distribution URIs (DM4) **/
    public static final String WEBCLIENT_COLOR = "dmx.webclient.color";
    public static final String DMX_FILE = "dmx.files.file";
    public static final String FILE_PATH = "dmx.files.path";
    public static final String TIME_CREATED = "dmx.timestamps.created";
    public static final String TIME_MODIFIED = "dmx.timestamps.modified";
    public static final String USERNAME = "dmx.accesscontrol.username";
    public static final String INSTITUTION = "dmx.contacts.institution";

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
     * Fetches all menut items related to the given website.
     * @param sitePrefix
     * @return All \"active\" menu items associated with the website for the given username..
     */
    List<MenuItem> getWebsiteMenuItems(String sitePrefix);

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
