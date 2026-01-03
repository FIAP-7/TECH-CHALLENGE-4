package com.fiap.techchallenge.infrastructure.dynamo;

import com.fiap.techchallenge.domain.model.Avaliacao;
import com.fiap.techchallenge.domain.model.PdfData;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class DynamoPdfDataService implements com.fiap.techchallenge.domain.service.PdfDataService {

    private final DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "FEEDBACK_DYNAMODB_TABLE_NAME")
    String table;

    public DynamoPdfDataService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public PdfData avaliacoesUltimaSemana() {
        ScanResponse response = dynamoDbClient.scan(
                ScanRequest.builder()
                        .tableName(table)
                        .build()
        );

        List<Avaliacao> itensDatabase = response.items().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        LocalDate umaSemanaAtras = LocalDate.now().minus(Period.ofWeeks(1));

        List<Avaliacao> itensSemana = itensDatabase.stream()
                .filter(item -> item.getDataEnvio() != null && !item.getDataEnvio().isEmpty())
                .filter(item -> {
                    try {
                        // Tenta ler como Timestamp ISO (2025-12-24T01:12:05Z)
                        LocalDate date = OffsetDateTime.parse(item.getDataEnvio()).toLocalDate();
                        return date.isAfter(umaSemanaAtras);
                    } catch (Exception e) {
                        try {
                            // Se falhar, tenta ler como data simples (2025-12-24)
                            LocalDate date = LocalDate.parse(item.getDataEnvio());
                            return date.isAfter(umaSemanaAtras);
                        } catch (Exception e2) {
                            return false; // Ignora registros com data inválida
                        }
                    }
                }).collect(Collectors.toList());

        double mediaNotas = itensSemana.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        long qtdAvaliacoes = (long) itensSemana.size();

        long qtdAvaliacoesCriticas = itensSemana.stream()
                .filter(item -> item.getNota() < 2)
                .count();

        return new PdfData("Avaliacoes.pdf", qtdAvaliacoes, mediaNotas, qtdAvaliacoesCriticas);
    }

    private Avaliacao toDomain(Map<String, AttributeValue> item) {
        return new Avaliacao(
                getItemString(item, "FeedbackID", "unknown"),
                getItemString(item, "descricao", ""),
                getItemInteger(item, "nota", 0),
                getItemString(item, "status", "PENDING"),
                getItemString(item, "dataEnvio", ""),
                getItemString(item, "userId", "anonymous")
        );
    }

    private String getItemString(Map<String, AttributeValue> item, String key, String defaultValue) {
        return Optional.ofNullable(item.get(key))
                .map(AttributeValue::s)
                .orElse(defaultValue);
    }

    private int getItemInteger(Map<String, AttributeValue> item, String key, int defaultValue) {
        return Optional.ofNullable(item.get(key))
                .map(AttributeValue::n)
                .map(n -> {
                    try {
                        return Integer.parseInt(n);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}