package com.fiap.techchallenge.notification.repository;

import com.fiap.techchallenge.notification.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FeedbackRepository {

    private static final Logger LOG = Logger.getLogger(FeedbackRepository.class);

    @Inject
    DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "feedback.dynamodb.table-name")
    String tableName;

    public Feedback findById(String feedbackId) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("FeedbackID", AttributeValue.builder().s(feedbackId).build()))
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(request);
            Map<String, AttributeValue> item = response.item();
            if (item == null || item.isEmpty()) {
                return null;
            }

            Feedback f = new Feedback();
            f.setFeedbackId(feedbackId);
            if (item.containsKey("nota") && item.get("nota").n() != null) {
                try {
                    f.setNota(Integer.parseInt(item.get("nota").n()));
                } catch (NumberFormatException e) {
                    LOG.warnf("Valor de nota inválido no item do DynamoDB para feedback %s: %s", feedbackId, item.get("nota").n());
                }
            }
            if (item.containsKey("descricao") && item.get("descricao").s() != null) {
                f.setDescricao(item.get("descricao").s());
            }
            if (item.containsKey("dataEnvio") && item.get("dataEnvio").s() != null) {
                f.setDataEnvio(item.get("dataEnvio").s());
            }
            if (item.containsKey("status") && item.get("status").s() != null) {
                f.setStatus(item.get("status").s());
            }
            if (item.containsKey("dataProcessamento") && item.get("dataProcessamento").s() != null) {
                f.setDataProcessamento(item.get("dataProcessamento").s());
            }
            return f;
        } catch (SdkException e) {
            throw new RuntimeException("Falha ao consultar o DynamoDB", e);
        }
    }

    public void updateStatus(String feedbackId, String status, String dataProcessamento) {
        try {
            Map<String, AttributeValue> key = Map.of("FeedbackID", AttributeValue.builder().s(feedbackId).build());
            Map<String, String> exprAttrNames = new HashMap<>();
            exprAttrNames.put("#s", "status");
            exprAttrNames.put("#dp", "dataProcessamento");

            Map<String, AttributeValue> exprAttrValues = new HashMap<>();
            exprAttrValues.put(":s", AttributeValue.builder().s(status).build());
            exprAttrValues.put(":dp", AttributeValue.builder().s(dataProcessamento).build());

            UpdateItemRequest update = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .updateExpression("SET #s = :s, #dp = :dp")
                    .expressionAttributeNames(exprAttrNames)
                    .expressionAttributeValues(exprAttrValues)
                    .build();
            dynamoDbClient.updateItem(update);
        } catch (SdkException e) {
            throw new RuntimeException("Falha ao atualizar status no DynamoDB", e);
        }
    }
}