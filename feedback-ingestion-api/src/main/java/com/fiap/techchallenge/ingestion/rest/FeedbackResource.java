package com.fiap.techchallenge.ingestion.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.ingestion.dto.FeedbackRequest;
import com.fiap.techchallenge.ingestion.model.Feedback;
import com.fiap.techchallenge.ingestion.service.FeedbackService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

@Path("/avaliacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackResource {

    @Inject
    FeedbackService service;

    @Context
    HttpHeaders headers;

    private static final String REQUIRED_GROUP = "Alunos";

    @POST
    public Response criarAvaliacao(@Valid FeedbackRequest request) {
        if (!isAluno(headers)) {
            return Response.status(Status.FORBIDDEN)
                    .entity(new ErrorMessage("Acesso negado: seu perfil não tem permissão para enviar avaliações."))
                    .build();
        }
        Feedback feedback = service.processarFeedback(request);
        return Response.status(Status.CREATED).entity(feedback).build();
    }

    private boolean isAluno(HttpHeaders headers) {
        try {
            String auth = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (auth == null || auth.isBlank()) return false;
            String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadJson);

            // Prefer Cognito claim "cognito:groups" (array). Fallback to "groups" if present.
            JsonNode groupsNode = payload.get("cognito:groups");
            if (groupsNode == null) {
                groupsNode = payload.get("groups");
            }
            if (groupsNode == null) return false;

            if (groupsNode.isArray()) {
                Iterator<JsonNode> it = groupsNode.elements();
                while (it.hasNext()) {
                    JsonNode n = it.next();
                    if (REQUIRED_GROUP.equalsIgnoreCase(n.asText())) return true;
                }
            } else if (groupsNode.isTextual()) {
                // Some setups send a single string or comma-separated list
                String text = groupsNode.asText();
                for (String g : text.split(",")) {
                    if (REQUIRED_GROUP.equalsIgnoreCase(g.trim())) return true;
                }
            }
        } catch (Exception ignored) {
            // Any parsing error -> treat as not authorized
        }
        return false;
    }

    // Simple error message DTO to keep response JSON consistent
    static class ErrorMessage {
        public final String message;
        ErrorMessage(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
