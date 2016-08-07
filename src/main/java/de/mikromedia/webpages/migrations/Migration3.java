package de.mikromedia.webpages.migrations;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.ViewConfiguration;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.workspaces.WorkspacesService;

public class Migration3 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

       TopicType tagType = dm4.getTopicType("de.mikromedia.page.web_alias");
        ViewConfiguration viewConfig = tagType.getViewConfig();
        viewConfig.addSetting("dm4.webclient.view_config",
                "dm4.webclient.simple_renderer_uri", "de.mikromedia.page.web_alias_renderer");

    }

}
