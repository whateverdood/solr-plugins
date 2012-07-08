$(document).ready(function() {
    $(".fieldRevealer").click(function(e) {
        // unreveal/turn-off highlighting for anything that's currently lit up
        $(".hit").toggleClass("hit")
        // reveal/highlight 
        var hitClass = $(this).attr("id") + "_hit"
        $("." + hitClass).toggleClass("hit")
    })
})