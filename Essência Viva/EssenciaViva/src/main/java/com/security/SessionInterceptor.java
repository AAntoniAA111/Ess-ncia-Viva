package com.essenciaviva.security;

import java.time.Duration;
import java.time.LocalDateTime;

public class SessionInterceptor {

    // Lê a duração configurada da variável de ambiente ou usa 15 minutos como padrão
    private static final long SESSION_TIMEOUT_MINUTES = Long.parseLong(
        System.getenv().getOrDefault("SESSION_TIMEOUT_MINUTES", "15")
    );

    /**
     * Valida no servidor se a sessão ainda é válida e se não ultrapassou o limite de inatividade.
     */
    public boolean validateAndUpdateSession(Sessao sessao) {
        if (sessao == null || !"ATIVA".equalsIgnoreCase(sessao.getStatus())) {
            return false; // Sessão inválida ou inativa
        }

        LocalDateTime agora = LocalDateTime.now();
        Duration tempoInativo = Duration.between(sessao.getUltimaAtividade(), agora);

        if (tempoInativo.toMinutes() >= SESSION_TIMEOUT_MINUTES) {
            // Expira a sessão no servidor
            sessao.setStatus("EXPIRADA");
            // Atualizar no banco de dados (sessaoRepository.save(sessao))
            return false; // Redirecionará para o login com mensagem de inatividade
        }

        // Se a sessão estiver válida, atualiza o timestamp da última atividade no SERVIDOR
        sessao.setUltimaAtividade(agora);
        // Salva no banco (sessaoRepository.save(sessao))
        return true;
    }
}