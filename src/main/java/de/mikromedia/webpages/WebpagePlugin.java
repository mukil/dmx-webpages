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
		// 0) fetch website globals for any of these templates
		prepareTemplateSiteData();
		// 1) is global page
		Topic pageAliasTopic = dms.getTopic("de.mikromedia.page.web_alias", new SimpleValue(webAlias));
		if (pageAliasTopic != null) {
			WebpageViewModel page = new WebpageViewModel(pageAliasTopic);
			if (page.isPublished()) {
				viewData("page", page);
				return view("page");
			} else if (!page.isPublished()) {
				log.fine("401 => /" + webAlias + " is yet unpublished.");
				return view("401");
			}
		}
		// 2) is redirect
		Topic redirectAliasTopic = dms.getTopic("de.mikromedia.redirect.web_alias", new SimpleValue(webAlias));
		if (redirectAliasTopic != null) {
			Topic redirectTopic = redirectAliasTopic.getRelatedTopic("dm4.core.composition", "dm4.core.child",
				"dm4.core.parent", "de.mikromedia.redirect");
			String redirectUrl = redirectTopic.getChildTopics().getString("de.mikromedia.redirect.target_url");
			int statusCode = redirectTopic.getChildTopics().getInt("de.mikromedia.redirect.status_code");
			handleRedirects(webAlias, redirectUrl, statusCode);
		}
		log.fine("404 => /" + webAlias + " not found.");
		// 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
		return view("404");
	}

    @GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/{username}/{pageWebAlias}")
	public Viewable getPageView(@PathParam("username") String username,
								@PathParam("pageWebAlias") String webAlias) {
		log.info("Requested Page /" + username + "/" + webAlias);
		// 0) fetch website globals for any of these templates
		prepareTemplateSiteData();
        // 1) Fetch username topic
        Topic user = acService.getUsernameTopic(username);
        Topic usersWebsite = user.getRelatedTopic("dm4.core.association", "dm4.core.default",
				"dm4.core.default", "de.mikromedia.site");
        log.info("Loaded website " + usersWebsite.getSimpleValue());
        // 2) Fetch related webpages
        ResultList<RelatedTopic> relatedWebpages = usersWebsite.getRelatedTopics("dm4.core.association",
                "dm4.core.default","dm4.core.default", "de.mikromedia.page", 0);
		// Topic pageAliasTopic = dms.getTopic("de.mikromedia.page.web_alias", new SimpleValue(webAlias));
        for (RelatedTopic webpage : relatedWebpages.getItems()) {
            Topic webpageTopic = dms.getTopic(webpage.getModel().getId()).loadChildTopics();
            log.info("> Checking webpage with Title: " + webpageTopic.getSimpleValue());
            String webpageAlias = webpageTopic.getChildTopics().getString("de.mikromedia.page.web_alias");
            log.info("> Checking webpage " + webpageAlias + " != " + webAlias);
            if (webpageAlias.equals(webAlias)) {
                Topic pageAliasTopic = webpageTopic.getChildTopics().getTopic("de.mikromedia.page.web_alias");
                WebpageViewModel page = new WebpageViewModel(pageAliasTopic);
                if (page.isPublished()) {
                    viewData("page", page);
                    return view("page");
                } else if (!page.isPublished()) {
                    log.fine("401 => /" + webAlias + " is yet unpublished.");
                    return view("401");
                }
            }
        }
        log.warning("Users Webpage NOT FOUND!");
		// 2) is redirect
		Topic redirectAliasTopic = dms.getTopic("de.mikromedia.redirect.web_alias", new SimpleValue(webAlias));
		if (redirectAliasTopic != null) {
			Topic redirectTopic = redirectAliasTopic.getRelatedTopic("dm4.core.composition", "dm4.core.child",
				"dm4.core.parent", "de.mikromedia.redirect");
			String redirectUrl = redirectTopic.getChildTopics().getString("de.mikromedia.redirect.target_url");
			int statusCode = redirectTopic.getChildTopics().getInt("de.mikromedia.redirect.status_code");
			handleRedirects(webAlias, redirectUrl, statusCode);
		}
		log.fine("404 => /" + webAlias + " not found.");
		// 3) web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
		return view("404");
	}

	/** Lists all currently published webpages in the system. */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{username}/page")
	public List<WebpageViewModel> getPublishedWebpages(@PathParam("username") String username) {
		log.info("Listing all published webpages for " + username);
		// fetch all pages with title and all childs
		ResultList<RelatedTopic> pages = dms.getTopics("de.mikromedia.page", 0);
		ArrayList<WebpageViewModel> result = new ArrayList();
		Iterator<RelatedTopic> iterator = pages.iterator();
		while (iterator.hasNext()) {
			WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dms);
			if (page.isPublished()) result.add(page);
		}
		return result;
	}

	/** Lists all currently active webpage menu items in the system. */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/menu/item")
	public List<MenuItemViewModel> getActiveMenuItems() {
		ResultList<RelatedTopic> allItems = dms.getTopics("de.mikromedia.menu.item", 0);
		ArrayList<MenuItemViewModel> result = new ArrayList();
		Iterator<RelatedTopic> iterator = allItems.iterator();
		while (iterator.hasNext()) {
			MenuItemViewModel menuItem = new MenuItemViewModel(iterator.next().getId(), dms);
			if (menuItem.isActive()) result.add(menuItem); // ### yet to come to my db, adapted migration
			// result.add(menuItem);
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

	private void prepareTemplateSiteData() {
        // TODO: reacto up on website topic
		viewData("siteName", getCustomSiteTitle());
		viewData("footerText", getCustomSiteFooter());
		viewData("customCssPath", getCustomCSSPath());
		viewData("menuItems", getActiveMenuItems());
	}

	private String getCustomSiteFooter() {
		Topic site = loadStandardSiteTopic();
		site.loadChildTopics("de.mikromedia.site.footer_text");
		return site.getChildTopics().getString("de.mikromedia.site.footer_text");
	}
	
	private String getCustomSiteTitle() {
		Topic site = loadStandardSiteTopic();
		site.loadChildTopics("de.mikromedia.site.name");
		return site.getChildTopics().getString("de.mikromedia.site.name");
	}
	
	private String getCustomCSSPath() {
		Topic site = loadStandardSiteTopic();
		site.loadChildTopics("de.mikromedia.site.css_path");
		return site.getChildTopics().getString("de.mikromedia.site.css_path");
	}
	
	private Topic loadStandardSiteTopic() {
		return dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
	}

}
