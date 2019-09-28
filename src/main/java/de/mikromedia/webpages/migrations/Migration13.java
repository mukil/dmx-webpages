package de.mikromedia.webpages.migrations;

import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.AssocType;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;


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
        TopicType sizeStyle = dmx.getTopicType("de.mikromedia.image.size_style");
        TopicType attachmentStyle = dmx.getTopicType("de.mikromedia.image.attachment_style");
        AssocType imageLarge = dmx.getAssocType("de.mikromedia.image.large");
        imageLarge.setDataTypeUri("dmx.core.identity");
        imageLarge.addCompDef(mf.newCompDefModel(imageLarge.getUri(), sizeStyle.getUri(), "dmx.core.one"));
        imageLarge.addCompDef(mf.newCompDefModel(imageLarge.getUri(), attachmentStyle.getUri(), "dmx.core.one"));
        AssocType imageSmall = dmx.getAssocType("de.mikromedia.image.small");
        imageSmall.setDataTypeUri("dmx.core.identity");
        imageSmall.addCompDef(mf.newCompDefModel(imageSmall.getUri(), sizeStyle.getUri(), "dmx.core.one"));
        imageSmall.addCompDef(mf.newCompDefModel(imageSmall.getUri(), attachmentStyle.getUri(), "dmx.core.one"));
    }

}
