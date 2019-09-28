package de.mikromedia.webpages.migrations;

import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration3 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // Defunct: Hook in two custom webclient renderers

    }

}
