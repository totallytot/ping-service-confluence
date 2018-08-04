package com.bstrctlmnt.job;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.bstrctlmnt.service.PagesDAOService;
import com.bstrctlmnt.service.PluginDataService;
import com.bstrctlmnt.mail.PingNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PingJob implements JobRunner {

    private final PluginDataService pluginDataService;
    private final PagesDAOService pagesDAOService;

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    @ComponentImport
    private final UserManager userManager;
    @ComponentImport
    private final SettingsManager settingsManager;


    @Autowired
    public PingJob(PageManager pageManager, TransactionTemplate transactionTemplate, UserManager userManager,
                   SettingsManager settingsManager, PluginDataService pluginDataService, PagesDAOService pagesDAOService) {
        this.pageManager = pageManager;
        this.transactionTemplate = transactionTemplate;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.pluginDataService = pluginDataService;
        this.pagesDAOService = pagesDAOService;
    }

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        if (request.isCancellationRequested()) {
            return JobRunnerResponse.aborted("Job cancelled.");
        }

        transactionTemplate.execute(() -> {
            //job
            long timeframe = Long.parseLong(pluginDataService.getTimeframe());
            Set<String> affectedSpaces = pluginDataService.getAffectedSpaces();
            Set<String> groups = pluginDataService.getAffectedGroups();


            if (timeframe != 0 && affectedSpaces != null && groups != null && affectedSpaces.size() > 0 && groups.size() > 0)
            {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime requiredDate = now.minusDays(timeframe);
                // format in db "2017-03-21 09:17:10";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                Timestamp tsDate = Timestamp.valueOf(requiredDate.format(formatter));
                List<Integer> outdatedPagesIds = pagesDAOService.getOutdatedPages(affectedSpaces, tsDate, groups);

                if (outdatedPagesIds != null && outdatedPagesIds.size() > 0)
                {
                    Multimap<ConfluenceUser, Page> multiMap = ArrayListMultimap.create();
                    outdatedPagesIds.forEach((id) -> {
                        Page page = pageManager.getPage(id);
                        ConfluenceUser creator = page.getCreator();
                        if (creator != null) multiMap.put(creator, page);
                    });
                    createNotificationAndSendEmail(multiMap, timeframe);
                }



                /*
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
                */

            }
            return null;
        });
        return JobRunnerResponse.success("Job finished successfully.");
    }
    //for removal
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