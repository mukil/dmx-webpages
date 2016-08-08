
function transform_title_to_url(value) {
    // removes reserved characters and german special characters from url
    result = value.replace(/\ /g, "-").replace(/-/g, "-")
        .replace(/&/g, "-").replace(/:/g, "-").replace(/=/g, "-")
        .replace(/\./g, "-").replace(/\?/g, "-")
        .replace(/\(/g, "-").replace(/\)/g, "-")
        .replace(/\:/g, "-").replace(/\+/g, "-")
        .replace(/\!/g, "-").replace(/\*/g, "-")
        .replace(/\'/g, "-").replace(/\//g, "-")
        .replace(/\\/g, "").replace(/@/g, "")
        .replace(/#/g, "").replace(/$/g, "")
        .replace(/\"/g, "").replace(/\[/g, "-").replace(/\]/g, "-")
        .replace(/\{/g, "-").replace(/\}/g, "-")
        .replace(/§/g, "-").replace(/\%/g, "-")
        .replace(/ä/g, "ae").replace(/ö/g, "oe")
        .replace(/ü/g, "ue").replace(/ß/g, "ss").replace(/\--/g, "-").toLowerCase()
    if (result.length === (result.lastIndexOf("-") + 1)) result = result.substr(0, result.length - 1)
    return result
}

dm4c.add_simple_renderer("de.mikromedia.page.web_alias_renderer", {

    render_info: function(page_model, parent_element) {
        dm4c.render.field_label(page_model, parent_element)
        var text = js.render_text(page_model.value)
        if (page_model.input_field_rows > 1) {
            text = $("<p>").append(text)
        }
        parent_element.append(text)
    },

    render_form: function(page_model, parent_element) {
        // 0) standard rendering of existing value in a disabled form element
        dm4c.render.field_label(page_model, parent_element)
        var $disabled = $("<input>").attr("type", "text").addClass("web-alias").attr("disabled", "true").val(page_model.value)
        parent_element.append($disabled)
        // 1) Deduce INITIAL Web Alias from headline valeu
        var manual_change = false
        if (page_model.value.length === 0) {
            return function() {
                var headline = $('input.headline').val()
                console.log("Initial Web Alias Calculation by Headline", headline)
                console.log("Initial Web Alias Calculation by Headline By PageCSS", $('de\.mikromedia\.page\.headline input'))
                var webalias = transform_title_to_url(headline)
                console.log("Initial Web Alias", webalias)
                return webalias
            }
        // 2) either render a "change" command (if some web alias exist)" or render nothing OR
        } else {
            var $unlock = $('<a href="#edit-url" class="field-label button">Change</a>')
                $unlock.click(function(e) {
                    $disabled.removeAttr("disabled")
                    manual_change = true
                    e.target.remove()
                })
            parent_element.append($unlock)
            return function() {
                if (manual_change) {
                    var changedWebAlias = transform_title_to_url($disabled.val())
                    console.log("Returning changed value", changedWebAlias)
                    return changedWebAlias
                } else {
                    return transform_title_to_url(page_model.value)
                }
            }
        }
    }
})
