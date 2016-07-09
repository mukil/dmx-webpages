package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.AssociationType;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;
import de.mikromedia.webpages.WebpagePlugin;

/**
 * Assigns all our custom types to the public "Webpages" workspace.
 * @author malted
 */
public class Migration4 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // 0) Assign standard site to public "Webpages" workspace upon installation of plugin
        Topic webpagesWorkspace = dm4.getAccessControl().getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        Topic standardSite = dm4.getTopicByUri("de.mikromedia.standard_site");
        workspacesService.assignToWorkspace(standardSite, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(standardSite.getChildTopics().getTopic("de.mikromedia.site.name"), webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(standardSite.getChildTopics().getTopic("de.mikromedia.site.stylesheet"), webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(standardSite.getChildTopics().getTopic("de.mikromedia.site.footer_html"), webpagesWorkspace.getId());
        // 1) Assing Website child types to new "Webpages" workspace too
        TopicType captionType = dm4.getTopicType("de.mikromedia.site.caption");
        TopicType nameType = dm4.getTopicType("de.mikromedia.site.name");
        TopicType footerType = dm4.getTopicType("de.mikromedia.site.footer_html");
        TopicType stylesheetType = dm4.getTopicType("de.mikromedia.site.stylesheet");
        TopicType aboutType = dm4.getTopicType("de.mikromedia.site.about_html");
        workspacesService.assignToWorkspace(captionType, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(nameType, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(footerType, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(stylesheetType, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(aboutType, webpagesWorkspace.getId());
        // 2) Assing Webpage child types to new "Administration" workspace too
        TopicType pageTitle = dm4.getTopicType("de.mikromedia.page.headline");
        TopicType pageHeader = dm4.getTopicType("de.mikromedia.page.main");
        TopicType pageAlias = dm4.getTopicType("de.mikromedia.page.web_alias");
        TopicType pageDescription = dm4.getTopicType("de.mikromedia.page.about");
        TopicType isDraft = dm4.getTopicType("de.mikromedia.page.is_draft");
        TopicType authorName = dm4.getTopicType("de.mikromedia.page.author_name");
        TopicType pageStylesheet = dm4.getTopicType("de.mikromedia.page.stylesheet");
        workspacesService.assignToWorkspace(pageTitle, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(pageHeader, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(pageAlias, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(pageDescription, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(isDraft, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(authorName, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(pageStylesheet, webpagesWorkspace.getId());
        // 3) Assing Webpage Section child types to new "webpages" workspace too
        TopicType section = dm4.getTopicType("de.mikromedia.section");
        TopicType headline = dm4.getTopicType("de.mikromedia.section.headline");
        TopicType sectionHtml = dm4.getTopicType("de.mikromedia.section.html");
        TopicType sectionAlignment = dm4.getTopicType("de.mikromedia.section.alignment");
        workspacesService.assignToWorkspace(section, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionHtml, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(headline, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(sectionAlignment, webpagesWorkspace.getId());
        // 4) Assign all Webpage Element child types to new "Webapges" workspace.
        TopicType element = dm4.getTopicType("de.mikromedia.element");
        TopicType elementHeadline = dm4.getTopicType("de.mikromedia.element.headline");
        TopicType elementContent = dm4.getTopicType("de.mikromedia.element.content");
        TopicType elementId = dm4.getTopicType("de.mikromedia.element.id");
        TopicType elementAttribution = dm4.getTopicType("de.mikromedia.element.attr");
        TopicType elementLinkTarget = dm4.getTopicType("de.mikromedia.element.link_target");
        AssociationType fileElementEdge = dm4.getAssociationType("de.mikromedia.element.file_edge");
        workspacesService.assignToWorkspace(element, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementHeadline, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementContent, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementAttribution, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementId, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(elementLinkTarget, webpagesWorkspace.getId());
        workspacesService.assignToWorkspace(fileElementEdge, webpagesWorkspace.getId());

    }

}
