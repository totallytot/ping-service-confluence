package com.bstrctlmnt.mail;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.spring.container.ContainerManager;

public class PingNotification {
    @ComponentImport
    private final MailServerManager mailServerManager;


    public PingNotification() {
        mailServerManager = (MailServerManager) ContainerManager.getInstance().getComponent("mailServerManager");
    }

    public void sendEmail(String emailAdr, String subject, String body) {
        SMTPMailServer smtpMailServer = mailServerManager.getDefaultSMTPMailServer();

        if (smtpMailServer != null && emailAdr.length() > 0) {
            Email email = new Email(emailAdr);
            email.setFrom(smtpMailServer.getDefaultFrom());
            email.setMimeType("text/html");
            email.setSubject(subject);
            email.setBody(body);

            try {
                smtpMailServer.send(email);

            } catch (MailException e) {
                e.printStackTrace();
            }
        }
        else {
            //do nothing
        }
    }
}
