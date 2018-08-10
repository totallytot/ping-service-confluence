package com.bstrctlmnt.service;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.rdbms.ConnectionCallback;
import com.atlassian.sal.api.rdbms.TransactionalExecutor;
import com.atlassian.sal.api.rdbms.TransactionalExecutorFactory;
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

    // files which contain query for outdated pages
    private static final String FILE_NAME_POSTGRE = "sql/pagesPostgre.sql";
    private static final String FILE_NAME = "sql/pages.sql";

    //The encoding used to transform stream to string.
    private static final String ENCODING = "UTF-8";
    private static final Logger log = Logger.getLogger(PluginDataServiceImpl.class);

    @ComponentImport
    private TransactionalExecutorFactory transactionalExecutorFactory;

    @Inject
    public PagesDAOServiceImpl(TransactionalExecutorFactory transactionalExecutorFactory) {
        this.transactionalExecutorFactory = transactionalExecutorFactory;
    }

    @Override
    public List<String> getOutdatedPages(Timestamp date) {
        final TransactionalExecutor transactionalExecutor = transactionalExecutorFactory.createExecutor(true, true);
        List<String> result = new ArrayList<>();

        transactionalExecutor.execute((ConnectionCallback<Void>) connection -> {
            InputStream iStream = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            try {
                String databaseName = connection.getMetaData().getDatabaseProductName();

                if (databaseName.equals("H2") || databaseName.equals("PostgreSQL")) {
                    iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME_POSTGRE);
                } else {
                    iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
                }
                StringWriter writer = new StringWriter();
                IOUtils.copy(iStream, writer, ENCODING);

                String query = writer.toString();
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, date);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String strId = resultSet.getString(1);
                    result.add(strId);
                }
            } catch (SQLException | IOException e) {
                log.error("Connecting to Confluence database error: " + e.getMessage());
            } finally {
                try {
                    if (resultSet != null)resultSet.close();
                    if (preparedStatement != null)preparedStatement.close();
                    if (iStream != null)iStream.close();
                } catch (SQLException | IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return null;
        });
        return result;
    }
}