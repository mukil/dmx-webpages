package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;
import java.util.List;

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
        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // 1) Assing Website child types to new "Webpages" workspace too
        TopicType sitePrefix = dm4.getTopicType("de.mikromedia.site.prefix");
        workspacesService.assignToWorkspace(sitePrefix, webpagesWorkspace.getId());
        // 2) Add website prefix to "Website" type
        TopicType siteType = dm4.getTopicType("de.mikromedia.site");
        siteType.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.composition_def", siteType.getUri(),
                sitePrefix.getUri(), "dm4.core.one", "dm4.core.one"));
        // 3) Fetch all website topcis and copy username (former prefix) into new site prefix child
        List<Topic> websites = dm4.getTopicsByType("de.mikromedia.site");
        for (Topic website : websites) {
            Topic username = website.getRelatedTopic("dm4.core.association", "dm4.core.default",
                    "dm4.core.default", "dm4.accesscontrol.username");
            if (username != null) {
                website.getChildTopics().set("de.mikromedia.site.prefix", username.getSimpleValue().toString());
            } else {
                website.getChildTopics().set("de.mikromedia.site.prefix", "standard");
            }
        }
    }

}
