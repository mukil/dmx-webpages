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

        // 1) Assign standard site topic and assoc to "DeepaMehta" assignment
        Topic deepaMehtaWorkspace = workspacesService.getWorkspace(WorkspacesService.DEEPAMEHTA_WORKSPACE_URI);
		Topic siteTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
        workspacesService.assignToWorkspace(siteTopic, deepaMehtaWorkspace.getId());

        // 2) Default "website" user association
        Topic adminTopic = accessControlService.getUsernameTopic(AccessControlService.ADMIN_USERNAME);
        Association assoc = dms.createAssociation(new AssociationModel("dm4.core.association",
                new TopicRoleModel(adminTopic.getId(), "dm4.core.default"),
                new TopicRoleModel(siteTopic.getId(), "dm4.core.default")));
        workspacesService.assignToWorkspace(assoc, deepaMehtaWorkspace.getId());

        // 3) Type workspace assignments to "DeepaMehta
        // Note: Child topic types are curerntly not assigned to any workspace
		TopicType siteType = dms.getTopicType("de.mikromedia.site");
        TopicType menuItemType = dms.getTopicType("de.mikromedia.menu.item");
        TopicType redirectType = dms.getTopicType("de.mikromedia.redirect");
        TopicType pageType = dms.getTopicType("de.mikromedia.page");
        workspacesService.assignTypeToWorkspace(siteType, deepaMehtaWorkspace.getId());
        workspacesService.assignTypeToWorkspace(menuItemType, deepaMehtaWorkspace.getId());
        workspacesService.assignTypeToWorkspace(redirectType, deepaMehtaWorkspace.getId());
        workspacesService.assignTypeToWorkspace(pageType, deepaMehtaWorkspace.getId());

    }
	
}
