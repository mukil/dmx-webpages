    
dm4c.add_simple_renderer("de.mikromedia.page.headline_renderer", {

    render_info: function(page_model, parent_element) {
        dm4c.render.field_label(page_model, parent_element)
        var text = js.render_text(page_model.value)
        parent_element.append(text)
    },

    render_form: function(page_model, parent_element) {
        dm4c.render.field_label(page_model, parent_element)
        var form_element = dm4c.render.form_element(page_model, parent_element)
        $(form_element[0]).addClass("headline")
        return function() {
            return form_element[0].value
        }
    }
})
