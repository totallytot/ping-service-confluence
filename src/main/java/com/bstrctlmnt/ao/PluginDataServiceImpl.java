package com.bstrctlmnt.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsService({PluginDataService.class})
@Named("pluginDataService")
public class PluginDataServiceImpl implements PluginDataService {

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public PluginDataServiceImpl(ActiveObjects ao) {
        this.ao = checkNotNull(ao);
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
    public void addAffectedGroup(String group) {
        ao.executeInTransaction(() -> {
            final AffectedGroups affectedGroups = ao.create(AffectedGroups.class);
            affectedGroups.setAffectedGroup(group);
            affectedGroups.save();
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
}
