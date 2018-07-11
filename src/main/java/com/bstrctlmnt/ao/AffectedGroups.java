package com.bstrctlmnt.ao;

import net.java.ao.Entity;

public interface AffectedGroups extends Entity {
    void setAffectedGroup(String key);

    String getAffectedGroup();

}
