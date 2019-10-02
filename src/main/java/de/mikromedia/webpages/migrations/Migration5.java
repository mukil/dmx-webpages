package de.mikromedia.webpages.migrations;

import de.mikromedia.webpages.WebpagePlugin;
import java.util.List;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

/**
 * Assigns all our custom types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration5 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dmx.getPrivilegedAccess().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // 1) Assing Website child types to new "Webpages" workspace too
        TopicType sitePrefix = dmx.getTopicType("de.mikromedia.site.prefix");
        workspacesService.assignTypeToWorkspace(sitePrefix, webpagesWorkspace.getId());
        // 2) Add website prefix to "Website" type
        TopicType siteType = dmx.getTopicType("de.mikromedia.site");
        siteType.addCompDef(mf.newCompDefModel(siteType.getUri(), sitePrefix.getUri(), "dmx.core.one"));
        // 3) Fetch all website topcis and copy username (former prefix) into new site prefix child
        List<Topic> websites = dmx.getTopicsByType("de.mikromedia.site");
        for (Topic website : websites) {
            Topic username = website.getRelatedTopic("dmx.core.association", "dmx.core.default",
                    "dmx.core.default", "dmx.accesscontrol.username");
            if (username != null) {
                website.getChildTopics().set("de.mikromedia.site.prefix", username.getSimpleValue().toString());
            } else {
                website.getChildTopics().set("de.mikromedia.site.prefix", "standard");
            }
        }
    }

}
