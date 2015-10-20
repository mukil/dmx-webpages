package de.mikromedia.webpages.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Migration;

public class Migration2 extends Migration {
	
	@Override
	public void run () {
		Topic siteTopic = dms.getTopic("uri", new SimpleValue("de.mikromedia.standard_site"));
		Topic siteType = dms.getTopicType("de.mikromedia.site");
		Topic pageType = dms.getTopicType("de.mikromedia.page");
		Topic defaultWorkspace = dms.getTopic("uri", new SimpleValue("de.workspaces.deepamehta"));
		// 
		dms.createAssociation(new AssociationModel("dm4.core.aggregation",
			new TopicRoleModel(siteTopic.getId(), "dm4.core.parent"),
			new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
		));
		dms.createAssociation(new AssociationModel("dm4.core.aggregation",
			new TopicRoleModel(siteType.getId(), "dm4.core.parent"),
			new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
		));
		dms.createAssociation(new AssociationModel("dm4.core.aggregation",
			new TopicRoleModel(pageType.getId(), "dm4.core.parent"),
			new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
		));
		
	}
	
}
