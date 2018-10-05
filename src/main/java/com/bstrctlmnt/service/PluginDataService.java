package com.bstrctlmnt.service;

import java.util.Set;

public interface PluginDataService {

    void addAffectedSpace(String spaceKey);

    Set<String> getAffectedSpaces();

    void removeAffectedSpace(String spaceKey);

    void addLabel(String label);

    Set<String> getLabels();

    void removeLabel(String label);

    void addAffectedGroup(String group);

    void removeAffectedGroup(String group);

    Set<String> getAffectedGroups();

    String getTimeframe();

    void updateTimeframe(String timeframe);

    void updateMailSubject(String subject);

    String getMailSubject();

    void updateMailBody(String body);

    String getMailBody();

}
