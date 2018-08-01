package com.bstrctlmnt.service;

import com.bstrctlmnt.servlet.JsonDataObject;

import java.util.Map;

public interface PluginConfigurationService {

    void updateConfigurationFromJSON(JsonDataObject jsonDataObject);

    Map<String, Object> getConfiguration();
}
