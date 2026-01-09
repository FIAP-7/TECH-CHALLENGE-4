package com.fiap.techchallenge.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RegisterForReflection
public class Avaliacao {

    public static DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static DateTimeFormatter FORMATTER_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private String feedbackId;
    private String descricao;
    private Integer nota;
    private String status;
    private String dataEnvioStr;
    private LocalDate dataEnvio;

    public Avaliacao(String feedbackId, String descricao, Integer nota, String status, String dataEnvioStr) {
        this.feedbackId = feedbackId;
        this.descricao = descricao;
        this.nota = nota;
        this.status = status;
        this.dataEnvioStr = dataEnvioStr;

        if (this.dataEnvioStr != null) {
            // Prefer parse as Instant (ISO_INSTANT like 2026-01-09T00:00:00Z) and convert to São Paulo local date
            try {
                Instant instant = Instant.parse(this.dataEnvioStr);
                dataEnvio = instant.atZone(ZONE_SAO_PAULO).toLocalDate();
            } catch (Exception e) {
                try {
                    // Fallback: try OffsetDateTime (keeps offset information) and convert to São Paulo date
                    dataEnvio = OffsetDateTime.parse(this.dataEnvioStr).atZoneSameInstant(ZONE_SAO_PAULO).toLocalDate();
                } catch (Exception e2) {
                    try {
                        // Fallback: already a local date string (yyyy-MM-dd)
                        dataEnvio = LocalDate.parse(this.dataEnvioStr);
                    } catch (Exception e3) {
                        System.out.println("Error ao converter data: " + this.dataEnvioStr);
                    }
                }
            }
        }
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataEnvioStr() {
        return dataEnvioStr;
    }

    public void setDataEnvioStr(String dataEnvioStr) {
        this.dataEnvioStr = dataEnvioStr;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
