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
 * Assigns all our custom Section types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration16 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        // Webpage Section
        TopicType section = dm4.getTopicType("de.mikromedia.section");
        TopicType sectionCss = dm4.getTopicType("de.mikromedia.section.css_class");
        DeepaMehtaType assocDef = section.addAssocDef(mf.newAssociationDefinitionModel("dm4.core.aggregation_def",
                section.getUri(), sectionCss.getUri(), "dm4.core.one", "dm4.core.one"));
        workspacesService.assignToWorkspace(sectionCss, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(assocDef, webpagesWorkspace.getId());
        Topic videoEmbedLayout = dm4.getTopicByUri("de.mikromedia.layout.embed");
        workspacesService.assignToWorkspace(videoEmbedLayout, webpagesWorkspace.getId());

    }

}
