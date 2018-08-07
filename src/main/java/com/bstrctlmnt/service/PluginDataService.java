package com.bstrctlmnt.service;

import java.util.Set;

public interface PluginDataService {

    void addAffectedSpace(String SpaceKey);

    Set<String> getAffectedSpaces();

    void removeAffectedSpace(String SpaceKey);

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
