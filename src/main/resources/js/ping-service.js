AJS.$(document).ready(function () {
    AJS.$(".multi-select").auiSelect2();
});

function sendAjax() {

    /*
    var monitoredSpaceKeys = AJS.$("#monitoredSpaceKeys").val();
    var affectedGroups = AJS.$("#affectedGroups").val();
    var timeframe = AJS.$("#timeframe").val();
    */

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
            //{
            //spaceKeys: monitoredSpaceKeys,
            //groups: affectedGroups,
            //timeframe: timeframe
        //},
        success: function (resp) {
            console.log(resp);

        },

        error: function(err) {
            console.log("ERROOOR");
            console.log(err);
        }

        /*
        error: function (data, status , er) {
            console.log("ERROOOR");
            alert("error: " + data + " status: " + status + " er:" + er);
            console.log(er);
        }
        */
    });
}


