package com.essenciaviva.repository;

import com.essenciaviva.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, Integer> {
    Optional<Sessao> findByTokenHashAndStatus(String tokenHash, String status);
}