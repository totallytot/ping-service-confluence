AJS.toInit(function () {
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

    AJS.$("#mail-sbj").change(function () {
        dataObject.mailSubject = AJS.$("#mail-sbj").val();
    });

    AJS.$("mail-textarea-id").change(function () {
        dataObject.mailBody = AJS.$("#mail-textarea-id").val();
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
                var successFlag = AJS.flag({
                    type: 'success',
                    body: 'Job has been configured.',
                });
            },
            error: function(err) {
                console.log("ERROR");
                console.log(err);
            }
        });
    });

    AJS.$("#clear-button").click(function () {
        AJS.$("#mail-textarea-id").val("");
        AJS.$("#mail-sbj").val("");
    });

    AJS.$("#default-button").click(function () {
        var template = "<html><body>Dear $creator,<br>\n" +
            "<br>Could you please take a look at the pages below. You are the owner of them, but looks like their content wasn't updated for a while $days day(s)):<br>\n"
            +"$links\n"+
            "</body></html>\n";
        var subject = "Notification: It's time to review your pages";
        AJS.$("#mail-textarea-id").val(template);
        AJS.$("#mail-sbj").val(subject);
    });

});