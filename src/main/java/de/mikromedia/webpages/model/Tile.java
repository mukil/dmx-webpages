package de.mikromedia.webpages.model;


import static systems.dmx.core.Constants.*;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static de.mikromedia.webpages.WebpageService.TILE_HEADLINE;
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
import static de.mikromedia.webpages.WebpageService.FONT_COLOR_ASSOC;
import static de.mikromedia.webpages.WebpageService.BACKGROUND_COLOR_ASSOC;
import static de.mikromedia.webpages.WebpageService.WEBCLIENT_COLOR;

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
        return this.content.getChildTopics().getString(TILE_HEADLINE, null);
    }

    public String getHtml() {
        return this.content.getChildTopics().getString(TILE_HTML, null);
    }

    public String getLink() {
        return this.content.getChildTopics().getString(LINK, null);
    }

    /**
     * If one DM file topic is directly associated with a "Tile" topic
     * that topic should be given priority in the templates and become the actual section content.
     * @return  Topic   A standard topic replacing the (title, html) section content.
     **/
    public Topic getRelatedTopic() {
        if (this.relatedTopic == null) {
            this.relatedTopic = this.content.getRelatedTopic(ASSOCIATION, DEFAULT,
                DEFAULT, "dmx.files.file");
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
            return this.relatedTopic.getChildTopics().getString("dmx.files.path", null);
        }
        return null;
    }

    public String getRelatedTopicFileSize() {
        if (this.relatedTopic != null && this.relatedTopic.getTypeUri().equals("dmx.files.file")) {
            return humanReadableByteCount(this.relatedTopic.getChildTopics().getLong("dmx.files.size", 0), true);
        }
        return null;
    }

    public String getRelatedTopicFileMediaType() {
        if (this.relatedTopic != null && this.relatedTopic.getTypeUri().equals("dmx.files.file")) {
            return this.relatedTopic.getChildTopics().getString("dmx.files.media_type", null);
        }
        return null;
    }

    public String getSmallImage() {
        Topic imageFile = this.content.getRelatedTopic(IMAGE_SMALL, DEFAULT,
                DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getString(FILE_PATH, null);
    }

    public String getLargeImage() {
        Topic imageFile = this.content.getRelatedTopic(IMAGE_LARGE, DEFAULT,
                DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getString(FILE_PATH, null);
    }

    public String getBackgroundColor() {
        return this.content.getChildTopics().getString(WEBCLIENT_COLOR + "#" + BACKGROUND_COLOR_ASSOC, null);
    }

    public String getFontColor() {
        return this.content.getChildTopics().getString(WEBCLIENT_COLOR + "#" + FONT_COLOR_ASSOC, null);
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
