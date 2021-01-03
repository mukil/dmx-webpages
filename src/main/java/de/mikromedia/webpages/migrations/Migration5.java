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
public class Migration5 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // 1) Assign new association types to "Webpages" workspace too
        AssocType desktopHeaderImage = dmx.getAssocType("de.mikromedia.image.large");
        workspacesService.assignTypeToWorkspace(desktopHeaderImage, webpagesWorkspace.getId());
        AssocType mobileHeaderImage = dmx.getAssocType("de.mikromedia.image.small");
        workspacesService.assignTypeToWorkspace(mobileHeaderImage, webpagesWorkspace.getId());
        // 2) Assign new topic types to "Webpages" workspace
        TopicType header = dmx.getTopicType("de.mikromedia.header");
        // ### Topic headerViewConfig = header.getRelatedTopic("dmx.core.composition", "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        // workspacesService.assignTypeToWorkspace(headerViewConfig, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(header, webpagesWorkspace.getId());
        TopicType headerTitle = dmx.getTopicType("de.mikromedia.header.title");
        workspacesService.assignTypeToWorkspace(headerTitle, webpagesWorkspace.getId());
        TopicType headerContent = dmx.getTopicType("de.mikromedia.header.html");
        workspacesService.assignTypeToWorkspace(headerContent, webpagesWorkspace.getId());
        AssocType headerColorBg = dmx.getAssocType("de.mikromedia.background.color");
        workspacesService.assignTypeToWorkspace(headerColorBg, webpagesWorkspace.getId());
        AssocType headerColorFont = dmx.getAssocType("de.mikromedia.font.color");
        workspacesService.assignTypeToWorkspace(headerColorFont, webpagesWorkspace.getId());
        TopicType headerScript = dmx.getTopicType("de.mikromedia.header.js");
        workspacesService.assignTypeToWorkspace(headerScript, webpagesWorkspace.getId());
        TopicType button = dmx.getTopicType("de.mikromedia.button");
        // ### Topic buttonViewConfig = button.getRelatedTopic("dmx.core.composition", "dmx.core.type", "dmx.core.view_config", "dmx.webclient.view_config");
        // workspacesService.assignTypeToWorkspace(buttonViewConfig, webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(button, webpagesWorkspace.getId());
        TopicType buttonTitle = dmx.getTopicType("de.mikromedia.button.title");
        workspacesService.assignTypeToWorkspace(buttonTitle, webpagesWorkspace.getId());
        TopicType buttonHref = dmx.getTopicType("de.mikromedia.link");
        workspacesService.assignTypeToWorkspace(buttonHref, webpagesWorkspace.getId());
        TopicType buttonStyle = dmx.getTopicType("de.mikromedia.button.style");
        workspacesService.assignTypeToWorkspace(buttonStyle, webpagesWorkspace.getId());

    }

}
