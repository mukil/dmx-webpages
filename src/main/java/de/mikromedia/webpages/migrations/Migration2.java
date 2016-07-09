package de.mikromedia.webpages.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.Association;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;

public class Migration2 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Create custom workspace for all our types and the standard site topics
        Topic webpagesWs = workspacesService.createWorkspace(WebpagePlugin.WEBPAGES_WS_NAME,
            WebpagePlugin.WEBPAGES_WS_URI, WebpagePlugin.WEBPAGES_SHARING_MODE);
        accessControlService.setWorkspaceOwner(webpagesWs, AccessControlService.ADMIN_USERNAME);
        // 1) Assign standard site topic and assoc to WS "Webpages"
        Topic siteTopic = dm4.getTopicByUri("de.mikromedia.standard_site");
        workspacesService.assignToWorkspace(siteTopic, webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.name"), webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.stylesheet"), webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.footer_html"), webpagesWs.getId());
        // 2) Our global/standard/default "website" is the website of user "admin" (we do this now to support multi-sites later)
        Topic adminTopic = accessControlService.getUsernameTopic(AccessControlService.ADMIN_USERNAME);
        Association assoc = dm4.createAssociation(mf.newAssociationModel("dm4.core.association",
                mf.newTopicRoleModel(adminTopic.getId(), "dm4.core.default"),
                mf.newTopicRoleModel(siteTopic.getId(), "dm4.core.default")));
        workspacesService.assignToWorkspace(assoc, webpagesWs.getId());
        // 3) Type workspace assignments to new "Administration" workspace
        TopicType siteType = dm4.getTopicType("de.mikromedia.site");
        TopicType menuItemType = dm4.getTopicType("de.mikromedia.menu.item"); // ### Child Types not assigned to workspace
        TopicType redirectType = dm4.getTopicType("de.mikromedia.redirect"); // ### Child Types not assigned to workspace
        TopicType pageType = dm4.getTopicType("de.mikromedia.page");
        workspacesService.assignTypeToWorkspace(siteType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(menuItemType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(redirectType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(pageType, webpagesWs.getId());

    }

}
