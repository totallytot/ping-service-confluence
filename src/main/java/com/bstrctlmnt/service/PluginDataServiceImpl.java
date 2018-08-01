package com.bstrctlmnt.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.bstrctlmnt.ao.AffectedGroups;
import com.bstrctlmnt.ao.AffectedSpaces;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsService({PluginDataService.class})
@Named("pluginDataService")
public class PluginDataServiceImpl implements PluginDataService {

    public static final String PLUGIN_STORAGE_KEY = "com.bstrctlmnt.servlet";
    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class);

    @ComponentImport
    private final ActiveObjects ao;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public PluginDataServiceImpl(ActiveObjects ao, PluginSettingsFactory pluginSettingsFactory) {
        this.ao = checkNotNull(ao);
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void addAffectedSpace(String spaceKey) {
        ao.executeInTransaction(() -> {
            final AffectedSpaces affectedSpaces = ao.create(AffectedSpaces.class);
            affectedSpaces.setAffectedSpaceKey(spaceKey);
            affectedSpaces.save();
            return affectedSpaces;
        });
    }

    @Override
    public Set<String> getAffectedSpaces() {
        Set<String> spaces = new HashSet<>();
        ao.executeInTransaction((TransactionCallback<Void>) () -> {
            for (AffectedSpaces affectedSpace : ao.find(AffectedSpaces.class))
                spaces.add(affectedSpace.getAffectedSpaceKey());
            return null;
        });
        return spaces;
    }

    @Override
    public void removeAffectedSpace(String SpaceKey) {
        ao.executeInTransaction((TransactionCallback<AffectedSpaces>) () -> {
            final AffectedSpaces affectedSpaces = ao.create(AffectedSpaces.class);
            for (AffectedSpaces as : ao.find(AffectedSpaces.class, "AFFECTED_SPACE_KEY = ?", SpaceKey)) {
                try {
                    as.getEntityManager().delete(as);
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
                affectedSpaces.save();
            }
            return affectedSpaces;
        });
    }

    @Override
    public void addAffectedGroup(String group) {
        ao.executeInTransaction(() -> {
            final AffectedGroups affectedGroups = ao.create(AffectedGroups.class);
            affectedGroups.setAffectedGroup(group);
            affectedGroups.save();
            return affectedGroups;
        });
    }

    @Override
    public void removeAffectedGroup(String group) {
        ao.executeInTransaction((TransactionCallback<AffectedGroups>) () -> {
                final AffectedGroups affectedGroups = ao.create(AffectedGroups.class);
                for (AffectedGroups ag : ao.find(AffectedGroups.class, "AFFECTED_GROUP = ?", group)) {
                    try {
                        ag.getEntityManager().delete(ag);
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    }
                    affectedGroups.save();
                }
                return affectedGroups;
        });
    }

    @Override
    public Set<String> getAffectedGroups() {
        Set<String> groups = new HashSet<>();
        ao.executeInTransaction((TransactionCallback<Void>) () -> {
            for (AffectedGroups affectedGroups : ao.find(AffectedGroups.class))
                groups.add(affectedGroups.getAffectedGroup());
            return null;
        });
        return groups;
    }

    @Override
    public String getTimeframe() {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe") == null) {
            String noTimeframe = "0";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".timeframe", noTimeframe);
        }
        return (String) pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe");
    }

    @Override
    public void updateTimeframe(String timeframe) {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(PLUGIN_STORAGE_KEY + ".timeframe", timeframe);
    }
}
