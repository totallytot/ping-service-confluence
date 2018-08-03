package com.bstrctlmnt.service;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.spring.container.ContainerManager;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ExportAsService({PagesDAOService.class})
@Named("pagesDAOService")
public class PagesDAOServiceImpl implements PagesDAOService {
    /**
     * The file name which contains query for outdated pages.
     */
    private static final String FILE_NAME = "pages.sql";
    /**
     * The encoding used to transform stream to string.
     */
    private static final String ENCODING = "UTF-8";
    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class);
/*
    @Inject
    public PagesDAOServiceImpl() {

    }
    */

    @Override
    public List<Integer> getOutdatedPages(Set<String> monitoredSpaceKeys, Timestamp date, Set<String> affectedGroups) {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        Connection confluenceConnection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        InputStream iStream = null;
        List<Integer> result = null;

        try {
            Thread.currentThread().setContextClassLoader(ContainerManager.class.getClassLoader());
            InitialContext ctx = new InitialContext();
            DataSource confluenceDs = (DataSource) ctx.lookup("java:comp/env/jdbc/ConfluenceDS");
            confluenceConnection = confluenceDs.getConnection();
            iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            StringWriter writer = new StringWriter();
            IOUtils.copy(iStream, writer, ENCODING);
            String query = writer.toString();
            ps = confluenceConnection.prepareStatement(query);

            final String[] keys = monitoredSpaceKeys.toArray(new String[monitoredSpaceKeys.size()]);
            final java.sql.Array sqlArrayKeys = confluenceConnection.createArrayOf("varchar", keys); //check type
            final String[] groups = monitoredSpaceKeys.toArray(new String[monitoredSpaceKeys.size()]);
            final java.sql.Array sqlArrayGroups = confluenceConnection.createArrayOf("varchar", groups);

            ps.setArray(1, sqlArrayKeys);
            //ps.setString(2, date);
            ps.setTimestamp(3, date);
            ps.setArray(3, sqlArrayGroups);
            rs = ps.executeQuery();
            rs.next();

            /*
            The sql query returns page IDs, we just get this value from first column and transform
            result from sql array to list.
             */
            result = Arrays.asList(((Integer[]) rs.getArray(1).getArray()));

        } catch (NamingException | SQLException | IOException e) {
            log.error("Connecting to Confluence database error: " + e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
            if (confluenceConnection != null) {
                try {
                    confluenceConnection.close();
                } catch (SQLException e) {
                    log.error("Connecting to Confluence database error: " + e.getMessage());
                }
            }
        }
        return result;
    }
}