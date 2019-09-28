package de.mikromedia.webpages.events;

import systems.dmx.core.service.EventListener;
import org.thymeleaf.context.AbstractContext;

/**
 *
 * @author malte
 */
public interface ResourceNotFoundListener extends EventListener {
    
    void resourceNotFound(AbstractContext context, String webAlias, String sitePrefix);

}
