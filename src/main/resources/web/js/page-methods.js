
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function register_input_handler() {
    document.getElementById("text-search").addEventListener('keyup', function(e) {
        if (e.target.value.length >= 3 && e.keyCode === 13) {
            do_fulltext_search()
        }
    })
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

