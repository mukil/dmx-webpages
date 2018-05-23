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
public class Migration16 extends Migration {

    @Inject WorkspacesService workspacesService;
    @Inject AccessControlService accessControlService;

    private Logger log = Logger.getLogger(getClass().getName());
    private String workspaceUri = "de.mikromedia.information_work";
    private Topic confidentialWorkspace = null;

    @Override
    public void run () {

        // 0) Create custom workspace for all our types and the standard site topics
        confidentialWorkspace = workspacesService.getWorkspace(workspaceUri);
        accessControlService.setWorkspaceOwner(confidentialWorkspace, AccessControlService.ADMIN_USERNAME);
        log.info("> Re-using confidential workspace \"" + confidentialWorkspace.getSimpleValue() + "\" for revision of information before publication");
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
        // Move topic
        workspacesService.assignToWorkspace(topic, confidentialWorkspace.getId());
        // Move childs too
        List<RelatedTopic> aggregatedChilds = topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.parent", null, null);
        for (RelatedTopic childA : aggregatedChilds) {
            checkAndMove(childA);
        }
        List<RelatedTopic> compositeChilds = topic.getRelatedTopics("dm4.core.composition", "dm4.core.parent", null, null);
        for (RelatedTopic compositeChild : compositeChilds) {
            if (compositeChild != null) {
                checkAndMove(compositeChild);
            }
        }
    }

    private void checkAndMove(RelatedTopic topic) {
        if (!topic.getTypeUri().startsWith("dm4.workspaces.") && !topic.getTypeUri().startsWith("dm4.core.")) {
            log.info(">> Concealing child topic: " + topic.getTypeUri()+ ", " + topic.getId() + " via moving into confidential Workspace");
            workspacesService.assignToWorkspace(topic, confidentialWorkspace.getId());
            workspacesService.assignToWorkspace(topic.getRelatingAssociation(), confidentialWorkspace.getId());
            moveWithChilds(topic);
        }
    }

}
