package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.DeepaMehtaType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;

/**
 * Extends website topic type to support multiple footer fragments identified by URI.
 * @author malted
 */
public class Migration19 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        TopicType site = dm4.getTopicType("de.mikromedia.site");
        DeepaMehtaType assocDef = site.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def",
                site.getUri(), "de.mikromedia.site.footer_fragment_name", "dm4.core.one", "dm4.core.one"));
        workspacesService.assignToWorkspace(dm4.getTopicType("de.mikromedia.site.footer_fragment_name"), webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(assocDef, webpagesWorkspace.getId());

    }

}
