AJS.$(document).ready(function () {

    AJS.$(".multi-select").auiSelect2();

    var dataObject = {};
    dataObject.keysToAdd = [];
    dataObject.keysToDel = [];
    dataObject.groupsToAdd = [];
    dataObject.groupsToDel = [];

    AJS.$(document).on('change', '#monitoredSpaceKeys', function(e) {
        if (e.added) {
            console.log('add ' + e.added.id);
            if (dataObject.keysToDel.includes(e.added.id)) dataObject.keysToDel.splice(dataObject.keysToDel.indexOf(e.added.id), 1);
            else dataObject.keysToAdd.push(e.added.id);
        }
        if (e.removed) {
            if (dataObject.keysToAdd.includes(e.removed.id)) dataObject.keysToAdd.splice(dataObject.keysToAdd.indexOf(e.removed.id), 1);
            else dataObject.keysToDel.push(e.removed.id);
            console.log('remove ' + e.removed.id);
        }
    });

    AJS.$(document).on('change', '#affectedGroups', function(e) {
        if (e.added) {
            console.log('add ' + e.added.id);
            if (dataObject.groupsToDel.includes(e.added.id)) dataObject.groupsToDel.splice(dataObject.groupsToDel.indexOf(e.added.id), 1);
            else dataObject.groupsToAdd.push(e.added.id);

        }
        if (e.removed) {
            if (dataObject.groupsToAdd.includes(e.removed.id)) dataObject.groupsToAdd.splice(dataObject.groupsToAdd.indexOf(e.removed.id), 1);
            else dataObject.groupsToDel.push(e.removed.id);
            console.log('remove ' + e.removed.id);
        }
    });

    AJS.$("#timeframe").change(function () {
        dataObject.timeframe = AJS.$("#timeframe").val();
    });

    AJS.$("#save-button").click(function (e){
        e.preventDefault();

        console.log(JSON.stringify(dataObject));

        AJS.$.ajax({
            url: AJS.contextPath() + '/plugins/servlet/pagesreview',
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(dataObject),
            success: function (resp) {
                console.log("SUCCESS");
                console.log(resp);
            },
            error: function(err) {
                console.log("ERROR");
                console.log(err);
            }
        });
    })
});