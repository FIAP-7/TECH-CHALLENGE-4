package com.fiap.techchallenge.infrastructure.pdf;

import com.fiap.techchallenge.domain.model.PdfData;
import com.fiap.techchallenge.domain.service.PdfGenerator;
import com.fiap.techchallenge.infrastructure.dynamo.DynamoPdfDataService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;

@ApplicationScoped
public class HtmlPdfGenerator implements PdfGenerator {

    private static final Logger LOG = Logger.getLogger(HtmlPdfGenerator.class);

    private final DynamoPdfDataService dynamoPdfDataService;

    public HtmlPdfGenerator(DynamoPdfDataService dynamoPdfDataService) {
        this.dynamoPdfDataService = dynamoPdfDataService;
    }

    @Override
    public byte[] gerarPdf() {
        PdfData pdfData = dynamoPdfDataService.avaliacoesUltimaSemana();

        String html = """
                <html>
                  <body style="font-family: Arial">
                    <h1>%s</h1>
                    <br/>
                    <p>
                        <span>Media de avaliações da ultima semana: %s</span>
                    </p>
                    <p>
                        <span>Quatidade de avaliações da ultima semana: %s</span>
                    </p>
                    <p>
                        <span>Quatidade de avaliações criticas: %s</span>
                    </p>
                  </body>
                </html>
                """
                .formatted(
                        pdfData.title(),
                        pdfData.mediaAvaliacoes(),
                        pdfData.quantidadeTotalAvaliacoes(),
                        pdfData.quantidadeAvaliacoesCriticas()
                );

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();

            LOG.debug("PDF Gerado com sucesso");

            return baos.toByteArray();
        }catch (Exception e){
            LOG.error("Ocorreu um erro ao gerar o PDF [" + e.getMessage() + "]");

            throw new RuntimeException("Ocorreu um erro ao gerar o PDF", e);
        }
    }
}
