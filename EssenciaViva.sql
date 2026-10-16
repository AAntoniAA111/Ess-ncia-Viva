-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS essencia_viva DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE essencia_viva;

-- Tabela de Usuários do Sistema (Operadores)
CREATE TABLE usuario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL, -- BCrypt com Salt
    perfil VARCHAR(20) NOT NULL, -- 'ADMINISTRADOR' ou 'RECEPCIONISTA'
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    tentativas_falhas INT DEFAULT 0, -- Controle de bloqueio temporário (máx 3)
    bloqueado_ate DATETIME NULL
);

-- Tabela de Sessões com Validação no Servidor
CREATE TABLE sessao (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE, -- Hash SHA-256 do token de sessão
    data_criacao DATETIME NOT NULL,
    ultima_atividade DATETIME NOT NULL, -- Atualizada a cada requisição
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabela de Clientes (Campos sensíveis criptografados com AES-256-GCM)
CREATE TABLE cliente (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL, -- Texto claro para ordenação/busca simples
    cpf_hash CHAR(64) NOT NULL UNIQUE, -- HMAC-SHA256 sobre dígitos para busca rápida
    cpf_cifrado VARBINARY(255) NOT NULL, -- AES-256-GCM (IV + Ciphertext + Tag)
    nascimento_cif VARBINARY(255),
    telefone_cif VARBINARY(255),
    email_cif VARBINARY(255),
    endereco_cif VARBINARY(512),
    observacoes_cif VARBINARY(2048),
    status ENUM('ativo', 'inativo') NOT NULL DEFAULT 'ativo'
);

-- Tabela de Profissionais
CREATE TABLE profissional (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    registro_profissional VARCHAR(50) NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    status ENUM('ativo', 'inativo') NOT NULL DEFAULT 'ativo'
);

-- Tabela de Procedimentos Estéticos
CREATE TABLE procedimento (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    duracao_minutos INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    status ENUM('ativo', 'inativo') NOT NULL DEFAULT 'ativo'
);

-- Tabela de Agendamentos
CREATE TABLE agendamento (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    profissional_id INT NOT NULL,
    data_hora_inicio DATETIME NOT NULL,
    data_hora_fim DATETIME NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'AGENDADO', -- 'AGENDADO', 'CONCLUIDO', 'CANCELADO'
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    FOREIGN KEY (profissional_id) REFERENCES profissional(id)
);

-- Tabela Associativa Agendamento <-> Procedimento (com historico de valor cobrado)
CREATE TABLE agendamento_procedimento (
    agendamento_id INT NOT NULL,
    procedimento_id INT NOT NULL,
    valor_cobrado DECIMAL(10,2) NOT NULL, -- Copiado no momento do agendamento
    PRIMARY KEY (agendamento_id, procedimento_id),
    FOREIGN KEY (agendamento_id) REFERENCES agendamento(id) ON DELETE CASCADE,
    FOREIGN KEY (procedimento_id) REFERENCES procedimento(id)
);