package com.bstrctlmnt.service;

import java.util.Set;

public interface PluginDataService {

    void addAffectedSpace(String SpaceKey);

    Set<String> getAffectedSpaces();

    void addAffectedGroup(String group);

    Set<String> getAffectedGroups();

}
