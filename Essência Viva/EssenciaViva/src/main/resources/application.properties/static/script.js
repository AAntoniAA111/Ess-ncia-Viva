// ============================================================================
// CONFIGURAÇÕES DE SESSÃO E VARIÁVEIS GLOBAIS DE INATIVIDADE
// ============================================================================

// Define o tempo limite de inatividade em segundos (Exemplo: 900 para 15 minutos, 60 para testes)
let tempoInatividadeLimite = 60; 

// Guarda a contagem de tempo sem nenhuma interação efetuada pelo usuário
let segundosInativo = 0; 

// Guarda a referência do temporizador que roda a cada 1 segundo no navegador
let temporizadorInatividade = null; 

// Guarda a referência do temporizador dos últimos 60 segundos antes de expirar
let temporizadorAvisoRegressivo = null; 

// Token de sessão gerado após a autenticação bem-sucedida do operador
let tokenSessaoAtiva = null; 

// ============================================================================
// EVENTOS DE INTERAÇÃO E REINÍCIO DE TEMPO DE INATIVIDADE
// ============================================================================

// Adiciona um ouvinte para interceptar qualquer clique do usuário no documento
document.addEventListener("click", reiniciarContadorInatividade);

// Adiciona um ouvinte para interceptar qualquer tecla pressionada no teclado
document.addEventListener("keypress", reiniciarContadorInatividade);

// Função responsável por zerar a contagem de segundos inativos quando o usuário interage
function reiniciarContadorInatividade() {
    // Verifica se existe um token de sessão ativo no sistema
    if (tokenSessaoAtiva) {
        // Zera o contador de inatividade local
        segundosInativo = 0; 
    }
}

// ============================================================================
// LOGICA DE SESSÃO COM VALIDAÇÃO E AVISO NOS ÚLTIMOS 60 SEGUNDOS
// ============================================================================

// Inicia o loop do cronômetro de inatividade do sistema
function iniciarMonitorDeInatividade() {
    // Limpa qualquer temporizador anterior para evitar duplicidade de execução
    clearInterval(temporizadorInatividade); 
    
    // Zera os segundos inativos
    segundosInativo = 0; 

    // Configura uma função para ser executada a cada 1000 milissegundos (1 segundo)
    temporizadorInatividade = setInterval(() => {
        // Incrementa em 1 segundo o tempo sem interação
        segundosInativo++; 

        // Calcula quantos segundos restam para atingir o limite configurado
        let tempoRestante = tempoInatividadeLimite - segundosInativo; 

        // Verifica se faltam exatamente 60 segundos ou menos para a sessão expirar
        if (tempoRestante <= 60 && tempoRestante > 0) {
            // Exibe a tela/modal de aviso de expiração nos últimos 60 segundos
            exibirAvisoExpiracao(tempoRestante); 
        }

        // Verifica se o tempo inativo atingiu ou ultrapassou o limite máximo
        if (segundosInativo >= tempoInatividadeLimite) {
            // Invoca o encerramento da sessão por inatividade
            encerrarSessaoPorInatividade(); 
        }
    }, 1000); 
}

// Exibe a janela de aviso nos últimos 60 segundos e atualiza o contador na tela
function exibirAvisoExpiracao(segundosRestantes) {
    // Obtém a referência do elemento modal no HTML
    const modalAviso = document.getElementById("modal-aviso-sessao"); 
    
    // Remove a classe "oculto" para tornar a janela visível
    modalAviso.classList.remove("oculto"); 
    
    // Atualiza o texto com a contagem regressiva em segundos
    document.getElementById("contador-segundos").innerText = segundosRestantes; 
}

// Oculta o modal de aviso de expiração
function fecharAvisoExpiracao() {
    // Adiciona a classe "oculto" na janela de aviso
    document.getElementById("modal-aviso-sessao").classList.add("oculto"); 
}

// Envia uma requisição de 'Ping' ao servidor para renovar o tempo de sessão
async function renovarSessaoServidor() {
    // Faz a chamada assíncrona ao backend Java no endpoint de renovação
    const resposta = await fetch("/api/sessao/renovar", {
        // Método HTTP POST para atualização da sessão
        method: "POST", 
        // Define o cabeçalho informando o envio do token
        headers: { "Authorization": "Bearer " + tokenSessaoAtiva } 
    });

    // Se o servidor responder que a sessão foi renovada com sucesso (Código 200 OK)
    if (resposta.ok) {
        // Esconde a janela de aviso da tela
        fecharAvisoExpiracao(); 
        // Zera o contador de segundos sem atividade
        segundosInativo = 0; 
    } else {
        // Caso o servidor já tenha expirado a sessão, força o encerramento no cliente
        encerrarSessaoPorInatividade(); 
    }
}

