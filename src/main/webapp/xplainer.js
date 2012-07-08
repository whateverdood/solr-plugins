$(document).ready(function() {

    $("#results").height(($(window).height() - $("#viz").height() - $(".header").height() - 10) + "px");
    
    $(window).resize(function() {
        $("#results").height(($(window).height() - $("#viz").height() - $(".lhs.header").height() - 10) + "px");
    });
    
    $(".fieldRevealer").click(function(e) {
        // unreveal/turn-off highlighting for anything that's currently lit up
        $(".hit").toggleClass("hit");
        // reveal/highlight 
        var hitClass = $(this).attr("id") + "_hit";
        $("." + hitClass).toggleClass("hit");
        var offset = $("." + hitClass).first().offset();
        $(".visualization").animate({scrollTop: offset.top});
    });
});