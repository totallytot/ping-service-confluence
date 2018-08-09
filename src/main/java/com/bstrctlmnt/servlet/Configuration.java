package com.bstrctlmnt.servlet;

import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceStatus;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.bstrctlmnt.service.PluginConfigurationService;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final PluginConfigurationService pluginConfigurationService;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final TemplateRenderer renderer;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final UserAccessor userAccessor;

    @Inject
    public Configuration(PluginConfigurationService pluginConfigurationService, UserManager userManager, LoginUriProvider loginUriProvider,
                         UserAccessor userAccessor, TemplateRenderer renderer, SpaceManager spaceManager) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.pluginConfigurationService = pluginConfigurationService;
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

        Map<String, Object> configData = pluginConfigurationService.getConfiguration();
        Map<String, Object> context = new HashMap<>();
        context.put("allSpaceKeys", spaceManager.getAllSpaceKeys(SpaceStatus.CURRENT));
        context.put("allGroups", userAccessor.getGroupsAsList());
        context.put("affectedSpaces", configData.get("monitoriedSpaceKeys"));
        context.put("affectedGroups", configData.get("affectedGroups"));
        context.put("timeframe", configData.get("timeframe"));
        context.put("mailSubject", configData.get("subject"));
        context.put("mailBody", configData.get("body"));
        resp.setContentType("text/html;charset=utf-8");
        renderer.render("configuration.vm", context, resp.getWriter());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jsonString = req.getReader().lines().collect(Collectors.joining());
        ObjectMapper mapper = new ObjectMapper();
        JsonDataObject jsonDataObject = mapper.readValue(jsonString, JsonDataObject.class);
        if (pluginConfigurationService.updateConfigurationFromJSON(jsonDataObject)) resp.sendError(HttpServletResponse.SC_OK);
        else resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        //Send response w/t body with status 200 if DB was successfully updated, or 500 in case of errors
        //resp.setContentType("application/json");
        //mapper.writeValue(resp.getWriter(), jsonDataObject);
        //resp.getWriter().close();
    }
}

