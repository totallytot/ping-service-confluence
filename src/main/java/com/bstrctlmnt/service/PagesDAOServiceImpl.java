package com.bstrctlmnt.service;

import com.atlassian.core.db.JDBCUtils;
import com.atlassian.hibernate.PluginHibernateSessionFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ExportAsService({PagesDAOService.class})
@Named("pagesDAOService")
public class PagesDAOServiceImpl implements PagesDAOService {
    /**
     * The file name which contains query for outdated pages.
     */
    private static final String FILE_NAME_POSTGRE = "sql/pagesPostgre.sql";
    private static final String FILE_NAME = "sql/pages.sql";
    /**
     * The encoding used to transform stream to string.
     */
    private static final String ENCODING = "UTF-8";
    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class);

    @ComponentImport
    private PluginHibernateSessionFactory sessionFactory;

    @Inject
    public PagesDAOServiceImpl(PluginHibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<String> getOutdatedPages(Timestamp date) {
        Session session = sessionFactory.getSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        InputStream iStream = null;
        List<String> result = null;

        try {
            Connection confluenceConnection = session.connection();
            String databaseName = confluenceConnection.getMetaData().getDatabaseProductName();

            if (databaseName.equals("H2") || databaseName.equals("PostgreSQL")) {
                iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME_POSTGRE);
            }
            else {
                iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            }

            StringWriter writer = new StringWriter();
            IOUtils.copy(iStream, writer, ENCODING);
            String query = writer.toString();
            preparedStatement = confluenceConnection.prepareStatement(query);
            preparedStatement.setTimestamp(1, date);
            resultSet = preparedStatement.executeQuery();
            /*
            The sql query returns page IDs, we just get all values from the first column.
             */
            result = new ArrayList<>();
            while (resultSet.next()) {
                String strId = resultSet.getString(1);
                result.add(strId);
            }
            result.forEach(System.out::println);

        } catch (SQLException | HibernateException | IOException e) {
            log.error("Connecting to Confluence database error: " + e.getMessage());
        } finally {
            JDBCUtils.close(resultSet);
            JDBCUtils.close(preparedStatement);
            IOUtils.closeQuietly(iStream);
        }
        return result;
    }
}