// Associa o clique do botão "Continuar conectado" à renovação no servidor
document.getElementById("btn-renovar-sessao").addEventListener("click", renovarSessaoServidor);

// Encerra a sessão atual e obriga o usuário a fazer login novamente
function encerrarSessaoPorInatividade() {
    // Para a execução do cronômetro de inatividade
    clearInterval(temporizadorInatividade); 
    
    // Limpa o token de sessão da memória do navegador
    tokenSessaoAtiva = null; 
    
    // Esconde a janela do sistema principal
    document.getElementById("sistema-principal").classList.add("oculto"); 
    
    // Oculta qualquer modal de aviso que esteja aberto
    fecharAvisoExpiracao(); 
    
    // Obtém a referência da div de mensagens da tela de login
    const msgDiv = document.getElementById("mensagem-login"); 
    
    // Define a mensagem exata exigida pelas diretrizes do SENAI
    msgDiv.innerText = "Sua sessão foi encerrada por inatividade. Faça login novamente."; 
    
    // Exibe a caixa de mensagem na cor de alerta
    msgDiv.classList.remove("oculto"); 
    
    // Reexibe a tela de login
    document.getElementById("tela-login").classList.remove("oculto"); 
}

// ============================================================================
// EFETUAÇÃO DE LOGIN E LOGOUT
// ============================================================================

// Associa a submissão do formulário de login à validação de credenciais
document.getElementById("form-login").addEventListener("submit", async function(e) {
    // Evita o comportamento padrão do navegador de recarregar a página
    e.preventDefault(); 

    // Captura o texto digitado no campo de usuário
    const usuario = document.getElementById("usuario").value; 
    
    // Captura o texto digitado no campo de senha
    const senha = document.getElementById("senha").value; 

    // Envia os dados de login para o servidor Java em formato JSON
    const resposta = await fetch("/api/auth/login", {
        // Método HTTP POST
        method: "POST", 
        // Informa que o conteúdo enviado é um objeto JSON
        headers: { "Content-Type": "application/json" }, 
        // Converte os dados digitados para texto JSON
        body: JSON.stringify({ usuario: usuario, senha: senha }) 
    });

    // Se o login for autenticado com sucesso
    if (resposta.ok) {
        // Converte os dados da resposta em JSON
        const dados = await resposta.json(); 
        
        // Guarda o token gerado pelo servidor
        tokenSessaoAtiva = dados.token; 
        
        // Oculta a tela de login
        document.getElementById("tela-login").classList.add("oculto"); 
        
        // Exibe o painel principal do sistema
        document.getElementById("sistema-principal").classList.remove("oculto"); 
        
        // Exibe o nome do operador na barra superior
        document.getElementById("usuario-logado").innerText = dados.nomeUsuario; 

        // Inicia o controle de inatividade de sessão
        iniciarMonitorDeInatividade(); 
        
        // Carrega a listagem de clientes cadastrados
        carregarClientes(); 
    } else {
        // Se a senha estiver incorreta ou o usuário estiver bloqueado por 3 tentativas
        const erro = await resposta.json(); 
        // Exibe a mensagem amigável vinda do servidor
        document.getElementById("mensagem-login").innerText = erro.mensagem; 
        // Torna o elemento de alerta visível
        document.getElementById("mensagem-login").classList.remove("oculto"); 
    }
});

// Encerramento explícito de sessão ao clicar no botão 'Sair'
document.getElementById("btn-sair").addEventListener("click", async function() {
    // Executa a invalidação da sessão diretamente no SERVIDOR
    await fetch("/api/auth/logout", {
        // Método POST
        method: "POST", 
        // Envia o token para ser marcado como inativo no banco de dados
        headers: { "Authorization": "Bearer " + tokenSessaoAtiva } 
    });
    
    // Redireciona o cliente para a tela inicial zerando a sessão local
    encerrarSessaoPorInatividade(); 
});

// ============================================================================
// CARREGAMENTO E MASCARAMENTO DE DADOS SENSÍVEIS DE CLIENTES
// ============================================================================

