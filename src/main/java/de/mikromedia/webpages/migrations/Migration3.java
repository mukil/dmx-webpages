package de.mikromedia.webpages.migrations;

import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.core.service.accesscontrol.SharingMode;
import systems.dmx.workspaces.WorkspacesService;

public class Migration3 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Create custom workspace for all our types and the standard site topics
        Topic webpagesWs = workspacesService.createWorkspace(WebpagePlugin.WEBPAGES_WS_NAME,
            WebpagePlugin.WEBPAGES_WS_URI, SharingMode.PUBLIC);
        accessControlService.setWorkspaceOwner(webpagesWs, AccessControlService.ADMIN_USERNAME);

        // 1) Assign standard site gets assigned to our very own "Webpages" workspace
        Topic siteTopic = dmx.getTopicByUri("de.mikromedia.standard_site");
        workspacesService.assignToWorkspace(siteTopic, webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.name"), webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.stylesheet"), webpagesWs.getId());
        workspacesService.assignToWorkspace(siteTopic.getChildTopics().getTopic("de.mikromedia.site.footer_html"), webpagesWs.getId());

        // 2) Our most upper (identity) types get a workspace assignment to our very own "Webpages" workspace
        TopicType siteType = dmx.getTopicType("de.mikromedia.site");
        TopicType menuItemType = dmx.getTopicType("de.mikromedia.menu.item"); // ### Child Types not assigned to workspace
        TopicType redirectType = dmx.getTopicType("de.mikromedia.redirect"); // ### Child Types not assigned to workspace
        TopicType pageType = dmx.getTopicType("de.mikromedia.page"); // ### Child Types not assigned to workspace
        TopicType fragmentName = dmx.getTopicType("de.mikromedia.site.footer_fragment_name"); 
        workspacesService.assignTypeToWorkspace(siteType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(menuItemType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(redirectType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(pageType, webpagesWs.getId());
        workspacesService.assignTypeToWorkspace(fragmentName, webpagesWs.getId());

    }

}
