package de.mikromedia.webpages;

import de.mikromedia.webpages.service.WebpagePluginService;
import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Simple HTML pages with DeepaMehta 4.
 * 
 * @author Malte Reißig (<malte@mikromedia.de>)
 * @version 0.1-SNAPSHOT - compatible with DeepaMehta 4.4
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends WebActivatorPlugin implements WebpagePluginService {

	private Logger log = Logger.getLogger(getClass().getName());
	
	@Inject AccessControlService acService;

	String frontPageResourceName = null, bundleContextUri = null;

	@Override
	public void init() {
		initTemplateEngine();
	}

	@Override
	public void postInstall() {
		// 1) Make standard site topic editable
		Topic siteTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
		acService.setCreator(siteTopic, "admin");
		acService.setOwner(siteTopic, "admin");
		acService.setACL(siteTopic, new AccessControlList(new ACLEntry(Operation.WRITE, UserRole.OWNER)));
		// 2) Hook up standard site topic with plugin topic
		Topic pluginTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.webpages"));
		dms.createAssociation(new AssociationModel("dm4.core.association",
			new TopicRoleModel(siteTopic.getId(), "dm4.core.child"),
			new TopicRoleModel(pluginTopic.getId(), "dm4.core.parent")
		));
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
	@Path("/{webAlias}")
	public Viewable getPageView(@PathParam("webAlias") String webAlias) {
		log.fine("Requested Page /" + webAlias);
		// 0) fetch website globals for any of these templates
		prepareTemplateSiteData();
		// 1) is local page
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
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/page")
	public ArrayList<WebpageViewModel> getPublishedWebpages () {
		log.info("Listing all published webpages.");
		// fetch all pages with title and stuff
		ResultList<RelatedTopic> pages = dms.getTopics("de.mikromedia.page", 0);
		ArrayList<WebpageViewModel> result = new ArrayList();
		Iterator<RelatedTopic> iterator = pages.iterator();
		while (iterator.hasNext()) {
			WebpageViewModel page = new WebpageViewModel(iterator.next().getId(), dms);
			if (page.isPublished()) result.add(page);
		}
		return result;
	}

	/** Deprecated as of 0.2-SNAPSHOT */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/page/{webAlias}")
	public Viewable getOldPageView(@PathParam("webAlias") String webAlias) {
		try {
			throw new WebApplicationException(Response.seeOther(new URI("/" + webAlias)).build());
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
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

	// ### Get Menu Items
	
	private void prepareTemplateSiteData() {
		viewData("siteName", getCustomSiteTitle());
		viewData("footerText", getCustomSiteFooter());
		viewData("customCssPath", getCustomCSSPath());
	}

	private String getCustomSiteFooter() {
		Topic site = loadCustomSiteTopic();
		site.loadChildTopics("de.mikromedia.site.footer_text");
		return site.getChildTopics().getString("de.mikromedia.site.footer_text");
	}
	
	private String getCustomSiteTitle() {
		Topic site = loadCustomSiteTopic();
		site.loadChildTopics("de.mikromedia.site.name");
		return site.getChildTopics().getString("de.mikromedia.site.name");
	}
	
	private String getCustomCSSPath() {
		Topic site = loadCustomSiteTopic();
		site.loadChildTopics("de.mikromedia.site.css_path");
		return site.getChildTopics().getString("de.mikromedia.site.css_path");
	}
	
	private Topic loadCustomSiteTopic() {
		return dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
	}

}
