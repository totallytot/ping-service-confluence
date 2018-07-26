AJS.$(document).ready(function () {
    AJS.$(".multi-select").auiSelect2();
});

function sendAjax() {
    var dataObject = new Object();
    dataObject.keys = AJS.$("#monitoredSpaceKeys").val();
    dataObject.groups = AJS.$("#affectedGroups").val();
    dataObject.timeframe = AJS.$("#timeframe").val();

    console.log(JSON.stringify(dataObject));

    AJS.$.ajax({
        url: AJS.contextPath() + '/plugins/servlet/pagesreview',
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(dataObject),
        success: function (resp) {
            console.log(resp);
        },
        error: function(err) {
            console.log("ERROR");
            console.log(err);
        }
    });
}


