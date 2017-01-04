package de.mikromedia.webpages.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.workspaces.WorkspacesService;

public class Migration5 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        TopicType webAlias = dm4.getTopicType("de.mikromedia.site.web_alias");
        TopicType website = dm4.getTopicType("de.mikromedia.site");
        website.addAssocDef(mf.newAssociationDefinitionModel(
            "dm4.core.composition_def", website.getUri(), webAlias.getUri(), "dm4.core.one", "dm4.core.one"));

    }

}
