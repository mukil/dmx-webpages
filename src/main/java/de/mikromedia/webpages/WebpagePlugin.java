package de.mikromedia.webpages;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.service.Inject;
import de.mikromedia.webpages.models.MenuItemViewModel;
import de.mikromedia.webpages.models.WebpageViewModel;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.accesscontrol.AccessControlService;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.accesscontrol.AccessControlException;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.thymeleaf.ThymeleafPlugin;
import de.deepamehta.workspaces.WorkspacesService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.util.logging.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Response.Status;
import org.osgi.framework.Bundle;

/**
 * Collaborative, multi-site standard HTML webpages with DeepaMehta 4.
 *
 * ### FIXME: Usernames might not be compatible URIComponents.
 * @author Malte Rei&szlig;ig (<a href="mailto:malte@mikromedia.de">Mail</a>)
 * @version 0.4 - compatible with DeepaMehta 4.8
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends ThymeleafPlugin implements WebpageService {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject AccessControlService acService;
    @Inject WorkspacesService workspacesService;

    public static final String WEBPAGES_WS_URI = "de.mikromedia.webpages_ws";
    public static final String WEBPAGES_WS_NAME = "Webpages";
    public static final SharingMode WEBPAGES_SHARING_MODE = SharingMode.PUBLIC;

    private static final String STANDARD_WEBSITE_PREFIX = "standard";

    private final String DM4_HOST_URL = System.getProperty("dm4.host.url");
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    String frontPageTemplateName = null;

    @Override
    public void init() {
        log.info("Initializing Webpages Thymeleaf Template Engine");
        initTemplateEngine();
        df.setTimeZone(tz);
    }

    /**
     * The method managing the root resource / frontpage.
     * @return  A processed Thymeleaf Template (<code>Viewable</code>).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getFrontpageView() {
        // 0) Set generic template data "authenticated" and "username"
        // 1) Check if a custom frontpage was registered by another plugin
        if (frontPageTemplateName != null) {
            setGlobalTemplateParameter(frontPageTemplateName, null);
            return view(frontPageTemplateName);
        } else { // 2) check if there is a redirect or page realted to the standard site and set to "/"
            return getWebsiteFrontpage(null);
        }
    }

    /**
     * The methode serving anything on the first resource level at best this resolves to
     * either a <em>Webpage</em> or a <em>Webpage Redirect</em> (301, 302) or <em>404</em>.
     *
     * @param webAlias  String  URI compliant name of the resource without leading slash.
     * @return  A processed Thymeleaf Template (<code>Viewable</code>).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{pageWebAlias}")
    public Viewable getPageView(@PathParam("pageWebAlias") String webAlias) {
        // 0) check if webAlias is valid username
        String pageAlias = webAlias.trim();
        log.info("Requesting Global Page /" + webAlias);
        Topic username = dm4.getAccessControl().getUsernameTopic(pageAlias);
        if (username != null) {
            return getWebsiteFrontpage(username.getSimpleValue().toString());
        }
        // 1) prepare standard website
        Topic standardWebsite = getStandardSiteTopicByURI();
        setWebsiteTemplateParameter(standardWebsite);
        // 2) is webpage of standard site
        Viewable webpage = getWebsitesWebpage(standardWebsite, pageAlias, STANDARD_WEBSITE_PREFIX);
        if (webpage != null) return webpage;
        log.info("=> /" + pageAlias + " webpage for standard website not found.");
        // 4) is redirect of admin
        handleWebsiteRedirects(standardWebsite, pageAlias);
        log.info("=> /" + pageAlias + " webpage redirect for standard website not found.");
        // 5) web alias is neither a published nor a drafted \"Webpage\" and not a \"Redirect\"
        return getWebsitesNotFoundPage(standardWebsite);
    }

    /**
     * Serving a specific webpage assigned to the website related to its given prefix (currently "username").
     *
     * @param prefix
     * @param webAlias
     * @return  A processed Thymeleaf Template (<code>Viewable</code>).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{username}/{pageWebAlias}")
    public Viewable getPageView(@PathParam("username") String prefix, @PathParam("pageWebAlias") String webAlias) {
        String pageAlias = webAlias.trim();
        log.info("Requesting Website Page /" + prefix + "/" + webAlias);
        // 1) Fetch users website topic
        Topic usersWebsite = getOrCreateWebsiteTopic(prefix);
        setWebsiteTemplateParameter(usersWebsite);
        // 2) check related webpages
        Viewable webpage = getWebsitesWebpage(usersWebsite, pageAlias, prefix);
        if (webpage != null) return webpage;
        log.info("=> /" + pageAlias + " webpage for \"" +prefix+ "\"s website not found.");
        // 2) check if it is a users redirect
        handleWebsiteRedirects(usersWebsite, pageAlias);
        // 3) Log that web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        log.info("=> /" + pageAlias + " webpage redirect for \"" +prefix+ "\"s website not found.");
        // 4) Return 404 page with users website footer
        return getWebsitesNotFoundPage(usersWebsite);
    }

    @Override
    public void overrideFrontpageTemplate(String fileName) {
        this.frontPageTemplateName = fileName;
    }

    @Override
    public void viewData(String key, Object value) {
        super.viewData(key, value);
    }

    @Override
    public Viewable view(String fileName) {
        return super.view(fileName);
    }

    @Override
    public void reinitTemplateEngine() {
        super.initTemplateEngine();
    }

    @Override
    public void addTemplateResolverBundle(Bundle bundle) {
        super.addTemplateResourceBundle(bundle);
    }

    @Override
    public void removeTemplateResolverBundle(Bundle bundle) {
        super.removeTemplateResourceBundle(bundle);
    }

    /** --------------------------------------------------------------------------------- REST API Resources ----- **/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/website/{username}")
    @Override
    public Topic getWebsiteByUsername(@PathParam("username") String username) {
        Topic website = null;
        // return the website topic for the requested username
        Topic usernameTopic = acService.getUsernameTopic(username);
        if (usernameTopic != null && usernameTopic.getSimpleValue().toString().equals(acService.getUsername())) {
            website = getOrCreateWebsiteTopic(username);
        }
        return website;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/website")
    public Topic getWebsite() {
        Topic website = null;
        String username = acService.getUsername();
        if (username != null) {
            website = getOrCreateWebsiteTopic(username);
        }
        return website;
    }

    @GET
    @Path("/browse/{websiteId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String doRedirectToWebsite(@PathParam("websiteId") long websiteId)
            throws WebApplicationException, URISyntaxException {
        Topic topic = dm4.getTopic(websiteId);
        if (topic.getTypeUri().equals("de.mikromedia.site")) {
            Topic username = getWebsiteRelatedUser(topic);
            if (username != null) { // browse user site
                throw new WebApplicationException(
                    Response.temporaryRedirect(new URI("/" + username.getSimpleValue().toString())).build());
            } else { // browse standard site
                throw new WebApplicationException(Response.temporaryRedirect(new URI("/")).build());
            }
        }
        throw new WebApplicationException(Response.status(Status.OK).build());
    }

    /**
     * Lists all currently published webpages for the usernames website.
     * @param username
     * @return All webpage topics associated with the website for the given username not marked as \"Drafts\".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{username}")
    @Override
    public List<WebpageViewModel> getPublishedWebpages(@PathParam("username") String username) {
        log.info("Listing all published webpages for \"" + username + "\"");
        // fetch all pages with title and all childs
        ArrayList<WebpageViewModel> result = new ArrayList();
        Topic website = getOrCreateWebsiteTopic(username);
        if (website != null) {
            List<RelatedTopic> pages = getWebsiteRelatedPages(website);
            Iterator<RelatedTopic> iterator = pages.iterator();
            while (iterator.hasNext()) {
                WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dm4);
                if (!page.isDraft()) result.add(page);
            }
        }
        return result;
    }

    public Viewable getWebsiteFrontpage(String username) {
        Topic website = null;
        if (username == null) {
            website = getStandardSiteTopicByURI();
            setGlobalTemplateParameter("frontpage", STANDARD_WEBSITE_PREFIX);
        } else {
            website = getOrCreateWebsiteTopic(username);
            setGlobalTemplateParameter("frontpage", username);
        }
        // website is null if the request does not have the necessary permissions to access it
        if (website != null) {
            // private workspace via our getRelatedTopics()-call
            setWebsiteTemplateParameter(website);
            // check if their is a redirect setup for this web alias
            handleWebsiteRedirects(website, "/"); // potentially throws WebAppException triggering a Redirect
            // collect all webpages associated with this website
            viewData("pages", getPublishedWebpages(website)); // ### sort by creation or modification date
            return view("frontpage");
        } else {
            return getWebsitesNotFoundPage(website); // which loads standard topic if website is null
        }
    }

    /**
     * Returns all topics of type <code>de.mikromedia.menu.item</code> related to the given `Website` topic.
     * @param site
     * @return
     */
    @Override
    public List<MenuItemViewModel> getActiveMenuItems(Topic site) {
        List<RelatedTopic> menuItems = site.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.menu.item");
        ArrayList<MenuItemViewModel> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItemViewModel menuItem = new MenuItemViewModel(iterator.next().getId(), dm4);
            if (menuItem.isActive()) result.add(menuItem);
        }
        return result;
    }

    /**
     * Lists and prepares all currently published webpages of the given website.
     * @param website
     * @return All webpage topics associated with the website for the given username and not marked as \"Drafts\".
     */
    public List<WebpageViewModel> getPublishedWebpages(Topic website) {
        log.info("Listing all published webpages for \"" + website+ "\" website");
        ArrayList<WebpageViewModel> result = new ArrayList();
        List<RelatedTopic> pages = getWebsiteRelatedPages(website);
        Iterator<RelatedTopic> iterator = pages.iterator();
        while (iterator.hasNext()) {
            WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dm4);
            if (!page.isDraft()) result.add(page);
        }
        return result;
    }

    // --- Private Utility Methods

    /**
     * @param username
     * @return A topic of type <code>de.mikromedia.site</code> for the given `username`, if none found creates one.
     */
    private Topic getOrCreateWebsiteTopic(final String username) {
        final Topic usernameTopic = acService.getUsernameTopic(username);
        Topic website = null;
        if (usernameTopic != null) {
            try {
                website = getRelatedWebsite(usernameTopic);
                if (website != null) {
                    log.info("Loaded website topic of user " + username);
                    return website;
                } else {
                    website = createWebsiteTopic(usernameTopic);
                }
            } catch (AccessControlException aex) {
                log.info("Request contains not the necessary permissions ot access website topic of user " + username);
                return website;
            }
        }
        if (website != null) {
            log.info("Created Website topic for " + username + ", assigned to Workspace ID: \""
                + dm4.getAccessControl().getAssignedWorkspaceId(website.getId()) + "\"");
        }
        return website;
    }

    private Topic createWebsiteTopic(final Topic username) {
        DeepaMehtaTransaction tx = dm4.beginTx();
        Topic website = null;
        try {
            final Topic websiteTopic = dm4.getAccessControl().runWithoutWorkspaceAssignment(new Callable<Topic>() {
                @Override
                public Topic call() {
                    Topic topic = dm4.createTopic(mf.newTopicModel("de.mikromedia.site", mf.newChildTopicsModel()
                        .put("de.mikromedia.site.name", "My collection of webpages")
                        .putRef("de.mikromedia.site.stylesheet", "de.mikromedia.standard_site_style")
                        .put("de.mikromedia.site.footer_html", "<p class=\"attribution\">Published with the "
                            + "<a href=\"http://github.com/mukil/dm4-webpages\" title=\"Source Coude: dm4-webpages\">"
                            + "dm4-webpages</a> module.</p>")
                    ));
                    Topic usersWorkspace = dm4.getAccessControl().getPrivateWorkspace(
                        username.getSimpleValue().toString());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics()
                        .getTopic("de.mikromedia.site.name"), usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics()
                        .getTopic("de.mikromedia.site.footer_html"), usersWorkspace.getId());
                    Association userWebsiteRelation = createWebsiteUsernameAssociation(username, topic);
                    dm4.getAccessControl().assignToWorkspace(userWebsiteRelation, usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic, usersWorkspace.getId());
                    return topic;
                }
            });
            website = websiteTopic;
            log.info("Created a NEW website topic (ID: " + website.getId() + ") in \"Private Workspace\" of \""
                + username.getSimpleValue() + "\"");
            tx.success();
            tx.finish();
        } catch (Exception e) {
            tx.failure();
            tx.finish();
            throw new RuntimeException(e);
        }
        return website;
    }

    private Association createWebsiteUsernameAssociation(Topic usernameTopic, Topic website) {
        return dm4.createAssociation(mf.newAssociationModel("dm4.core.association",
                mf.newTopicRoleModel(usernameTopic.getId(), "dm4.core.default"),
                mf.newTopicRoleModel(website.getId(), "dm4.core.default")));
    }

    /**
     * @param website
     * @param webAlias
     * @param sitePrefix
     * @return A processed thymeleaf template if a webpage is related to that website, <code>null</code> otherwise.
     */
    private Viewable getWebsitesWebpage(Topic website, String webAlias, String sitePrefix) {
        Topic webpageAliasTopic = getWebpageByAlias(website, webAlias);
        if (webpageAliasTopic != null) {
            setGlobalTemplateParameter(webAlias, sitePrefix);
            setWebsiteTemplateParameter(website);
            return getWebpageTemplate(webpageAliasTopic);
        }
        return null;
    }

    /**
     * Performs a HTTP redirect if a <code>de.mikromedia.redirect</code> is associated with the given `Website` topic.
     * @param site
     * @param webAlias
     */
    private void handleWebsiteRedirects(Topic site, String webAlias) {
        List<RelatedTopic> redirectTopics = site.getRelatedTopics("dm4.core.association",
                "dm4.core.default","dm4.core.default", "de.mikromedia.redirect");
        Iterator<RelatedTopic> iterator = redirectTopics.iterator();
        while (iterator.hasNext()) {
            Topic redirectTopic = dm4.getTopic(iterator.next().getModel().getId()).loadChildTopics();
            String redirectAliasVaue = redirectTopic.getChildTopics().getString("de.mikromedia.redirect.web_alias");
            if (redirectAliasVaue.equals(webAlias)) {
                String redirectUrl = redirectTopic.getChildTopics().getString("de.mikromedia.redirect.target_url");
                int statusCode = redirectTopic.getChildTopics().getInt("de.mikromedia.redirect.status_code");
                doRedirect(webAlias, redirectUrl, statusCode);
            }
        }
    }

    /**
     * This method performs HTTP redirects to whatever the caller wants.
     * @param webAlias
     * @param redirectUrl
     * @param statusCode
     */
    private void doRedirect(String webAlias, String redirectUrl, int statusCode) {
        try {
            if (statusCode == 302 || statusCode == 303 || statusCode == 307) {
                log.fine(" => /" + webAlias + " temporary redirects to " + redirectUrl);
                throw new WebApplicationException(Response.temporaryRedirect(new URI(redirectUrl)).build());
            } else if (statusCode == 301 || statusCode == 308) {
                log.fine(" => /" + webAlias + " permanently redirects to " + redirectUrl);
                throw new WebApplicationException(Response.seeOther(new URI(redirectUrl)).build());
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns a topic of type <code>de.mikromedia.page</code> if its associated with the given `Website` topic.
     * @param site
     * @param webAlias
     * @return
     */
    private Topic getWebpageByAlias(Topic site, String webAlias) {
        List<RelatedTopic> relatedWebpages = getWebsiteRelatedPages(site);
        for (RelatedTopic webpage : relatedWebpages) {
            Topic webpageTopic = dm4.getTopic(webpage.getModel().getId()).loadChildTopics();
            String webpageAlias = webpageTopic.getChildTopics().getString("de.mikromedia.page.web_alias");
            if (webpageAlias.equals(webAlias)) {
                log.info("Loaded webpage with web alias \"" + webAlias + "\" Title: " + webpageTopic.getSimpleValue());
                return webpageTopic.getChildTopics().getTopic("de.mikromedia.page.web_alias");
            }
        }
        return null;
    }

    /**
     * Prepares some session data used across all our Thymeleaf page templates.
     * @param websiteTopic
     */
    private void setGlobalTemplateParameter(String filename, String websitePrefix) {
        String username = acService.getUsername();
        viewData("authenticated", (username != null));
        viewData("username", username);
        viewData("website", websitePrefix);
        viewData("template", filename);
        viewData("hostUrl", DM4_HOST_URL);
    }

    /**
     * Prepares the most basic data used across all our Thymeleaf page templates.
     * @param website
     */
    private void setWebsiteTemplateParameter(Topic website) {
        viewData("siteName", getWebsiteName(website));
        viewData("siteCaption", getWebsiteCaption(website));
        viewData("siteAbout", getWebsiteAboutHTML(website));
        viewData("footerText", getWebsiteFooter(website));
        viewData("customSiteCss", getWebsiteStylesheetPath(website));
        viewData("menuItems", getActiveMenuItems(website));
    }

    private Viewable getWebpageTemplate(Topic webAliasTopic) {
        try {
            WebpageViewModel page = new WebpageViewModel(webAliasTopic);
            // while logged in users can (potentially) browse a drafted webpage
            if (isNotAllowedToAccessDraft(page)) {
                log.fine("401 => /" + webAliasTopic.getSimpleValue() + " is a DRAFT (yet unpublished)");
                return view("401");
            } else {
                viewData("customPageCss", page.getStylesheet());
                viewData("dateCreated", df.format(page.getCreationDate()));
                viewData("dateModified", df.format(page.getModificationDate()));
                viewData("page", page);
                return view("page");
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("Page Template for Web Alias (ID: "
                + webAliasTopic.getId() + ") could not be prepared", re);
        }
    }

    private Viewable getWebsitesNotFoundPage(Topic standardWebsite) {
        // 1) If no page was returned by now we use the footer of the standard / admins website in our 404 page
        Topic website = standardWebsite;
        if (standardWebsite == null) {
            website = getStandardSiteTopicByURI();
        }
        // 2) fetch standard website globals for filling up the basics in our "404" page
        setGlobalTemplateParameter("404", STANDARD_WEBSITE_PREFIX);
        setWebsiteTemplateParameter(website);
        return view("404");
    }

    private boolean isNotAllowedToAccessDraft(WebpageViewModel page) {
        return (page.isDraft() && acService.getUsername() == null);
    }

    private Topic getRelatedWebsite(Topic username) {
        return username.getRelatedTopic("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "de.mikromedia.site");
    }

    private Topic getWebsiteRelatedUser(Topic website) {
        return website.getRelatedTopic("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "dm4.accesscontrol.username");
    }

    private List<RelatedTopic> getWebsiteRelatedPages(Topic website) {
        return website.getRelatedTopics("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "de.mikromedia.page");
    }

    private String getWebsiteFooter(Topic site) {
        site.loadChildTopics("de.mikromedia.site.footer_html");
        return site.getChildTopics().getString("de.mikromedia.site.footer_html");
    }

    private String getWebsiteCaption(Topic site) {
        site.loadChildTopics("de.mikromedia.site.caption");
        return site.getChildTopics().getStringOrNull("de.mikromedia.site.caption");
    }

    private String getWebsiteAboutHTML(Topic site) {
        site.loadChildTopics("de.mikromedia.site.about_html");
        return site.getChildTopics().getStringOrNull("de.mikromedia.site.about_html");
    }

    private String getWebsiteName(Topic site) {
        site.loadChildTopics("de.mikromedia.site.name");
        return site.getChildTopics().getString("de.mikromedia.site.name");
    }

    private String getWebsiteStylesheetPath(Topic site) {
        site.loadChildTopics("de.mikromedia.site.stylesheet");
        return site.getChildTopics().getTopic("de.mikromedia.site.stylesheet").getSimpleValue().toString();
    }

    /** Bring it back the old "Standard Site", give "admin" back her personal one! */
    private Topic getStandardSiteTopicByURI() {
        return dm4.getTopicByUri("de.mikromedia.standard_site");
    }

}
