package com.essenciaviva.repository;

import com.essenciaviva.model.Profissional; // <--- ESTA LINHA É OBRIGATÓRIA
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<Profissional, Integer> {
}