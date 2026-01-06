package com.fiap.techchallenge.domain.model;

import java.util.List;
import java.util.Map;

public record PdfData(
        String title,
        Long quantidadeTotalAvaliacoes,
        Double mediaAvaliacoes,
        Long quantidadeAvaliacoesCriticas,
        List<Avaliacao> avaliacoes,
        Map<String, Long> mapQuatidadeAvaliacaoData,
        Map<Integer, Long> mapQuantidadeAvaliacaoNota
        ) {
}
