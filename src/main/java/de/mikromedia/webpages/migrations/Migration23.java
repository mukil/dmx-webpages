package de.mikromedia.webpages.migrations;


import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.CompDef;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.ViewConfig;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration23 extends Migration {

    @Inject WorkspacesService workspaces;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // Migrate default "page" template into all existing "Webpage" topics
        // Activate "Select" Widgets for new Child Topic Types
        TopicType websote = dmx.getTopicType("de.mikromedia.site");
        CompDef footerFragment = websote.getCompDef("de.mikromedia.site.footer_fragment_name");
        ViewConfig cdefAttachment = footerFragment.getViewConfig();
        cdefAttachment.setConfigValueRef("dmx.webclient.view_config", "dmx.webclient.widget", "dmx.webclient.select");
        TopicType webpage = dmx.getTopicType("de.mikromedia.page");
        CompDef pageTemplate = webpage.getCompDef("de.mikromedia.page.template");
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
