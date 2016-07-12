package de.mikromedia.webpages;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Inject;
import de.mikromedia.webpages.models.MenuItemViewModel;
import de.mikromedia.webpages.models.WebpageViewModel;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.accesscontrol.AccessControlService;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple HTML webpages with DeepaMehta 4.
 * 
 * @author Malte Rei&szlig;ig (<a href="mailto:malte@mikromedia.de">Mail</a>)
 * @version 0.3.1-SNAPSHOT - compatible with DeepaMehta 4.8 just for the WTUI
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends ThymeleafPlugin implements WebpagePluginService {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject AccessControlService acService;
    @Inject WorkspacesService workspacesService;

    String frontPageResourceName = null, bundleContextUri = null;

    @Override
    public void init() {
        initTemplateEngine();
    }

    @Override
    public void setFrontpageResource(String fileName, String bundleUri) {
        this.frontPageResourceName = fileName;
        this.bundleContextUri = bundleUri;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream getFrontpageView() {
        // ### Replace InputStream with Viewable here.. see https://trac.deepamehta.de/ticket/873
        if (frontPageResourceName != null && bundleContextUri != null) {
            // 1) check if a custom frontpage was registered by another plugin
            return dm4.getPlugin(bundleContextUri).getStaticResource(frontPageResourceName);
        } else {
            // 2) check if there is a redirect of user "admin" set on "/"
            Topic adminsWebsite = getWebsiteTopic(AccessControlService.ADMIN_USERNAME); // is more flexible
            handleWebsiteRedirects(adminsWebsite, "/"); // potentially throws WebAppException triggering a Redirect
            // 3) Fetch some static default HTML..
            /** // fetch website globals for any of these templates
            prepareTemplateSiteData();
            // fetch all pages with title and stuff **/
            return getStaticResource("/views/welcome.html");
        }
    }

    /**
     * Serves either the <em>Webpage</em> topics content in the page template, a <em>Webpage Redirect</em> (301,
     * 302) or  <em>404</em> as response.
     *
     * @param webAlias  String  URI compliant name of the resource without leading slash.
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{pageWebAlias}")
    public Viewable getPageView(@PathParam("pageWebAlias") String webAlias) {
        log.fine("Requested Global Page /" + webAlias);
        // 0) prepare admin website
        // Topic website = loadStandardSiteTopic();
        Topic adminsWebsite = getWebsiteTopic(AccessControlService.ADMIN_USERNAME); // is more flexible
        prepareTemplateSiteData(adminsWebsite);
        // 1) is webpage of admin
        Topic webpageAliasTopic = getWebpageByAlias(adminsWebsite, webAlias);
        if (webpageAliasTopic != null) {
            WebpageViewModel page = new WebpageViewModel(webpageAliasTopic);
            if (page.isPublished()) {
                viewData("page", page);
                return view("page");
            } else if (!page.isPublished()) {
                log.fine("401 => /" + webAlias + " is yet unpublished.");
                return view("401");
            }
        }
        log.fine("=> /" + webAlias + " webpage for admins website not found.");
        // 2) is redirect of admin
        handleWebsiteRedirects(adminsWebsite, webAlias);
        log.fine("=> /" + webAlias + " webpage redirect for admins website not found.");
        // 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        return view("404");
    }

    /**
     * For "admin" only webpages this methods serves exactly the same responses as its sister method
     * (@see getPageView()) just under a different (username specific) url.
     *
     * This method is just here for forward-compatibility reasons. Users of this plugin can now share a URL which
     * still will be the URL of their content in the future, e.g. especially when upgrading to a multi-ste
     * installation.
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
        log.info("Requested Page /" + username + "/" + pageAlias);
        // 0) Fetch users website topic
        Topic usersWebsite = getWebsiteTopic(username);
        log.fine("Loaded website \"" + usersWebsite.getSimpleValue() + "\"");
        // 1) fetch website globals for any of these templates
        prepareTemplateSiteData(usersWebsite);
        // 2) check related webpages
        Topic pageAliasTopic = getWebpageByAlias(usersWebsite, pageAlias);
        if (pageAliasTopic != null) {
            WebpageViewModel page = new WebpageViewModel(pageAliasTopic);
            if (page.isPublished()) {
                viewData("page", page);
                return view("page");
            } else if (!page.isPublished()) {
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

    /** Lists all currently published webpages for the users website. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{username}/page")
    public List<WebpageViewModel> getPublishedWebpages(@PathParam("username") String username) {
        log.info("Listing all published webpages for " + username);
        // fetch all pages with title and all childs
        Topic website = getWebsiteTopic(username);
        List<RelatedTopic> pages = website.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.page");
        ArrayList<WebpageViewModel> result = new ArrayList();
        Iterator<RelatedTopic> iterator = pages.iterator();
        while (iterator.hasNext()) {
            WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dm4);
            if (page.isPublished()) result.add(page);
        }
        return result;
    }

    // --- Hooks

    /** @Override
    public void postCreateTopic(Topic topic) {
        // ### Try this with reacting upon "dm4.accesscontrol.user_account" creation.
        if (topic.getTypeUri().equals("dm4.accesscontrol.user_account")) {
            log.info("User Account Creation Post Create Listener: " + topic.toJSON().toString());
            topic.loadChildTopics("dm4.accesscontrol.username");
            Topic username = topic.getChildTopics().getTopic("dm4.accesscontrol.username");
            // create an empty website topic for the new user
            // TODO: Write migration covering the following setup for existing user accounts, too!
            Topic website = getWebsiteTopic(username.getSimpleValue().toString());
            log.info("Trying to fetch private workspace of new user: " + username.getSimpleValue().toString());
            // Fails currently, see https://trac.deepamehta.de/ticket/889 for details
            Topic privateWorkspace = dms.getAccessControl().getPrivateWorkspace(username.getSimpleValue().toString());
            workspacesService.assignToWorkspace(website, privateWorkspace.getId());
            // associate an empty website topic to the new username topic
            Association assoc = createWebsiteUsernameAssociation(username, website);
            workspacesService.assignToWorkspace(assoc, privateWorkspace.getId());
        }
    } **/



    // --- Private Utility Methods

    /** private Association createWebsiteUsernameAssociation(Topic usernameTopic, Topic website) {
        return dms.createAssociation(new AssociationModel("dm4.core.association",
                new TopicRoleModel(usernameTopic.getId(), "dm4.core.default"),
                new TopicRoleModel(website.getId(), "dm4.core.default")));
    } **/

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
     * Prepares the most basic data used across all our Thymeleaf page templates.
     * @param websiteTopic
     */
    private void prepareTemplateSiteData(Topic websiteTopic) {
        viewData("siteName", getCustomSiteTitle(websiteTopic));
        viewData("footerText", getCustomSiteFooter(websiteTopic));
        viewData("customCssPath", getCustomCSSPath(websiteTopic));
        viewData("menuItems", getActiveMenuItems(websiteTopic));
    }

    /**
     * Returns a topic of type <code>de.mikromedia.site</code> for the given `username` or null.
     * @param username
     * @return
     */
    private Topic getWebsiteTopic(String username) {
        Topic usernameTopic = acService.getUsernameTopic(username);
        Topic website = usernameTopic.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
        if (website == null) {
            // create a new website topic
            return dm4.createTopic(mf.newTopicModel("de.mikromedia.site"));
        } else {
            // return the website topic
            return website;
        }
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
        site.loadChildTopics("de.mikromedia.site.footer_text");
        return site.getChildTopics().getString("de.mikromedia.site.footer_text");
    }

    private String getCustomSiteTitle(Topic site) {
        site.loadChildTopics("de.mikromedia.site.name");
        return site.getChildTopics().getString("de.mikromedia.site.name");
    }

    private String getCustomCSSPath(Topic site) {
        site.loadChildTopics("de.mikromedia.site.css_path");
        return site.getChildTopics().getString("de.mikromedia.site.css_path");
    }

    /** private Topic loadWebsiteTopic(String username) {
        Topic user = acService.getUsernameTopic(username);
        return user.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
    } **/

    /** private Topic loadStandardSiteTopic() {
        return dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
    } **/

}
