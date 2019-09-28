package de.mikromedia.webpages.migrations;



import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.AssocType;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

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
        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // 1) Assign new association types to "Webpages" workspace too
        AssocType desktopHeaderImage = dmx.getAssocType("de.mikromedia.image.large");
        workspacesService.assignToWorkspace(desktopHeaderImage, webpagesWorkspace.getId());
        AssocType mobileHeaderImage = dmx.getAssocType("de.mikromedia.image.small");
        workspacesService.assignToWorkspace(mobileHeaderImage, webpagesWorkspace.getId());
        // 2) Assign new topic types to "Webpages" workspace
        TopicType header = dmx.getTopicType("de.mikromedia.header");
        // ### Topic headerViewConfig = header.getRelatedTopic("dmx.core.composition", "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        // workspacesService.assignToWorkspace(headerViewConfig, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(header, webpagesWorkspace.getId());
        TopicType headerTitle = dmx.getTopicType("de.mikromedia.header.title");
        workspacesService.assignToWorkspace(headerTitle, webpagesWorkspace.getId());
        TopicType headerContent = dmx.getTopicType("de.mikromedia.header.html");
        workspacesService.assignToWorkspace(headerContent, webpagesWorkspace.getId());
        TopicType headerColorBg = dmx.getTopicType("de.mikromedia.background.color");
        workspacesService.assignToWorkspace(headerColorBg, webpagesWorkspace.getId());
        TopicType headerColorFont = dmx.getTopicType("de.mikromedia.font.color");
        workspacesService.assignToWorkspace(headerColorFont, webpagesWorkspace.getId());
        TopicType headerScript = dmx.getTopicType("de.mikromedia.header.js");
        workspacesService.assignToWorkspace(headerScript, webpagesWorkspace.getId());
        TopicType button = dmx.getTopicType("de.mikromedia.button");
        // ### Topic buttonViewConfig = button.getRelatedTopic("dmx.core.composition", "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        // workspacesService.assignToWorkspace(buttonViewConfig, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(button, webpagesWorkspace.getId());
        TopicType buttonTitle = dmx.getTopicType("de.mikromedia.button.title");
        workspacesService.assignToWorkspace(buttonTitle, webpagesWorkspace.getId());
        TopicType buttonHref = dmx.getTopicType("de.mikromedia.link");
        workspacesService.assignToWorkspace(buttonHref, webpagesWorkspace.getId());
        TopicType buttonStyle = dmx.getTopicType("de.mikromedia.button.style");
        workspacesService.assignToWorkspace(buttonStyle, webpagesWorkspace.getId());

    }

}
