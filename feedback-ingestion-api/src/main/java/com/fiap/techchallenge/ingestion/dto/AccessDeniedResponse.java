package com.fiap.techchallenge.ingestion.dto;

@RegisterForReflection
public class AccessDeniedResponse {

    public final String message;

    public AccessDeniedResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}