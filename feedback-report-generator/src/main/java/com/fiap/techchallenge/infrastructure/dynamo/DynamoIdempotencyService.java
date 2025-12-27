package com.fiap.techchallenge.infrastructure.dynamo;

import com.fiap.techchallenge.domain.service.IdempotencyService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.Map;

@ApplicationScoped
public class DynamoIdempotencyService implements IdempotencyService {

    private final DynamoDbClient dynamo;

    @ConfigProperty(name="idempotency.dynamodb.table-name")
    String table;

    public DynamoIdempotencyService(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public boolean foiProcessado(String jobId) {
        GetItemResponse response = dynamo.getItem(
          GetItemRequest.builder()
                  .tableName(table)
                  .key(Map.of(
                          "job_id", AttributeValue.fromS(jobId)
                  ))
                  .build()
        );

        return response.hasItem();
    }

    @Override
    public void sendoProcessado(String jobId) {
        dynamo.putItem(
          PutItemRequest.builder()
                  .tableName(table)
                  .item(Map.of(
                          "job_id", AttributeValue.fromS(jobId),
                          "dt_processamento", AttributeValue.fromS(Instant.now().toString())
                  ))
                  .build()
        );
    }
}
