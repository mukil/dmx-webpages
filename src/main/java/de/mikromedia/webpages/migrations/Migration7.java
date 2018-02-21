package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.AssociationType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;

/**
 * Assigns all our custom types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration7 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // 1) Assign new association types to "Webpages" workspace too
        AssociationType desktopHeaderImage = dm4.getAssociationType("de.mikromedia.image.large");
        workspacesService.assignToWorkspace(desktopHeaderImage, webpagesWorkspace.getId());
        AssociationType mobileHeaderImage = dm4.getAssociationType("de.mikromedia.image.small");
        workspacesService.assignToWorkspace(mobileHeaderImage, webpagesWorkspace.getId());
        // 2) Assign new topic types to "Webpages" workspace
        TopicType header = dm4.getTopicType("de.mikromedia.header");
        Topic headerViewConfig = header.getRelatedTopic("dm4.core.aggregation", "dm4.core.type", "dm4.core.view_config", "dm4.webclient.view_config");
        workspacesService.assignToWorkspace(headerViewConfig, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(header, webpagesWorkspace.getId());
        TopicType headerTitle = dm4.getTopicType("de.mikromedia.header.title");
        workspacesService.assignToWorkspace(headerTitle, webpagesWorkspace.getId());
        TopicType headerContent = dm4.getTopicType("de.mikromedia.header.html");
        workspacesService.assignToWorkspace(headerContent, webpagesWorkspace.getId());
        TopicType headerColorBg = dm4.getTopicType("de.mikromedia.background.color");
        workspacesService.assignToWorkspace(headerColorBg, webpagesWorkspace.getId());
        TopicType headerColorFont = dm4.getTopicType("de.mikromedia.font.color");
        workspacesService.assignToWorkspace(headerColorFont, webpagesWorkspace.getId());
        TopicType headerScript = dm4.getTopicType("de.mikromedia.header.js");
        workspacesService.assignToWorkspace(headerScript, webpagesWorkspace.getId());
        TopicType button = dm4.getTopicType("de.mikromedia.button");
        Topic buttonViewConfig = button.getRelatedTopic("dm4.core.aggregation", "dm4.core.type", "dm4.core.view_config", "dm4.webclient.view_config");
        workspacesService.assignToWorkspace(buttonViewConfig, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(button, webpagesWorkspace.getId());
        TopicType buttonTitle = dm4.getTopicType("de.mikromedia.button.title");
        workspacesService.assignToWorkspace(buttonTitle, webpagesWorkspace.getId());
        TopicType buttonHref = dm4.getTopicType("de.mikromedia.link");
        workspacesService.assignToWorkspace(buttonHref, webpagesWorkspace.getId());
        TopicType buttonStyle = dm4.getTopicType("de.mikromedia.button.style");
        workspacesService.assignToWorkspace(buttonStyle, webpagesWorkspace.getId());

    }

}
