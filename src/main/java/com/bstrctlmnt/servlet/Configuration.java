package com.bstrctlmnt.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.EntityException;
import com.atlassian.user.GroupManager;
import com.bstrctlmnt.ao.AffectedGroups;
import com.bstrctlmnt.ao.AffectedSpaces;
import com.bstrctlmnt.ao.PluginData;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Scanned
public class Configuration extends HttpServlet implements PluginData {

    public static final String PLUGIN_STORAGE_KEY = "com.bstrctlmnt.servlet";
    @ComponentImport
    private final ActiveObjects ao;
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final TemplateRenderer renderer;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final GroupManager groupManager;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public Configuration(UserManager userManager, ActiveObjects ao, LoginUriProvider loginUriProvider,
                         TemplateRenderer renderer, SpaceManager spaceManager, GroupManager groupManager,
                         PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.ao = checkNotNull(ao);
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.spaceManager = spaceManager;
        this.groupManager = groupManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
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
        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe") == null){
            String noTimeframe = "0";
            pluginSettings.put(PLUGIN_STORAGE_KEY +".timeframe", noTimeframe);
        }

        resp.setContentType("text/html;charset=utf-8");
        Map<String, Object> context = new HashMap<>();
        context.put("affectedSpaces", getAffectedSpacesAsString());
        context.put("affectedGroups", getAffectedGroupsAsString());
        context.put("timeframe", pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe"));
        renderer.render("configuration.vm", context, resp.getWriter());
        resp.getWriter().close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String affectedSpaceKey = req.getParameter("spacekey").trim();
        final String spaceKeyToRemove = req.getParameter("rspacekey").trim();
        final String affectedGroup = req.getParameter("group").trim();
        final String groupToRemove = req.getParameter("rgroup").trim();
        final String timeframe = req.getParameter("timeframe").trim();

        //add timeframe
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(PLUGIN_STORAGE_KEY + ".timeframe", timeframe);

        //add space key to DB
        if (spaceManager.getSpace(affectedSpaceKey) != null && !getPublicSpacesFromAO(ao).contains(affectedSpaceKey) && !affectedSpaceKey.equals(spaceKeyToRemove)) {
            ao.executeInTransaction(new TransactionCallback<AffectedSpaces>() {
                @Override
                public AffectedSpaces doInTransaction() {
                    final AffectedSpaces affectedSpaces = ao.create(AffectedSpaces.class);
                    affectedSpaces.setAffectedSpaceKey(affectedSpaceKey);
                    affectedSpaces.save();
                    return affectedSpaces;
                }
            });
        }

        //remove space key
        if (getPublicSpacesFromAO(ao).contains(spaceKeyToRemove) && !spaceKeyToRemove.equals(affectedSpaceKey)) {
            ao.executeInTransaction(new TransactionCallback<AffectedSpaces>() {
                @Override
                public AffectedSpaces doInTransaction() {
                    final AffectedSpaces affectedSpaces = ao.create(AffectedSpaces.class);
                    for (AffectedSpaces as : ao.find(AffectedSpaces.class, "AFFECTED_SPACE_KEY = ?", spaceKeyToRemove)) {
                        try {
                            as.getEntityManager().delete(as);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    affectedSpaces.save();
                    return affectedSpaces;
                }
            });
        }

        //add affected group
        try {
            if (groupManager.getGroup(affectedGroup) != null && !affectedGroup.equals(groupToRemove) && !getGroupsFromAO(ao).contains(affectedGroup)) {
                ao.executeInTransaction(new TransactionCallback<AffectedGroups>() {
                    @Override
                    public AffectedGroups doInTransaction() {
                        final AffectedGroups affectedGroups = ao.create(AffectedGroups.class);
                        affectedGroups.setAffectedGroup(affectedGroup);
                        affectedGroups.save();
                        return affectedGroups;
                    }
                });
            }
        } catch (EntityException e) {
            e.printStackTrace();
        }

        //remove group
        if (!groupToRemove.equals(affectedGroup) && getGroupsFromAO(ao).contains(groupToRemove)) {
            ao.executeInTransaction(new TransactionCallback<AffectedGroups>() {
                @Override
                public AffectedGroups doInTransaction() {
                    final AffectedGroups affectedGroups = ao.create(AffectedGroups.class);
                    for (AffectedGroups ag : ao.find(AffectedGroups.class, "AFFECTED_GROUP = ?", groupToRemove)) {
                        try {
                            ag.getEntityManager().delete(ag);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    affectedGroups.save();
                    return affectedGroups;
                }
            });
        }

        Map<String, Object> context = new HashMap<>();
        context.put("affectedSpaces", getAffectedSpacesAsString());
        context.put("affectedGroups", getAffectedGroupsAsString());
        context.put("timeframe", pluginSettings.get(PLUGIN_STORAGE_KEY + ".timeframe"));
        renderer.render("configuration.vm", context, resp.getWriter());
        resp.getWriter().close();
    }

        private String getAffectedGroupsAsString() {
            String [] result = {""};
            Set<String> groups = getGroupsFromAO(ao);
            if (groups.size() < 1) {
                return "No Affected Groups";
            }
            else {
                groups.forEach(s -> result[0] += s + ", ");
                return result[0].substring(0, result[0].length()-2);
            }
        }

        private String getAffectedSpacesAsString() {
            String [] result = {""};
            Set<String> spaces = getPublicSpacesFromAO(ao);
            if (spaces.size() < 1) {
                return "No Affected Spaces";
            }
            else {
                spaces.forEach(s -> result[0] += s + ", ");
                return result[0].substring(0, result[0].length()-2);
            }
        }
    }

