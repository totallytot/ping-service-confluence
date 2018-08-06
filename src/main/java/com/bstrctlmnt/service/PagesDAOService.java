package com.bstrctlmnt.service;

import java.sql.Timestamp;
import java.util.List;

public interface PagesDAOService {
    List<String> getOutdatedPages(Timestamp date);
}
