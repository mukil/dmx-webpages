package de.mikromedia.webpages.model;


import static de.mikromedia.webpages.WebpageService.ASSOCIATION;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import static de.mikromedia.webpages.WebpageService.SECTION;
import static de.mikromedia.webpages.WebpageService.SECTION_LAYOUT;
import static de.mikromedia.webpages.WebpageService.SECTION_PLACEMENT;
import static de.mikromedia.webpages.WebpageService.SECTION_TITLE;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static de.mikromedia.webpages.WebpageService.BACKGROUND_COLOR;
import static de.mikromedia.webpages.WebpageService.DEFAULT_ATTACHMENT;
import static de.mikromedia.webpages.WebpageService.DEFAULT_SIZE;
import static de.mikromedia.webpages.WebpageService.FONT_COLOR;
import static de.mikromedia.webpages.WebpageService.IMAGE_ATTACHMENT_STYLE;
import static de.mikromedia.webpages.WebpageService.IMAGE_LARGE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SIZE_STYLE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SMALL;
import static de.mikromedia.webpages.WebpageService.SECTION_CSS_CLASS;
import static de.mikromedia.webpages.WebpageService.TILE;
import java.util.Collections;
import java.util.Comparator;
import systems.dmx.core.Assoc;
import systems.dmx.core.RelatedTopic;
import systems.dmx.core.Topic;
import systems.dmx.core.model.SimpleValue;
import systems.dmx.core.util.JavaUtils;
import static de.mikromedia.webpages.WebpageService.DMX_FILE;

public class Section {

    private RelatedTopic pageSection;
    private RelatedTopic relatedTopic;

    public Section(RelatedTopic pageSection) {
        this.pageSection = pageSection;
        if (!isSectionTopic()) {
            throw new IllegalArgumentException("Given topic is not of type WebpageSection");
        }
        this.pageSection.loadChildTopics();
    }

    public long getId() {
        return this.pageSection.getId();
    }

    public Topic getTopic() {
        return this.pageSection;
    }

    public int getOrdinalNumber() {
        Assoc assoc = this.pageSection.getRelatingAssoc();
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
        return this.pageSection.getChildTopics().getStringOrNull(SECTION_TITLE);
    }

    public String getAnchorId() {
        String title = getTitle();
        if (title == null) return "" + getId();
        return JavaUtils.encodeURIComponent(title).toLowerCase();
    }

    public List<Tile> getContents() {
        List<Tile> tiles = new ArrayList();
        List<RelatedTopic> contents = this.pageSection.getChildTopics().getTopics(TILE);
        for (RelatedTopic content : contents) {
            Tile sectionContent = new Tile(content);
            tiles.add(sectionContent);
        }
        return getTilesSorted(tiles);
    }

    public String getCustomClassName() {
        String value = this.pageSection.getChildTopics().getStringOrNull(SECTION_CSS_CLASS);
        return (value != null) ? value : "";
    }

    public String getSmallImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getSmallImageAttachment() {
        RelatedTopic imageSmall = this.pageSection.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        String val = null;
        if (imageSmall != null) {
            Assoc imageConfig = imageSmall.getRelatingAssoc();
            val = imageConfig.getChildTopics().getStringOrNull(IMAGE_ATTACHMENT_STYLE);
        }
        return (val == null) ? DEFAULT_ATTACHMENT : val.toLowerCase();
    }

    public String getSmallImageSize() {
        RelatedTopic imageSmall = this.pageSection.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        String val = null;
        if (imageSmall == null) getSmallImage();
        if (imageSmall != null) {
            Assoc imageConfig = imageSmall.getRelatingAssoc();
            val = imageConfig.getChildTopics().getStringOrNull(IMAGE_SIZE_STYLE);
        }
        return (val == null) ? DEFAULT_SIZE : val.toLowerCase();
    }

