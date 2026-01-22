package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless(name = "EmailEJB")
public class EmailBean {
    @Resource(name = "java:/jboss/mail/fakeSMTP")
    private Session session;


    private static final Logger logger = Logger.getLogger("EmailBean.logger");

    private static final List<Map<String, String>> sentEmails = new ArrayList<>();

    public void send(String to, String subject, String body) {
        Thread emailJob = new Thread(() -> {
            Message message = new MimeMessage(session);
            Date date = new Date();
            try{
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
                message.setSubject(subject);
                message.setText(body);
                message.setSentDate(date);
                Transport.send(message);

                synchronized (sentEmails) {
                    sentEmails.add(Map.of(
                            "to", to,
                            "subject", subject,
                            "body", body,
                            "sentAt", date.toString()
                    ));
                }
            }catch(MessagingException e){
                logger.log(Level.SEVERE,e.getMessage());
            }
        });
        emailJob.start();
    }

    public List<Map<String, String>> getEmailsForUser(String email) {
        synchronized (sentEmails) {
            List<Map<String, String>> userEmails = new ArrayList<>();
            for (Map<String, String> e : sentEmails) {
                if (e.get("to").equals(email)) {
                    userEmails.add(e);
                }
            }
            return userEmails;
        }
    }

    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "Password Reset Request";
        String body = "Para redefinir a sua password, use o seguinte token:\\n\\n\" +\n" + resetToken +
                "            \"\\n\\n\" +\n" +
                "            \"Cole este token no campo apropriado da aplicação e defina a sua nova password.\\n\\n\" +\n" +
                "            \"Se não solicitou isto, ignore este email.    ";

        send(email, subject, body);
    }

    public void notifyTagSubscribers(Tag tag, Publication publication, String message) {
        if (tag.getSubscribers() == null || tag.getSubscribers().isEmpty()) {
            return;
        }

        for (User subscriber : tag.getSubscribers()) {
            send(
                    subscriber.getEmail(),
                    "Update on tag: " + tag.getName(),
                    message + "\n\nPublication: " + publication.getTitle()
            );
        }
    }

}
