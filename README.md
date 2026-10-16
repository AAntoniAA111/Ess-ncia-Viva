# 🌿 Clínica Estética Essência Viva — Sistema de Gestão

Sistema de gestão e agendamento para a clínica estética **Essência Viva**, desenvolvido com **Java 17+ e Spring Boot** no backend e uma interface web interativa em HTML, CSS e JavaScript no frontend.

O projeto tem como foco a segurança das informações, a proteção de dados sensíveis conforme a **Lei Geral de Proteção de Dados (LGPD)** e o controle rigoroso das sessões dos usuários.

---

## 🛠️ Tecnologias Utilizadas

- **Backend:** Java 17+, Spring Boot, Spring Data JPA e Spring Security.
- **Frontend:** HTML5, CSS3 e JavaScript (ES6+).
- **Banco de Dados:** MySQL, PostgreSQL ou SQLite.
- **Gerenciamento de Dependências:** Apache Maven.
- **Segurança e Criptografia:** AES-256-GCM, HMAC-SHA256 e BCrypt.

---

## 🔐 Requisitos e Funcionalidades de Segurança

### 1. Autenticação e Controle de Sessão

- Armazenamento de senhas utilizando hash BCrypt com salt.
- Controle de inatividade validado pelo servidor a cada requisição.
- Aviso na interface nos últimos 60 segundos antes da expiração da sessão.
- Opção para renovar a sessão antes do encerramento.
- Invalidação do token no servidor após o logout.
- Bloqueio temporário da conta após três tentativas incorretas de login.

### 2. Criptografia de Dados — LGPD

- Criptografia de campos sensíveis, como CPF, data de nascimento, telefone, e-mail, endereço e observações, utilizando AES-256-GCM.
- Utilização de um vetor de inicialização (IV) único para cada operação de criptografia.
- Pesquisa e verificação de unicidade do CPF por meio de índice HMAC-SHA256, sem necessidade de descriptografar os dados armazenados.
- Armazenamento seguro das chaves criptográficas por meio de variáveis de ambiente.
- Mascaramento de informações sensíveis nas listagens, como `***.456.789-**`.

### 3. Regras de Negócio

- Validação no servidor para impedir a sobreposição de horários de um mesmo profissional.
- Impedimento da exclusão física de clientes com histórico de atendimentos concluídos, alterando automaticamente seu estado para `INATIVO`.
- Registro do valor do procedimento no momento do agendamento, preservando o preço acordado.

---

## ⚙️ Variáveis de Ambiente

O sistema utiliza variáveis de ambiente para evitar a exposição de credenciais e chaves criptográficas no repositório.

| Variável | Descrição | Exemplo |
|---|---|---|
| `APP_ENCRYPTION_KEY` | Chave secreta utilizada na criptografia AES-256-GCM. | Chave configurada localmente |
| `SESSION_TIMEOUT_MINUTES` | Tempo limite de inatividade da sessão, em minutos. | `15` |

> ⚠️ **Importante:** Nunca publique chaves criptográficas, senhas ou credenciais reais no GitHub. Utilize variáveis de ambiente e mantenha arquivos `.env` fora do controle de versão.

---

## 🚀 Instalação e Execução

### Pré-requisitos

Antes de iniciar, verifique se você possui:

- Java JDK 17 ou superior.
- Apache Maven instalado.
- Servidor MySQL em execução.
- Git instalado, caso queira clonar o repositório.

### 1. Clonar o repositório

```bash
git clone URL_DO_SEU_REPOSITORIO
cd NOME_DO_PROJETO
```

Substitua `URL_DO_SEU_REPOSITORIO` pelo endereço real do repositório e `NOME_DO_PROJETO` pelo nome da pasta criada.

### 2. Configurar o banco de dados

Crie o banco de dados e execute o script de criação das tabelas, caso esteja disponível na pasta `database/`.

```bash
mysql -u root -p < database/schema.sql
```

Configure também as credenciais de acesso ao banco de dados conforme as propriedades utilizadas pela aplicação.

### 3. Configurar as variáveis de ambiente

