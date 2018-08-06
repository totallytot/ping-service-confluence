package com.bstrctlmnt.service;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.bstrctlmnt.servlet.JsonDataObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExportAsService({PluginConfigurationService.class})
@Named("pluginConfigurationService")
public class PluginConfigurationServiceImpl implements PluginConfigurationService{

    public static final String PLUGIN_STORAGE_KEY = "com.bstrctlmnt.servlet";
    private final PluginDataService pluginDataService;

    @Inject
    public PluginConfigurationServiceImpl(PluginDataService pluginDataService) {
        this.pluginDataService = pluginDataService;
    }

    @Override
    public void updateConfigurationFromJSON(JsonDataObject jsonDataObject) {
        List<String> keysToAdd = jsonDataObject.getKeysToAdd();
        List<String> keysToDel = jsonDataObject.getKeysToDel();
        List<String> groupsToAdd = jsonDataObject.getGroupsToAdd();
        List<String> groupsToDel = jsonDataObject.getGroupsToDel();
        String timeframe = jsonDataObject.getTimeframe();

        if (keysToAdd != null && keysToAdd.size() > 0) keysToAdd.forEach(pluginDataService::addAffectedSpace);
        if (keysToDel != null && keysToDel.size() > 0) keysToDel.forEach(pluginDataService::removeAffectedSpace);
        if (groupsToAdd != null && groupsToAdd.size() > 0) groupsToAdd.forEach(pluginDataService::addAffectedGroup);
        if (groupsToDel != null && groupsToDel.size() > 0) groupsToDel.forEach(pluginDataService::removeAffectedGroup);
        if (timeframe != null && timeframe.length() > 0) pluginDataService.updateTimeframe(timeframe);
    }

    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> configData = new HashMap<>(3);
        configData.put("monitoriedSpaceKeys", pluginDataService.getAffectedSpaces());
        configData.put("affectedGroups", pluginDataService.getAffectedGroups());
        configData.put("timeframe", pluginDataService.getTimeframe());
        return configData;
    }
}