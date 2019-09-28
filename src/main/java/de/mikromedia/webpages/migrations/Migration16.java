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
 * Assigns all our custom Section types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration16 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // Webpage Section
        TopicType section = dmx.getTopicType("de.mikromedia.section");
        TopicType sectionCss = dmx.getTopicType("de.mikromedia.section.css_class");
        DMXType assocDef = section.addCompDef(mf.newCompDefModel(section.getUri(), sectionCss.getUri(), "dmx.core.one"));
        workspacesService.assignToWorkspace(sectionCss, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(assocDef, webpagesWorkspace.getId());
        Topic videoEmbedLayout = dmx.getTopicByUri("de.mikromedia.layout.embed");
        workspacesService.assignToWorkspace(videoEmbedLayout, webpagesWorkspace.getId());

    }

}
