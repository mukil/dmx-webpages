package de.mikromedia.webpages;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.service.Inject;
import de.mikromedia.webpages.models.MenuItemViewModel;
import de.mikromedia.webpages.models.WebpageViewModel;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.accesscontrol.AccessControlService;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
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
 * Simple HTML webpages with DeepaMehta 4.
 * 
 * ### FIXME: Usernames might not be compatible URIComponents.
 * @author Malte Rei&szlig;ig (<a href="mailto:malte@mikromedia.de">Mail</a>)
 * @version 0.4-SNAPSHOT - compatible with DeepaMehta 4.8.2
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

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getFrontpageView() {
        // 0) Set generic template data "authenticated" and "username"
        // 1) Check if a custom frontpage was registered by another plugin
        if (frontPageTemplateName != null) {
            prepareGenericTemplateData(frontPageTemplateName, null);
            return view(frontPageTemplateName);
        } else { // 2) check if there is a redirect of user "admin" set on "/"
            return getFrontpageResource(AccessControlService.ADMIN_USERNAME);
        }
    }
    
    public Viewable getFrontpageResource(@PathParam("username") String username) {
        Topic usersWebsite = getOrCreateWebsiteTopic(username);
        handleWebsiteRedirects(usersWebsite, "/"); // potentially throws WebAppException triggering a Redirect
        prepareGenericTemplateData("frontpage", username);
        prepareSiteTemplate(usersWebsite);
        viewData("pages", getPublishedWebpages(username)); // sort by creation or modification date
        return view("frontpage");
    }

    /**
     * Serves either the <em>Webpage</em> topics content in the page template, a <em>Webpage Redirect</em> (301,
     * 302) or <em>404</em> as response.
     *
     * @param webAlias  String  URI compliant name of the resource without leading slash.
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{pageWebAlias}")
    public Viewable getPageView(@PathParam("pageWebAlias") String webAlias) {
        log.fine("Requested Global Page /" + webAlias);
        // 0) check if webAlias is username
        Topic username = dm4.getAccessControl().getUsernameTopic(webAlias.trim());
        if (username != null) {
            return getFrontpageResource(username.getSimpleValue().toString());
        }
        // 1) prepare admin website
        Topic adminsWebsite = getOrCreateWebsiteTopic(AccessControlService.ADMIN_USERNAME);
        prepareSiteTemplate(adminsWebsite);
        // 2) is webpage of admin
        Topic webpageAliasTopic = getWebpageByAlias(adminsWebsite, webAlias);
        if (webpageAliasTopic != null) {
            return preparePageTemplate(webpageAliasTopic, AccessControlService.ADMIN_USERNAME);
        }
        log.fine("=> /" + webAlias + " webpage for admins website not found.");
        // 3) is redirect of admin
        handleWebsiteRedirects(adminsWebsite, webAlias);
        log.fine("=> /" + webAlias + " webpage redirect for admins website not found.");
        // 4) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        return view("404");
    }

    /**
     * For "admin" only webpages (or redirects) this methods serves exactly the same responses as its sister method
     * (@see getPageView()) just under a different (username specific) url.
     *
     * @param username
     * @param pageAlias
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{username}/{pageWebAlias}")
    public Viewable getPageView(@PathParam("username") String username,
            @PathParam("pageWebAlias") String pageAlias) {
        log.info("Request Page /" + username + "/" + pageAlias);
        // 0) Fetch users website topic
        Topic usersWebsite = getOrCreateWebsiteTopic(username);
        log.fine("Load website \"" + usersWebsite.getSimpleValue() + "\"");
        // 1) fetch website globals for any of these templates
        prepareSiteTemplate(usersWebsite);
        // 2) check related webpages
        Topic pageAliasTopic = getWebpageByAlias(usersWebsite, pageAlias);
        if (pageAliasTopic != null) {
            WebpageViewModel page = new WebpageViewModel(pageAliasTopic);
            if (!page.isDraft()) {
                return preparePageTemplate(pageAliasTopic, username);
            } else if (page.isDraft()) {
                log.fine("401 => /" + pageAlias + " is yet unpublished.");
                return view("401");
            }
        }
        log.fine("=> /" + pageAlias + " webpage for \"" +username+ "\"s website not found.");
        // 2) check if it is a users redirect
        handleWebsiteRedirects(usersWebsite, pageAlias);
        // 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        log.fine("=> /" + pageAlias + " webpage redirect for \"" +username+ "\"s website not found.");
        return view("404");
    }



    /** --------------------------------------------------------------------------------- REST API Resources ----- **/


    
    /**
     * Lists all currently published webpages for the users website.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{username}")
    public List<WebpageViewModel> getPublishedWebpages(@PathParam("username") String username) {
        log.info("Listing all published webpages for \"" + username + "\"");
        // fetch all pages with title and all childs
        ArrayList<WebpageViewModel> result = new ArrayList();
        Topic website = getOrCreateWebsiteTopic(username);
        if (website != null) {
            List<RelatedTopic> pages = website.getRelatedTopics("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "de.mikromedia.page");
            Iterator<RelatedTopic> iterator = pages.iterator();
            while (iterator.hasNext()) {
                WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dm4);
                if (!page.isDraft()) result.add(page);
            }
        }
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/website/{username}")
    public Topic getWebsiteByUsername(@PathParam("username") String username) {
        Topic topic = acService.getUsernameTopic(username);
        Topic website = null;
        if (topic != null && topic.getSimpleValue().toString().equals(acService.getUsername())) {
            website = getOrCreateWebsiteTopic(username);
        }
        return website;
    }

    @GET
    @Path("/website/{username}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hasWebsiteByUsername(@PathParam("username") String username) throws WebApplicationException {
        Topic topic = acService.getUsernameTopic(username);
        if (topic != null && topic.getSimpleValue().toString().equals(acService.getUsername())) {
            if (getWebsiteTopicAssociated(topic) != null) {
                throw new WebApplicationException(Response.status(Status.OK).build());
            }
        }
        throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
    }

    @GET
    @Path("/browse/{websiteId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String doBrowseWebsite(@PathParam("websiteId") long websiteId) throws WebApplicationException, URISyntaxException {
        Topic topic = dm4.getTopic(websiteId);
        if (topic.getTypeUri().equals("de.mikromedia.site")) {
            Topic username = getUsernameAssociated(topic);
            if (username != null) {
                throw new WebApplicationException(Response.temporaryRedirect(new URI("/" + username.getSimpleValue().toString())).build());
            }
        }
        throw new WebApplicationException(Response.status(Status.OK).build());
    }

    // --- Private Utility Methods

    private Topic getWebsiteTopicAssociated(Topic username) {
        return username.getRelatedTopic("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "de.mikromedia.site");
    }

    private Topic getUsernameAssociated(Topic website) {
        return website.getRelatedTopic("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "dm4.accesscontrol.username");
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
                            + "<a href=\"http://github.com/mukil/dm4-webpages\" title=\"dm4-webpages Website\">"
                            + "dm4-webpages</a> module written by <a href=\"http://www.mikromedia.de\">Malte Rei&szlig;ig</a>, 2015-2016.</p>")
                    ));
                    Topic usersWorkspace = dm4.getAccessControl().getPrivateWorkspace(username.getSimpleValue().toString());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics().getTopic("de.mikromedia.site.name"), usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics().getTopic("de.mikromedia.site.stylesheet"), usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic.getChildTopics().getTopic("de.mikromedia.site.footer_html"), usersWorkspace.getId());
                    Association userWebsiteRelation = createWebsiteUsernameAssociation(username, topic);
                    dm4.getAccessControl().assignToWorkspace(userWebsiteRelation, usersWorkspace.getId());
                    dm4.getAccessControl().assignToWorkspace(topic, usersWorkspace.getId());
                    return topic;
                }
            });
            website = websiteTopic;
            log.info("Created a NEW website topic (ID: " + website.getId() + ") in \"Private Workspace\" of \"" + username.getSimpleValue() + "\"");
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
     * If a topic of type <code>de.mikromedia.redirect</code> is simply associated with the given `Website` topic,
     * the related is performed.
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
                handleRedirects(webAlias, redirectUrl, statusCode);
            }
        }
    }

    /**
     * This method performs HTTP redirects to whatever the caller wants.
     * @param webAlias
     * @param redirectUrl
     * @param statusCode
     */
    private void handleRedirects(String webAlias, String redirectUrl, int statusCode) {
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
        List<RelatedTopic> relatedWebpages = site.getRelatedTopics("dm4.core.association",
                "dm4.core.default","dm4.core.default", "de.mikromedia.page");
        for (RelatedTopic webpage : relatedWebpages) {
            Topic webpageTopic = dm4.getTopic(webpage.getModel().getId()).loadChildTopics();
            String webpageAlias = webpageTopic.getChildTopics().getString("de.mikromedia.page.web_alias");
            if (webpageAlias.equals(webAlias)) {
                return webpageTopic.getChildTopics().getTopic("de.mikromedia.page.web_alias");
            }
        }
        return null;
    }

    /**
     * Prepares some session data used across all our Thymeleaf page templates.
     * @param websiteTopic
     */
    private void prepareGenericTemplateData(String filename, String websiteAlias) {
        String username = acService.getUsername();
        viewData("authenticated", (username != null));
        viewData("username", username);
        viewData("website", websiteAlias);
        viewData("template", filename);
        viewData("hostUrl", DM4_HOST_URL);
    }

    /**
     * Prepares the most basic data used across all our Thymeleaf page templates.
     * @param websiteTopic
     */
    private void prepareSiteTemplate(Topic websiteTopic) {
        viewData("siteName", getCustomSiteTitle(websiteTopic));
        viewData("siteCaption", getCustomSiteCaption(websiteTopic));
        viewData("siteAbout", getCustomSiteAboutHTML(websiteTopic));
        viewData("footerText", getCustomSiteFooter(websiteTopic));
        viewData("customSiteCss", getCustomCSSPath(websiteTopic));
        viewData("menuItems", getActiveMenuItems(websiteTopic));
    }

    private Viewable preparePageTemplate(Topic webAliasTopic, String websiteAlias) {
        try {
            WebpageViewModel page = new WebpageViewModel(webAliasTopic);
            // while logged in users can (potentially) browse a drafted webpage
            if (page.isDraft() && acService.getUsername() == null) {
                log.fine("401 => /" + webAliasTopic.getSimpleValue() + " is a DRAFT (yet unpublished)");
                return view("401");
            } else {
                prepareGenericTemplateData("page", websiteAlias);
                viewData("customPageCss", page.getStylesheet());
                viewData("dateCreated", df.format(page.getCreationDate()));
                viewData("dateModified", df.format(page.getModificationDate()));
                viewData("page", page);
                return view("page");
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("Page Template for Web Alias (ID: "+webAliasTopic.getId()+") could not be prepared", re);
        }
    }

    /**
     * Returns a topic of type <code>de.mikromedia.site</code> for the given `username`, if none found creates one.
     * @param username
     * @return
     */
    private Topic getOrCreateWebsiteTopic(final String username) {
        final Topic usernameTopic = acService.getUsernameTopic(username);
        Topic website = null;
        if (usernameTopic != null) {
            website = getWebsiteTopicAssociated(usernameTopic);
            if (website == null) {
                // create new Website Topic
                website = createWebsiteTopic(usernameTopic);
            } else {
                // return existing website topic
                return website;
            }
        }
        log.info("Fetched Website topic for "+username+ ", assigned to Workspace ID: \""
            + workspacesService.getAssignedWorkspace(website.getId()).getId() + "\"");
        return website;
    }

    /**
     * Returns all topics of type <code>de.mikromedia.menu.item</code> related to the given `Website` topic.
     * @param site
     * @return
     */
    private List<MenuItemViewModel> getActiveMenuItems(Topic site) {
        List<RelatedTopic> menuItems = site.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.menu.item");
        ArrayList<MenuItemViewModel> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItemViewModel menuItem = new MenuItemViewModel(iterator.next().getId(), dm4);
            if (menuItem.isActive()) result.add(menuItem); // ### yet to come to my db, adapted migration
            // result.add(menuItem);
        }
        return result;
    }

    private String getCustomSiteFooter(Topic site) {
        site.loadChildTopics("de.mikromedia.site.footer_html");
        return site.getChildTopics().getString("de.mikromedia.site.footer_html");
    }

    private String getCustomSiteCaption(Topic site) {
        site.loadChildTopics("de.mikromedia.site.caption");
        return site.getChildTopics().getStringOrNull("de.mikromedia.site.caption");
    }

    private String getCustomSiteAboutHTML(Topic site) {
        site.loadChildTopics("de.mikromedia.site.about_html");
        return site.getChildTopics().getStringOrNull("de.mikromedia.site.about_html");
    }

    private String getCustomSiteTitle(Topic site) {
        site.loadChildTopics("de.mikromedia.site.name");
        return site.getChildTopics().getString("de.mikromedia.site.name");
    }

    private String getCustomCSSPath(Topic site) {
        site.loadChildTopics("de.mikromedia.site.stylesheet");
        return site.getChildTopics().getTopic("de.mikromedia.site.stylesheet").getSimpleValue().toString();
    }

    private Topic loadWebsiteTopic(String username) {
        Topic user = acService.getUsernameTopic(username);
        return user.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
    }

    private Topic loadStandardSiteTopic() {
        return dm4.getTopicByUri("de.mikromedia.standard_site");
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


}
