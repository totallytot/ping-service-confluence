package com.bstrctlmnt.job;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.bstrctlmnt.service.PagesDAOService;
import com.bstrctlmnt.service.PluginDataService;
import com.bstrctlmnt.mail.PingNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ExportAsService({PingJob.class})
@Named("pingJob")
public class PingJob implements JobRunner {

    private final PluginDataService pluginDataService;
    private final PagesDAOService pagesDAOService;
    private final PingNotification pingNotification;

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    @ComponentImport
    private final SettingsManager settingsManager;


    @Inject
    public PingJob(PageManager pageManager, TransactionTemplate transactionTemplate, SettingsManager settingsManager,
                   PluginDataService pluginDataService, PagesDAOService pagesDAOService) {
        this.pageManager = pageManager;
        this.transactionTemplate = transactionTemplate;
        this.settingsManager = settingsManager;
        this.pluginDataService = pluginDataService;
        this.pagesDAOService = pagesDAOService;
        this.pingNotification = new PingNotification();
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

            if (timeframe != 0 && affectedSpaces != null
                    && groups != null && affectedSpaces.size() > 0 && groups.size() > 0)
            {
                //get expiration date
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime requiredDate = now.minusDays(timeframe);

                //meet format in DB: "2017-03-21 09:17:10";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                Timestamp tsDate = Timestamp.valueOf(requiredDate.format(formatter));
                List<String> outdatedPagesIds = pagesDAOService.getOutdatedPages(tsDate);

                //sort pages by creator and send email
                if (outdatedPagesIds != null && outdatedPagesIds.size() > 0)
                {
                    Multimap<ConfluenceUser, Page> multiMap = ArrayListMultimap.create();
                    outdatedPagesIds.forEach((id) -> {
                        Page page = pageManager.getPage(Long.parseLong(id));
                        ConfluenceUser creator = page.getCreator();
                        if (creator != null) multiMap.put(creator, page);
                    });
                    createNotificationAndSendEmail(multiMap, timeframe, pingNotification);
                }
            }
            return null;
        });
        return JobRunnerResponse.success("Job finished successfully.");
    }

    private void createNotificationAndSendEmail(Multimap<ConfluenceUser, Page> multiMap, Long timeframe,
                                                PingNotification pingNotification) {
        Set<ConfluenceUser> keys = multiMap.keySet();

        for (ConfluenceUser confluenceUser : keys)
        {
            StringBuilder links = new StringBuilder();
            Collection<Page> pages = multiMap.get(confluenceUser);

            pages.forEach((page) -> {
                links.append("- ")
                        .append(String.format("<a href=\"%s/pages/viewpage.action?pageId=%s\">%s</a>",
                                settingsManager.getGlobalSettings().getBaseUrl(), page.getId(), page.getDisplayTitle()))
                        .append(" (last modified: ");
                Instant instant = page.getLastModificationDate().toInstant();
                LocalDate localDate = instant.atOffset(ZoneOffset.UTC).toLocalDate();
                links.append(localDate)
                        .append(")<br>");
            });

            // mail variables
            String mailbody = pluginDataService.getMailBody().replace("$creator", confluenceUser.getFullName())
                    .replace("$days", timeframe.toString())
                    .replace("$links", links.toString());

            pingNotification.sendEmail(confluenceUser.getEmail(), pluginDataService.getMailSubject(), mailbody);
        }
    }
}