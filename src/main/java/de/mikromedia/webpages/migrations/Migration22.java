package de.mikromedia.webpages.migrations;


import de.mikromedia.webpages.WebpagePlugin;
import systems.dmx.accesscontrol.AccessControlService;
import static systems.dmx.core.Constants.ONE;
import systems.dmx.core.Topic;
import systems.dmx.core.TopicType;
import systems.dmx.core.service.Inject;
import systems.dmx.core.service.Migration;
import systems.dmx.workspaces.WorkspacesService;

public class Migration22 extends Migration {

    @Inject WorkspacesService workspaces;
    @Inject AccessControlService accessControlService;

    @Override
    public void run () {

        // Assign new default pages
        Topic webpagesWs = workspaces.getWorkspace(WebpagePlugin.WEBPAGES_WS_URI);
        Topic bookmarksPage = dmx.getTopicByUri("de.mikromedia.bookmarks.page");
        Topic contactsPage = dmx.getTopicByUri("de.mikromedia.contacts.page");
        Topic notesPage = dmx.getTopicByUri("de.mikromedia.notes.page");
        Topic promutCss = dmx.getTopicByUri("de.mikromedia.iass_promut_style");
        workspaces.assignToWorkspace(bookmarksPage, webpagesWs.getId());
        workspaces.assignToWorkspace(contactsPage, webpagesWs.getId());
        workspaces.assignToWorkspace(notesPage, webpagesWs.getId());
        workspaces.assignToWorkspace(promutCss, webpagesWs.getId());
        // Add "Page Template" value to "Webpage"
        TopicType webpage = dmx.getTopicType("de.mikromedia.page");
        TopicType pageTemplate = dmx.getTopicType("de.mikromedia.page.template");
        webpage.addCompDef(mf.newCompDefModel(webpage.getUri(), pageTemplate.getUri(), ONE));
        workspaces.assignTypeToWorkspace(webpage, webpagesWs.getId());

    }

}
