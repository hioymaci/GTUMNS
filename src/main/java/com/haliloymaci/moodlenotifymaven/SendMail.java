package com.haliloymaci.moodlenotifymaven;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Crunchify.com
 *
 */
public class SendMail {

    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;
    
    /**
     * Send a test mail
     * @param args
     * @throws MessagingException 
     */
    public static void main(String[] args) throws MessagingException {
        generateAndSendEmail("yourmail@yourmalservice.com", "subject", "url", "courseid", "diff", "yourSourceGMailId", "yourPassword");
    }
    
    /**
     * Send email source mail: gtu.moodle.notification
     *
     * @param targetEmailAddress target email address
     * @param subject sending email header
     * @param url course link in moodle
     * @param courseID course real ID such as CSE495
     * @param differences changes in website
     * @param sourceGmailID gmail account id, email address excluding @gmail.com
     * @param sourceGmailPassword gmail password
     * @throws AddressException
     * @throws MessagingException
     */
    public static void generateAndSendEmail(String targetEmailAddress, String subject, String url, String courseID, String differences, String sourceGmailID, String sourceGmailPassword) throws AddressException, MessagingException {

        String message = "New activity is occured in course:<a href=\"" + url + "\">" + courseID + "</a> at GTU Moodle.\n"
                + "<br><br>\n"
                + "<b>Differences:</b><br><br>\n"
                + differences
                + "<br><br>\n"
                + "\n"
                + "link: " + url + "\n"
                + "<br>\n"
                + "<hr>\n"
                + "To unsubscribe, contact to <a href=\"http://www.haliloymaci.com\">hioymaci@gmail.com</a>";

        // Step1
//        System.out.println("\n 1st ===> setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
//        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
//        System.out.println("\n\n 2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.setSubject(subject, "UTF-8");
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmailAddress));
        generateMailMessage.setSubject(subject);
        generateMailMessage.setContent(message, "text/html; charset=UTF-8");
//        System.out.println("Mail Session has been created successfully..");

        // Step3
//        System.out.println("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", sourceGmailID, sourceGmailPassword);
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        System.out.println("Sent message to " + targetEmailAddress + ".");
    }
}
