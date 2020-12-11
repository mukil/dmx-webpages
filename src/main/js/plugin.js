export default ({dmx, store, axios}) => {

  function isDraft() {
    return (store.state.object.children["de.mikromedia.page.is_draft"].value)
  }

  function getRelatedWebsites(topicId) {
    return dmx.rpc.getTopicRelatedTopics(topicId, {
      "assocTypeUri": "dmx.core.association",
      "othersTopicTypeUri": "de.mikromedia.site"
    })
  }

  return {
    contextCommands: {
      topic: topic => {
        if (topic.typeUri === 'de.mikromedia.site') {
          return [{
            label: 'Browse',
            handler: id => {
              dmx.rpc.getTopic(id, true)
                .then(function(response) {
                  var prefix = response.children["de.mikromedia.site.prefix"].value
                  var win = window.open('/' + prefix, '_blank')
                  win.focus()
                })
            }
          },
          {
            label: "Add Webpage",
            handler: id => {
              console.log("[Webpages] Add Webpage to Site", id)
            }
          },
          {
            label: "Add Section",
            handler: id => {
              console.log("[Webpages] Add Section to Site", id)
            }
          },
          {
            label: "Add Header",
            handler: id => {
              console.log("[Webpages] Add Header to Site", id)
            }
          }]
        } else if (topic.typeUri === 'de.mikromedia.page') {
          let commandLabel = isDraft() ? "View Draft" : "Browse"
          let connectedWebsites = getRelatedWebsites(topic.id)
          return [{
            label: commandLabel,
            handler: id => {
              /** axios.get("/webpage/path/" + id).then(function(response) {
                  var prefix = response.children["de.mikromedia.site.prefix"].value
                  var win = window.open('/' + prefix, '_blank')
                  win.focus()
                })**/
              console.log("Webpage", id, "Connected Websites", connectedWebsites)
            }
          },
          {
            label: "Website",
            handler: id => {
              console.log("[Webpages] Show Website of Webpage", id)
            }
          },
          {
            label: "Add Section",
            handler: id => {
              console.log("[Webpages] Add Section to Webpage", id)
            }
          },
          {
            label: "Add Header",
            handler: id => {
              console.log("[Webpages] Add Header to Webpage", id)
            }
          }]
        } else if (topic.typeUri === 'dmx.accesscontrol.username') {
          let user = store.state.accesscontrol.username
          return [{
            label: 'My Website',
            handler: id => {
              if (user) {
                // fetches website topic of user selected on map
                topic = axios.get("/webpages/" + user).then(function(response) {
                  store.dispatch("revealRelatedTopic", {relTopic: new dmx.Topic(response.data)})
                })
              } else {
                // fetches website topic of currently logged in user
                axios.get("/webpages").then(function(response) {
                  store.dispatch("revealRelatedTopic", {relTopic: new dmx.Topic(response.data)})
                })
              }
            }
          }]
        }
      }
    }
  }
}

/**
 * // dm4c.selected_object
 * // -> to inspect "children"
 * // dm4c.do_reveal_topic(id, "show")
 * // dm4c.do_reveal_related_topic(id, "show")
 * // dm4c.create_topic(typeUri)
 * // dm4c.create_assoc("dmx.core.association",
            {topic_id: webpage.id, role_type_uri: "dmx.core.default"},
            {topic_id: section.id, role_type_uri: "dmx.core.default"}
        )
        dm4c.show_topic(section, "edit", undefined, true) // do_center=true
        dm4c.show_association(assoc, "none")
  // dm4c.get_topic_related_topics(webpageId, {
            "assoc_type": "dmx.core.association",
            "others_topic_type_uri": "de.mikromedia.site"
     }, false)
 * function openInNewTab(url) {
 *   //
 * }
 * function notifyAboutMissingWebsiteAssoc() {
 *  this.$notify(
 *    message: 'To browse this webpage you need to associate it with a <em>Website</em>'
      // To reveal your personal website click here')
        var reveal = $('<a>').text('click here.')
        reveal.click(function(e) {
          show_personal_website()
        })
    }
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
**/