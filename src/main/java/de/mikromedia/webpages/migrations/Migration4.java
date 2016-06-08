package de.mikromedia.webpages.migrations;

import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationDefinitionModel;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.plugins.accesscontrol.AccessControlService;
import de.deepamehta.plugins.workspaces.WorkspacesService;

public class Migration4 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {
        // 1) Type workspace assignments to new "Administration" workspace
        TopicType webpageType = dms.getTopicType("de.mikromedia.page");
        TopicType sectionType = dms.getTopicType("de.mikromedia.page.section");
        TopicType graphicSectionType = dms.getTopicType("de.mikromedia.page.graphic_section");
        TopicType graphicType = dms.getTopicType("de.mikromedia.page.graphic");
        TopicType doReferencesType = dms.getTopicType("de.mikromedia.page.do_references");
        /** As of DM 4.8
        Topic administrationWorkspace = dms.getAccessControl().getWorkspace(AccessControlPlugin.ADMINISTRATION_WORKSPACE_URI);
        Note: Child topic types are currently not assigned to any workspace
        workspacesService.assignToWorkspace(sectionType, administrationWorkspace.getId());
        workspacesService.assignToWorkspace(graphicSectionType, administrationWorkspace.getId());
        workspacesService.assignToWorkspace(graphicType, administrationWorkspace.getId());
        workspacesService.assignToWorkspace(doReferencesType, administrationWorkspace.getId()); **/
        // 2) Rename Published Flag
        TopicType isPublished = dms.getTopicType("de.mikromedia.page.is_published"); // ### rename to "is_draft"
            isPublished.setSimpleValue("Draft (Unpublished)");
            isPublished.setUri("de.mikromedia.page.is_draft");
        // 3) Do let "Webpages" contain "Sections" and "Graphic Sections"
        /** webpageType.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            webpageType.getUri(), sectionType.getUri(), "dm4.core.many", "dm4.core.many"));
        webpageType.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
            webpageType.getUri(), graphicSectionType.getUri(), "dm4.core.many", "dm4.core.many")); **/
        // 4) Let the software attempt to extract and render an automatically generated "References" section
        webpageType.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
            webpageType.getUri(), doReferencesType.getUri(), "dm4.core.one", "dm4.core.one"));
        // 5) Rename "Webpage Menu Item" to "Menu Item"
        TopicType menuItem = dms.getTopicType("de.mikromedia.menu.item");
            menuItem.setSimpleValue("Menu Item");
        // 6) Rename "Webpage Redirect" to "Redirect"
        TopicType redirect =  dms.getTopicType("de.mikromedia.redirect");
            redirect.setSimpleValue("Redirect");
    }

}
