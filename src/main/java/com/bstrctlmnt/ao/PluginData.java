package com.bstrctlmnt.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import java.util.HashSet;
import java.util.Set;

public interface PluginData {
    default Set<String> getPublicSpacesFromAO(ActiveObjects ao){
        Set<String> spaces = new HashSet<>();
        ao.executeInTransaction(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                for (AffectedSpaces affectedSpace : ao.find(AffectedSpaces.class))
                {
                    if (affectedSpace.getAffectedSpaceKey() != null)  spaces.add(affectedSpace.getAffectedSpaceKey());
                }
                return null;
            }
        });
        return spaces;
    }

    default Set<String> getGroupsFromAO(ActiveObjects ao) {
        Set<String> groups = new HashSet<>();
        ao.executeInTransaction(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                for (AffectedGroups affectedGroups : ao.find(AffectedGroups.class))
                {
                    if (affectedGroups.getAffectedGroup() != null)  groups.add(affectedGroups.getAffectedGroup());
                }
                return null;
            }
        });
        return groups;
    }
}
