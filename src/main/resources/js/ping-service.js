AJS.$(document).ready(function () {

    AJS.$(".multi-select").auiSelect2();

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
            console.log(err);
        }
    });

});
