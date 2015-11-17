package de.mikromedia.webpages;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.Association;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.event.PostCreateTopicListener;
import de.deepamehta.core.service.accesscontrol.AccessControl;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import de.deepamehta.plugins.accesscontrol.AccessControlService;
import de.deepamehta.plugins.workspaces.WorkspacesService;
import de.mikromedia.webpages.models.MenuItemViewModel;
import de.mikromedia.webpages.models.WebpageViewModel;

import com.sun.jersey.api.view.Viewable;
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
 * @author Malte Reißig (<malte@mikromedia.de>)
 * @version 0.3-SNAPSHOT - compatible with DeepaMehta 4.7
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends WebActivatorPlugin implements WebpagePluginService,
        PostCreateTopicListener {

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
        // ### Replace InputStream with Viewable here..
        if (frontPageResourceName != null && bundleContextUri != null) {
            return dms.getPlugin(bundleContextUri).getStaticResource(frontPageResourceName);
        } else {
            /** // fetch website globals for any of these templates
            prepareTemplateSiteData();
            // fetch all pages with title and stuff **/
            return getStaticResource("/views/welcome.html");
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{pageWebAlias}")
    public Viewable getPageView(@PathParam("pageWebAlias") String webAlias) {
        log.fine("Requested Global Page /" + webAlias);
        // 0) prepare admin website
        // Topic website = loadStandardSiteTopic();
        Topic website = loadWebsiteTopic(AccessControlService.ADMIN_USERNAME); // is more flexible
        prepareTemplateSiteData(website);
        // 1) is webpage of admin
        // 2) is redirect of admin
        log.fine("404 => /" + webAlias + " not found.");
        // 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        return view("404");
    }

    // TODO: /{username}/(home)

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{username}/{pageWebAlias}")
    public Viewable getPageView(@PathParam("username") String username,
                                @PathParam("pageWebAlias") String pageAlias) {
        log.info("Requested Page /" + username + "/" + pageAlias);
        // 0) Fetch users website topic
        Topic usersWebsite = loadWebsiteTopic(username);
        log.info("Loaded website " + usersWebsite.getSimpleValue());
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
        // 2) check if it is a users redirect
        handleWebsiteRedirects(usersWebsite, pageAlias);
        // 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        log.fine("404 => /" + pageAlias + " not found.");
        return view("404");
    }

    /** Lists all currently published webpages for the users website. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{username}/")
    public List<WebpageViewModel> getPublishedWebpages(@PathParam("username") String username) {
        log.info("Listing all published webpages for " + username);
        // fetch all pages with title and all childs
        Topic website = loadWebsiteTopic(username);
        ResultList<RelatedTopic> pages = website.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.page", 0);
        ArrayList<WebpageViewModel> result = new ArrayList();
        Iterator<RelatedTopic> iterator = pages.iterator();
        while (iterator.hasNext()) {
            WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dms);
            if (page.isPublished()) result.add(page);
        }
        return result;
    }

    // --- Hooks

    @Override
    public void postCreateTopic(Topic topic) {
        if (topic.getTypeUri().equals("dm4.accesscontrol.username")) {
            // create an empty website topic for the new user
            Topic website = getWebsiteTopic(topic.getSimpleValue().toString());
            Topic deepaMehtaWorkspace = workspacesService.getWorkspace(WorkspacesService.DEEPAMEHTA_WORKSPACE_URI);
            log.info("Trying to fetch private workspace of new user: " + topic.getSimpleValue().toString());
            Topic privateWorkspace = dms.getAccessControl().getPrivateWorkspace(topic.getSimpleValue().toString());
            workspacesService.assignToWorkspace(website, privateWorkspace.getId());
            // associate an empty website topic to the new username topic
            Association assoc = createWebsiteUsernameAssociation(topic, website);
            workspacesService.assignToWorkspace(assoc, privateWorkspace.getId());
        }
    }



    // --- Private Utility Methods

    private Association createWebsiteUsernameAssociation(Topic usernameTopic, Topic website) {
        return dms.createAssociation(new AssociationModel("dm4.core.association",
                new TopicRoleModel(usernameTopic.getId(), "dm4.core.default"),
                new TopicRoleModel(website.getId(), "dm4.core.default")));
    }

    private void handleWebsiteRedirects(Topic site, String webAlias) {
        ResultList<RelatedTopic> redirectTopics = site.getRelatedTopics("dm4.core.association",
                "dm4.core.default","dm4.core.default", "de.mikromedia.redirect", 0);
        Iterator<RelatedTopic> iterator = redirectTopics.iterator();
        while (iterator.hasNext()) {
            Topic redirectTopic = dms.getTopic(iterator.next().getModel().getId()).loadChildTopics();
            String redirectUrl = redirectTopic.getChildTopics().getString("de.mikromedia.redirect.target_url");
            int statusCode = redirectTopic.getChildTopics().getInt("de.mikromedia.redirect.status_code");
            handleRedirects(webAlias, redirectUrl, statusCode);
        }
    }

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

    private Topic getWebpageByAlias(Topic site, String webAlias) {
        ResultList<RelatedTopic> relatedWebpages = site.getRelatedTopics("dm4.core.association",
                "dm4.core.default","dm4.core.default", "de.mikromedia.page", 0);
        for (RelatedTopic webpage : relatedWebpages.getItems()){
            Topic webpageTopic=dms.getTopic(webpage.getModel().getId()).loadChildTopics();
            log.info("> Checking webpage with Title: "+webpageTopic.getSimpleValue());
            String webpageAlias=webpageTopic.getChildTopics().getString("de.mikromedia.page.web_alias");
            log.info("> Checking webpage "+webpageAlias+" != "+webAlias);
            if(webpageAlias.equals(webAlias)){
                Topic pageAliasTopic=webpageTopic.getChildTopics().getTopic("de.mikromedia.page.web_alias");
            }
        }
        return null;
    }

    private void prepareTemplateSiteData(Topic websiteTopic) {
        viewData("siteName", getCustomSiteTitle(websiteTopic));
        viewData("footerText", getCustomSiteFooter(websiteTopic));
        viewData("customCssPath", getCustomCSSPath(websiteTopic));
        viewData("menuItems", getActiveMenuItems(websiteTopic));
    }

    private Topic getWebsiteTopic(String username) {
        Topic usernameTopic = acService.getUsernameTopic(username);
        Topic website = usernameTopic.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
        if (website == null) {
            // create a new website topic
            return dms.createTopic(new TopicModel("de.mikromedia.site"));
        } else {
            // return the website topic
            return website;
        }
    }

    private List<MenuItemViewModel> getActiveMenuItems(Topic site) {
        ResultList<RelatedTopic> menuItems = site.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.menu.item", 0);
        ArrayList<MenuItemViewModel> result = new ArrayList();
        Iterator<RelatedTopic> iterator = menuItems.iterator();
        while (iterator.hasNext()) {
            MenuItemViewModel menuItem = new MenuItemViewModel(iterator.next().getId(), dms);
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

    private Topic loadWebsiteTopic(String username) {
        Topic user = acService.getUsernameTopic(username);
        return user.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "de.mikromedia.site");
    }

    private Topic loadStandardSiteTopic() {
        return dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
    }

}
