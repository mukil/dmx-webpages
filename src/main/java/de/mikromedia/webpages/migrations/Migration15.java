package de.mikromedia.webpages.migrations;

import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.deepamehta.workspaces.WorkspacesService;
import java.util.List;
import java.util.logging.Logger;

/**
 * Assigns all standard topics to new confidential "Workspace" Workspace.
 * @author malted
 */
public class Migration15 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    private Logger log = Logger.getLogger(getClass().getName());
    private String workspaceName = "Infowork";
    private String workspaceUri = "de.mikromedia.information_work";
    private Topic confidentialWorkspace = null;

    @Override
    public void run () {

        // 0) Create custom workspace for all our types and the standard site topics
        confidentialWorkspace = workspacesService.createWorkspace(workspaceName,
            workspaceUri, SharingMode.CONFIDENTIAL);
        accessControlService.setWorkspaceOwner(confidentialWorkspace, AccessControlService.ADMIN_USERNAME);
        log.info("> Created new confidential workspace \"" + confidentialWorkspace.getSimpleValue() + "\" for revision of information before publication");
        List<Topic> insts = dm4.getTopicsByType("dm4.contacts.institution");
        log.info("> Moving " + insts.size() + " Institition topics into new confidential workspace");
        for (Topic inst : insts) {
            moveWithChilds(inst);
        }
        List<Topic> persons = dm4.getTopicsByType("dm4.contacts.person");
        log.info("> Moving " + persons.size() + " Person topics into new confidential workspace");
        for (Topic person : persons) {
            moveWithChilds(person);
        }
        List<Topic> bookmarks = dm4.getTopicsByType("dm4.webbrowser.web_resource");
        log.info("> Moving " + bookmarks.size() + " Web Resource topics into new confidential workspace");
        for (Topic bookmark : bookmarks) {
            moveWithChilds(bookmark);
        }
        List<Topic> notes = dm4.getTopicsByType("dm4.notes.note");
        log.info("> Moving " + notes.size() + " Note topics into new confidential workspace");
        for (Topic note : notes) {
            moveWithChilds(note);
        }
        List<Topic> files = dm4.getTopicsByType("dm4.files.file");
        log.info("> Moving " + files.size() + " File topics into new confidential workspace");
        for (Topic file : files) {
            moveWithChilds(file);
        }
        List<Topic> folders = dm4.getTopicsByType("dm4.files.folder");
        log.info("> Moving " + folders.size() + " Folder topics into new confidential workspace");
        for (Topic folder : folders) {
            moveWithChilds(folder);
        }

    }

    private void moveWithChilds(Topic topic) {
        List<RelatedTopic> aggregatedChilds = topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.parent", null, null);
        for (RelatedTopic childA : aggregatedChilds) {
            log.info(">> Concealing child topic: " + topic.getTypeUri()+ ", " + topic.getId() + " via moving into confidential Workspace");
            workspacesService.assignToWorkspace(childA, confidentialWorkspace.getId());
            workspacesService.assignToWorkspace(childA.getRelatingAssociation(), confidentialWorkspace.getId());
            moveWithChilds(childA);
        }
        RelatedTopic aggreatedChild = topic.getRelatedTopic("dm4.core.aggregation", "dm4.core.parent", null, null);
        if (aggregatedChilds != null) {
            log.info(">> Concealing child topic: " + topic.getTypeUri()+ ", " + topic.getId() + " via moving into confidential Workspace");
            workspacesService.assignToWorkspace(aggreatedChild, confidentialWorkspace.getId());
            workspacesService.assignToWorkspace(aggreatedChild.getRelatingAssociation(), confidentialWorkspace.getId());
            moveWithChilds(aggreatedChild);
        }
        RelatedTopic compositeChild = topic.getRelatedTopic("dm4.core.composition", "dm4.core.parent", null, null);
        if (compositeChild != null) {
            log.info(">> Concealing child topic: " + topic.getTypeUri()+ ", " + topic.getId() + " via moving into confidential Workspace");
            workspacesService.assignToWorkspace(compositeChild, confidentialWorkspace.getId());
            workspacesService.assignToWorkspace(compositeChild.getRelatingAssociation(), confidentialWorkspace.getId());
            moveWithChilds(compositeChild);
        }
    }

}
