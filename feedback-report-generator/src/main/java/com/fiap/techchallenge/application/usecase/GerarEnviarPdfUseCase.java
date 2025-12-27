package com.fiap.techchallenge.application.usecase;

import com.fiap.techchallenge.domain.model.JobInput;
import com.fiap.techchallenge.domain.service.EmailSender;
import com.fiap.techchallenge.domain.service.IdempotencyService;
import com.fiap.techchallenge.domain.service.PdfGenerator;
import com.fiap.techchallenge.domain.service.StorageService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GerarEnviarPdfUseCase {

    private final PdfGenerator pdfGenerator;
    private final StorageService storageService;
    private final EmailSender emailSender;
    private final IdempotencyService idempotencyService;

    public GerarEnviarPdfUseCase(
            PdfGenerator pdfGenerator,
            StorageService storageService,
            EmailSender emailSender,
            IdempotencyService idempotencyService
    ) {
        this.pdfGenerator = pdfGenerator;
        this.storageService = storageService;
        this.emailSender = emailSender;
        this.idempotencyService = idempotencyService;
    }

    public void execute(JobInput input) {
        if (idempotencyService.foiProcessado(input.jobId())) {
            return;
        }

        byte[] pdf = pdfGenerator.gerarPdf();

        String url = storageService.store(pdf, "relatorio.pdf");

        emailSender.send(
                "Relatório disponível",
                "Seu relatório foi gerado: " + url
        );

        idempotencyService.sendoProcessado(input.jobId());
    }
}
