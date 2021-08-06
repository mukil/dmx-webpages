package de.mikromedia.webpages.migrations;


import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import static systems.dmx.core.Constants.ONE;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.ViewConfig;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration11 extends Migration {

    @Inject WorkspacesService workspaces;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        /** Upcoming after New in 0.9 release **/
        // Defunct: Hook in two custom webclient renderers (?)
        // Fixme: ### If webclient is capable of providing identityAttributes register site.prefix as identityAttr=True
        // Assign new default pages
        Topic webpagesWs = workspaces.getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        Topic bookmarksPage = dmx.getTopicByUri("de.mikromedia.bookmarks.page");
        Topic contactsPage = dmx.getTopicByUri("de.mikromedia.contacts.page");
        Topic notesPage = dmx.getTopicByUri("de.mikromedia.notes.page");
        Topic promutCss = dmx.getTopicByUri("de.mikromedia.iass_promut_style");
        workspaces.assignToWorkspace(bookmarksPage, webpagesWs.getId());
        workspaces.assignToWorkspace(contactsPage, webpagesWs.getId());
        workspaces.assignToWorkspace(notesPage, webpagesWs.getId());
        workspaces.assignToWorkspace(promutCss, webpagesWs.getId());
        // Add "Page Template" value to "Webpage"
        TopicType webpage = dmx.getTopicType("de.mikromedia.page");
        TopicType pageTemplate = dmx.getTopicType("de.mikromedia.page.template");
        webpage.addCompDef(mf.newCompDefModel(webpage.getUri(), pageTemplate.getUri(), ONE));
        workspaces.assignTypeToWorkspace(webpage, webpagesWs.getId());
        // ### Todo: Migrate default "page" template into all existing "Webpage" topics
        ViewConfig styleConfig = pageTemplate.getViewConfig();
        styleConfig.setConfigValueRef("dmx.webclient.view_config", "dmx.webclient.widget", "dmx.webclient.select");
        webpage.removeCompDef("de.mikromedia.page.author_name");
        // Removing "Author Name" from "Webpage" topic
        TopicType authorName = dmx.getTopicType("de.mikromedia.page.author_name");
        authorName.delete();
        // Remove Un-Maintained Section Layouts
        Topic mapWidget = dmx.getTopicByUri("de.mikromedia.layout.map_widget");
        mapWidget.delete();
        Topic embed = dmx.getTopicByUri("de.mikromedia.layout.embed");
        embed.delete();
        // Authors/Contributors
        // User Profile
        // Rename Topics
        // de.mikromedia.footer.footer-new
        // de.mikromedia.footer.qpq-footer
        // de.mikromedia.footer.dmx-footer
        // de.mikromedia.footer.footer-links
        // de.mikromedia.layout.native_embed
        // de.mikromedia.layout.single_tile
        // de.mikromedia.layout.quote_section
        // de.mikromedia.layout.two_tiles
        // de.mikromedia.layout.tiles_ten_six
        // de.mikromedia.layout.n_tiles
        // de.mikromedia.layout.2_and_n_tiles
        // de.mikromedia.standard_site

    }

}
