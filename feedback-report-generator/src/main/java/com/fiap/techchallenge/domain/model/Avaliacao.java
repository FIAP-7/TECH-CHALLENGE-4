package com.fiap.techchallenge.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RegisterForReflection
public class Avaliacao {

    public static DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static DateTimeFormatter FORMATTER_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

        if(this.dataEnvioStr != null){
            try {
                dataEnvio = OffsetDateTime.parse(this.dataEnvioStr).toLocalDate();
            } catch (Exception e) {
                try {
                    dataEnvio = LocalDate.parse(this.dataEnvioStr);
                } catch (Exception e2) {
                    System.out.println("Error ao converter data");
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
