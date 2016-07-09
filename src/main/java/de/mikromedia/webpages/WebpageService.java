package de.mikromedia.webpages;

import com.sun.jersey.api.view.Viewable;
import org.osgi.framework.Bundle;

public interface WebpageService {

    /**
     * IMPORTANT: If you register your own bundle as a resource for thymeleaf templates you must call
     * reinitTemplateEngine afterwards.
     */
    void addTemplateResolverBundle(Bundle bundle);

    void removeTemplateResolverBundle(Bundle bundle);

    void reinitTemplateEngine();

    void overrideFrontpageTemplate(String templateName);

    void viewData(String key, Object value);

    Viewable view(String templateName);

}
