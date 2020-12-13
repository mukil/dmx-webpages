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

  function createAndRelate(othersTypeUri, assocTypeUri, topic) {
    dmx.rpc.createTopic(othersTypeUri).then(topic => {
      console.log("Created", topic)
        var webpage = topic
        /** dmx.createAssoc(assocTypeUri)
        var assoc = dm4c.create_association("dmx.core.association",
            {topic_id: webpage.id, role_type_uri: "dmx.core.default"},
            {topic_id: section.id, role_type_uri: "dmx.core.default"}
        )
        dm4c.show_topic(section, "edit", undefined, true) // do_center=true
        dm4c.show_association(assoc, "none") **/
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
              createAndRelate("de.mikromedia.webpage", undefined, topic)
            }
          },
          {
            label: "Add Section",
            handler: id => {
              console.log("[Webpages] Add Section to Site", id)
              createAndRelate("de.mikromedia.section", undefined, topic)
            }
          },
          {
            label: "Add Header",
            handler: id => {
              console.log("[Webpages] Add Header to Site", id)
              createAndRelate("de.mikromedia.header", undefined, topic)
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
              console.log("[Webpages] TODO: Show Website of Webpage", id)
            }
          },
          {
            label: "Add Section",
            handler: id => {
              console.log("[Webpages] Add Section to Webpage", id)
              createAndRelate("de.mikromedia.section", undefined, topic)
            }
          },
          {
            label: "Add Header",
            handler: id => {
              console.log("[Webpages] Add Header to Webpage", id)
              createAndRelate("de.mikromedia.header", undefined, topic)
            }
          }]
        } else if (topic.typeUri === 'dmx.accesscontrol.username') {
          let user = store.state.accesscontrol.username
          // Todo: Show "My Website" is username is "me"
          return [{
            label: 'Website',
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
 * TODO: notifiyAboutMisingWebsiteAssoc, openInNewTab, getRelatedWebsite, getWebsitePrefix, showRelatedWebsite
**/
