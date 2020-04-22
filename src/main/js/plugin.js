export default ({dm5, axios}) => ({

  contextCommands: {
    topic: topic => {
      if (topic.typeUri === 'de.mikromedia.site') {
        return [{
          label: 'Browse',
          handler: id => {
            dm5.restClient.getTopic(id, true)
              .then(function(response) {
                var prefix = response.children["de.mikromedia.site.prefix"].value
                var win = window.open('/' + prefix, '_blank')
                win.focus()
              })
          }
        }]
      }
    }
  }

})

/**
 * if (topic.type_uri === 'dmx.accesscontrol.user_account' && dm4c.restc.get_username()) {
            commands.push({is_separator: true, context: 'context-menu'})
            commands.push({
                label: 'My Website',
                handler: show_personal_website,
                context: ['context-menu', 'detail-panel-show']
            })
        } else if (topic.type_uri === 'de.mikromedia.site') {
            commands.push({is_separator: true, context: 'context-menu'})
            commands.push({
                label: 'Add Header',
                handler: add_webpage_header,
                context: ['context-menu', 'detail-panel-show']
            })
            commands.push({
                label: 'Add Section',
                handler: add_webpage_section,
                context: ['context-menu', 'detail-panel-show']
            })
            commands.push({
                label: 'Add Webpage',
                handler: add_webpage,
                context: ['context-menu', 'detail-panel-show']
            })
        } else if (topic.type_uri === 'de.mikromedia.page') {
            connected_websites = get_related_website(topic.id)
            if (connected_websites && connected_websites.length > 0) {
                var button_label = (webpage_is_draft()) ? "View Draft" : "Browse"
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: button_label,
                    handler: browse_webpage,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: 'Add Section',
                    handler: add_webpage_section,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({
                    label: 'Add Header',
                    handler: add_webpage_header,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: 'Website',
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
 */
/** dm4c.add_plugin("de.mikromedia.webpages", function() {

    var connected_websites = undefined

    function show_personal_website() {
        var topic = undefined
        if (dm4c.selected_object.children.hasOwnProperty("dmx.accesscontrol.username")) {
            // fetches website topic of user selected on map
            topic = dm4c.restc.request("GET", "/webpages/" + dm4c.selected_object.children["dmx.accesscontrol.username"].value)
        } else {
            // fetches website topic of currently logged in user
            topic = dm4c.restc.request("GET", "/webpages")
        }
        dm4c.do_reveal_topic(topic.id, "show")
    }

    function show_related_website() {
        dm4c.do_reveal_related_topic(connected_websites[0].id, "show")
    }

    function browse_website() {
        var prefix = dm4c.selected_object.children["de.mikromedia.site.prefix"].value
        open_in_new_tab('/' + prefix)
    }

    function webpage_is_draft() {
        if (dm4c.selected_object.children.hasOwnProperty("de.mikromedia.page.is_draft")) {
            return dm4c.selected_object.children["de.mikromedia.page.is_draft"].value
        } else {
            return true
        }
    }

    function add_webpage() {
        var website = dm4c.selected_object
        var webpage = dm4c.create_topic("de.mikromedia.page")
        var assoc = dm4c.create_association("dmx.core.association",
            {topic_id: website.id, role_type_uri: "dmx.core.default"},
            {topic_id: webpage.id, role_type_uri: "dmx.core.default"}
        )
        dm4c.show_topic(webpage, "edit", undefined, true) // do_center=true
        dm4c.show_association(assoc, "none")
    }

    function add_webpage_section() {
        var webpage = dm4c.selected_object
        var section = dm4c.create_topic("de.mikromedia.section")
        var assoc = dm4c.create_association("dmx.core.association",
            {topic_id: webpage.id, role_type_uri: "dmx.core.default"},
            {topic_id: section.id, role_type_uri: "dmx.core.default"}
        )
        dm4c.show_topic(section, "edit", undefined, true) // do_center=true
        dm4c.show_association(assoc, "none")
    }

    function add_webpage_header() {
        var webpage = dm4c.selected_object
        var header = dm4c.create_topic("de.mikromedia.header")
        var assoc = dm4c.create_association("dmx.core.association",
            {topic_id: webpage.id, role_type_uri: "dmx.core.default"},
            {topic_id: header.id, role_type_uri: "dmx.core.default"}
        )
        dm4c.show_topic(header, "edit", undefined, true) // do_center=true
        dm4c.show_association(assoc, "none")
    }

    function browse_webpage() {
        var url = "/"
        if (connected_websites && connected_websites.length > 0) {
            var websiteOne = connected_websites[0]
            if (websiteOne.uri === "de.mikromedia.standard_site") {
                url += dm4c.selected_object.children["de.mikromedia.page.web_alias"].value
            } else { // user site
                var prefix = get_website_prefix(websiteOne)
                url += prefix + "/" + dm4c.selected_object.children["de.mikromedia.page.web_alias"].value
            }
            open_in_new_tab(url)
        } else {
            console.warn("Webpage is not connected to a website, cannot construct its URL for browsing")
        }
    }



    function get_related_website(webpageId) {
        return dm4c.restc.get_topic_related_topics(webpageId, {
            "assoc_type": "dmx.core.association",
            "others_topic_type_uri": "de.mikromedia.site"
        }, false)
    }

    function get_website_prefix(topic) {
        var website =  dm4c.restc.get_topic_by_id(topic.id, true, false)
        return website.children["de.mikromedia.site.prefix"].value
    }

    dm4c.add_listener('topic_commands', function (topic) {

        // Note: create permission now managed by core
        var commands = []
        if (topic.type_uri === 'dmx.accesscontrol.user_account' && dm4c.restc.get_username()) {
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
            commands.push({
                label: 'Add Header',
                handler: add_webpage_header,
                context: ['context-menu', 'detail-panel-show']
            })
            commands.push({
                label: 'Add Section',
                handler: add_webpage_section,
                context: ['context-menu', 'detail-panel-show']
            })
            commands.push({
                label: 'Add Webpage',
                handler: add_webpage,
                context: ['context-menu', 'detail-panel-show']
            })
        } else if (topic.type_uri === 'de.mikromedia.page') {
            connected_websites = get_related_website(topic.id)
            if (connected_websites && connected_websites.length > 0) {
                var button_label = (webpage_is_draft()) ? "View Draft" : "Browse"
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: button_label,
                    handler: browse_webpage,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: 'Add Section',
                    handler: add_webpage_section,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({
                    label: 'Add Header',
                    handler: add_webpage_header,
                    context: ['context-menu', 'detail-panel-show']
                })
                commands.push({is_separator: true, context: 'context-menu'})
                commands.push({
                    label: 'Website',
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

**/