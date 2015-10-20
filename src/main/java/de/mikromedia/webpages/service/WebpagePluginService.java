package de.mikromedia.webpages.service;

import de.deepamehta.core.service.PluginService;

/**
 *
 * @author malte
 */
public interface WebpagePluginService extends PluginService {
	
	void setFrontpageResource(String frontpageResourceName, String bundleContextUri);
	
}
