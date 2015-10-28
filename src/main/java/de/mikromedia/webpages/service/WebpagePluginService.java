package de.mikromedia.webpages.service;

import de.deepamehta.core.service.PluginService;

public interface WebpagePluginService extends PluginService {
	
	void setFrontpageResource(String frontpageResourceName, String bundleContextUri);
	
}
