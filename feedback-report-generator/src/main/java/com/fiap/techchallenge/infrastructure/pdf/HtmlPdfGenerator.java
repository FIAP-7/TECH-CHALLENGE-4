package com.fiap.techchallenge.infrastructure.pdf;

import com.fiap.techchallenge.domain.model.PdfData;
import com.fiap.techchallenge.domain.service.PdfGenerator;
import com.fiap.techchallenge.infrastructure.dynamo.DynamoPdfDataService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.awt.*;
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

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph(pdfData.title(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            Font subtituloFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font destaqueFont = new Font(Font.HELVETICA, 12, Font.BOLD);

            Font bodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
            document.add(new Paragraph(new Phrase("Media de avaliações da ultima semana: " + pdfData.mediaAvaliacoes(), bodyFont)));
            document.add(new Paragraph(new Phrase("Quatidade de avaliações da ultima semana: " + pdfData.quantidadeTotalAvaliacoes(), bodyFont)));
            document.add(new Paragraph(new Phrase("Quatidade de avaliações criticas: " + pdfData.quantidadeAvaliacoesCriticas(), bodyFont)));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Quantidade de avaliações por dia", subtituloFont));
            document.add(new Paragraph(" "));

            PdfPTable tableDia = new PdfPTable(2);

            tableDia.setWidthPercentage(100);
            tableDia.setWidths(new float[]{4f,2f});

            tableDia.addCell(new Phrase("Data da Avaliação", destaqueFont));
            tableDia.addCell(new Phrase("Quantidade de Avaliações", destaqueFont));

            pdfData.mapQuatidadeAvaliacaoData().forEach((chave, valor) -> {
                tableDia.addCell(chave);
                tableDia.addCell(String.valueOf(valor));
            });

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Quantidade de avaliações por urgência", subtituloFont));
            document.add(new Paragraph(" "));

            PdfPTable tableUrgencia = new PdfPTable(2);

            tableUrgencia.setWidthPercentage(100);
            tableUrgencia.setWidths(new float[]{4f,2f});

            tableUrgencia.addCell(new Phrase("Nivel de urgência", destaqueFont));
            tableUrgencia.addCell(new Phrase("Quantidade de Avaliações", destaqueFont));

            pdfData.mapQuatidadeAvaliacaoData().forEach((chave, valor) -> {
                tableUrgencia.addCell(chave);
                tableUrgencia.addCell(String.valueOf(valor));
            });

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Avalições", subtituloFont));
            document.add(new Paragraph(" "));

            if (pdfData.avaliacoes() == null || pdfData.avaliacoes().isEmpty()) {
                document.add(new Paragraph("Não possui avaliações nessa semana.", bodyFont));
            } else{
                pdfData.avaliacoes().forEach(item -> {
                    linhaAvaliacao("Data de envio: ", destaqueFont, item.getDataEnvio(), bodyFont, document);
                    linhaAvaliacao("Urgencia: ", destaqueFont, item.getStatus(), bodyFont, document);
                    linhaAvaliacao("Descrição: ", destaqueFont, item.getDescricao(), bodyFont, document);

                    LineSeparator line = new LineSeparator();
                    line.setLineWidth(0.5f);
                    line.setPercentage(80);
                    line.setLineColor(Color.GRAY);
                    line.setOffset(5);

                    document.add(line);
                });
            }

            document.close();

            LOG.debug("PDF Gerado com sucesso");
            return baos.toByteArray();
        } catch (Exception e) {
            LOG.error("Ocorreu um erro ao gerar o PDF [" + e.getMessage() + "]");
            throw new RuntimeException("Ocorreu um erro ao gerar o PDF", e);
        }
    }

    private static void linhaAvaliacao(String contentSubtitulo, Font destaqueFont, String conteudo, Font bodyFont, Document document) {
        Chunk subtituloItem = new Chunk(contentSubtitulo, destaqueFont);

        Chunk dataItem = new Chunk(conteudo, bodyFont);

        Phrase phrase = new Phrase();

        phrase.add(subtituloItem);
        phrase.add(dataItem);

        document.add(new Paragraph(phrase));
    }
}
