AJS.toInit(function () {
    AJS.$(".multi-select").auiSelect2();
    var dataObject = {};
    dataObject.keysToAdd = [];
    dataObject.keysToDel = [];
    dataObject.groupsToAdd = [];
    dataObject.groupsToDel = [];

    AJS.$(document).on('change', '#monitoredSpaceKeys', function (e) {
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

    AJS.$(document).on('change', '#affectedGroups', function (e) {
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

    AJS.$("#save-button").click(function (e) {
        e.preventDefault();
        var mailBody = AJS.$("#mail-textarea-id").val();
        var mailSubject = AJS.$("#mail-sbj").val();
        if (mailBody === null || mailBody.length === 0 || mailSubject === null || mailSubject.length === 0) {
            AJS.flag({
                type: 'error',
                body: 'Please populate mail subject and body!',
                close: "auto"
            });
        }
        else {
            dataObject.mailBody = mailBody;
            dataObject.mailSubject = mailSubject;
            AJS.$.ajax({
                url: AJS.contextPath() + '/plugins/servlet/pagesreview',
                type: 'POST',
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(dataObject),
                success: function (resp) {
                    console.log("SUCCESS");
                    console.log(resp);
                    AJS.flag({
                        type: 'success',
                        body: 'Job has been configured.',
                        close: "auto"
                    });
                },
                error: function (err) {
                    console.log("ERROR");
                    console.log(err);
                    AJS.flag({
                        type: 'error',
                        body: 'Something went wrong! Check logs!',
                        close: "auto"
                    });
                }
            });
        }
    });

    AJS.$("#clear-button").click(function () {
        AJS.$("#mail-textarea-id").val("");
        AJS.$("#mail-sbj").val("");
    });

    AJS.$("#default-button").click(function () {
        // language=HTML
        var defaultTemplate = "<html><body>Dear $creator,<br>\n<br>Could you please take a look at the pages below. You are the owner of them, but looks like their content wasn't updated for a while $days day(s)):<br>\n$links\n</body></html>\n";
        var defaulTsubject = "Notification: It's time to review your pages";
        AJS.$("#mail-textarea-id").val(defaultTemplate);
        AJS.$("#mail-sbj").val(defaulTsubject);
    });

});