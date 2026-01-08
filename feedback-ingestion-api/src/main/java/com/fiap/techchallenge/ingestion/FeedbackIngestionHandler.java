package com.fiap.techchallenge.ingestion;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.ingestion.dto.FeedbackRequest;
import com.fiap.techchallenge.ingestion.model.Feedback;
import com.fiap.techchallenge.ingestion.service.FeedbackService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

@ApplicationScoped
@Named("feedback")
public class FeedbackIngestionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger LOG = Logger.getLogger(FeedbackIngestionHandler.class);

    @Inject
    FeedbackService service;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        try {
            String bodyIn = (input == null) ? null : input.getBody();
            if (bodyIn == null || bodyIn.isBlank()) {
                return response(400, "{\"message\":\"Corpo da requisição vazio\"}");
            }

            FeedbackRequest request = objectMapper.readValue(bodyIn, FeedbackRequest.class);
            Feedback feedback = service.processarFeedback(request);
            String body = objectMapper.writeValueAsString(feedback);
            return response(201, body);
        } catch (Exception e) {
            LOG.error("Erro ao processar requisição de feedback", e);
            return response(500, "{\"message\":\"Erro interno\"}");
        }
    }

    private APIGatewayV2HTTPResponse response(int status, String body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(status)
                .withHeaders(java.util.Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*"
                ))
                .withBody(body)
                .build();
    }
}
