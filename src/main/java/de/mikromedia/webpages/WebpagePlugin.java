package de.mikromedia.webpages;

import de.mikromedia.webpages.events.WebpageRequestedListener;
import de.mikromedia.webpages.events.FrontpageRequestedListener;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.service.Inject;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.accesscontrol.AccessControlService;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.RoleModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.DeepaMehtaEvent;
import de.deepamehta.core.service.EventListener;
import de.deepamehta.core.service.accesscontrol.AccessControlException;
import de.deepamehta.core.service.event.PreCreateAssociationListener;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.core.util.DeepaMehtaUtils;
import de.deepamehta.thymeleaf.ThymeleafPlugin;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.model.MenuItem;
import de.mikromedia.webpages.model.SearchResult;
import de.mikromedia.webpages.model.SearchResultList;
import de.mikromedia.webpages.model.Webpage;
import de.mikromedia.webpages.model.Website;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONException;
import org.osgi.framework.Bundle;
import de.mikromedia.webpages.events.CustomRootResourceRequestedListener;
import de.mikromedia.webpages.model.Section;

/**
 * Collaborative, multi-site standard HTML web pages with DeepaMehta 4.
 *
 * ### FIXME: Usernames might not be compatible URIComponents.
 * @author Malte Rei&szlig;ig
 * @version 0.4.5-SNAPSHOT - compatible with DeepaMehta 4.8.6+
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends ThymeleafPlugin implements WebpageService, PreCreateAssociationListener {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject AccessControlService accesscontrol;
    @Inject WorkspacesService workspaces;

    private final String DM4_HOST_URL = System.getProperty("dm4.host.url");
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    private static final String FRONTPAGE_TEMPLATE_NAME = "frontpage";

    /** A name of a Viewable registered by another plugin to be served at "/". **/
    String frontPageTemplateName = null;

    /** A map with "name" of Viewables as values registered by other plugins to be served at "/webalias". **/
    HashMap<String, String[]> frontpageTemplateAliases = new HashMap<String, String[]>();

    /**
     * Custom event fired up on HTTP request to a website's frontpage.
     *
     * @return Topic	The website topic.
     */
    static DeepaMehtaEvent FRONTPAGE_REQUESTED = new DeepaMehtaEvent(FrontpageRequestedListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((FrontpageRequestedListener) listener).frontpageRequested((Topic) params[0], (String) params[1]);
        }
    };

    static DeepaMehtaEvent CUSTOM_ROOT_RESOURCE_REQUESTED = new DeepaMehtaEvent(CustomRootResourceRequestedListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((CustomRootResourceRequestedListener) listener).frontpageRequested((Topic) params[0]);
        }
    };

    /**
     * Custom event fired up on HTTP request to a website's frontpage.
     *
     * @return Topic	The webpage topic.
     */
    static DeepaMehtaEvent WEBPAGE_REQUESTED = new DeepaMehtaEvent(WebpageRequestedListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((WebpageRequestedListener) listener).webpageRequested((Webpage) params[0], (String) params[1]);
        }
    };

    @Override
    public void init() {
        log.info("Initializing Mikromedia DM 4 Webpages Thymeleaf Engine");
        initTemplateEngine();
        df.setTimeZone(tz);
    }

    /**
     * The method managing the root resource / frontpage.
     * Note: If overrideFrontpageTemplate() is used (frontpageTemplateName != null),
     * the frontpage for the "standard" site is inaccessible.
     * @return  A processed Thymeleaf Template (<code>Viewable</code>).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getIndexWebpage() {
        Topic website = null;
        String location = "/";
        // 1) Check if a custom frontpage was registered by another plugin
        if (frontPageTemplateName != null) {
            // Set generic template data "authenticated" and "username"
            prepareGenericViewData(frontPageTemplateName, STANDARD_WEBSITE_PREFIX);
            // expose published "webpages" and "menu items" of the standard website to third party frontpages
            website = getStandardWebsite();
            dm4.fireEvent(FRONTPAGE_REQUESTED, website, location);
            log.info("Preparing 3rd PARTY FRONTPAGE view data in dm4-webpages plugin...");
            prepareWebsiteViewData(website, location);
            preparePageHeader(website);
            preparePageSections(website);
            return view(frontPageTemplateName);
        } else { // 2) check if there is a redirect or page realted to the standard site and set to "/"
            website = getWebsiteFrontpage(null);
            // private workspace via our getRelatedTopics()-call
            dm4.fireEvent(FRONTPAGE_REQUESTED, website, location);
            log.info("Preparing STANDARD FRONTPAGE view data for website ("
                    + website.toString() + ") in dm4-webpages plugin...");
            prepareWebsiteViewData(website, location);
            preparePageHeader(website);
            preparePageSections(website);
            // check if their is a redirect setup for this web alias
            handleWebsiteRedirects(website, "/"); // potentially throws WebAppException triggering a Redirect
            return getWebsiteTemplate(website);
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
    public Viewable getWebpage(@PathParam("pageWebAlias") String webAlias) {
        String pageAlias = webAlias.trim();
        // 1) check if for the given "/webAlias" a template was registered by other plugins
        Viewable registeredPage = getCustomRootResourcePage(pageAlias);
        if (registeredPage != null) {
            log.info("Preparing CUSTOM ROOT RESOURCE Page in dm4-webpages plugin...");
            dm4.fireEvent(CUSTOM_ROOT_RESOURCE_REQUESTED, pageAlias);
            return registeredPage;
        }
        log.info("Requesting Webpage /" + pageAlias);
        // 2) check if webAlias matches to a special website prefix
        Topic website = getWebsiteByPrefix(pageAlias);
        if (website != null) {
            log.info("Preparing USER FRONTPAGE view data in dm4-webpages plugin...");
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, pageAlias);
            prepareWebsiteViewData(website, pageAlias);
            preparePageHeader(website);
            preparePageSections(website);
            dm4.fireEvent(FRONTPAGE_REQUESTED, website, pageAlias);
            return getWebsiteTemplate(website);
        }
        // 3) if not, use standard website for page preparation
        website = getStandardWebsite();
        if (website != null) {
            prepareWebsiteViewData(website, webAlias);
        }
        // 4) is webpage of standard site
        Webpage webpage = getWebsitesWebpage(website, pageAlias, STANDARD_WEBSITE_PREFIX);
        if (webpage != null) {
            dm4.fireEvent(WEBPAGE_REQUESTED, webpage, STANDARD_WEBSITE_PREFIX);
            log.info("Preparing WEBPAGE view data ("+webpage.getPageTitle().toString()+") of " + website + " plugin...");
            Viewable webpageTemplate = getWebpageTemplate(webpage);
            return webpageTemplate;
        }
        log.fine("=> /" + pageAlias + " webpage for standard website not found.");
        // 5) is redirect of admin
        handleWebsiteRedirects(website, pageAlias);
        log.fine("=> /" + pageAlias + " redirect for standard website not found.");
        // 6) Resource is neither a custom webAlias page, nor a published or drafted \"Webpage\" or \"Redirect\"
        return getWebsiteNotFoundPage(website);
    }

    /**
     * Serving a specific webpage assigned to the website related to its given prefix (currently "username").
     *
     * @param sitePrefix
     * @param webAlias
     * @return  A processed Thymeleaf Template (<code>Viewable</code>).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{site}/{pageWebAlias}")
    public Viewable getWebsitePage(@PathParam("site") String sitePrefix, @PathParam("pageWebAlias") String webAlias) {
        String pageAlias = webAlias.trim();
        String location = "/" + sitePrefix + "/" + webAlias;
        log.info("Requesting Website Page " + location);
        // 1) Fetch some website topic
        Topic usersWebsite = getWebsiteByPrefix(sitePrefix);
        if (usersWebsite != null) {
            prepareWebsiteViewData(usersWebsite, location);
        }
        // 2) check related webpages
        Webpage webpage = getWebsitesWebpage(usersWebsite, pageAlias, sitePrefix);
        if (webpage != null) {
            dm4.fireEvent(WEBPAGE_REQUESTED, webpage, sitePrefix);
            log.info("Preparing WEBPAGE view data ("+webpage.toString()+") in dm4-webpages plugin...");
            return getWebpageTemplate(webpage);
        }
        log.info("=> /" + pageAlias + " webpage for \"" +sitePrefix+ "\"s website not found.");
        // 3) check if it is a users redirect
        handleWebsiteRedirects(usersWebsite, pageAlias);
        // 4) Log that web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        log.info("=> /" + pageAlias + " webpage redirect for \"" +sitePrefix+ "\"s website not found.");
        // 5) Return 404 page with users website footer
        return getWebsiteNotFoundPage(usersWebsite);
    }

    /** --------------------------------------------------------------------------------- REST API Resources ----- **/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/website/{username}")
    @Override
    public Topic getWebsiteByUsername(@PathParam("username") String username) {
        Topic website = null;
        // return the website topic for the requested username
        Topic usernameTopic = accesscontrol.getUsernameTopic(username);
        if (usernameTopic != null && usernameTopic.getSimpleValue().toString().equals(accesscontrol.getUsername())) {
            website = getOrCreateWebsiteTopic(username);
        }
        return website;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/websites/search")
    public SearchResultList searchWebsites(@QueryParam("q") String query, @QueryParam("t") String typeName) throws JSONException {
        SearchResultList response = new SearchResultList();
        List<Topic> pages = searchWebpageContents(query);
        List<Topic> sites = searchWebsites(query);
        for (Topic page : pages) {
            response.putPageResult(new SearchResult(page));
        }
        for (Topic site : sites) {
            response.putWebsiteResult(new SearchResult(site));
        }
        log.info("Query matched " + pages.size() + " pages, " + sites.size() + " sites");
        return response;
    }

    private List<Topic> searchWebpageContents(String query) {
        List<Topic> results = new ArrayList<Topic>();
        for (Topic headline : dm4.searchTopics("*" + query.trim() + "*", "de.mikromedia.page.headline")) {
            Topic webpage = getParentPage(headline);
            if (!results.contains(webpage) && !webpage.getChildTopics().getBoolean("de.mikromedia.page.is_draft")) {
                results.add(webpage);
            }
        }
        for (Topic content : dm4.searchTopics("*" + query.trim() + "*", "de.mikromedia.page.main")) {
            Topic webpage = getParentPage(content);
            if (!results.contains(webpage) && !webpage.getChildTopics().getBoolean("de.mikromedia.page.is_draft")) {
                results.add(webpage);
            }
        }
        return results;
    }

    private List<Topic> searchWebsites(String query) {
        List<Topic> results = new ArrayList<Topic>();
        for (Topic siteName : dm4.searchTopics("*" + query.trim() + "*", WEBSITE_NAME)) {
            Topic website = getParentSite(siteName);
            if (!results.contains(website)) results.add(website);
        }
        for (Topic siteCaption : dm4.searchTopics("*" + query.trim() + "*", WEBSITE_CAPTION)) {
            Topic website = getParentSite(siteCaption);
            if (!results.contains(website)) results.add(website);
        }
        for (Topic siteFooter : dm4.searchTopics("*" + query.trim() + "*", WEBSITE_FOOTER)) {
            Topic website = getParentSite(siteFooter);
            if (!results.contains(website)) results.add(website);
        }
        return results;
    }

    private Topic getParentPage(Topic child) {
        return child.getRelatedTopic(COMPOSITION, ROLE_CHILD, ROLE_PARENT, WEBPAGE);
    }

    private Topic getParentSite(Topic child) {
        return child.getRelatedTopic(COMPOSITION, ROLE_CHILD, ROLE_PARENT, WEBSITE);
    }

    public Topic getRelatedHeader(Topic topic) {
        Topic header = topic.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT, ROLE_DEFAULT, HEADER);
        if (header != null) {
            header.loadChildTopics();
        }
        return header;
    }

    public List<RelatedTopic> getRelatedWebpageSections(Topic page) {
        List<RelatedTopic> sections = null;
        if (page != null ) {
            sections = page.getRelatedTopics(ASSOCIATION, ROLE_DEFAULT, ROLE_DEFAULT, SECTION);
            if (sections != null && sections.size() > 0) {
                DeepaMehtaUtils.loadChildTopics(sections);
            }
        } else {
            log.warning("Webpage Sections are NULL");
        }
        return sections;
    }

    public Topic getRelatedHeaderDesktopImage(Topic header) {
        Topic desktopImage = header.getRelatedTopic(DESKTOP_IMAGE_ASSOC, ROLE_DEFAULT, ROLE_DEFAULT, DEEPAMEHTA_FILE);
        if (desktopImage != null) {
            desktopImage.loadChildTopics();
        }
        return desktopImage;
    }

    public Topic getRelatedHeaderMobileImage(Topic header) {
        Topic mobileImage = header.getRelatedTopic(MOBILE_IMAGE_ASSOC, ROLE_DEFAULT, ROLE_DEFAULT, DEEPAMEHTA_FILE);
        if (mobileImage != null) {
            mobileImage.loadChildTopics();
        }
        return mobileImage;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/website")
    public Topic getWebsite() {
        Topic website = null;
        String username = accesscontrol.getUsername();
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
        Website site = new Website(topic, dm4);
        Topic username = site.getRelatedUsername();
        if (username != null) { // browse user site
            throw new WebApplicationException(
                Response.temporaryRedirect(new URI("/" + username.getSimpleValue().toString())).build());
        } else { // browse standard site
            throw new WebApplicationException(Response.temporaryRedirect(new URI("/")).build());
        }
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
    public List<Webpage> getPublishedWebpages(@PathParam("username") String username) {
        log.info("Listing all published webpages for \"" + username + "\"");
        Topic website = getOrCreateWebsiteTopic(username);
        if (website != null) {
            Website site = new Website(website, dm4);
            if (site.isWebsiteTopic()) {
                return site.getRelatedWebpagesPublished();
            }
        } else {
            log.warning("No website available under username/prefix \"/" + username + "\" - Returning empty list");
        }
        return new ArrayList<Webpage>();
    }

    /**
     * Lists and prepares all currently published webpages of the given website.
     * @param websiteTopic
     * @return All webpage topics associated with the website for the given username and not marked as \"Drafts\".
     */
    @Override
    public List<Webpage> getPublishedWebpages(Topic websiteTopic) {
        log.info("Listing all published webpages for \"" + websiteTopic.getSimpleValue()+ "\" website");
        Website site = new Website(websiteTopic, dm4);
        if (site.isWebsiteTopic()) {
            return site.getRelatedWebpagesPublished();
        }
        return new ArrayList<Webpage>();
    }

    /**
     * Returns all topics of type <code>de.mikromedia.menu.item</code> related to the given `Website` topic.
     * @param website
     * @return All topics of type menu item associated with the given website.
     */
    @Override
    public List<MenuItem> getActiveMenuItems(Topic website) {
        Website site = new Website(website, dm4);
        return site.getActiveMenuItems();
    }

    /**
     * Fetches the global standard website.
     * @return A topic representing the global standard website which is unrelated to any username.
     * Its pages are accessible under the root resource.
     */
    @Override
    public Topic getStandardWebsite() {
        return dm4.getTopicByUri(STANDARD_WEBSITE_URI);
    }

    public Topic getWebsiteByPrefix(String value) {
        Topic prefix = dm4.getTopicByValue(WEBSITE_PREFIX, new SimpleValue(value));
        Topic website = null;
        if (prefix != null) {
            website = prefix.getRelatedTopic(COMPOSITION, ROLE_CHILD, ROLE_PARENT, WEBSITE);
        }
        return website;
    }

    // --- Private Utility Methods

    /**
     * Performs a HTTP redirect if a <code>de.mikromedia.redirect</code> is associated with the given `Website` topic.
     * @param site
     * @param webAlias
     */
    private void handleWebsiteRedirects(Topic site, String webAlias) {
        if (site != null) {
            Website website = new Website(site, dm4);
            List<RelatedTopic> redirects = website.getConfiguredRedirects();
            Iterator<RelatedTopic> iterator = redirects.iterator();
            while (iterator.hasNext()) {
                Topic redirectTopic = dm4.getTopic(iterator.next().getModel().getId()).loadChildTopics();
                String redirectAliasVaue = redirectTopic.getChildTopics().getString(REDIRECT_WEB_ALIAS);
                if (redirectAliasVaue.equals(webAlias)) {
                    String redirectUrl = redirectTopic.getChildTopics().getString(REDIRECT_TARGET_URL);
                    int statusCode = redirectTopic.getChildTopics().getInt(REDIRECT_STATUS_CODE);
                    performRedirect(webAlias, redirectUrl, statusCode);
                }
            }
        }
    }

    private Topic getUserRelatedWebsite(Topic username) {
        return username.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT, ROLE_DEFAULT, WEBSITE);
    }

    /**
     * @param username
     * @return A topic of type <code>de.mikromedia.site</code> for the given `username`, if none found creates one.
     */
    private Topic getOrCreateWebsiteTopic(final String username) {
        final Topic usernameTopic = accesscontrol.getUsernameTopic(username);
        Topic website = null;
        if (usernameTopic != null) {
            try {
                website = getUserRelatedWebsite(usernameTopic);
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
                    Topic topic = dm4.createTopic(mf.newTopicModel(WEBSITE, mf.newChildTopicsModel()
                        .put(WEBSITE_NAME, "My collection of webpages")
                        .putRef(WEBSITE_STYLESHEET, STANDARD_STYLESHEET_URI)
                        .put(WEBSITE_PREFIX, username.getSimpleValue().toString())
                        .put(WEBSITE_FOOTER, "<p class=\"attribution\">Published with the "
                            + "<a href=\"http://github.com/mukil/dm4-webpages\" title=\"Source Coude: dm4-webpages\">"
                            + "dm4-webpages</a> module.</p>")
                    ));
                    Topic usersWorkspace = dm4.getAccessControl().getPrivateWorkspace(
                        username.getSimpleValue().toString());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics()
                        .getTopic(WEBSITE_NAME), usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics()
                        .getTopic(WEBSITE_FOOTER), usersWorkspace.getId());
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
        return dm4.createAssociation(mf.newAssociationModel(ASSOCIATION,
                mf.newTopicRoleModel(usernameTopic.getId(), ROLE_DEFAULT),
                mf.newTopicRoleModel(website.getId(), ROLE_DEFAULT)));
    }

    /**
     * This method performs HTTP redirects to whatever the caller wants.
     * @param webAlias
     * @param redirectUrl
     * @param statusCode
     */
    private void performRedirect(String webAlias, String redirectUrl, int statusCode) {
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
     * @param website
     * @param webAlias
     * @param sitePrefix
     * @return A processed thymeleaf template if a webpage is related to that website, <code>null</code> otherwise.
     */
    private Webpage getWebsitesWebpage(Topic website, String webAlias, String sitePrefix) {
        Website site = new Website(website, dm4);
        if (site.isWebsiteTopic()) {
            Topic webpageAliasTopic = site.getWebpageByAlias(webAlias);
            if (webpageAliasTopic != null) {
                String location = "/" + sitePrefix + "/" + webAlias;
                prepareGenericViewData("page", sitePrefix);
                prepareWebsiteViewData(website, location);
                return new Webpage(webpageAliasTopic);
            }
        }
        return null;
    }

    private Viewable getWebsiteTemplate(Topic website) {
        // website is null if the request does not have the necessary permissions to access it
        if (website != null) {
            return view(FRONTPAGE_TEMPLATE_NAME);
        } else {
            return getWebsiteNotFoundPage(website); // which loads standard topic if website is null
        }
    }

    private Topic getWebsiteFrontpage(String username) {
        Topic website = null;
        if (username == null) {
            website = getStandardWebsite();
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, STANDARD_WEBSITE_PREFIX);
        } else {
            website = getOrCreateWebsiteTopic(username);
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, username);
        }
        return website;
    }

    private Viewable getWebsiteNotFoundPage(Topic standardWebsite) {
        // 1) If no page was returned by now we use the value placed in "Footer" of the standard / admins website in our 404 page
        // ### Fixme: Can we respond with HTTP Status 404 and a template, too? Serving robots and humans?
        Topic website = standardWebsite;
        if (website == null) {
            website = getStandardWebsite();
        }
        prepareGenericViewData("404", STANDARD_WEBSITE_PREFIX);
        prepareWebsiteViewData(website, "404");
        return view("404");
    }

    private Viewable getCustomRootResourcePage(String pageAlias) {
        if (isFrontpageAliasRegistered(pageAlias)) {
            String[] templateValue = frontpageTemplateAliases.get(pageAlias);
            String templateName = templateValue[0];
            log.info("Loading template \"views/" + templateName + ".html\" for \"" + pageAlias + "\"");
            prepareGenericViewData(templateName, pageAlias);
            Topic standardSite = getStandardWebsite();
            prepareWebsiteViewData(standardSite, pageAlias);
            viewData("website", pageAlias); // Fixme: used as url prefix? Otherwise drop.
            viewData("siteName", pageAlias);
            return view(templateName);
        }
        return null;
    }

    private Viewable getWebpageTemplate(Webpage page) {
        try {
            // while logged in users can (potentially) browse a drafted webpage
            if (isNotAllowedToAccessDraft(page)) {
                log.fine("401 => /" + page.getWebAlias() + " is a DRAFT (yet unpublished)");
                return view("401");
            } else {
                viewData("customPageCss", page.getStylesheet());
                viewData("dateCreated", df.format(page.getCreationDate()));
                viewData("dateModified", df.format(page.getModificationDate()));
                viewData("page", page);
                preparePageHeader(page.getTopic());
                preparePageSections(page.getTopic());
                return view("page");
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("Page Template for Webpage Topic (ID: "
                + page.getId()+ ") could not be prepared", re);
        }
    }

    /**
     * Prepares some session data used across all our Thymeleaf page templates.
     * @param websiteTopic
     */
    private void prepareGenericViewData(String filename, String websitePrefix) {
        String username = accesscontrol.getUsername();
        viewData("authenticated", (username != null));
        viewData("username", username);
        viewData("website", websitePrefix);
        viewData("template", filename);
        viewData("hostUrl", DM4_HOST_URL);
    }

    private void preparePageHeader(Topic topic) {
        log.info("Preparing Page Header for " + topic.getSimpleValue().toString());
        Topic header = getRelatedHeader(topic);
        if (header != null) {
            log.info("Found Page Header " + header.getSimpleValue().toString());
            // 1.) set custom Header data
            viewData("header", header);
            // 2.) fetch and set custom header background images
            Topic desktopHeaderImage = getRelatedHeaderDesktopImage(header);
            if (desktopHeaderImage != null) {
                viewData("desktopHeaderImage", desktopHeaderImage);
            }
            Topic mobileHeaderImage = getRelatedHeaderMobileImage(header);
            if (mobileHeaderImage != null) {
                viewData("mobileHeaderImage", mobileHeaderImage);
            }
            // 3.) fetch and set custom header buttons
            List<RelatedTopic> buttons = header.getRelatedTopics(AGGREGATION, ROLE_PARENT, ROLE_CHILD, BUTTON);
            DeepaMehtaUtils.loadChildTopics(buttons);
            viewData("headerButtons", buttons);
        }
    }

    private void preparePageSections(Topic topic) {
        List<RelatedTopic> sections = getRelatedWebpageSections(topic);
        List<Section> above = new ArrayList();
        List<Section> below = new ArrayList();
        List<Section> asideLeft = new ArrayList();
        List<Section> asideRight = new ArrayList();
        if (sections != null && sections.size() > 0) {
            for (Topic section : sections) {
                Section pageSection = new Section(section);
                Topic placement = pageSection.getPlacement();
                if (placement != null && placement.getUri().equals(PLACEMENT_ABOVE)) {
                    above.add(pageSection);
                } else if (placement != null && placement.getUri().equals(PLACEMENT_BELOW)) {
                    below.add(pageSection);
                }
            }
            // 1.) set custom Header data
            if (above.size() > 0) viewData("sectionsAbove", above);
            if (below.size() > 0) viewData("sectionsBelow", below);
        }
    }

    /**
     * Prepares the most basic data used across all our Thymeleaf page templates.
     * @param website
     */
    private void prepareWebsiteViewData(Topic website, String href) {
        if (website != null) {
            Website site = new Website(website, dm4);
            viewData("siteName", site.getName());
            viewData("siteCaption", site.getCaption());
            viewData("siteAbout", site.getAboutHTML());
            viewData("siteId", website.getId());
            viewData("footerText", site.getFooter());
            viewData("customSiteCss", site.getStylesheetPath());
            viewData("menuItems", site.getActiveMenuItems());
            viewData("location", href);
            List<Webpage> webpages = getPublishedWebpages(website);
            // sort webpages on websites frontpage by modification time
            viewData("webpages", getWebpagesSortedByTimestamp(webpages, true)); // false=creationDate */
        } else {
            log.warning("Preparing webpage template failed because a given website could not be found");
        }
    }

    private boolean isFrontpageAliasRegistered(String frontpageAlias) {
        return (frontpageTemplateAliases.get(frontpageAlias) != null);
    }

    private boolean isNotAllowedToAccessDraft(Webpage page) {
        return (page.isDraft() && accesscontrol.getUsername() == null);
    }

    @Override
    public List<Webpage> getWebpagesSortedByTimestamp(List<Webpage> all, final boolean lastModified) {
        Collections.sort(all, new Comparator<Webpage>() {
            public int compare(Webpage t1, Webpage t2) {
                try {
                    Date one = null;
                    Date two = null;
                    if (lastModified) {
                        one = t1.getModificationDate();
                        two = t2.getModificationDate();
                    } else { // Default
                        one = t1.getCreationDate();
                        two = t2.getCreationDate();
                    }
                    if ( one.getTime() < two.getTime() ) return 1;
                    if ( one.getTime() > two.getTime() ) return -1;
                } catch (Exception nfe) {
                    log.warning("Error while accessing modification timestamp of Webpage1: " + t1.getId() + " Webpage2: "
                            + t2.getId() + " nfe: " + nfe.getMessage());
                    return 0;
                }
                return 0;
            }
        });
        return all;
    }

    @Override
    public void overrideFrontpageTemplate(String fileName) {
        this.frontPageTemplateName = fileName;
    }

    /** TODO: Renanem to configureRootResources() **/
    @Override
    public void setFrontpageAliases(HashMap aliases) {
        log.info("Configured " + aliases.size() + " webAliases under webpages \"/\" resource");
        this.frontpageTemplateAliases = aliases;
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

    @Override
    public void preCreateAssociation(AssociationModel am) {
        if (am.getTypeUri().equals(ASSOCIATION)) {
            RoleModel player1 = am.getRoleModel1();
            RoleModel player2 = am.getRoleModel2();
            Topic topic1 = dm4.getTopic(player1.getPlayerId());
            Topic topic2 = dm4.getTopic(player2.getPlayerId());
            // Between Header and File we auto-type to "de.mikromedia.header.desktop_image"
            if (topic1.getTypeUri().equals(HEADER) || topic2.getTypeUri().equals(HEADER)) {
                if (topic1.getTypeUri().equals(DEEPAMEHTA_FILE) || topic2.getTypeUri().equals(DEEPAMEHTA_FILE) ) {
                    DeepaMehtaUtils.associationAutoTyping(am, HEADER,
                        DEEPAMEHTA_FILE, DESKTOP_IMAGE_ASSOC, ROLE_DEFAULT, ROLE_DEFAULT, dm4);
                }
            }
        }
    }

}
