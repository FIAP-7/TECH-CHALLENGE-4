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
                .filter(item -> item.getDataEnvioStr() != null && !item.getDataEnvioStr().isEmpty())
                .filter(item -> {
                    try {
                        LocalDate date = OffsetDateTime.parse(item.getDataEnvioStr()).toLocalDate();
                        return date.isAfter(umaSemanaAtras);
                    } catch (Exception e) {
                        try {
                            LocalDate date = LocalDate.parse(item.getDataEnvioStr());
                            return date.isAfter(umaSemanaAtras);
                        } catch (Exception e2) {
                            return false;
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

        Map<String, Long> mapQuatidadeAvaliacaoData = itensSemana.stream().collect(
                Collectors.groupingBy(
                        avaliacao -> avaliacao.getDataEnvio().format(Avaliacao.FORMATTER_DATE),
                        Collectors.counting()
                )
        );

        Map<Integer, Long> mapQuantidadeAvaliacaoNota = itensSemana.stream().collect(
                Collectors.groupingBy(
                        Avaliacao::getNota,
                        Collectors.counting()
                )
        );

        return new PdfData("Avaliacoes", qtdAvaliacoes, mediaNotas, qtdAvaliacoesCriticas, itensSemana, mapQuatidadeAvaliacaoData, mapQuantidadeAvaliacaoNota);
    }

    private Avaliacao toDomain(Map<String, AttributeValue> item) {
        return new Avaliacao(
                getItemString(item, "FeedbackID", "unknown"),
                getItemString(item, "descricao", ""),
                getItemInteger(item, "nota", 0),
                getItemString(item, "status", "PENDING"),
                getItemString(item, "dataEnvio", "")
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