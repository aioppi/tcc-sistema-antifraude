package com.tcc.antifraude_seguro.repository;

import com.tcc.antifraude_seguro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    // PRONTO! Spring cria tudo automaticamente!
}