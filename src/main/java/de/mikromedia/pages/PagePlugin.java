package de.mikromedia.pages;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.Inject;
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
import java.nio.file.attribute.AclEntry;
import javax.ws.rs.PathParam;

/**
 * Simple HTML pages with DeepaMehta 4.
 * 
 * @author Malte Reißig (<malte@mikromedia.de>)
 * @version 0.1-SNAPSHOT - compatible with DeepaMehta 4.4
 */
@Path("/page")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PagePlugin extends WebActivatorPlugin {

	private Logger log = Logger.getLogger(getClass().getName());
	
	@Inject AccessControlService acService;

	@Override
	public void init() {
		initTemplateEngine();
	}
	
	@Override
	public void postInstall() {
		Topic siteTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
		acService.setCreator(siteTopic, "admin");
		acService.setOwner(siteTopic, "admin");
		acService.setACL(siteTopic, new AccessControlList(new ACLEntry(Operation.WRITE, UserRole.OWNER)));
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/{webAlias}")
	public Viewable getPage(@PathParam("webAlias") String webAlias) {
		log.info("Requested Page " + webAlias);
		Topic pageAliasTopic = dms.getTopic("de.mikromedia.page.web_alias", new SimpleValue(webAlias));
		if (pageAliasTopic == null) return view("404");
		viewData("siteName", getDefaultSiteTitle());
		viewData("footerText", getDefaultSiteFooter());
		PageModel page = new PageModel(pageAliasTopic);
		if (page.isPublished()) {
			viewData("page", page);
			return view("page");
		}
		return view("401");
	}
	
	private String getDefaultSiteFooter() {
		Topic footer = loadDefaultSiteTopic();
		footer.loadChildTopics("de.mikromedia.site.footer_text");
		return footer.getChildTopics().getString("de.mikromedia.site.footer_text");
	}
	
	private String getDefaultSiteTitle() {
		Topic footer = loadDefaultSiteTopic();
		footer.loadChildTopics("de.mikromedia.site.name");
		return footer.getChildTopics().getString("de.mikromedia.site.name");
	}
	
	private Topic loadDefaultSiteTopic() {
		return dms.getTopic("uri", new SimpleValue("dm4.mikromedia.standard_site"));
	}

}
