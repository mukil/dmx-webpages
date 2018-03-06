package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.AssociationType;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;

/**
 * Assigns all our custom types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration13 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {
        // 1) Add attachment style to "Image Small" and "Image Large" Edges
        TopicType sizeStyle = dm4.getTopicType("de.mikromedia.image.size_style");
        TopicType attachmentStyle = dm4.getTopicType("de.mikromedia.image.attachment_style");
        AssociationType imageLarge = dm4.getAssociationType("de.mikromedia.image.large");
        imageLarge.setDataTypeUri("dm4.core.composite");
        imageLarge.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def", imageLarge.getUri(),
                sizeStyle.getUri(), "dm4.core.one", "dm4.core.one"));
        imageLarge.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def", imageLarge.getUri(),
                attachmentStyle.getUri(), "dm4.core.one", "dm4.core.one"));
        AssociationType imageSmall = dm4.getAssociationType("de.mikromedia.image.small");
        imageSmall.setDataTypeUri("dm4.core.composite");
        imageSmall.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def", imageSmall.getUri(),
                sizeStyle.getUri(), "dm4.core.one", "dm4.core.one"));
        imageSmall.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def", imageSmall.getUri(),
                attachmentStyle.getUri(), "dm4.core.one", "dm4.core.one"));
    }

}
