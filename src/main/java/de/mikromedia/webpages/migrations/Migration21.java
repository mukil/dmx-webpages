package de.mikromedia.webpages.migrations;


import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Remove unsupported Section Layouts.
 */
public class Migration21 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // Todo: Remove Un-Maintained Section Layouts
        // - Contact Form
        // - Map Widget
        // - Embed

    }

}
