package de.mikromedia.webpages.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.Association;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.plugins.workspaces.WorkspacesService;
import de.deepamehta.plugins.accesscontrol.AccessControlService;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;

public class Migration2 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 1) Assign standard site topic and assoc to "DeepaMehta", make admins/ our standard site public.
        Topic deepaMehtaWorkspace = workspacesService.getWorkspace(WorkspacesService.DEEPAMEHTA_WORKSPACE_URI);
        Topic siteTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
        workspacesService.assignToWorkspace(siteTopic, deepaMehtaWorkspace.getId());

        // 2) Our default "website" is the website of user "admin" (we do this now to support multi-sites later)
        Topic adminTopic = accessControlService.getUsernameTopic(AccessControlService.ADMIN_USERNAME);
        Association assoc = dms.createAssociation(new AssociationModel("dm4.core.association",
                new TopicRoleModel(adminTopic.getId(), "dm4.core.default"),
                new TopicRoleModel(siteTopic.getId(), "dm4.core.default")));
        workspacesService.assignToWorkspace(assoc, deepaMehtaWorkspace.getId());

        // 3) Type workspace assignments to admins "Private Workspace"
        Topic adminPrivateWorkspace = dms.getAccessControl().getPrivateWorkspace(AccessControlService.ADMIN_USERNAME);
        // Note: Child topic types are currently not assigned to any workspace
        TopicType siteType = dms.getTopicType("de.mikromedia.site");
        TopicType menuItemType = dms.getTopicType("de.mikromedia.menu.item");
        TopicType redirectType = dms.getTopicType("de.mikromedia.redirect");
        TopicType pageType = dms.getTopicType("de.mikromedia.page");
        workspacesService.assignTypeToWorkspace(siteType, adminPrivateWorkspace.getId());
        workspacesService.assignTypeToWorkspace(menuItemType, adminPrivateWorkspace.getId());
        workspacesService.assignTypeToWorkspace(redirectType, adminPrivateWorkspace.getId());
        workspacesService.assignTypeToWorkspace(pageType, adminPrivateWorkspace.getId());

    }

}
