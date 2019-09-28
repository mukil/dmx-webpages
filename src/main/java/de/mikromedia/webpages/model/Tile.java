package de.mikromedia.webpages.model;


import static de.mikromedia.webpages.WebpageService.ASSOCIATION;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static de.mikromedia.webpages.WebpageService.TILE_HEADLINE;
import static de.mikromedia.webpages.WebpageService.BACKGROUND_COLOR;
import static de.mikromedia.webpages.WebpageService.FONT_COLOR;
import static de.mikromedia.webpages.WebpageService.IMAGE_LARGE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SMALL;
import static de.mikromedia.webpages.WebpageService.LINK;
import static de.mikromedia.webpages.WebpageService.TILE;
import static de.mikromedia.webpages.WebpageService.TILE_HTML;
import systems.dmx.core.Assoc;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.model.SimpleValue;
import static de.mikromedia.webpages.WebpageService.DMX_FILE;

public class Tile {
    
    private RelatedTopic content;
    private Topic relatedTopic;

    public Tile(RelatedTopic sectionContent) {
        this.content = sectionContent;
        if (!isSectionContentTopic()) {
            throw new IllegalArgumentException("Given topic is not of type Section Content");
        }
        this.content.loadChildTopics();
    }

    public long getId() {
        return this.content.getId();
    }

    public Topic getTopic() {
        return this.content;
    }

    public int getOrdinalNumber() {
        Assoc assoc = this.content.getRelatingAssoc();
        SimpleValue assocText = assoc.getSimpleValue();
        int ordinalNumber = 0;
        try {
            ordinalNumber = (assocText == null || assocText.toString().isEmpty()) ? 0
                : Integer.parseInt(assocText.toString());
        } catch (NumberFormatException ex) {
            // log.debug("");
        }
        return ordinalNumber;
    }

    // --- Custom Section Data Accessors
    
    public String getTitle() {
        return this.content.getChildTopics().getStringOrNull(TILE_HEADLINE);
    }

    public String getHtml() {
        return this.content.getChildTopics().getStringOrNull(TILE_HTML);
    }

    public String getLink() {
        return this.content.getChildTopics().getStringOrNull(LINK);
    }

    /**
     * If one DM file topic is directly associated with a "Tile" topic
     * that topic should be given priority in the templates and become the actual section content.
     * @return  Topic   A standard topic replacing the (title, html) section content.
     **/
    public Topic getRelatedTopic() {
        if (this.relatedTopic == null) {
            this.relatedTopic = this.content.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT,
                ROLE_DEFAULT, "dmx.files.file");
        }
        return this.relatedTopic;
    }

    public String getRelatedTopicTypeUri() {
        if (this.relatedTopic != null) {
            return this.relatedTopic.getTypeUri();
        }
        return null;
    }

    public String getRelatedTopicFilePath() {
        if (this.relatedTopic != null && this.relatedTopic.getTypeUri().equals("dmx.files.file")) {
            return this.relatedTopic.getChildTopics().getStringOrNull("dmx.files.path");
        }
        return null;
    }

    public String getRelatedTopicFileSize() {
        if (this.relatedTopic != null && this.relatedTopic.getTypeUri().equals("dmx.files.file")) {
            return humanReadableByteCount(this.relatedTopic.getChildTopics().getLongOrNull("dmx.files.size"), true);
        }
        return null;
    }

    public String getRelatedTopicFileMediaType() {
        if (this.relatedTopic != null && this.relatedTopic.getTypeUri().equals("dmx.files.file")) {
            return this.relatedTopic.getChildTopics().getStringOrNull("dmx.files.media_type");
        }
        return null;
    }

    public String getSmallImage() {
        Topic imageFile = this.content.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getLargeImage() {
        Topic imageFile = this.content.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getBackgroundColor() {
        return this.content.getChildTopics().getStringOrNull(BACKGROUND_COLOR);
    }

    public String getFontColor() {
        return this.content.getChildTopics().getStringOrNull(FONT_COLOR);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("title", getTitle())
                .put("html", getHtml())
                .put("related_topic", getRelatedTopic())
                .put("font_color", getFontColor())
                .put("bg_color", getBackgroundColor());
        } catch (JSONException ex) {
            Logger.getLogger(Webpage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isSectionContentTopic() {
        if (this.content == null) return false;
        return (this.content.getTypeUri().equals(TILE));
    }

    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
