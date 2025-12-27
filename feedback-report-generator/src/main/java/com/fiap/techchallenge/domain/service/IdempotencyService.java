package com.fiap.techchallenge.domain.service;

public interface IdempotencyService {
    boolean foiProcessado(String jobId);
    void sendoProcessado(String jobId);
}
