package com.bstrctlmnt.service;

import java.util.List;
import java.util.Map;

public interface PluginConfigurationService {

    void updateConfiguration(List<String> keysToAdd, List<String> keysToDel, List<String> groupsToAdd, List<String> groupsToDel, String timeframe);

    Map<String, Object> getConfiguration();
}
