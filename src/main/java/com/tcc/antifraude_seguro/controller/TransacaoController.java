package com.tcc.antifraude_seguro.controller;

import com.tcc.antifraude_seguro.model.Transacao;
import com.tcc.antifraude_seguro.repository.TransacaoRepository;
import com.tcc.antifraude_seguro.service.AnalisadorRiscoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoRepository repository;
    private final AnalisadorRiscoService analisador;  // ← NOVO!

    // Spring injeta AMBOS automaticamente
    public TransacaoController(TransacaoRepository repository,
                               AnalisadorRiscoService analisador) {
        this.repository = repository;
        this.analisador = analisador;  // ← NOVO!
    }

    @GetMapping("/status")
    public String status() {
        return "Sistema Anti-Fraude Operacional - TCC 2025";
    }

    @PostMapping
    public Transacao criar(@RequestBody Transacao transacao) {
        // Define data/hora atual
        transacao.setDataHora(LocalDateTime.now());

        // ========== AQUI É A MÁGICA! ==========
        analisador.analisar(transacao);  // ← Sistema PENSA!
        // ======================================

        // Salva no banco com status e score já definidos
        return repository.save(transacao);
    }

    @GetMapping
    public List<Transacao> listar() {
        return repository.findAll();
    }

    @GetMapping("/estatisticas")
    public Map<String, Object> estatisticas() {
        List<Transacao> todas = repository.findAll();

        long total = todas.size();
        long bloqueadas = todas.stream()
                .filter(t -> "BLOQUEADO".equals(t.getStatus()))
                .count();
        long aprovadas = todas.stream()
                .filter(t -> "APROVADO".equals(t.getStatus()))
                .count();
        long revisao = todas.stream()
                .filter(t -> "REVISAO".equals(t.getStatus()))
                .count();

        // Calcula valores
        double valorTotal = todas.stream()
                .mapToDouble(t -> t.getValor() != null ? t.getValor() : 0)
                .sum();

        double valorBloqueado = todas.stream()
                .filter(t -> "BLOQUEADO".equals(t.getStatus()))
                .mapToDouble(t -> t.getValor() != null ? t.getValor() : 0)
                .sum();

        // Score médio
        double scoreMedia = todas.stream()
                .mapToDouble(t -> t.getScoreRisco() != null ? t.getScoreRisco() : 0)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("bloqueadas", bloqueadas);
        stats.put("aprovadas", aprovadas);
        stats.put("emRevisao", revisao);
        stats.put("valorTotal", String.format("R$ %.2f", valorTotal));
        stats.put("valorBloqueado", String.format("R$ %.2f", valorBloqueado));
        stats.put("scoreMedia", String.format("%.1f%%", scoreMedia));

        // Taxa de bloqueio
        double taxaBloqueio = total > 0 ? (bloqueadas * 100.0 / total) : 0;
        stats.put("taxaBloqueio", String.format("%.1f%%", taxaBloqueio));

        return stats;
    }

    // ========== NOVO ENDPOINT: Explicar decisão ==========
    @GetMapping("/{id}/explicacao")
    public Map<String, Object> explicarDecisao(@PathVariable Long id) {
        Transacao transacao = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        String explicacao = analisador.explicarScore(transacao);

        Map<String, Object> response = new HashMap<>();
        response.put("transacaoId", id);
        response.put("status", transacao.getStatus());
        response.put("scoreRisco", transacao.getScoreRisco());
        response.put("explicacao", explicacao);

        return response;
    }
}