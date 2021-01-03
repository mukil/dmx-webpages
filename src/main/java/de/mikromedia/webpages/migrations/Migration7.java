package de.mikromedia.webpages.migrations;

import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Assigns all our custom Section types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration7 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // Webpage Section
        TopicType section = dmx.getTopicType("de.mikromedia.section");
        // ### Topic sectionViewConfig = section.getRelatedTopic(null, "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        TopicType sectionTitle = dmx.getTopicType("de.mikromedia.section.title");
        TopicType sectionContent = dmx.getTopicType("de.mikromedia.tile");
        TopicType sectionLayout = dmx.getTopicType("de.mikromedia.section.layout");
        TopicType sectionPlacement = dmx.getTopicType("de.mikromedia.section.placement");
        Topic videoEmbedLayout = dmx.getTopicByUri("de.mikromedia.layout.embed");
        workspacesService.assignToWorkspace(videoEmbedLayout, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(section, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(sectionTitle, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(sectionContent, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(sectionLayout, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(sectionPlacement, webpagesWorkspace.getId());
        // workspacesService.assignToWorkspace(sectionViewConfig, webpagesWorkspace.getId());
        // Section Content
        // ### Topic sectionContentViewConfig = sectionContent.getRelatedTopic("dmx.core.composition", "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        // workspacesService.assignToWorkspace(sectionContentViewConfig, webpagesWorkspace.getId());
        TopicType headline = dmx.getTopicType("de.mikromedia.tile.headline");
        TopicType sectionHtml = dmx.getTopicType("de.mikromedia.tile.html");
        workspacesService.assignTypeToWorkspace(sectionHtml, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(headline, webpagesWorkspace.getId());

    }

}
