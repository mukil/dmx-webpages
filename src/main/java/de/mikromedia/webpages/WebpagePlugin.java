package de.mikromedia.webpages;

import de.mikromedia.webpages.events.WebpageRequestedListener;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.container.ContainerResponse;

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
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONException;
import org.osgi.framework.Bundle;
import de.mikromedia.webpages.events.CustomRootResourceRequestedListener;
import de.mikromedia.webpages.events.ResourceNotFoundListener;
import de.mikromedia.webpages.model.Header;
import de.mikromedia.webpages.model.Section;
import systems.dmx.core.service.EventListener;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.thymeleaf.context.AbstractContext;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Assoc;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.model.AssocModel;
import systems.dmx.core.model.PlayerModel;
import systems.dmx.core.model.SimpleValue;
import systems.dmx.core.service.DMXEvent;
import systems.dmx.core.service.Inject;
import static systems.dmx.core.Constants.*;
import systems.dmx.core.Topic;
import systems.dmx.core.ViewConfig;
import systems.dmx.core.model.TopicModel;
import systems.dmx.core.service.accesscontrol.AccessControlException;
import systems.dmx.core.service.event.PostCreateTopic;
import systems.dmx.core.service.event.PreCreateAssoc;
import systems.dmx.core.service.event.ServiceResponseFilter;
import systems.dmx.core.storage.spi.DMXTransaction;
import systems.dmx.core.util.DMXUtils;
import static systems.dmx.core.util.JavaUtils.stripHTML;
import systems.dmx.thymeleaf.ThymeleafPlugin;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Collaborative, multi-site standard HTML web pages with DMX 5.0.
 *
 * Once the webpages module is installed web developers hook into this thymeleaf
 * processing by using context() in event listeners fired for one of
 * the following contexts:
 * 
 * - /                          root
 * - /{pluginName}              plugin
 * - /{sitePrefix}              site
 * - /{sitePrefix}/{webAlias}   page
 * - 404                        page not found
 * 
 * Additionally your plugin can override dmx-webpages thymeleaf template fragments with OGNL expression language.
 * To override the templates place them in your plugins src/main/resources/ folder:
 * 
 * - "/views/fragments/navigation.html"
 * - "/views/fragments/footer.html"
 * - "/views/fragments/widgets.html"
 * - "/views/fragments/tracker.html"
 * 
 * The REST API  for search is located under "/webpages".
 * 
 * @author Malte Rei&szlig;ig <a href="mailto:malte@mikromedia.de">malte@mikromedia.de></a>
 * @version 0.8.0-SNAPSHOT - Source code compatible with DMX 5.0-beta-7
 * 
 * Last modified: 21.04.2020
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebpagePlugin extends ThymeleafPlugin implements ServiceResponseFilter,
                                                              WebpageService,
                                                              PreCreateAssoc,
                                                              PostCreateTopic {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject AccessControlService accesscontrol;
    @Inject WorkspacesService workspaces;
    // @Inject SendgridService sendgrid;
    @Context UriInfo uriInfo;

    private final String DM4_HOST_URL = System.getProperty("dmx.host.url");
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    private static final String FRONTPAGE_TEMPLATE_NAME = "frontpage";
    private static final String SIMPLE_PAGE_TEMPLATE_NAME = "page";

    /** A name of a Viewable registered by another plugin to be served at "/". **/
    String frontPageTemplateName = null;

    /** A map with "name" of Viewables as values registered by other plugins to be served at "/webalias". **/
    HashMap<String, String[]> frontpageTemplateAliases = new HashMap<String, String[]>();

    /**
     * Custom event fired up on HTTP request to a website's frontpage or custom root resource
     */
    static DMXEvent CUSTOM_ROOT_RESOURCE_REQUESTED = new DMXEvent(CustomRootResourceRequestedListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((CustomRootResourceRequestedListener) listener).frontpageRequested((AbstractContext) params[0],
                    (Topic) params[1], (String) params[2], (UriInfo) params[3]);
        }
    };

    /**
     * Custom event fired up on HTTP request to a valid webAlias of a website.
     */
    static DMXEvent WEBPAGE_REQUESTED = new DMXEvent(WebpageRequestedListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((WebpageRequestedListener) listener).webpageRequested((AbstractContext) params[0], (String) params[1], (String) params[2]);
        }
    };

    /**
     * Custom event fired up on HTTP request with no associated webalias.
     */
    static DMXEvent PAGE_NOT_FOUND = new DMXEvent(ResourceNotFoundListener.class) {
        @Override
        public void dispatch(EventListener listener, Object... params) {
            ((ResourceNotFoundListener) listener).resourceNotFound((AbstractContext) params[0], (String) params[1], (String) params[2]);
        }
    };

    @Override
    public void init() {
        log.info("Initializing DMX Webpages Thymeleaf Engine");
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
            prepareGenericViewData(frontPageTemplateName, STANDARD_WEBSITE_PREFIX, location);
            // expose published "webpages" and "menu items" of the standard website to third party frontpages
            website = getStandardWebsite();
            dmx.fireEvent(CUSTOM_ROOT_RESOURCE_REQUESTED, context(), website, location, uriInfo);
            log.info("Preparing 3rd PARTY FRONTPAGE view data in dmx-webpages plugin..., queryParameters: " + uriInfo.getQueryParameters());
            prepareWebsiteViewData(website, location);
            preparePageSections(website);
            return view(frontPageTemplateName);
        } else { // 2) check if there is a redirect or page realted to the standard site and set to "/"
            website = getWebsiteFrontpage(null);
            // private workspace via our getRelatedTopics()-call
            dmx.fireEvent(CUSTOM_ROOT_RESOURCE_REQUESTED, context(), website, location, uriInfo);
            log.info("Preparing STANDARD FRONTPAGE (website="+ website.getSimpleValue() + ") in dmx-webpages plugin...");
            prepareWebsiteViewData(website, location);
            preparePageSections(website);
            // check if their is a redirect setup for this web alias
            handleWebsiteRedirects(website, "/"); // potentially throws WebAppException triggering a Redirect
            return getWebsiteTemplate(website, location);
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
        Topic website;
        String pageAlias = webAlias.trim();
        if (pageAlias.equals("favicon.ico")) return null;
        // 1) check if for the given "/webAlias" a template was registered by other plugins
        Viewable registeredPage = getCustomRootResourcePage(pageAlias);
        if (registeredPage != null) {
            log.info("Preparing CUSTOM ROOT RESOURCE Page in dmx-webpages plugin...");
            website = getStandardWebsite();
            dmx.fireEvent(CUSTOM_ROOT_RESOURCE_REQUESTED, context(), website, pageAlias, uriInfo);
            prepareGenericViewData("undefined", STANDARD_WEBSITE_PREFIX, pageAlias);
            prepareWebsiteViewData(website, pageAlias);
            return registeredPage;
        }
        log.info("Requesting Webpage /" + pageAlias);
        // 2) check if webAlias matches to a special website prefix
        website = getWebsiteByPrefix(pageAlias);
        if (website != null) {
            log.info("Preparing USER FRONTPAGE view data in dmx-webpages plugin...");
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, pageAlias, null);
            prepareWebsiteViewData(website, pageAlias);
            preparePageSections(website);
            dmx.fireEvent(CUSTOM_ROOT_RESOURCE_REQUESTED, context(), website, pageAlias, uriInfo);
            return getWebsiteTemplate(website, pageAlias);
        }
         // 3) if no website frontpage exist for that prefix, we continue with our standard website for page preparation
        website = getStandardWebsite();
        dmx.fireEvent(WEBPAGE_REQUESTED, context(), pageAlias, STANDARD_WEBSITE_PREFIX);
        log.info("Preparing STANDARD FRONTPAGE view data in dmx-webpages plugin...");
        prepareWebsiteViewData(website, webAlias);
        // 4) check for existing pageAlias and fetch and return that webpage
        Webpage webpage = getWebsitesWebpage(website, pageAlias);
        if (webpage != null) {
            log.info("Preparing WEBPAGE view data \""+webpage.getTitle().toString()+"\" ...");
            prepareGenericViewData(SIMPLE_PAGE_TEMPLATE_NAME, STANDARD_WEBSITE_PREFIX, pageAlias);
            preparePageViewData(webpage);
            return getWebpageTemplate(webpage);
        }
        log.fine("=> /" + pageAlias + " webpage for standard website not found.");
        // 5) Check for redirects related to "standard" webpage
        handleWebsiteRedirects(website, pageAlias);
        log.fine("=> /" + pageAlias + " redirect for standard website not found.");
        // 6) Requested resource is neither a "Website", nor a "Custom Root Resource", nor
        //    a published or drafted \"Webpage\" nor a \"Redirect\"
        dmx.fireEvent(PAGE_NOT_FOUND, context(), pageAlias, STANDARD_WEBSITE_PREFIX);
        return getWebsiteNotFoundPage(website, pageAlias);
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
        // 1) Query for website topic
        Topic usersWebsite = getWebsiteByPrefix(sitePrefix);
        if (usersWebsite != null) {
            prepareGenericViewData(SIMPLE_PAGE_TEMPLATE_NAME, sitePrefix, pageAlias);
            prepareWebsiteViewData(usersWebsite, location);
        }
        // 2) check related webpages
        Webpage webpage = getWebsitesWebpage(usersWebsite, pageAlias);
        if (webpage != null) {
            dmx.fireEvent(WEBPAGE_REQUESTED, context(), pageAlias, sitePrefix);
            log.info("Preparing WEBPAGE view data \""+webpage.getTitle().toString()+"\" ...");
            preparePageViewData(webpage);
            return getWebpageTemplate(webpage);
        }
        log.info("=> /" + pageAlias + " webpage for \"" +sitePrefix+ "\"s website not found.");
        // 3) check if it is a users redirect
        handleWebsiteRedirects(usersWebsite, pageAlias);
        // 4) Log that web alias is neither a published nor an un-published \"Page\" and not a \"Redirect\"
        log.info("=> /" + pageAlias + " webpage redirect for \"" +sitePrefix+ "\"s website not found.");
        // 5) Return 404 page with users website footer
        dmx.fireEvent(PAGE_NOT_FOUND, context(), pageAlias, sitePrefix);
        return getWebsiteNotFoundPage(usersWebsite, pageAlias);
    }

    /** --------------------------------------------------------------------------------- REST API Resources ----- **/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/webpages/{username}")
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
    @Path("/webpages/search")
    public SearchResultList searchWebsites(@QueryParam("q") String query) throws JSONException {
        SearchResultList response = new SearchResultList();
        List<Topic> pages = searchWebpageContents(query);
        List<Topic> sites = searchWebsiteFields(query);
        for (Topic page : pages) {
            try {
                response.putPageResult(new SearchResult(page));
            } catch (Exception aex) {
                log.warning("Error constructing a search result from query, matching page topic ("
                        + page.getSimpleValue().toString()
                        + "), caused by: " + aex.getCause().getMessage());
            }
        }
        for (Topic site : sites) {
            try {
                response.putWebsiteResult(new SearchResult(site));
            } catch (AccessControlException aex) {
                // log.info("User has no read permission on search result");
            }
        }
        log.info("Query \""+query+"\" matched " + pages.size() + " pages, " + sites.size() + " sites");
        return response;
    }

    @GET
    @Path("/webpages/contact-form-submission")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Viewable processContactFormSubmission(@QueryParam("name") String name, @QueryParam("message") String message,
            @QueryParam("contact") String contact, @QueryParam("website") String website, @QueryParam("webalias") String webalias) throws URISyntaxException {
        log.info("Sender: " + name + " Message: " + message);
        log.info("Request From: /" + website + "/" + webalias);
        // Send Mail
        String emailBody = "Nachricht von " + stripHTML(name) + "<br/><br/>"
                + stripHTML(message) + "<br/><br/>"
                + "Kontakt: " + stripHTML(contact).trim() + "<br/><br/>"
                + "Sincerely<br/>Your QPQ-Website";
        // ### sendgrid.doEmailSystemMailbox("Kontaktformular QPQ-Website", emailBody);
        // Page Redirect
        viewData("contactFormUsed", true);
        if (website == null) {
            return getIndexWebpage();
        } else if (website.equals(STANDARD_WEBSITE_PREFIX) && webalias == null) {
            return getIndexWebpage();
        } else if (website.equals(STANDARD_WEBSITE_PREFIX) && webalias != null) {
            return getWebpage(webalias);
        } else if (!website.equals(STANDARD_WEBSITE_PREFIX) && webalias == null) {
            return getWebpage(website);
        } else if (!website.equals(STANDARD_WEBSITE_PREFIX) && webalias != null) {
            return getWebsitePage(website, webalias);
        } else {
            return getIndexWebpage();
        }
    }

    private List<Topic> searchWebpageContents(String query) {
        List<Topic> results = new ArrayList<Topic>();
        String luceneQuery = preparePhraseOrTermLuceneQuery(query);
        log.info("> webpagesSearch: \"" + luceneQuery + "\"");
        for (Topic headline : dmx.queryTopics(WEBPAGE_TITLE, luceneQuery)) {
            Topic webpage = getRelatedWebpageTopic(headline);
            if (webpage != null && !results.contains(webpage) &&
                !webpage.getChildTopics().getBoolean("de.mikromedia.page.is_draft", false)) {
                results.add(webpage);
            }
        }
        for (Topic content : dmx.queryTopics(WEBPAGE_CONTENT, luceneQuery)) {
            Topic webpage = getRelatedWebpageTopic(content);
            if (webpage != null && !results.contains(webpage) &&
                !webpage.getChildTopics().getBoolean("de.mikromedia.page.is_draft", false)) {
                results.add(webpage);
            }
        }
        for (Topic content : dmx.queryTopics(SECTION_TITLE, luceneQuery)) {
            List<RelatedTopic> sections = getParentSections(content);
            addRelatedWebpagesToResults(sections, results);
        }
        for (Topic content : dmx.queryTopics(TILE_HEADLINE, luceneQuery)) {
            List<RelatedTopic> tiles = getParentTiles(content);
            for (RelatedTopic tile : tiles) {
                List<RelatedTopic> sections = getParentSections(tile);
                addRelatedWebpagesToResults(sections, results);
            }
        }
        for (Topic content : dmx.queryTopics(TILE_HTML, luceneQuery)) {
            List<RelatedTopic> tiles = getParentTiles(content);
            for (RelatedTopic tile : tiles) {
                List<RelatedTopic> sections = getParentSections(tile);
                addRelatedWebpagesToResults(sections, results);
            }
        }
        return results;
    }

    /** Copy from dmx-kiezatlas-website module **/
    private String preparePhraseOrTermLuceneQuery(String userQuery) {
        StringBuilder queryPhrase = new StringBuilder();
        if (userQuery.contains(" ")) {
            queryPhrase.append("\"" + userQuery + "\"");
            queryPhrase.append(" OR ");
            queryPhrase.append("" + userQuery.replaceAll(" ", "?") + "*");
            queryPhrase.append(" OR ");
            String[] words = userQuery.split(" ");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                queryPhrase.append("*" + word + "*");
                if (i < words.length -1) {
                    queryPhrase.append(" AND ");
                } else {
                    queryPhrase.append(" ");
                }
            }
        } else {
            queryPhrase.append("*" + userQuery + "*");
            queryPhrase.append(" OR ");
            queryPhrase.append(userQuery + "~0.5"); // 0.5 default fuzzy value
        }
        return queryPhrase.toString();
    }

    private void addRelatedWebpagesToResults(List<RelatedTopic> sections, List<Topic> results) {
        if (sections != null) {
            for (Topic section : sections) {
                Topic webpage = getRelatedWebpageTopic(section);
                if (webpage != null && !results.contains(webpage) && !webpage.getChildTopics().getBoolean("de.mikromedia.page.is_draft", false)) {
                    results.add(webpage);
                }
            }
        }
    }

    private List<Topic> searchWebsiteFields(String query) {
        List<Topic> results = new ArrayList<Topic>();
        for (Topic siteName : dmx.queryTopics(WEBSITE_NAME, "*" + query.trim() + "*")) {
            Topic website = getRelatedWebsiteTopic(siteName);
            if (!results.contains(website)) results.add(website);
        }
        for (Topic siteCaption : dmx.queryTopics(WEBSITE_CAPTION, "*" + query.trim() + "*")) {
            Topic website = getRelatedWebsiteTopic(siteCaption);
            if (!results.contains(website)) results.add(website);
        }
        for (Topic siteFooter : dmx.queryTopics(WEBSITE_FOOTER, "*" + query.trim() + "*")) {
            Topic website = getRelatedWebsiteTopic(siteFooter);
            if (!results.contains(website)) results.add(website);
        }
        return results;
    }

    private List<RelatedTopic> getParentTiles(Topic child) {
        return child.getRelatedTopics(COMPOSITION, null, null, TILE);
    }

    private List<RelatedTopic> getParentSections(Topic child) {
        if (child == null) return null;
        return child.getRelatedTopics(COMPOSITION, null, null, SECTION);
    }

    private Topic getRelatedWebpageTopic(Topic child) {
        if (child == null) return null;
        return child.getRelatedTopic(null, null, null, WEBPAGE);
    }

    private Topic getRelatedWebsiteTopic(Topic child) {
        return child.getRelatedTopic(null, null, null, WEBSITE);
    }

    public Topic getRelatedHeaderTopic(Topic topic) {
        Topic header = topic.getRelatedTopic(ASSOCIATION, DEFAULT, DEFAULT, HEADER);
        if (header != null) {
            header.loadChildTopics();
        }
        return header;
    }

    public List<RelatedTopic> getRelatedWebpageSections(Topic page) {
        List<RelatedTopic> sections = null;
        if (page != null ) {
            sections = page.getRelatedTopics(ASSOCIATION, DEFAULT, DEFAULT, SECTION);
            if (sections != null && sections.size() > 0) {
                DMXUtils.loadChildTopics(sections);
            }
        } else {
            log.warning("Webpage Sections are NULL");
        }
        return sections;
    }

    public Topic getLargeImageRelated(Topic header) {
        Topic desktopImage = header.getRelatedTopic(IMAGE_LARGE, DEFAULT, DEFAULT, DMX_FILE);
        if (desktopImage != null) {
            desktopImage.loadChildTopics();
        }
        return desktopImage;
    }

    public Topic getSmallImageRelated(Topic header) {
        Topic mobileImage = header.getRelatedTopic(IMAGE_SMALL, DEFAULT, DEFAULT, DMX_FILE);
        if (mobileImage != null) {
            mobileImage.loadChildTopics();
        }
        return mobileImage;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/webpages")
    public Topic getWebsite() {
        Topic website = null;
        String username = accesscontrol.getUsername();
        if (username != null) {
            website = getOrCreateWebsiteTopic(username);
        }
        return website;
    }

    @GET
    @Path("/webpages/browse/{websiteId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String doRedirectToWebsite(@PathParam("websiteId") long websiteId)
            throws WebApplicationException, URISyntaxException {
        Topic topic = dmx.getTopic(websiteId);
        Website site = new Website(topic, dmx);
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
     * @param sitePrefix
     * @return All webpage topics associated with the website for the given username not marked as \"Drafts\".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sitePrefix}")
    @Override
    public List<Webpage> getPublishedWebpages(@PathParam("sitePrefix") String sitePrefix) {
        Topic website = getOrCreateWebsiteTopic(sitePrefix);
        if (website != null) {
            Website site = new Website(website, dmx);
            if (site.isWebsiteTopic()) {
                log.info("Loading JSON data on website \"" + sitePrefix + "\" and its published webpages");
                return site.getRelatedWebpagesPublished();
            }
        } else {
            log.warning("No website available with prefix \"/" + sitePrefix + "\" - Returning empty list");
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
        Website site = new Website(websiteTopic, dmx);
        if (site.isWebsiteTopic()) {
            log.info("Loading website related and published webpages for \"" + site.getSitePrefix() + "\"");
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
        Website site = new Website(website, dmx);
        return site.getActiveMenuItems();
    }

    /**
     * Returns all topics of type <code>de.mikromedia.menu.item</code> related to the given `Website` topic.
     * @param sitePrefix
     * @return All topics of type menu item associated with the given website.
     */
    @Override
    public List<MenuItem> getWebsiteMenuItems(String sitePrefix) {
        Topic website = getWebsiteByPrefix(sitePrefix);
        if (website != null) {
            Website site = new Website(website, dmx);
            return site.getActiveMenuItems();
        }
        return null;
    }

    /**
     * Fetches the global standard website.
     * @return A topic representing the global standard website which is unrelated to any username.
     * Its pages are accessible under the root resource.
     */
    @Override
    public Topic getStandardWebsite() {
        return dmx.getTopicByUri(STANDARD_WEBSITE_URI);
    }

    public Topic getWebsiteByPrefix(String value) {
        Topic prefix = dmx.getTopicByValue(WEBSITE_PREFIX, new SimpleValue(value));
        Topic website = null;
        if (prefix != null) {
            website = prefix.getRelatedTopic(COMPOSITION, CHILD, PARENT, WEBSITE);
        }
        return website;
    }

    public String getTypeIconPath(String typeUri) {
        TopicType webpage = dmx.getTopicType(typeUri);
        ViewConfig viewConfig = webpage.getViewConfig();
        Topic icon = viewConfig.getConfigTopic("dmx.webclient.icon");
        return (icon != null) ? icon.getSimpleValue().toString() : "";
    }

    // --- Private Utility Methods

    /**
     * Performs a HTTP redirect if a <code>de.mikromedia.redirect</code> is associated with the given `Website` topic.
     * @param site
     * @param webAlias
     */
    private void handleWebsiteRedirects(Topic site, String webAlias) {
        if (site != null) {
            Website website = new Website(site, dmx);
            List<RelatedTopic> redirects = website.getConfiguredRedirects();
            Iterator<RelatedTopic> iterator = redirects.iterator();
            while (iterator.hasNext()) {
                Topic redirectTopic = dmx.getTopic(iterator.next().getModel().getId()).loadChildTopics();
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
        return username.getRelatedTopic(ASSOCIATION, DEFAULT, DEFAULT, WEBSITE);
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
                + dmx.getPrivilegedAccess().getAssignedWorkspaceId(website.getId()) + "\"");
        }
        return website;
    }

    private Topic createWebsiteTopic(final Topic username) {
        DMXTransaction tx = dmx.beginTx();
        Topic website = null;
        try {
            Topic usersWorkspace = dmx.getPrivilegedAccess().getPrivateWorkspace(username.getSimpleValue().toString());
            Topic websiteTopic = dmx.getPrivilegedAccess().runInWorkspaceContext(usersWorkspace.getId(), () -> {
                Topic topic = dmx.createTopic(mf.newTopicModel(WEBSITE, mf.newChildTopicsModel()
                        .set(WEBSITE_NAME, username + "'s Webpages")
                        .setRef(WEBSITE_STYLESHEET, STANDARD_STYLESHEET_URI)
                        .set(WEBSITE_PREFIX, username.getSimpleValue().toString())
                        .set(WEBSITE_FOOTER, "<p class=\"attribution\">Published with "
                                + "<a href=\"https://github.com/mukil/dmx-webpages\" title=\"Source Coude: dmx-webpages\">"
                                + "webpages</a>, an application of the <a href=\"https://dmx.systems\""
                                + " title=\"Visit DMX Systems Webpage\">dmx-platform</a>.</p>")
                ));
                createWebsiteUsernameAssoc(username, topic);
                return topic;
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

    private Assoc createWebsiteUsernameAssoc(Topic usernameTopic, Topic website) {
        return dmx.createAssoc(mf.newAssocModel(ASSOCIATION,
                mf.newTopicPlayerModel(usernameTopic.getId(), DEFAULT),
                mf.newTopicPlayerModel(website.getId(), DEFAULT)));
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
     *
     * @return A webpage if it is related to that website, <code>null</code> otherwise.
     */
    private Webpage getWebsitesWebpage(Topic website, String webAlias) {
        Website site = new Website(website, dmx);
        if (site.isWebsiteTopic()) {
            Topic webpageAliasTopic = site.getWebpageByAlias(webAlias);
            if (webpageAliasTopic != null) {
                return new Webpage(webpageAliasTopic);
            }
        }
        return null;
    }

    private Viewable getWebsiteTemplate(Topic website, String location) {
        // website is null if the request does not have the necessary permissions to access it
        if (website != null) {
            return view(FRONTPAGE_TEMPLATE_NAME);
        } else {
            return getWebsiteNotFoundPage(website, location); // which loads standard topic if website is null
        }
    }

    private Topic getWebsiteFrontpage(String username) {
        Topic website = null;
        if (username == null) {
            website = getStandardWebsite();
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, STANDARD_WEBSITE_PREFIX, null);
        } else {
            website = getOrCreateWebsiteTopic(username);
            prepareGenericViewData(FRONTPAGE_TEMPLATE_NAME, username, null);
        }
        return website;
    }

    private Viewable getWebsiteNotFoundPage(Topic standardWebsite, String location) {
        // 1) If no page was returned by now we use the value placed in "Footer" of the standard / admins website in our 404 page
        // ### Fixme: Can we respond with HTTP Status 404 and a template, too? Serving robots and humans?
        Topic website = standardWebsite;
        if (website == null) {
            website = getStandardWebsite();
        }
        prepareGenericViewData("404", STANDARD_WEBSITE_PREFIX, location);
        prepareWebsiteViewData(website, "404");
        return view("404");
    }

    private Viewable getCustomRootResourcePage(String pageAlias) {
        if (isFrontpageAliasRegistered(pageAlias)) {
            String[] templateValue = frontpageTemplateAliases.get(pageAlias);
            String templateName = templateValue[0];
            log.info("Loading template \"views/" + templateName + ".html\" for \"" + pageAlias + "\"");
            prepareGenericViewData(templateName, pageAlias, null);
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
                return view(SIMPLE_PAGE_TEMPLATE_NAME);
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
    private void prepareGenericViewData(String filename, String websitePrefix, String webAlias) {
        String username = accesscontrol.getUsername();
        String footerName = getFooterFragmentName(websitePrefix);
        viewData("authenticated", (username != null));
        viewData("is_publisher", hasWritePermissionOnWebsite(websitePrefix, username));
        viewData("username", username);
        viewData("website", websitePrefix);
        viewData("webalias", webAlias);
        viewData("template", filename);
        viewData("footer_key", footerName);
        viewData("hostUrl", DM4_HOST_URL);
    }

    private String getFooterFragmentName(String websitePrefix) {
        Topic website = getWebsiteByPrefix(websitePrefix);
        Topic footerFragment = website.getChildTopics().getTopicOrNull("de.mikromedia.site.footer_fragment_name");
        return (footerFragment != null) ? footerFragment.getUri().substring(21) : "footer-new"; // stripping "de.mikromedia.footer." from topic URI as name
    }

    private boolean hasWritePermissionOnWebsite(String websitePrefix, String username) {
        Topic website = getWebsiteByPrefix(websitePrefix);
        Topic websiteWorkspace = workspaces.getAssignedWorkspace(website.getId());
        accesscontrol.isMember(username, websiteWorkspace.getId());
        String wsSharingMode = websiteWorkspace.getChildTopics().getString("dmx.workspaces.sharing_mode");
        // Username has WRITE access to that website topic through its membership
        if (wsSharingMode.equals("Public") || wsSharingMode.equals("Collaborative") || wsSharingMode.equals("Common")) {
            return true;
        }
        // Is user site
        if (website != null) {
            Topic userSite = getWebsiteByUsername(username);
            if (userSite.getId() == website.getId()) {
                return true;
            }
        }
        return false;
    }

    private void preparePageSections(Topic topic) {
        List<RelatedTopic> sections = getRelatedWebpageSections(topic);
        List<Section> above = new ArrayList();
        List<Section> below = new ArrayList();
        // ### TODO: Aside Left and Right
        if (sections != null && sections.size() > 0) {
            for (RelatedTopic section : sections) {
                Section pageSection = new Section(section);
                Topic placement = pageSection.getPlacement();
                if (placement != null && placement.getUri().equals(PLACEMENT_ABOVE)) {
                    above.add(pageSection);
                } else if (placement != null && placement.getUri().equals(PLACEMENT_BELOW)) {
                    below.add(pageSection);
                }
            }
            // 1.) set custom Header data
            if (above.size() > 0) {
                viewData("sectionsAbove", getSectionsSorted(above));
            }
            if (below.size() > 0) {
                viewData("sectionsBelow", getSectionsSorted(below));
            }
        }
    }

    /**
     * Prepares the most basic data used across all our Thymeleaf page templates.
     * @param website
     */
    private void prepareWebsiteViewData(Topic website, String pageAlias) {
        if (website != null) {
            Website site = new Website(website, dmx);
            viewData("siteName", site.getName());
            viewData("siteCaption", site.getCaption());
            viewData("siteAbout", site.getAboutHTML());
            viewData("siteId", website.getId());
            viewData("siteLogoPath", site.getLogoPath());
            viewData("footerText", site.getFooter());
            viewData("customSiteCss", site.getStylesheetPath());
            viewData("menuItems", site.getActiveMenuItems());
            // ### Think of revising this "LD String" to become a sensible chain of statements
            String linkedData = site.getInstitutionLD();
            viewData("institution", linkedData);
            viewData("location", pageAlias);
            List<Webpage> webpages = getPublishedWebpages(website);
            // sort webpages on websites frontpage by modification time
            viewData("webpages", getWebpagesSortedByTimestamp(webpages, false)); // false=creationDate */
            // Site related "Headers" are used for providing a consistent look of webpages related to one "Website" 
            // (fallback if no page Specific "Headers" are configured)
            Topic headerTopic = getRelatedHeaderTopic(website);
            if (headerTopic != null) {
                Header header = new Header(headerTopic);
                viewData("_header", header);
            }
        } else {
            log.warning("Preparing webpage template failed because a given website could not be found");
        }
    }

    private void preparePageViewData(Webpage webpage) {
        viewData("customPageCss", webpage.getStylesheet());
        List<RelatedTopic> scripts = webpage.getJavascripts();
        if (scripts != null) {
            for (RelatedTopic script : scripts) {
                viewData("customPageScript", script.getSimpleValue().toString());
            }
        }
        viewData("dateCreated", df.format(webpage.getCreationDate()));
        viewData("dateModified", df.format(webpage.getModificationDate()));
        viewData("page", webpage);
        Topic headerTopic = getRelatedHeaderTopic(webpage.getTopic());
        if (headerTopic != null) {
            Header header = new Header(headerTopic);
            viewData("header", header);
        }
        preparePageSections(webpage.getTopic());
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

    private List<Section> getSectionsSorted(List<Section> all) {
        Collections.sort(all, new Comparator<Section>() {
            public int compare(Section s1, Section s2) {
                try {
                    if ( s1.getOrdinalNumber() > s2.getOrdinalNumber() ) return 1;
                    if ( s1.getOrdinalNumber() < s2.getOrdinalNumber() ) return -1;
                } catch (Exception nfe) {
                    log.warning("Error while accessing ordinal number Section: " + s1.getId() + " Section: "
                            + s2.getId() + " nfe: " + nfe.getMessage());
                    return 0;
                }
                return 0;
            }
        });
        return all;
    }

    /** Upcoming in New in 0.9 release **/
    private String getTemplateFileName(Topic templateName) {
        if (templateName == null) return SIMPLE_PAGE_TEMPLATE_NAME;
        return templateName.getUri().substring("de.mikromedia.template_".length());
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
    public void postCreateTopic(Topic topic) {
        if (topic.getTypeUri().equals(WEBPAGE)) {
            String webpageTitle = topic.getSimpleValue().toString();
            if (!webpageTitle.isEmpty()) {
                String webAlias = transformStringToURL(webpageTitle);
                log.fine("Relative Webpage Path => /" + webAlias);
                TopicModel webage = topic.getModel();
                webage.getChildTopics().set(WEBPAGE_ALIAS, webAlias);
                dmx.updateTopic(webage);
            }
        }
    }

    @Override
    public void preCreateAssoc(AssocModel am) {
        if (am.getTypeUri().equals(ASSOCIATION)) {
            PlayerModel player1 = am.getPlayer1();
            PlayerModel player2 = am.getPlayer2();
            Topic topic1 = dmx.getTopic(player1.getId());
            Topic topic2 = dmx.getTopic(player2.getId());
            // Between Header and File we auto-type to "de.mikromedia.header.desktop_image"
            if (topic1.getTypeUri().equals(HEADER) || topic2.getTypeUri().equals(HEADER)) {
                if (topic1.getTypeUri().equals(DMX_FILE) || topic2.getTypeUri().equals(DMX_FILE) ) {
                    DMXUtils.assocAutoTyping(am, HEADER,
                        DMX_FILE, IMAGE_LARGE, DEFAULT, DEFAULT);
                }
            } else if (topic1.getTypeUri().equals(TILE) || topic2.getTypeUri().equals(TILE)) {
                if (topic1.getTypeUri().equals(DMX_FILE) || topic2.getTypeUri().equals(DMX_FILE) ) {
                    DMXUtils.assocAutoTyping(am, TILE,
                        DMX_FILE, IMAGE_LARGE, DEFAULT, DEFAULT);
                }
            } else if (topic1.getTypeUri().equals(SECTION) || topic2.getTypeUri().equals(SECTION)) {
                if (topic1.getTypeUri().equals(DMX_FILE) || topic2.getTypeUri().equals(DMX_FILE) ) {
                    DMXUtils.assocAutoTyping(am, SECTION,
                        DMX_FILE, IMAGE_LARGE, DEFAULT, DEFAULT);
                }
            }
        }
    }

    @Override
    public void serviceResponseFilter(ContainerResponse cr) {
        cr.getHttpHeaders().add("X-XSS-Protection", 1);
    }

    private String transformStringToURL(String value) {
        // removes reserved characters and german special characters from url
        String result = value.replaceAll(" ", "-").replaceAll("\\&", "-")
            .replaceAll(":", "-").replaceAll("=", "-")
            .replaceAll("\\.", "-").replaceAll("\\?", "-")
            .replaceAll("\\(", "-").replaceAll("\\)", "-")
            .replaceAll("\\:", "-").replaceAll("\\+", "-")
            .replaceAll("\\!", "-").replaceAll("\\*", "-")
            .replaceAll("\\'", "-").replaceAll("\\/", "")
            .replaceAll("@", "").replaceAll("\\]", "-")
            .replaceAll("#", "").replaceAll("$", "")
            .replaceAll("\"", "").replaceAll("\\[", "-")
            .replaceAll("\\{", "-").replaceAll("\\}", "-")
            .replaceAll("§", "-").replaceAll("%", "-")
            .replaceAll("ä", "ae").replaceAll("ö", "oe")
            .replaceAll("ü", "ue").replaceAll("ß", "ss");
        result = result.toLowerCase();
        if (result.length() == (result.lastIndexOf("-") + 1)) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
