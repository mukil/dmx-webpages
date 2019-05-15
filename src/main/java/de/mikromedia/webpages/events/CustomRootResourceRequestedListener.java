package de.mikromedia.webpages.events;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.EventListener;
import javax.ws.rs.core.UriInfo;
import org.thymeleaf.context.AbstractContext;

/**
 *
 * @author malte
 */
public interface CustomRootResourceRequestedListener extends EventListener {

    void frontpageRequested(AbstractContext context, Topic website, String name, UriInfo uriInfo);

}
