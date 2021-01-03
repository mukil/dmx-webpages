export default ({dmx, store, axios}) => {

  function isDraft(topic) {
    if (topic.children.hasOwnProperty("de.mikromedia.page.is_draft")) {
      return topic.children["de.mikromedia.page.is_draft"].value
    }
    return false
  }

  function getRelatedWebsites(topicId) {
    return dmx.rpc.getTopicRelatedTopics(topicId, {
      "assocTypeUri": "dmx.core.association",
      "othersTopicTypeUri": "de.mikromedia.site"
    })
  }

  function createAndRelate(topicBody, assocTypeUri, selectedTopic) {
    dmx.rpc.createTopic(topicBody).then(otherPlayer => {
      store.dispatch("revealTopic", {topic: otherPlayer, pos: undefined, noSelect: true})
      dmx.rpc.createAssoc({"typeUri":assocTypeUri, 
        "player1":{"roleTypeUri":"dmx.core.default","topicId":selectedTopic.id},
        "player2":{"roleTypeUri":"dmx.core.default","topicId":otherPlayer.id}
      }).then(assoc => {
        store.dispatch("revealAssoc", {assoc: assoc, noSelect: true})
      })
    })
  }
  
  const CONTENT_ASSOC_TYPE = "dmx.core.association"

  return {
    contextCommands: {
      topic: topic => {
        if (topic.typeUri === 'de.mikromedia.site') {
          return [{
            label: 'Browse',
            handler: id => {
              dmx.rpc.getTopic(id, true).then(function(response) {
                var prefix = response.children["de.mikromedia.site.prefix"].value
                var win = window.open('/' + prefix, '_blank')
                win.focus()
              })
            }
          },
          {
            label: "Add Page",
            handler: id => {
              createAndRelate({typeUri: "de.mikromedia.page",
                children: {"de.mikromedia.page.headline": "New Webpage"}},
                CONTENT_ASSOC_TYPE, topic)
            }
          }]
        } else if (topic.typeUri === 'de.mikromedia.page') {
          let commandLabel = isDraft(topic) ? "View Draft" : "Browse"
          return [{
            label: commandLabel,
            handler: id => {
              getRelatedWebsites(id).then(response => {
                if (response.length === 0) return
                dmx.rpc.getTopic(response[0].id, true).then(function(websiteTopic) {
                  dmx.rpc.getTopic(topic.id, true).then(function(pageTopic) {
                  var prefix = websiteTopic.children["de.mikromedia.site.prefix"].value
                  var pageUrl = '/' + prefix + '/' + pageTopic.children["de.mikromedia.page.web_alias"].value
                  var win = window.open(pageUrl, '_blank')
                  win.focus()
                })
              })
            })
          }},
          {
            label: "Add Section",
            handler: id => {
              createAndRelate({typeUri: "de.mikromedia.section", children: 
                {"de.mikromedia.section.title": "New Section: " + Math.floor((Math.random() * 10000) + 1), 
                  "de.mikromedia.section.layout": "ref_uri:de.mikromedia.layout.single_tile",
                  "de.mikromedia.section.placement": "ref_uri:de.mikromedia.placement.above"
                }}, CONTENT_ASSOC_TYPE, topic)
            }
          },
          { // ### Fixme: Avoid that users accidentially create more then 1 header
            label: "Add Header",
            handler: id => {
              createAndRelate({typeUri: "de.mikromedia.header",
                children: {"de.mikromedia.header.title": "New Header: " + Math.floor((Math.random() * 10000) + 1)
              }}, CONTENT_ASSOC_TYPE, topic)
            }
          }]
        } else if (topic.typeUri === 'dmx.accesscontrol.username') {
          let user = (topic.value === store.state.accesscontrol.username)
          // Todo: Show "My Website" is username is "me"
          return (user) ? [{
            label: 'My Website',
            handler: id => {{
              // fetches website topic of user selected on map
              topic = axios.get("/webpages/" + topic.value).then(function(response) {
                if (response.data) {
                  store.dispatch("revealRelatedTopic", {relTopic: new dmx.Topic(response.data)})
                }
              })
            }
          }}] : []
        }
      }
    }
  }
}

/**
 * TODO: notifiyAboutMisingWebsiteAssoc, openInNewTab, getRelatedWebsite, getWebsitePrefix, showRelatedWebsite
**/
