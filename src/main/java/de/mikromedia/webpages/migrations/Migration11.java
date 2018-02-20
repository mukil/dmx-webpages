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
public class Migration11 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        TopicType element = dm4.getTopicType("de.mikromedia.element");
        TopicType elementHeadline = dm4.getTopicType("de.mikromedia.element.headline");
        TopicType elementContent = dm4.getTopicType("de.mikromedia.element.content");
        TopicType elementId = dm4.getTopicType("de.mikromedia.element.id");
        TopicType elementAttribution = dm4.getTopicType("de.mikromedia.element.attr");
        TopicType elementLinkTarget = dm4.getTopicType("de.mikromedia.element.link_target");
        AssociationType fileElementEdge = dm4.getAssociationType("de.mikromedia.element.file_edge");
        workspacesService.assignToWorkspace(element, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementHeadline, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementContent, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementAttribution, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementId, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementLinkTarget, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(fileElementEdge, webpagesWorkspace.getId());

    }

}
