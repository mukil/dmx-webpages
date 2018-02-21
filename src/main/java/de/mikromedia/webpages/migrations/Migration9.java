package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;

/**
 * Assigns all our custom Section types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration9 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // Webpage Section
        TopicType section = dm4.getTopicType("de.mikromedia.section");
        Topic sectionViewConfig = section.getRelatedTopic("dm4.core.aggregation", "dm4.core.type", "dm4.core.view_config", "dm4.webclient.view_config");
        TopicType sectionTitle = dm4.getTopicType("de.mikromedia.section.title");
        TopicType sectionContent = dm4.getTopicType("de.mikromedia.tile");
        TopicType sectionLayout = dm4.getTopicType("de.mikromedia.section.layout");
        TopicType sectionPlacement = dm4.getTopicType("de.mikromedia.section.placement");
        workspacesService.assignToWorkspace(section, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionTitle, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionContent, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionLayout, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionPlacement, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionViewConfig, webpagesWorkspace.getId());
        // Section Content
        Topic sectionContentViewConfig = sectionContent.getRelatedTopic("dm4.core.aggregation", "dm4.core.type", "dm4.core.view_config", "dm4.webclient.view_config");
        workspacesService.assignToWorkspace(sectionContentViewConfig, webpagesWorkspace.getId());
        TopicType headline = dm4.getTopicType("de.mikromedia.tile.headline");
        TopicType sectionHtml = dm4.getTopicType("de.mikromedia.tile.html");
        workspacesService.assignToWorkspace(sectionHtml, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(headline, webpagesWorkspace.getId());

    }

}
