AJS.$(document).ready(function () {
    AJS.$(".multi-select").auiSelect2();
});

function sendAjax() {

    var monitoredSpaceKeys = AJS.$("#monitoredSpaceKeys").val();
    var affectedGroups = AJS.$("#affectedGroups").val();
    var timeframe = AJS.$("#timeframe").val();

    AJS.$.ajax({
        url: AJS.contextPath() + '/plugins/servlet/pagesreview',
        type: 'POST',
        dataType: 'json',
        data: {
            spaceKeys: monitoredSpaceKeys,
            groups: affectedGroups,
            timeframe: timeframe
        },
        success: function (resp) {
            console.log(resp);
        },
        error: function(err) {
            console.log("ERROOOR")
            console.log(err);
        }

        /*error: function (data, status , err, er) {
            console.log("ERROOOR")
            alert("error: " + data + " status: " + status + " err:" + err);
            console.log(er);
        }*/
    });
}


