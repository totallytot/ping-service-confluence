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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @ComponentImport
    private PluginHibernateSessionFactory sessionFactory;

    @Inject
    public PagesDAOServiceImpl(PluginHibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<String> getOutdatedPages(Set<String> monitoredSpaceKeys, Timestamp date, Set<String> affectedGroups) {
        Session session = sessionFactory.getSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        InputStream iStream = null;
        List<String> result = null;


        try {
            Connection confluenceConnection = session.connection();
            iStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            StringWriter writer = new StringWriter();
            IOUtils.copy(iStream, writer, ENCODING);
            String query = writer.toString();
            preparedStatement = confluenceConnection.prepareStatement(query);

            /*
            final String[] keys = monitoredSpaceKeys.toArray(new String[monitoredSpaceKeys.size()]);
            final java.sql.Array sqlArrayKeys = confluenceConnection.createArrayOf("VARCHAR", keys); //check type
            final String[] groups = monitoredSpaceKeys.toArray(new String[monitoredSpaceKeys.size()]);
            final java.sql.Array sqlArrayGroups = confluenceConnection.createArrayOf("VARCHAR", groups);

            error: Feature not supported: "createArray"
            */

            //preparedStatement.setArray(1, sqlArrayKeys);
            preparedStatement.setTimestamp(1, date); //set 2, 1 - for test with date
            //preparedStatement.setArray(3, sqlArrayGroups);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            /*
            The sql query returns page IDs, we just get this value from first column and transform
            result from sql array to list.
             */
            //result = Arrays.asList(((Long[]) resultSet.getArray(1).getArray()));
            //[Ljava.lang.Object; cannot be cast to [Ljava.lang.Integer;']
            //[Ljava.lang.Object; cannot be cast to [Ljava.lang.Long;']
            //[Ljava.lang.Object; cannot be cast to [Ljava.lang.String;']

            Array array = resultSet.getArray(1);
            //Object[] objects = (Object[]) array.getArray();

            //boolean a = objects[0] instanceof String;
            //System.out.println("> String? " + a); //true
            //String[] strings = (String[]) objects;
            //[Ljava.lang.Object; cannot be cast to [Ljava.lang.String;']
            //String[] strings = (String[]) array.getArray();
            //[Ljava.lang.Object; cannot be cast to [Ljava.lang.String;']

            //works
            //System.out.println("Length: " + objects.length);
            //System.out.println("String: " + objects[0].toString());
            //String str = (String) objects[0];
            //System.out.println(Long.parseLong(str));
            /*
            result = new ArrayList<>(objects.length);
            for (Object object : objects) {
                result.add(Long.parseLong((String) object));
            }
            result.forEach(System.out::println);
            */

            //shorter, will parse long in ping job
            result = Arrays.stream((Object[]) array.getArray()).map(Object::toString).collect(Collectors.toList());
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