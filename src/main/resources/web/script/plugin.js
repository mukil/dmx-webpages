dm4c.add_plugin("de.mikromedia.webpages", function() {

    function show_personal_website() {
        var topic = dm4c.restc.request("GET", "/website/" + dm4c.selected_object.childs["dm4.accesscontrol.username"].value)
        dm4c.do_reveal_topic(topic.id, "show")
    }

    function browse_website() {
        window.document.location.assign("/browse/" + dm4c.selected_object.id)
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
        }
        return commands
    })

})
