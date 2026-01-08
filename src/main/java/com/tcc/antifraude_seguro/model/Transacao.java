package com.tcc.antifraude_seguro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Usuário ID é obrigatório")
    @Size(min = 3, max = 50, message = "Usuário ID deve ter entre 3 e 50 caracteres")
    private String usuarioId;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Max(value = 1000000, message = "Valor não pode exceder R$ 1.000.000")
    private Double valor;

    @NotBlank(message = "Tipo é obrigatório")
    @Pattern(
            regexp = "PIX|TED|DOC|BOLETO|CARTAO",
            message = "Tipo deve ser: PIX, TED, DOC, BOLETO ou CARTAO"
    )
    private String tipo;

    private LocalDateTime dataHora;

    private String status;

    @Min(value = 0, message = "Score de risco não pode ser negativo")
    @Max(value = 100, message = "Score de risco não pode exceder 100")
    private Double scoreRisco;

    // Construtor vazio (obrigatório pro JPA)
    public Transacao() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getScoreRisco() {
        return scoreRisco;
    }

    public void setScoreRisco(Double scoreRisco) {
        this.scoreRisco = scoreRisco;
    }
}