package de.mikromedia.webpages.events;

import de.deepamehta.core.service.EventListener;
import org.thymeleaf.context.AbstractContext;

/**
 *
 * @author malte
 */
public interface ResourceNotFoundListener extends EventListener {
    
    void resourceNotFound(AbstractContext context, String webAlias, String sitePrefix);

}
