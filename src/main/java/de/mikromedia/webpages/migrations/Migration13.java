package de.mikromedia.webpages.migrations;

import systems.dmx.accesscontrol.AccessControlService;
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
        // ### Moved to migration6.json as the migration caused problems with DMX 5.0-beta-5
        // ### My guess is that type.setDataTypeUri() is not yet supported by the platform
        // ### as Serialization always failed at these two types (in an fetchAllAssocTypes operation)
    }

}
