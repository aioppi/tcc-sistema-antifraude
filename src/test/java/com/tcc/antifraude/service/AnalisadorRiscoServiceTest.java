package com.tcc.antifraude.service;

import com.tcc.antifraude_seguro.model.Transacao;
import com.tcc.antifraude_seguro.service.AnalisadorRiscoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalisadorRiscoServiceTest {

    private AnalisadorRiscoService analisador;

    @BeforeEach
    void setUp() {
        analisador = new AnalisadorRiscoService();
    }

    @Test
    @DisplayName("REGRA 1: Valor acima de R$ 15.000 deve adicionar 50 pontos")
    void testValorAlto() {
        Transacao transacao = new Transacao();
        transacao.setValor(20000.0);
        transacao.setTipo("PIX");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(75.0, transacao.getScoreRisco());
    }

    @Test
    @DisplayName("REGRA 1: Valor entre R$ 5.000 e R$ 10.000 deve adicionar 40 pontos")
    void testValorMedio() {
        Transacao transacao = new Transacao();
        transacao.setValor(8000.0);
        transacao.setTipo("BOLETO");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(45.0, transacao.getScoreRisco());
    }

    @Test
    @DisplayName("REGRA 1: Valor abaixo de R$ 500 deve adicionar 0 pontos")
    void testValorBaixo() {
        Transacao transacao = new Transacao();
        transacao.setValor(300.0);
        transacao.setTipo("BOLETO");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(5.0, transacao.getScoreRisco());
        assertEquals("APROVADA", transacao.getStatus());
    }

    @Test
    @DisplayName("REGRA 2: Madrugada (00h-06h) deve adicionar 30 pontos")
    void testHorarioMadrugada() {
        Transacao transacao = new Transacao();
        transacao.setValor(1000.0);         // ← R$ 1.000
        transacao.setTipo("PIX");           // ← PIX
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 3, 0));  // ✅ CORRETO

        analisador.analisar(transacao);

        assertEquals(75.0, transacao.getScoreRisco());  // ← ESPERAVA 75
    }

    @Test
    @DisplayName("REGRA 3: PIX deve adicionar 25 pontos")
    void testTipoPIX() {
        Transacao transacao = new Transacao();
        transacao.setValor(100.0);  // Valor baixo (não adiciona pontos)
        transacao.setTipo("PIX");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(25.0, transacao.getScoreRisco());  // 0+0+25+0 = 25
        assertEquals("APROVADA", transacao.getStatus());
    }

    @Test
    @DisplayName("REGRA 3: BOLETO deve adicionar 5 pontos")
    void testTipoBOLETO() {
        Transacao transacao = new Transacao();
        transacao.setValor(100.0);  // Valor baixo (não adiciona pontos)
        transacao.setTipo("BOLETO");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(5.0, transacao.getScoreRisco());  // 0+0+5+0 = 5
        assertEquals("APROVADA", transacao.getStatus());
    }

    @Test
    @DisplayName("REGRA 4: Sábado deve adicionar 10 pontos")
    void testDiaSabado() {
        Transacao transacao = new Transacao();
        transacao.setValor(100.0);  // Valor baixo (não adiciona pontos)
        transacao.setTipo("BOLETO");  // BOLETO = 5 pontos
        transacao.setDataHora(LocalDateTime.of(2026, 1, 17, 14, 0));  // Sábado

        analisador.analisar(transacao);

        assertEquals(15.0, transacao.getScoreRisco());  // 0+0+5+10 = 15
        assertEquals("APROVADA", transacao.getStatus());
    }

    @Test
    @DisplayName("DECISÃO: Score >= 70 deve resultar em BLOQUEADA")
    void testStatusBloqueada() {
        Transacao transacao = new Transacao();
        transacao.setValor(20000.0);
        transacao.setTipo("PIX");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 3, 0));

        analisador.analisar(transacao);

        assertTrue(transacao.getScoreRisco() >= 70);
        assertEquals("BLOQUEADA", transacao.getStatus());
    }

    @Test
    @DisplayName("DECISÃO: Score entre 40-69 deve resultar em REVISAO")
    void testStatusRevisao() {
        Transacao transacao = new Transacao();
        transacao.setValor(8000.0);
        transacao.setTipo("PIX");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(65.0, transacao.getScoreRisco());
        assertEquals("REVISAO", transacao.getStatus());
    }

    @Test
    @DisplayName("DECISÃO: Score < 40 deve resultar em APROVADA")
    void testStatusAprovada() {
        Transacao transacao = new Transacao();
        transacao.setValor(300.0);
        transacao.setTipo("BOLETO");
        transacao.setDataHora(LocalDateTime.of(2026, 1, 13, 14, 0));

        analisador.analisar(transacao);

        assertEquals(5.0, transacao.getScoreRisco());
        assertEquals("APROVADA", transacao.getStatus());
    }
}