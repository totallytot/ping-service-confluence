package com.bstrctlmnt.servlet;

import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceStatus;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.Group;
import com.bstrctlmnt.ao.PluginDataService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Scanned
public class Configuration extends HttpServlet {

    private static final Logger log = Logger.getLogger(Configuration.class);
    public static final String PLUGIN_STORAGE_KEY = "com.bstrctlmnt.servlet";

    private final PluginDataService pluginDataService;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final TemplateRenderer renderer;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final UserAccessor userAccessor;

    @Inject
    public Configuration(UserManager userManager, LoginUriProvider loginUriProvider, UserAccessor userAccessor, TemplateRenderer renderer,
                         SpaceManager spaceManager, PluginSettingsFactory pluginSettingsFactory, PluginDataService pluginDataService) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.spaceManager = spaceManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.pluginDataService = pluginDataService;
        this.userAccessor = userAccessor;
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        UserProfile username = userManager.getRemoteUser(req);
        if (username == null || !userManager.isAdmin(username.getUserKey())) {
            redirectToLogin(req, resp);
            return;
        }

        //store timeframe
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe") == null) {
            String noTimeframe = "0";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".timeframe", noTimeframe);
        }

        List<Group> allGroups = userAccessor.getGroupsAsList();

        resp.setContentType("text/html;charset=utf-8");
        Map<String, Object> context = new HashMap<>();
        context.put("allSpaceKeys", spaceManager.getAllSpaceKeys(SpaceStatus.CURRENT));
        context.put("allGroups", allGroups);
        context.put("affectedSpaces", pluginDataService.getAffectedSpaces());
        context.put("affectedGroups", pluginDataService.getAffectedGroups());
        context.put("timeframe", pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe"));
        renderer.render("configuration.vm", context, resp.getWriter());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String jsonString = req.getReader().lines().collect(Collectors.joining());

        //update DB here via plunging manager or service + add timeframe
        //PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        //pluginSettings.put(PLUGIN_STORAGE_KEY + ".timeframe", timeframe);

        //Send response w/t body with status 200 if DB was successfully updated, or 500 in case of errors
        ObjectMapper mapper = new ObjectMapper();
        JsonDataObject jsonDataObject = mapper.readValue(jsonString, JsonDataObject.class);
        resp.setContentType("application/json");
        mapper.writeValue(resp.getWriter(), jsonDataObject);
        resp.getWriter().close();
    }
}