    public String getLargeImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getLargeImageAttachment() {
        RelatedTopic imageLarge = this.pageSection.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        String val = null;
        if (imageLarge != null) {
            Assoc imageConfig = imageLarge.getRelatingAssoc();
            val = imageConfig.getChildTopics().getStringOrNull(IMAGE_ATTACHMENT_STYLE);
        }
        return (val == null) ? DEFAULT_ATTACHMENT : val.toLowerCase();
    }

    public String getLargeImageSize() {
        RelatedTopic imageLarge = this.pageSection.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DMX_FILE);
        String val = null;
        if (imageLarge == null) getSmallImage();
        if (imageLarge != null) {
            Assoc imageConfig = imageLarge.getRelatingAssoc();
            val = imageConfig.getChildTopics().getStringOrNull(IMAGE_SIZE_STYLE);
        }
        return (val == null) ? DEFAULT_SIZE : val.toLowerCase();
    }

    /**
     * If one DM file topic is directly associated with a "Tile" topic
     * that topic should be given priority in the templates and become the actual section content.
     * @return  Topic   A standard topic replacing the (title, html) section content.
     **/
    public Topic getRelatedTopic() {
        if (this.relatedTopic == null) {
            this.relatedTopic = this.pageSection.getRelatedTopic(ASSOCIATION, ROLE_DEFAULT,
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

    public String getLayoutName() {
        String layoutName = null;
        Topic layout = this.pageSection.getChildTopics().getTopicOrNull(SECTION_LAYOUT);
        if (layout != null) {
            if (layout.getUri().equals("de.mikromedia.layout.tiles_ten_six")) {
                layoutName = "ten-six-grid";
            } else if (layout.getUri().equals("de.mikromedia.layout.two_tiles")) {
                layoutName = "two-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.n_tiles")) {
                layoutName = "n-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.2_and_n_tiles")) {
                layoutName = "two-and-n-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.accordion")) {
                layoutName = "accordion-styled";
            } else if (layout.getUri().equals("de.mikromedia.layout.single_tile")) {
                layoutName = "single-tile";
            } else if (layout.getUri().equals("de.mikromedia.layout.quote_section")) {
                layoutName = "quote-tiles";
            } else if (layout.getUri().equals("de.mikromedia.layout.map_widget")) {
                layoutName = "map-widget";
            } else if (layout.getUri().equals("de.mikromedia.layout.contact_form")) {
                layoutName = "contact-form";
            } else if (layout.getUri().equals("de.mikromedia.layout.embed")) {
                layoutName = "embed";
            } else if (layout.getUri().equals("de.mikromedia.layout.native_embed")) {
                layoutName = "native-video";
            }
        }
        return layoutName;
    }

    public Topic getPlacement() {
        return this.pageSection.getChildTopics().getTopicOrNull(SECTION_PLACEMENT);
    }

    public String getBackgroundColor() {
        return this.pageSection.getChildTopics().getStringOrNull(BACKGROUND_COLOR);
    }

    public String getFontColor() {
        return this.pageSection.getChildTopics().getStringOrNull(FONT_COLOR);
    }

    private List<Tile> getTilesSorted(List<Tile> all) {
        Collections.sort(all, new Comparator<Tile>() {
            public int compare(Tile t1, Tile t2) {
                try {
                    if ( t1.getOrdinalNumber() > t2.getOrdinalNumber() ) return 1;
                    if ( t1.getOrdinalNumber() < t2.getOrdinalNumber() ) return -1;
                } catch (Exception nfe) {
                    return 0;
                }
                return 0;
            }
        });
        return all;
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("title", getTitle())
                .put("contents", getContents())
                .put("layout", getLayoutName())
                .put("placement", getPlacement())
                .put("font_color", getFontColor())
                .put("bg_color", getBackgroundColor());
        } catch (JSONException ex) {
            Logger.getLogger(Webpage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isSectionTopic() {
        if (this.pageSection == null) return false;
        return (this.pageSection.getTypeUri().equals(SECTION));
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
