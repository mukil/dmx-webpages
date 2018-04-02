
function show_search_options() {
    console.log("### todo: show search options")
}

function visit_page(url) {
    console.log("Visit URL", url, "History Enabled", window.history)
    /** if (window.history) {
        history.pushState(undefined, undefined, url);
        window.location.reload()
    } else { **/
        window.location.href = url
    // }
}

function render_page() {
    // fix menu when passed
    $('.masthead').visibility({
        once: false,
        onBottomPassed: function () {
            $('.fixed.menu').transition('fade in');
        },
        onBottomPassedReverse: function () {
            $('.fixed.menu').transition('fade out');
        }
    })
    // pro-actively create sidebar and attach to menu open
    $('.ui.sidebar').sidebar('attach events', '.toc.item')
    // pro-actively activate any dropdown menu items
    $('.ui.dropdown').dropdown()
    // configure search input fields
    var searchConfig = {
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
    }
    $('.right.menu .ui.search').search(searchConfig)
    $('.sidebar.menu .ui.search').search(searchConfig)
        /**             error : {
                source      : 'Fehler im Setup des JavaScript-Suchmoduls.',
                noResults   : 'Wir konnten zu dieser Suchanfrage keine Ergebnisse finden.',
                serverError : 'Bei der Verarbeitung dieser Suchanfarge ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut.'
            }, **/
    // pro-actively activate add table styles to all pages
    $('table').addClass('ui celled table')
    // pro-actively activate accordion interactions on any page
    $('.ui.accordion').accordion()
    $('#text-search').focus()
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