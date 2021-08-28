package com.studies.libraryapi.service.impl;

import com.studies.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.default-remetent}")
    private String rememtent;

    @Override
    public void sendEmails(String message, List<String> mailsList) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String[] mails = mailsList.toArray(new String[mailsList.size()]);

        mailMessage.setFrom(rememtent);
        mailMessage.setSubject("Loan Late Book");
        mailMessage.setText(message);
        mailMessage.setTo(mails);

        javaMailSender.send(mailMessage);
    }

}
