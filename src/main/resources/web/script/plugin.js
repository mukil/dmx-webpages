dm4c.add_plugin("de.mikromedia.webpages", function() {

    var connected_websites = undefined

    function show_personal_website() {
        var topic = dm4c.restc.request("GET", "/website/" + dm4c.selected_object.childs["dm4.accesscontrol.username"].value)
        dm4c.do_reveal_topic(topic.id, "show")
    }

    function browse_website() {
        window.document.location.assign("/browse/" + dm4c.selected_object.id)
    }

    function webpage_is_draft() {
        return dm4c.selected_object.childs["de.mikromedia.page.is_draft"].value
    }

    function browse_webpage() {
        var url = "/"
        if (connected_websites && connected_websites.length > 0) {
            if (connected_websites[0].uri === "de.mikromedia.standard_site") {
                url += dm4c.selected_object.childs["de.mikromedia.page.web_alias"].value
                console.log("Browse Webpage of Website related to Standard Site", url)
            } else {
                var usernames = get_related_username(connected_websites[0].id)
                url += dm4c.restc.get_username() + "/"
                    + dm4c.selected_object.childs["de.mikromedia.page.web_alias"].value
                console.log("Browse Webpage of Website of Username", url)
            }
            // ### fetch url prefix of webpage related website
            window.document.location.assign(url)
        } else {
            console.warn("Webpage is not connected to a website, cannot construct its URL for browsing")
        }
    }

    function get_related_username(id) {
        return dm4c.restc.get_topic_related_topics(id, {
            "assoc_type": "dm4.core.association",
            "others_topic_type_uri": "dm4.accesscontrol.username"
        }, false)
    }

    function get_related_website(webpageId) {
        return dm4c.restc.get_topic_related_topics(webpageId, {
            "assoc_type": "dm4.core.association",
            "others_topic_type_uri": "de.mikromedia.site"
        }, false)
    }

    dm4c.add_listener('topic_commands', function (topic) {

        // Note: create permission now managed by core
        var commands = []
        if (topic.type_uri === 'dm4.accesscontrol.user_account' && dm4c.restc.get_username()) {
            commands.push({is_separator: true, context: 'context-menu'})
            commands.push({
                label: 'My Website',
                handler: show_personal_website,
                context: ['context-menu', 'detail-panel-show']
            })
        } else if (topic.type_uri === 'de.mikromedia.site') {
            commands.push({is_separator: true, context: 'context-menu'})
            commands.push({
                label: 'Browse',
                handler: browse_website,
                context: ['context-menu', 'detail-panel-show']
            })
        } else if (topic.type_uri === 'de.mikromedia.page') {
            connected_websites = get_related_website(topic.id)
            if (connected_websites && connected_websites.length > 0) {
                var button_label = (webpage_is_draft()) ? "Browse Draft" : "Browse"
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: button_label,
                    handler: browse_webpage,
                    context: ['context-menu', 'detail-panel-show']
                })
            } else {
                console.log("No website connected, no topic command to render")
            }
        }
        return commands
    })

})
