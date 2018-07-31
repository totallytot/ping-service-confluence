AJS.$(document).ready(function () {

    AJS.$(".multi-select").auiSelect2();

    var dataObject = new Object();
    dataObject.keysToAdd = new Set();
    dataObject.keysToDel = new Set();
    dataObject.groupsToAdd = new Set();
    dataObject.groupsToDel = new Set();

    var keys = AJS.$('#monitoredSpaceKeys');
    var valKeys = (keys.val()) ? keys.val() : [];
    keys.change(function(){

        var val = $(this).val(),
            numVals = (val) ? val.length : 0,
            changes;

        if (numVals != valKeys.length)
        {
            var longerSet, shortSet;
            (numVals > valKeys.length) ? longerSet = val : longerSet = valKeys;
            (numVals > valKeys.length) ? shortSet = valKeys : shortSet = val;
            //create array of values that changed - either added or removed
            changes = $.grep(longerSet, function(n) {
                return $.inArray(n, shortSet) == -1;
            });
            addChanges(changes, (numVals > valKeys.length) ? 'key selected' : 'key removed');
        }
        else
        {
            // if change event occurs and previous array length same as new value array : items are removed and added at same time
            addChanges(valKeys, 'key removed');
            addChanges(val, 'key selected');
        }
        valKeys = (val) ? val : [];
    });

    var groups = AJS.$('#affectedGroups');
    var valGroups = (groups.val()) ? groups.val() : [];
    groups.change(function(){

        var val = $(this).val(),
            numVals = (val) ? val.length : 0,
            changes;

        if (numVals != valGroups.length)
        {
            var longerSet, shortSet;
            (numVals > valGroups.length) ? longerSet = val : longerSet = valGroups;
            (numVals > valGroups.length) ? shortSet = valGroups : shortSet = val;
            //create array of values that changed - either added or removed
            changes = $.grep(longerSet, function(n) {
                return $.inArray(n, shortSet) == -1;
            });
            addChanges(changes, (numVals > valGroups.length) ? 'group selected' : 'group removed');
        }
        else
        {
            // if change event occurs and previous array length same as new value array : items are removed and added at same time
            addChanges(valGroups, 'group removed');
            addChanges(val, 'group selected');
        }
        valGroups = (val) ? val : [];
    });

    function addChanges(array, type) {
        var value = array[0];

        if (type == "key selected")
        {
            if (dataObject.keysToDel.has(value)) dataObject.keysToDel.delete(value);
            else dataObject.keysToAdd.add(value);
        }
        else if (type == "key removed")
        {
            if (dataObject.keysToAdd.has(value)) dataObject.keysToAdd.delete(value);
            else dataObject.keysToDel.add(value);
        }
        else if (type == "group selected")
        {
            if (dataObject.groupsToDel.has(value)) dataObject.groupsToDel.delete(value);
            else dataObject.groupsToAdd.add(value);
        }
        else if (type == "group removed")
        {
            if (dataObject.groupsToAdd.has(value)) dataObject.groupsToAdd.delete(value);
            else dataObject.groupsToDel.add(value);

        }
    }

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


