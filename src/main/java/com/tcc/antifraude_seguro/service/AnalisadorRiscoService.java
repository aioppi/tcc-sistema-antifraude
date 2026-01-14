package com.tcc.antifraude_seguro.service;

import com.tcc.antifraude_seguro.model.Transacao;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
public class AnalisadorRiscoService {

    /**
     * Analisa a transação e define status + score de risco
     */
    public void analisar(Transacao transacao) {
        double score = calcularScore(transacao);
        transacao.setScoreRisco(score);

        // Define status baseado no score
        if (score >= 70) {
            transacao.setStatus("BLOQUEADA");  // ← CORRIGIDO!
        } else if (score >= 40) {
            transacao.setStatus("REVISAO");
        } else {
            transacao.setStatus("APROVADA");   // ← CORRIGIDO!
        }
    }

    /**
     * Calcula score de risco (0-100%)
     */
    private double calcularScore(Transacao transacao) {
        double score = 0;

        // REGRA 1: Valor da transação (peso 40%)
        score += calcularScoreValor(transacao.getValor());

        // REGRA 2: Horário (peso 30%)
        score += calcularScoreHorario(transacao.getDataHora().toLocalTime());

        // REGRA 3: Tipo de transação (peso 20%)
        score += calcularScoreTipo(transacao.getTipo());

        // REGRA 4: Dia da semana (peso 10%)
        score += calcularScoreDiaSemana(transacao.getDataHora().getDayOfWeek());

        // Garante que score não ultrapassa 100
        return Math.min(score, 100.0);
    }

    /**
     * REGRA 1: Valor alto = mais suspeito (AJUSTADO)
     */
    private double calcularScoreValor(Double valor) {
        if (valor == null || valor <= 0) {
            return 0;
        }

        if (valor > 15000) {
            return 50;  // Valor extremo
        } else if (valor > 10000) {
            return 45;
        } else if (valor > 5000) {
            return 40;
        } else if (valor > 2000) {
            return 30;
        } else if (valor >= 1000) {  // ← CORRIGIDO: >= em vez de >
            return 20;
        } else if (valor >= 500) {   // ← CORRIGIDO: >= em vez de >
            return 10;
        }

        return 0;  // Valor baixo = seguro
    }

    /**
     * REGRA 2: Madrugada = mais suspeito
     */
    private double calcularScoreHorario(LocalTime hora) {
        if (hora == null) {
            return 0;
        }

        int h = hora.getHour();

        // Madrugada (00h-06h)
        if (h >= 0 && h < 6) {
            return 30;
        }

        // Noite (22h-23h)
        if (h >= 22) {
            return 20;
        }

        // Final da tarde (17h-19h) - horário de pico legítimo
        if (h >= 17 && h < 20) {
            return 0;
        }

        // Horário comercial (09h-17h) - mais seguro
        if (h >= 9 && h < 17) {
            return 0;
        }

        // Manhã cedo (06h-09h)
        if (h >= 6 && h < 9) {
            return 5;
        }

        // Noite inicial (20h-22h)
        return 10;
    }

    /**
     * REGRA 3: PIX mais usado em fraudes (AJUSTADO)
     */
    private double calcularScoreTipo(String tipo) {
        if (tipo == null) {
            return 0;
        }

        switch (tipo.toUpperCase()) {
            case "PIX":
                return 25;  // AUMENTADO! PIX é muito usado em fraudes
            case "TED":
                return 15;  // AUMENTADO!
            case "CARTAO":
                return 12;  // AUMENTADO!
            case "DOC":
                return 8;
            case "BOLETO":
                return 5;
            default:
                return 0;
        }
    }

    /**
     * REGRA 4: Final de semana = menos suporte
     */
    private double calcularScoreDiaSemana(DayOfWeek diaSemana) {
        if (diaSemana == null) {
            return 0;
        }

        // Sábado ou Domingo
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            return 10;
        }

        // Sexta-feira (véspera de feriado, fraudadores aproveitam)
        if (diaSemana == DayOfWeek.FRIDAY) {
            return 5;
        }

        // Dias úteis normais
        return 0;
    }

    /**
     * Método auxiliar para debug - retorna explicação do score
     */
    public String explicarScore(Transacao transacao) {
        StringBuilder explicacao = new StringBuilder();
        explicacao.append("Score: ").append(transacao.getScoreRisco()).append("%\n");
        explicacao.append("Motivos:\n");

        double scoreValor = calcularScoreValor(transacao.getValor());
        if (scoreValor > 0) {
            explicacao.append("- Valor: R$ ")
                    .append(transacao.getValor())
                    .append(" (+").append(scoreValor).append("%)\n");
        }

        double scoreHorario = calcularScoreHorario(transacao.getDataHora().toLocalTime());
        if (scoreHorario > 0) {
            explicacao.append("- Horário: ")
                    .append(transacao.getDataHora().toLocalTime())
                    .append(" (+").append(scoreHorario).append("%)\n");
        }

        double scoreTipo = calcularScoreTipo(transacao.getTipo());
        if (scoreTipo > 0) {
            explicacao.append("- Tipo: ")
                    .append(transacao.getTipo())
                    .append(" (+").append(scoreTipo).append("%)\n");
        }

        double scoreDia = calcularScoreDiaSemana(transacao.getDataHora().getDayOfWeek());
        if (scoreDia > 0) {
            explicacao.append("- Dia: ")
                    .append(transacao.getDataHora().getDayOfWeek())
                    .append(" (+").append(scoreDia).append("%)\n");
        }

        return explicacao.toString();
    }
}