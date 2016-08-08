dm4c.add_plugin("de.mikromedia.webpages", function() {

    var connected_websites = undefined

    function show_personal_website() {
        var topic = undefined
        if (dm4c.selected_object.childs.hasOwnProperty("dm4.accesscontrol.username")) {
            topic = dm4c.restc.request("GET", "/website/" + dm4c.selected_object.childs["dm4.accesscontrol.username"].value)
        } else {
            topic = dm4c.restc.request("GET", "/website")
        }
        dm4c.do_reveal_topic(topic.id, "show")
    }

    function show_related_website() {
        dm4c.do_reveal_related_topic(connected_websites[0].id, "show")
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
                url += usernames[0].value + "/" + dm4c.selected_object.childs["de.mikromedia.page.web_alias"].value
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
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: 'Reveal Website',
                    handler: show_related_website,
                    context: ['context-menu', 'detail-panel-show']
                })
            } else {
                if ($('.page-message.hint').length === 0 && $('#page-content input').length === 0) {
                    var $label = $('<div class="page-message hint">')
                        $label.append('To browse this webpage you need to associate it with a '
                            + '<em>Website</em>.<br/>To reveal your personal website ')
                    var reveal = $('<a>').text('click here.')
                        reveal.click(function(e) {
                            show_personal_website()
                            $label.remove()
                        })
                        $label.append(reveal)
                    var $close = $('<a>').text("X").addClass("close").attr("title", "Hide this message")
                        $close.click(function(e) {
                            $label.remove()
                        })
                        $label.append($close)
                    $label.insertBefore('#page-toolbar')
                    setTimeout(function(e) {
                        $label.remove()
                    }, 4200)
                }
            }
        }
        return commands
    })

})
