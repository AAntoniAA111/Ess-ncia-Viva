package com.essenciaviva.model;

import jakarta.persistence.*;

@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "registro_profissional", nullable = false, length = 50)
    private String registroProfissional;

    @Column(nullable = false, length = 100)
    private String especialidade;

    @Column(nullable = false)
    private String status = "ativo";

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRegistroProfissional() { return registroProfissional; }
    public void setRegistroProfissional(String registroProfissional) { this.registroProfissional = registroProfissional; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}