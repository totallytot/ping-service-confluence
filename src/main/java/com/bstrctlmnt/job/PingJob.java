package com.bstrctlmnt.job;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.bstrctlmnt.service.PluginDataService;
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

@Component
public class PingJob implements JobRunner {

    private final PluginDataService pluginDataService;

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final SettingsManager settingsManager;
    @ComponentImport
    private final UserAccessor userAccessor;

    @Autowired
    public PingJob(PageManager pageManager, SpaceManager spaceManager, TransactionTemplate transactionTemplate, PluginSettingsFactory pluginSettingsFactory,
                   UserManager userManager, SettingsManager settingsManager, UserAccessor userAccessor, PluginDataService pluginDataService) {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.transactionTemplate = transactionTemplate;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.userAccessor = userAccessor;
        this.pluginDataService = pluginDataService;
    }

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        if (request.isCancellationRequested()) {
            return JobRunnerResponse.aborted("Job cancelled.");
        }

        transactionTemplate.execute(() -> {
            //job
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
            long timeframe = Long.parseLong((String) pluginSettings.get(Configuration.PLUGIN_STORAGE_KEY + ".timeframe"));

            Set<String> affectedSpaces = pluginDataService.getAffectedSpaces();
            Set<String> groups = pluginDataService.getAffectedGroups();
            LocalDateTime now = LocalDateTime.now();

            if (timeframe != 0 && affectedSpaces != null && groups != null && affectedSpaces.size() > 0 && groups.size() > 0)
            {
                Multimap<ConfluenceUser, Page> multiMap = ArrayListMultimap.create();

                affectedSpaces.forEach(spaceStr -> {
                    Space space = spaceManager.getSpace(spaceStr);
                    List<Page> pages = pageManager.getPages(space, true);

                    pages.forEach(page -> {
                        Instant instant = Instant.ofEpochMilli(page.getLastModificationDate().getTime());
                        LocalDateTime pageLastUpdateDate = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                        Duration deltaTime = Duration.between(pageLastUpdateDate, now);
                        long delta = deltaTime.toDays();

                        ConfluenceUser creator = page.getCreator();
                        if (creator != null && delta > timeframe && !userAccessor.isDeactivated(creator) && checkUserMembership(creator, groups))
                            multiMap.put(creator, page);
                    });
                });
                createNotificationAndSendEmail(multiMap, timeframe);
            }
            return null;
        });
        return JobRunnerResponse.success("Job finished successfully.");
    }

    private boolean checkUserMembership(ConfluenceUser confluenceUser, Set<String> groups) {
         return groups.stream().anyMatch(group -> userManager.isUserInGroup(confluenceUser.getKey(), group));
    }

    private void createNotificationAndSendEmail(Multimap<ConfluenceUser, Page> multiMap, long timeframe) {
        Set<ConfluenceUser> keys = multiMap.keySet();

        for (ConfluenceUser confluenceUser : keys)
        {
            StringBuilder body = new StringBuilder();
            Collection<Page> values = multiMap.get(confluenceUser);

            body.append(String.format("<html><body>Dear %s,<br><br>Could you please take a look at the pages below. You are the owner of them, but looks like their content wasn't updated for a while (%d day(s)):<br>", confluenceUser.getFullName(), timeframe));
            values.forEach(page -> {
                body.append(String.format("<a href=\"%s/pages/viewpage.action?pageId=%s\">%s</a>", settingsManager.getGlobalSettings().getBaseUrl(), page.getId(), page.getDisplayTitle()));
                body.append("<br>");
            });
            body.append("</body></html>");

            PingNotification notification = new PingNotification();
            notification.sendEmail(confluenceUser.getEmail(), "notification: It's time to review your pages", body.toString());
        }
    }
}