// Busca no servidor a lista de clientes com o CPF devidamente mascarado
async function carregarClientes() {
    // Faz a chamada GET para a API REST de clientes
    const resposta = await fetch("/api/clientes", {
        // Passa o token de autorização
        headers: { "Authorization": "Bearer " + tokenSessaoAtiva } 
    });

    // Se o servidor retornar a lista
    if (resposta.ok) {
        // Converte a lista em array de objetos JavaScript
        const clientes = await resposta.json(); 
        
        // Obtém o elemento tbody da tabela no HTML
        const corpoTabela = document.getElementById("tabela-clientes-corpo"); 
        
        // Limpa as linhas antigas da tabela
        corpoTabela.innerHTML = ""; 

        // Itera sobre cada cliente recebido
        clientes.forEach(cliente => {
            // Aplica a máscara no CPF exibido na listagem (Ex: ***.456.789-**)
            const cpfMascarado = mascararCPF(cliente.cpf); 

            // Cria uma nova linha HTML na tabela
            const linha = document.createElement("tr"); 

            // Preenche as colunas da linha com os dados protegidos
            linha.innerHTML = `
                <td>${cliente.nome}</td>
                <td>${cpfMascarado}</td>
                <td>${cliente.telefone}</td>
                <td><span class="status">${cliente.status}</span></td>
                <td>
                    <button onclick="visualizarDadosCompletos(${cliente.id})" class="btn-secundario">Visualizar</button>
                    <button onclick="confirmarExclusaoCliente(${cliente.id})" class="btn-sair">Excluir</button>
                </td>
            `;

            // Adiciona a linha construída à tabela HTML
            corpoTabela.appendChild(linha); 
        });
    }
}

// Aplica máscara ao CPF para proteger a privacidade na listagem pública
function mascararCPF(cpfLimpo) {
    // Caso o CPF esteja indisponível, retorna texto vazio
    if (!cpfLimpo) return ""; 
    // Garante apenas os 11 dígitos numéricos do CPF
    const digitos = cpfLimpo.replace(/\D/g, ""); 
    // Substitui o início por asteriscos deixando visível apenas a parte central
    return `***.${digitos.substring(3, 6)}.${digitos.substring(6, 9)}-**`; 
}

// Exibe os dados sensíveis sem máscara mediante confirmação do usuário autorizada
async function visualizarDadosCompletos(clienteId) {
    // Busca os dados descriptografados do cliente no servidor
    const resposta = await fetch(`/api/clientes/${clienteId}/detalhes`, {
        // Envia a credencial de sessão
        headers: { "Authorization": "Bearer " + tokenSessaoAtiva } 
    });

    // Se o perfil do usuário tiver permissão e os dados forem retornados
    if (resposta.ok) {
        // Converte a resposta em objeto JSON
        const c = await resposta.json(); 
        
        // Constrói o HTML com as informações detalhadas em texto limpo
        document.getElementById("corpo-detalhes-cliente").innerHTML = `
            <p><strong>Nome:</strong> ${c.nome}</p>
            <p><strong>CPF Completo:</strong> ${c.cpf}</p>
            <p><strong>Data de Nascimento:</strong> ${c.dataNascimento}</p>
            <p><strong>Telefone:</strong> ${c.telefone}</p>
            <p><strong>E-mail:</strong> ${c.email}</p>
            <p><strong>Endereço:</strong> ${c.endereco}</p>
            <p><strong>Observações Médicas:</strong> ${c.observacoes}</p>
        `;
        
        // Remove a classe "oculto" para mostrar a tela modal com os detalhes
        document.getElementById("modal-detalhes-cliente").classList.remove("oculto"); 
    }
}

// Oculta a janela de detalhes do cliente
function fecharModalDetalhes() {
    // Oculta a caixa modal
    document.getElementById("modal-detalhes-cliente").classList.add("oculto"); 
}

// Confirma a exclusão/inativação de um cliente do sistema
async function confirmarExclusaoCliente(clienteId) {
    // Exibe uma caixa de diálogo de confirmação amigável
    if (confirm("Tem certeza que deseja excluir este cliente do sistema?")) {
        // Envia a requisição DELETE para a API Java
        const resposta = await fetch(`/api/clientes/${clienteId}`, {
            // Método DELETE
            method: "DELETE", 
            // Autorização
            headers: { "Authorization": "Bearer " + tokenSessaoAtiva } 
        });

        // Converte o retorno em mensagem textual
        const resultado = await resposta.json(); 
        
        // Exibe o aviso ao usuário (se foi deletado ou inativado devido a agendamentos concluídos)
        alert(resultado.mensagem); 
        
        // Recarrega a tabela de clientes atualizada
        carregarClientes(); 
    }
}