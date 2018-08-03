package com.bstrctlmnt.service;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.spring.container.ContainerManager;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@ExportAsService({PagesDAOService.class})
@Named("pagesDAOService")
public class PagesDAOServiceImpl implements PagesDAOService {
    /**
     * The file name which contains query for counting descendants.
     */
    private static final String FILE_NAME = "pages.sql";
    /**
     * The encoding used to transform stream to string.
     */
    private static final String ENCODING = "UTF-8";
    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class);

    @Inject
    public PagesDAOServiceImpl() {

    }

    @Override
    public List<String> getOutdatedpages() {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        Connection confluenceConnection = null;

        try {
            Thread.currentThread().setContextClassLoader(ContainerManager.class.getClassLoader());
            InitialContext ctx = new InitialContext();
            DataSource confluenceDs = (DataSource) ctx.lookup("java:comp/env/jdbc/ConfluenceDS");

            confluenceConnection = confluenceDs.getConnection();

            Statement stmnt = confluenceConnection.createStatement();
            ResultSet result = stmnt.executeQuery("SELECT count(*) FROM spaces");

            // Process result

        } catch (NamingException | SQLException e) {
            log.error("Connecting to Confluence database error: " + e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
            if (confluenceConnection != null) {
                try {
                    confluenceConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }
}
