package de.mikromedia.webpages.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.workspaces.WorkspacesService;
import java.util.List;

public class Migration6 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        List<Topic> websites = dm4.getTopicsByType("de.mikromedia.site");
        for (Topic website : websites) {
            Topic username = website.getRelatedTopic("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "dm4.accesscontrol.username");
            website.getChildTopics().set("de.mikromedia.site.web_alias", username.getSimpleValue());
        }

    }

}
