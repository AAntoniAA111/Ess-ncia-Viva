package com.essenciaviva.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "cpf_hash", nullable = false, unique = true, length = 64)
    private String cpfHash;

    @Lob
    @Column(name = "cpf_cifrado", nullable = false)
    private byte[] cpfCifrado;

    @Lob
    @Column(name = "nascimento_cif")
    private byte[] nascimentoCif;

    @Lob
    @Column(name = "telefone_cif")
    private byte[] telefoneCif;

    @Lob
    @Column(name = "email_cif")
    private byte[] emailCif;

    @Lob
    @Column(name = "endereco_cif")
    private byte[] enderecoCif;

    @Lob
    @Column(name = "observacoes_cif")
    private byte[] observacoesCif;

    @Column(nullable = false)
    private String status = "ativo";

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfHash() { return cpfHash; }
    public void setCpfHash(String cpfHash) { this.cpfHash = cpfHash; }

    public byte[] getCpfCifrado() { return cpfCifrado; }
    public void setCpfCifrado(byte[] cpfCifrado) { this.cpfCifrado = cpfCifrado; }

    public byte[] getNascimentoCif() { return nascimentoCif; }
    public void setNascimentoCif(byte[] nascimentoCif) { this.nascimentoCif = nascimentoCif; }

    public byte[] getTelefoneCif() { return telefoneCif; }
    public void setTelefoneCif(byte[] telefoneCif) { this.telefoneCif = telefoneCif; }

    public byte[] getEmailCif() { return emailCif; }
    public void setEmailCif(byte[] emailCif) { this.emailCif = emailCif; }

    public byte[] getEnderecoCif() { return enderecoCif; }
    public void setEnderecoCif(byte[] enderecoCif) { this.enderecoCif = enderecoCif; }

    public byte[] getObservacoesCif() { return observacoesCif; }
    public void setObservacoesCif(byte[] observacoesCif) { this.observacoesCif = observacoesCif; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}