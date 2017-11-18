package de.mikromedia.webpages.events;

import de.deepamehta.core.service.EventListener;
import de.mikromedia.webpages.model.Webpage;

/**
 *
 * @author malte
 */
public interface WebpageRequestedListener extends EventListener {
    
    void webpageRequested(Webpage webpage, String sitePrefix);

}
