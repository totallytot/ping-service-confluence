package com.bstrctlmnt.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

public interface PagesDAOService {
    List<String> getOutdatedPages(Set<String> monitoredSpaceKeys, Timestamp date, Set<String> affectedGroups);
}
