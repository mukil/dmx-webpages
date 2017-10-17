
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function show_search_options() {
    console.log("### todo: show search options")
}

function render_page() {
    $('.ui.dropdown').dropdown()
    $('.right.menu .ui.search').search({
            apiSettings: {
                url: '/websites/search?q={query}',
                onRequest: function() {
                    $('.right.menu .ui.category.search').addClass("loading")
                    $('.right.menu .ui.category.search .icon.my').show()
                },
                onResponse: function() {
                    $('.right.menu .ui.category.search').removeClass("loading")
                    $('.right.menu .ui.category.search .icon.my').hide()
                }
            },
            fields: {
                results : 'results',
                title   : 'name',
                description: 'zusatz',
                url     : 'link'
            },
            minCharacters : 2,
            type: 'category'
        })
        /**             error : {
                source      : 'Fehler im Setup des JavaScript-Suchmoduls.',
                noResults   : 'Wir konnten zu dieser Suchanfrage keine Ergebnisse finden.',
                serverError : 'Bei der Verarbeitung dieser Suchanfarge ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut.'
            }, **/
}

function register_input_handler() {
    var searchInput = document.getElementById("text-search")
    if (searchInput) {
        searchInput.addEventListener('keyup', function (e) {
            if (e.target.value.length >= 3 && e.keyCode === 13) {
                do_fulltext_search()
            }
        })
    }
}

function do_fulltext_search() {
    var siteId = parseInt(document.getElementById('siteId').getAttribute("content"))
    var userQuery = document.getElementById("text-search").value
    // 
    console.log("Let's fulltext search for \"" + userQuery + "\" across webpages of site", siteId)
}

function page_init() {
    register_input_handler()
}

