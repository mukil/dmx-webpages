package de.mikromedia.webpages.model;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.CoreService;
import static de.mikromedia.webpages.WebpageService.DEEPAMEHTA_FILE;
import static de.mikromedia.webpages.WebpageService.DESKTOP_IMAGE_ASSOC;
import static de.mikromedia.webpages.WebpageService.FILE_PATH;
import static de.mikromedia.webpages.WebpageService.MOBILE_IMAGE_ASSOC;
import static de.mikromedia.webpages.WebpageService.ROLE_DEFAULT;
import static de.mikromedia.webpages.WebpageService.SECTION;
import static de.mikromedia.webpages.WebpageService.SECTION_BG_COLOR;
import static de.mikromedia.webpages.WebpageService.SECTION_COLOR;
import static de.mikromedia.webpages.WebpageService.SECTION_CONTENT;
import static de.mikromedia.webpages.WebpageService.SECTION_LAYOUT;
import static de.mikromedia.webpages.WebpageService.SECTION_PLACEMENT;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malt
 */
public class Section {
    
    private Topic pageSection;

    public Section(Topic pageSection) {
        this.pageSection = pageSection;
        if (!isSectionTopic()) {
            throw new IllegalArgumentException("Given topic is not of type WebpageSection");
        }
        this.pageSection.loadChildTopics();
    }

    public Section(long topicId, CoreService dms) {
        this.pageSection = dms.getTopic(topicId);
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

    // --- Custom Section Data Accessors
    
    public String getTitle() {
        return this.pageSection.getSimpleValue().toString();
    }

    public List<RelatedTopic> getContents() {
        return this.pageSection.getChildTopics().getTopics(SECTION_CONTENT);
    }

    public String getMobileImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(MOBILE_IMAGE_ASSOC, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getString(FILE_PATH);
    }

    public String getDesktopImage() {
        Topic imageFile = this.pageSection.getRelatedTopic(DESKTOP_IMAGE_ASSOC, ROLE_DEFAULT,
                ROLE_DEFAULT, DEEPAMEHTA_FILE);
        return (imageFile == null) ? "" : imageFile.getChildTopics().getString(FILE_PATH);
    }

    public String getLayout() {
        String layoutName = null;
        Topic layout = this.pageSection.getChildTopics().getTopicOrNull(SECTION_LAYOUT);
        if (layout != null) {
            if (layout.getUri().equals("de.mikromedia.layout.stackable_ten_six")) {
                layoutName = "ten-six-grid";
            } else if (layout.getUri().equals("de.mikromedia.layout.fixed_two_columns")) {
                layoutName = "two-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.stackable_n_column")) {
                layoutName = "n-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.stackable_2_and_n")) {
                layoutName = "2-and-n-columns";
            } else if (layout.getUri().equals("de.mikromedia.layout.accordion_styled")) {
                layoutName = "accordion-styled";
            }
        }
        return layoutName;
    }

    public Topic getPlacement() {
        return this.pageSection.getChildTopics().getTopicOrNull(SECTION_PLACEMENT);
    }

    public String getBackgroundColor() {
        return this.pageSection.getChildTopics().getStringOrNull(SECTION_BG_COLOR);
    }

    public String getFontColor() {
        return this.pageSection.getChildTopics().getStringOrNull(SECTION_COLOR);
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                .put("titl", getTitle())
                .put("contents", getContents())
                .put("layout", getLayout())
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
