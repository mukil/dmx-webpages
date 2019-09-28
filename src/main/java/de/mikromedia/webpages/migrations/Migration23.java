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
public class Migration23 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        TopicType element = dmx.getTopicType("de.mikromedia.element");
        TopicType elementHeadline = dmx.getTopicType("de.mikromedia.element.headline");
        TopicType elementContent = dmx.getTopicType("de.mikromedia.element.content");
        TopicType elementId = dmx.getTopicType("de.mikromedia.element.id");
        TopicType elementAttribution = dmx.getTopicType("de.mikromedia.element.attr");
        TopicType elementLinkTarget = dmx.getTopicType("de.mikromedia.element.link_target");
        AssocType fileElementEdge = dmx.getAssocType("de.mikromedia.element.file_edge");
        workspacesService.assignToWorkspace(element, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementHeadline, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementContent, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementAttribution, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementId, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementLinkTarget, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(fileElementEdge, webpagesWorkspace.getId());

    }

}
