package de.mikromedia.webpages.events;

import systems.dmx.core.service.EventListener;
import javax.ws.rs.core.UriInfo;
import org.thymeleaf.context.AbstractContext;
import systems.dmx.core.Topic;

/**
 *
 * @author malte
 */
public interface CustomRootResourceRequestedListener extends EventListener {

    void frontpageRequested(AbstractContext context, Topic website, String name, UriInfo uriInfo);

}
