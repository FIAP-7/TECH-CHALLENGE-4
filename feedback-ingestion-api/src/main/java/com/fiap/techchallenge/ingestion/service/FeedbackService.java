package com.fiap.techchallenge.ingestion.service;

import com.fiap.techchallenge.ingestion.dto.FeedbackRequest;
import com.fiap.techchallenge.ingestion.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FeedbackService {

    private static final Logger LOG = Logger.getLogger(FeedbackService.class);

    // Lazy initialize AWS SDK clients to drastically reduce Lambda cold-start time
    @Inject
    Provider<DynamoDbClient> dynamoDbClientProvider;

    @Inject
    Provider<SqsClient> sqsClientProvider;

    @ConfigProperty(name = "feedback.dynamodb.table-name")
    String tableName;

    @ConfigProperty(name = "feedback.sqs.queue-url")
    String queueUrl;

    public Feedback processarFeedback(FeedbackRequest request) {
        String id = UUID.randomUUID().toString();
        String agora = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        Feedback feedback = new Feedback();
        feedback.setFeedbackId(id);
        feedback.setDescricao(request.getDescricao());
        feedback.setNota(request.getNota());
        feedback.setStatus("PENDENTE");
        feedback.setDataEnvio(agora);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("FeedbackID", AttributeValue.builder().s(id).build());
        if (request.getDescricao() != null) {
            item.put("descricao", AttributeValue.builder().s(request.getDescricao()).build());
        }
        if (request.getNota() != null) {
            item.put("nota", AttributeValue.builder().n(Integer.toString(request.getNota())).build());
        }
        item.put("status", AttributeValue.builder().s("PENDENTE").build());
        item.put("dataEnvio", AttributeValue.builder().s(agora).build());

        PutItemRequest put = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        // Initialize DynamoDB client only when needed (first request)
        dynamoDbClientProvider.get().putItem(put);

        LOG.infof("INFO: Feedback [%s] criado com status PENDENTE.", id);

        String payload = "{\"feedbackId\":\"" + id + "\"}";
        if (queueUrl != null && !queueUrl.isBlank()) {
            SendMessageRequest send = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();
            // Initialize SQS client only when needed
            sqsClientProvider.get().sendMessage(send);
        } else {
            LOG.warn("FEEDBACK_SUBMITTED_QUEUE_URL não configurada; pulando envio para SQS.");
        }

        return feedback;
    }
}
