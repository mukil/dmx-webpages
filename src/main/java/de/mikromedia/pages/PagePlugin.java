package de.mikromedia.pages;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
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

	@Override
	public void init() {
		initTemplateEngine();
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
		return dms.getTopic("uri", new SimpleValue("de.mikromedia.default_site"));
	}

}
