package ch.so.agi.ilivalidator.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.File;

@Service
public class MailService {

    private String mailUsername;

    private JavaMailSender emailSender;
    
    public MailService(@Value("${spring.mail.username}") String mailUsername, JavaMailSender emailSender) {
        this.mailUsername = mailUsername;
        this.emailSender = emailSender;
    }
    
    public void send(String toAddress, String subject, String content, String fileToAttach) throws MailException {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception
            {                
                FileSystemResource file = new FileSystemResource(new File(fileToAttach));
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.addTo(new InternetAddress(toAddress));
                helper.setFrom(new InternetAddress(mailUsername));
                helper.setSubject(subject);
                helper.setText(content);
                helper.addAttachment(file.getFilename(), file);
            }
        };

        emailSender.send(preparator);            
    }

}
