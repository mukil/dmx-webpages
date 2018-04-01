package de.mikromedia.webpages.events;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.EventListener;
import org.thymeleaf.context.AbstractContext;

/**
 *
 * @author malte
 */
public interface FrontpageRequestedListener extends EventListener {
    
    void frontpageRequested(AbstractContext context, Topic website, String webAlias);

}
