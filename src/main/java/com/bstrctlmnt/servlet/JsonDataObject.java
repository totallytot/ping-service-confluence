package com.bstrctlmnt.servlet;

import java.util.List;

public class JsonDataObject {

    private List<String> keysToAdd;

    private List<String> keysToDel;

    private List<String> groupsToAdd;

    private List<String> groupsToDel;

    private String timeframe;

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

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
}
