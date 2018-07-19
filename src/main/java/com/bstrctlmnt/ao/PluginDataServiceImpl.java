package com.bstrctlmnt.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class PluginDataServiceImpl implements PluginDataService {

    private final ActiveObjects ao;

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