**Linux/macOS:**

```bash
export APP_ENCRYPTION_KEY="SUA_CHAVE_SEGURA"
export SESSION_TIMEOUT_MINUTES=15
```

**Windows — PowerShell:**

```powershell
$env:APP_ENCRYPTION_KEY="SUA_CHAVE_SEGURA"
$env:SESSION_TIMEOUT_MINUTES="15"
```

> A chave utilizada em produção deve seguir os requisitos criptográficos da implementação AES-256-GCM. Não utilize uma chave de exemplo em ambientes reais.

### 4. Compilar e executar a aplicação

Na raiz do projeto, execute:

```bash
mvn clean package
```

Em seguida, inicie a aplicação:

```bash
java -jar target/essenciaviva-0.0.1-SNAPSHOT.jar
```

Se a compilação e a inicialização forem concluídas corretamente, acesse:

**http://localhost:8080**

> O nome do arquivo `.jar` pode variar conforme a configuração do projeto Maven.

---

## 🎬 Guia de Demonstração

Esta seção apresenta sugestões para demonstrar os principais requisitos de segurança e as regras de negócio do sistema.

### 1. Controle de Inatividade

1. Configure `SESSION_TIMEOUT_MINUTES=1`.
2. Inicie a aplicação.
3. Faça login no sistema.
4. Aguarde sem interagir com a aplicação.
5. Verifique se o aviso de expiração aparece antes do encerramento da sessão.
6. Teste as opções de continuar conectado e deixar a sessão expirar.
7. Confirme se o servidor encerra a sessão e se a aplicação solicita um novo login.

### 2. Verificação da Criptografia no Banco de Dados

Execute uma consulta adaptada aos nomes reais das tabelas e colunas do projeto:

```sql
SELECT cpf_cifrado, telefone_cif
FROM cliente;
```

Verifique se os campos protegidos estão armazenados de forma cifrada, sem revelar os dados originais.

**Observação:** os nomes das colunas e da tabela devem corresponder ao esquema efetivamente utilizado pelo sistema.

### 3. Teste de Agendamento

1. Faça login no sistema.
2. Agende um atendimento para determinado profissional.
3. Tente cadastrar outro atendimento para o mesmo profissional em um horário conflitante.
4. Verifique se o sistema impede a sobreposição.
5. Teste também agendamentos em horários diferentes para confirmar que são permitidos quando não há conflitos.

### 4. Teste de Bloqueio de Login

1. Acesse a tela de login.
2. Informe uma senha incorreta três vezes.
3. Verifique se a conta é temporariamente bloqueada.
4. Confirme se o comportamento segue a regra de segurança definida pelo sistema.

---

## 📁 Estrutura do Projeto

A estrutura abaixo é uma sugestão de organização para o projeto:

```text
EssenciaViva/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
├── database/
│   └── schema.sql
├── .gitignore
├── pom.xml
└── README.md
```

A estrutura real pode variar conforme a organização dos pacotes Java e dos arquivos do frontend.

---

## 🔒 Boas Práticas de Segurança

- Não armazenar senhas em texto puro.
- Não versionar chaves criptográficas, senhas ou credenciais.
- Validar dados no backend, mesmo quando já existe validação no frontend.
- Proteger rotas e operações conforme as permissões do usuário.
- Utilizar HTTPS em ambientes de produção.
- Registrar eventos de segurança sem expor dados pessoais sensíveis.
- Garantir que as sessões sejam invalidadas corretamente no logout.
- Restringir o acesso aos dados pessoais de acordo com a finalidade e a autorização do usuário.

---

## 🎯 Objetivo do Projeto

Desenvolver um sistema de gestão para uma clínica estética, reunindo funcionalidades de agendamento, controle de clientes e procedimentos, autenticação de usuários e proteção de dados pessoais.

O projeto também busca aplicar, na prática, conceitos de desenvolvimento backend, integração com banco de dados, segurança da informação e boas práticas de programação.

---

**Projeto acadêmico desenvolvido para fins educacionais — SENAI.**
