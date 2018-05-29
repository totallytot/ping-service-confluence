package com.bstrctlmnt.job;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.bstrctlmnt.mail.PingNotification;
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

    @ComponentImport
    private final PageManager pageManager;
    @ComponentImport
    private final SpaceManager spaceManager;
    @ComponentImport
    private final TransactionTemplate transactionTemplate;
    private Map<Page, String> pagesForPing;
    private Set<String> emails;

    @Autowired
    public PingJob(PageManager pageManager, SpaceManager spaceManager, TransactionTemplate transactionTemplate) {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.transactionTemplate = transactionTemplate;
        this.pagesForPing = new HashMap<>();
        this.emails = new HashSet<>();
    }

    private void checkPagesUpdates(String spaceKey) {
        Space space = spaceManager.getSpace(spaceKey);
        List<Page> pages = pageManager.getPages(space, true);

        pages.forEach((page) -> {

            String pageTitlePage = page.getDisplayTitle();
            Date lastUpdateDate = page.getLastModificationDate();

            Instant instant = Instant.ofEpochMilli(lastUpdateDate.getTime());
            LocalDateTime lud = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            LocalDateTime now = LocalDateTime.now();
            Duration deltaTime = Duration.between(lud, now);
            long difference = deltaTime.toHours();

            if (difference > 2) {
                pagesForPing.put(page, page.getCreator().getEmail());
                emails.add(page.getCreator().getEmail());
            }

            //String email = page.getCreator().getEmail();
            //String body = pageTitlePage +  " was updated on " + lastUpdateDate + " by " + page.getLastModifier().getName() + "| delta between now and updated date in hours:  " + difference;
            //PingNotification notification = new PingNotification();
            //notification.sendEmail(email, pageTitlePage, body);
        });
    }

    private void compoundEmailBodyAndSendEmail(Map<Page, String> pagesForPing, Set<String> emails) {
        //final int[] count = {0};
        emails.forEach((email) -> {
            StringBuilder body = new StringBuilder();
            pagesForPing.forEach((page, creatorEmail) ->{
                if (email.equals(creatorEmail)) {
                    body.append(page.getDisplayTitle());
                    body.append(" | ");
                    body.append(page.getLastModificationDate());
                    body.append(" by " + page.getLastModifier().getName());
                    body.append("\n");
                }
            });

            PingNotification notification = new PingNotification();
            notification.sendEmail(email, "Requires Update", body.toString() );
        });
    }

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        if (request.isCancellationRequested()) {
            return JobRunnerResponse.aborted("Job cancelled.");
        }

        transactionTemplate.execute(new TransactionCallback(){
            @Override
            public Void doInTransaction() {
             //job

                checkPagesUpdates("TEST");
                compoundEmailBodyAndSendEmail(pagesForPing, emails);
                return null;
            }
        });
        return JobRunnerResponse.success("Job finished successfully.");
    }
}