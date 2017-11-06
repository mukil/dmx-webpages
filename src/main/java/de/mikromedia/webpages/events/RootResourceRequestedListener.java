package de.mikromedia.webpages.events;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.EventListener;

/**
 *
 * @author malte
 */
public interface RootResourceRequestedListener extends EventListener {
    
    void frontpageRequested(Topic website);

}
