package com.tcc.antifraude_seguro.service;

import com.tcc.antifraude_seguro.model.Transacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
public class AnalisadorRiscoService {

    @Autowired(required = false)
    private MlFraudeClient mlFraudeClient;

    public void analisar(Transacao transacao) {
        double score = calcularScore(transacao);
        transacao.setScoreRisco(score);

        if (score >= 70) {
            transacao.setStatus("BLOQUEADA");
        } else if (score >= 40) {
            // Zona cinzenta: consulta o modelo ML para refinar
            String statusMl = consultarML(transacao.getValor(), score);
            transacao.setStatus(statusMl);
        } else {
            transacao.setStatus("APROVADA");
        }
    }

    private String consultarML(double valor, double scoreRegras) {
        if (mlFraudeClient == null) {
            return "REVISAO"; // fallback: sem ML, mantém revisão
        }

        Double probFraude = mlFraudeClient.obterProbabilidadeFraude(valor);

        if (probFraude == null) {
            return "REVISAO"; // fallback: ML indisponível
        }

        // Combina score de regras (60%) com ML (40%)
        double scoreFinal = (scoreRegras * 0.6) + (probFraude * 100 * 0.4);

        if (scoreFinal >= 70) return "BLOQUEADA";
        if (scoreFinal >= 40) return "REVISAO";
        return "APROVADA";
    }

    private double calcularScore(Transacao transacao) {
        double score = 0;
        score += calcularScoreValor(transacao.getValor());
        score += calcularScoreHorario(transacao.getDataHora().toLocalTime());
        score += calcularScoreTipo(transacao.getTipo());
        score += calcularScoreDiaSemana(transacao.getDataHora().getDayOfWeek());
        return Math.min(score, 100.0);
    }

    private double calcularScoreValor(Double valor) {
        if (valor == null || valor <= 0) return 0;
        if (valor > 15000) return 50;
        else if (valor > 10000) return 45;
        else if (valor > 5000) return 40;
        else if (valor > 2000) return 30;
        else if (valor >= 1000) return 20;
        else if (valor >= 500) return 10;
        return 0;
    }

    private double calcularScoreHorario(LocalTime hora) {
        if (hora == null) return 0;
        int h = hora.getHour();
        if (h >= 0 && h < 6) return 30;
        if (h >= 22) return 20;
        if (h >= 17 && h < 20) return 0;
        if (h >= 9 && h < 17) return 0;
        if (h >= 6 && h < 9) return 5;
        return 10;
    }

    private double calcularScoreTipo(String tipo) {
        if (tipo == null) return 0;
        switch (tipo.toUpperCase()) {
            case "PIX": return 25;
            case "TED": return 15;
            case "CARTAO": return 12;
            case "DOC": return 8;
            case "BOLETO": return 5;
            default: return 0;
        }
    }

    private double calcularScoreDiaSemana(DayOfWeek diaSemana) {
        if (diaSemana == null) return 0;
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) return 10;
        if (diaSemana == DayOfWeek.FRIDAY) return 5;
        return 0;
    }

    public String explicarScore(Transacao transacao) {
        StringBuilder explicacao = new StringBuilder();
        explicacao.append("Score: ").append(transacao.getScoreRisco()).append("%\n");
        explicacao.append("Motivos:\n");

        double scoreValor = calcularScoreValor(transacao.getValor());
        if (scoreValor > 0)
            explicacao.append("- Valor: R$ ").append(transacao.getValor())
                    .append(" (+").append(scoreValor).append("%)\n");

        double scoreHorario = calcularScoreHorario(transacao.getDataHora().toLocalTime());
        if (scoreHorario > 0)
            explicacao.append("- Horário: ").append(transacao.getDataHora().toLocalTime())
                    .append(" (+").append(scoreHorario).append("%)\n");

        double scoreTipo = calcularScoreTipo(transacao.getTipo());
        if (scoreTipo > 0)
            explicacao.append("- Tipo: ").append(transacao.getTipo())
                    .append(" (+").append(scoreTipo).append("%)\n");

        double scoreDia = calcularScoreDiaSemana(transacao.getDataHora().getDayOfWeek());
        if (scoreDia > 0)
            explicacao.append("- Dia: ").append(transacao.getDataHora().getDayOfWeek())
                    .append(" (+").append(scoreDia).append("%)\n");

        return explicacao.toString();
    }
}