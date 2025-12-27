package com.fiap.techchallenge.infrastructure.email;

import com.fiap.techchallenge.domain.service.EmailSender;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@ApplicationScoped
public class SesEmailSender implements EmailSender {

    private final SesClient sesClient;

    @ConfigProperty(name = "email.admin.address")
    String adminEmail;

    @ConfigProperty(name = "email.source.address")
    String sourceEmail;

    public SesEmailSender(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    @Override
    public void send(String tituloEmail, String corpoEmail) {
        SendEmailRequest email = SendEmailRequest.builder()
                .destination(Destination.builder()
                        .toAddresses(adminEmail)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder().data(tituloEmail).charset("UTF-8").build())
                        .body(Body.builder()
                                .text(Content.builder().data(corpoEmail).charset("UTF-8").build())
                                .build())
                        .build())
                .source(sourceEmail)
                .build();

        sesClient.sendEmail(email);
    }
}
