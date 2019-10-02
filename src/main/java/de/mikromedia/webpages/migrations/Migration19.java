package de.mikromedia.webpages.migrations;


import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.DMXType;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Extends website topic type to support multiple footer fragments identified by URI.
 * @author malted
 */
public class Migration19 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        TopicType site = dmx.getTopicType("de.mikromedia.site");
        DMXType assocDef = site.addCompDef(mf.newCompDefModel(site.getUri(), "de.mikromedia.site.footer_fragment_name", "dmx.core.one"));
        workspacesService.assignTypeToWorkspace(dmx.getTopicType("de.mikromedia.site.footer_fragment_name"), webpagesWorkspace.getId());
        workspacesService.assignTypeToWorkspace(assocDef, webpagesWorkspace.getId());

    }

}
