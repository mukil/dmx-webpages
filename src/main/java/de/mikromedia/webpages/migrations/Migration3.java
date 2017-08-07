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

        // Hook in two custom webclient renderers
        TopicType headline = dm4.getTopicType("de.mikromedia.page.headline");
        ViewConfiguration viewConfig = headline.getViewConfig();
        viewConfig.setConfigValue("dm4.webclient.view_config",
                "dm4.webclient.simple_renderer_uri", "de.mikromedia.page.headline_renderer");

        TopicType aliasType = dm4.getTopicType("de.mikromedia.page.web_alias");
        ViewConfiguration viewConfigAlias = aliasType.getViewConfig();
        viewConfigAlias.setConfigValue("dm4.webclient.view_config",
                "dm4.webclient.simple_renderer_uri", "de.mikromedia.page.web_alias_renderer");

    }

}
