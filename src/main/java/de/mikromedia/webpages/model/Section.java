package de.mikromedia.webpages.model;

import de.deepamehta.core.Association;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import static de.mikromedia.webpages.WebpageService.DEEPAMEHTA_FILE;
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
import static de.mikromedia.webpages.WebpageService.FONT_COLOR;
import static de.mikromedia.webpages.WebpageService.IMAGE_LARGE;
import static de.mikromedia.webpages.WebpageService.IMAGE_SMALL;
import static de.mikromedia.webpages.WebpageService.TILE;
import java.util.Collections;
import java.util.Comparator;

public class Section {

    private RelatedTopic pageSection;

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
        Association assoc = this.pageSection.getRelatingAssociation();
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

    public List<Tile> getContents() {
        List<Tile> tiles = new ArrayList();
        List<RelatedTopic> contents = this.pageSection.getChildTopics().getTopics(TILE);
        for (RelatedTopic content : contents) {
            Tile sectionContent = new Tile(content);
            tiles.add(sectionContent);
        }
        return getTilesSorted(tiles);
    }

    public String getSmallImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(IMAGE_SMALL, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
    }

    public String getLargeImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(IMAGE_LARGE, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getStringOrNull(FILE_PATH);
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

}
