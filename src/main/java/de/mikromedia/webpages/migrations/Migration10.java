/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mikromedia.webpages.migrations;

import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 *
 * @author MRG
 */
public class Migration10 extends Migration {

    @Inject WorkspacesService workspaces;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // Defunct: Hook in two custom webclient renderers (?)
        // ### Todo: Migrate default "page" template into all existing "Webpage" topics
        /** ViewConfig styleConfig = pageTemplate.getViewConfig();
        styleConfig.setConfigValueRef("dmx.webclient.view_config", "dmx.webclient.widget", "dmx.webclient.select");
        webpage.removeCompDef("de.mikromedia.page.author_name");
        // Removing "Author Name" from "Webpage" topic
        TopicType authorName = dmx.getTopicType("de.mikromedia.page.author_name");
        authorName.delete();
        // Remove Un-Maintained Section Layouts
        Topic mapWidget = dmx.getTopicByUri("de.mikromedia.layout.map_widget");
        mapWidget.delete();
        Topic embed = dmx.getTopicByUri("de.mikromedia.layout.embed");
        embed.delete(); **/
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
