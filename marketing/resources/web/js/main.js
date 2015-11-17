(function() {
    $("button").click(function() {
        $.ajax("/", {
            contentType: "application/json",
            data: JSON.stringify({email: $("#email").val()}),
            method: "POST"
        });
        $("#marketing").html("<div>Спасибо вам большое!</div>");
    });
})();


