package com.bstrctlmnt.servlet;

import java.util.List;

public class JsonDataObject {

    private List<String> keysToAdd;

    private List<String> keysToDel;

    private List<String> groupsToAdd;

    private List<String> groupsToDel;

    private List<String> labelsToAdd;

    private List<String> labelsToDel;

    private String timeframe, mailSubject, mailBody;

    public List<String> getKeysToAdd() {
        return keysToAdd;
    }

    public void setKeysToAdd(List<String> keysToAdd) {
        this.keysToAdd = keysToAdd;
    }

    public List<String> getKeysToDel() {
        return keysToDel;
    }

    public void setKeysToDel(List<String> keysToDel) {
        this.keysToDel = keysToDel;
    }

    public List<String> getGroupsToAdd() {
        return groupsToAdd;
    }

    public void setGroupsToAdd(List<String> groupsToAdd) {
        this.groupsToAdd = groupsToAdd;
    }

    public List<String> getGroupsToDel() {
        return groupsToDel;
    }

    public void setGroupsToDel(List<String> groupsToDel) {
        this.groupsToDel = groupsToDel;
    }

    public List<String> getLabelsToAdd() {
        return labelsToAdd;
    }

    public void setLabelsToAdd(List<String> labelsToAdd) {
        this.labelsToAdd = labelsToAdd;
    }

    public List<String> getLabelsToDel() {
        return labelsToDel;
    }

    public void setLabelsToDel(List<String> labelsToDel) {
        this.labelsToDel = labelsToDel;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }
}
