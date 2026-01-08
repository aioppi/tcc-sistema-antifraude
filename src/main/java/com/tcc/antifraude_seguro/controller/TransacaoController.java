package com.tcc.antifraude_seguro.controller;

import com.tcc.antifraude_seguro.model.Transacao;
import com.tcc.antifraude_seguro.repository.TransacaoRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoRepository repository;

    // Spring injeta automaticamente (Dependency Injection)
    public TransacaoController(TransacaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/status")
    public String status() {
        return "Sistema Anti-Fraude Operacional - TCC 2025";
    }

    @PostMapping
    public Transacao criar(@RequestBody Transacao transacao) {
        // Define data/hora atual
        transacao.setDataHora(LocalDateTime.now());

        // Por enquanto, aprova tudo (depois adicionamos lógica)
        transacao.setStatus("PENDENTE");
        transacao.setScoreRisco(0.0);

        // Salva no banco e retorna
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
        long pendentes = todas.stream()
                .filter(t -> "PENDENTE".equals(t.getStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("bloqueadas", bloqueadas);
        stats.put("aprovadas", aprovadas);
        stats.put("pendentes", pendentes);

        return stats;
    }
}