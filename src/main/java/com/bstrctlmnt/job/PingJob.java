package com.bstrctlmnt.job;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.user.GroupManager;
import com.bstrctlmnt.ao.PluginData;
import com.bstrctlmnt.mail.PingNotification;
import com.bstrctlmnt.servlet.Configuration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class PingJob implements JobRunner, PluginData {

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final ActiveObjects ao;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final SettingsManager settingsManager;

    private final Multimap<ConfluenceUser, Page> multiMap = ArrayListMultimap.create();

    @Autowired
    public PingJob(PageManager pageManager, SpaceManager spaceManager, TransactionTemplate transactionTemplate, PluginSettingsFactory pluginSettingsFactory,
                   ActiveObjects ao, GroupManager groupManager, UserManager userManager, SettingsManager settingsManager) {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.transactionTemplate = transactionTemplate;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.userManager = userManager;
        this.ao = checkNotNull(ao);
        this.settingsManager = settingsManager;
    }

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        if (request.isCancellationRequested()) {
            return JobRunnerResponse.aborted("Job cancelled.");
        }

        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Void doInTransaction() {
                //job
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                long timeframe = Long.parseLong((String) pluginSettings.get(Configuration.PLUGIN_STORAGE_KEY + ".timeframe"));

                Set<String> affectedSpaces = getPublicSpacesFromAO(ao);

                Set<String> groupsStr = getGroupsFromAO(ao);
                Set<String> groups = getGroupsFromAO(ao);

                LocalDateTime now = LocalDateTime.now();

                if (timeframe != 0 && affectedSpaces != null && groups != null && affectedSpaces.size() > 0 && groups.size() > 0) {

                    affectedSpaces.forEach(spaceStr -> {
                        Space space = spaceManager.getSpace(spaceStr);
                        List<Page> pages = pageManager.getPages(space, true);

                        pages.forEach(page -> {

                            Instant instant = Instant.ofEpochMilli(page.getLastModificationDate().getTime());
                            LocalDateTime pageLastUpdateDate = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                            Duration deltaTime = Duration.between(pageLastUpdateDate, now);
                            long delta = deltaTime.toHours();


                            //check Anonymous
                            ConfluenceUser creator = page.getCreator();

                            if (delta > timeframe && checkUserMembership(creator, groups)) {
                                multiMap.put(creator, page);
                            }
                        });
                    });

                    createNotificationAndSendEmail(multiMap);
                }
                return null;
            }
        });
        return JobRunnerResponse.success("Job finished successfully.");
    }

    private boolean checkUserMembership(ConfluenceUser confluenceUser, Set<String> groups) {
        final boolean[] hasMemberShip = {false};
        groups.forEach(group -> {
            if (userManager.isUserInGroup(confluenceUser.getKey(), group)) {
                hasMemberShip[0] = true;
                return;
            }
        });
        return hasMemberShip[0];
    }

    private void createNotificationAndSendEmail(Multimap<ConfluenceUser, Page> multiMap) {
        Set<ConfluenceUser> keys = multiMap.keySet();
        for (ConfluenceUser confluenceUser : keys) {
            StringBuilder body = new StringBuilder();
            Collection<Page> values = multiMap.get(confluenceUser);

            body.append("<html><body>");
            values.forEach(page -> {
                body.append(String.format("<a href=\"%s/pages/viewpage.action?pageId=%s\">%s</a>", settingsManager.getGlobalSettings().getBaseUrl(), page.getId(), page.getDisplayTitle()));
                body.append("<br>");
            });
            body.append("</body></html>");

            PingNotification notification = new PingNotification();
            notification.sendEmail(confluenceUser.getEmail(), "Requires Update", body.toString());
        }
    }
}