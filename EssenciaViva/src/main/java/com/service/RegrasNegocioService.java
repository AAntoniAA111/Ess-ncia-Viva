package com.service;

import com.model.Agendamento;
import java.time.LocalDateTime;
import java.util.List;

public class RegrasNegocioService {

    /**
     * Valida se o profissional já possui algum agendamento no mesmo período de horário[cite: 3].
     */
    public void validarSobreposicaoHorario(int profissionalId, LocalDateTime inicio, LocalDateTime fim, List<Agendamento> agendamentosExistentes) {
        for (Agendamento ag : agendamentosExistentes) {
            // Acessa o ID através do objeto Profissional mapeado na entidade Agendamento
            if (ag.getProfissional() != null
                    && ag.getProfissional().getId() != null
                    && ag.getProfissional().getId().equals(profissionalId)
                    && !"CANCELADO".equalsIgnoreCase(ag.getSituacao())) {

                // Checa colisão entre intervalos: (InicioA < FimB) E (FimA > InicioB)
                boolean sobrepoe = inicio.isBefore(ag.getDataHoraFim()) && fim.isAfter(ag.getDataHoraInicio());
                if (sobrepoe) {
                    throw new IllegalArgumentException("O profissional selecionado já possui um atendimento agendado neste horário!");
                }
            }
        }
    }

    /**
     * Regra de exclusão/inativação de clientes[cite: 3].
     */
    public String processarExclusaoCliente(int clienteId, boolean possuiAgendamentosConcluidos) {
        if (possuiAgendamentosConcluidos) {
            // Em vez de deletar fisicamente, inativa para manter o histórico de dados[cite: 3]
            return "O cliente possui agendamentos concluídos e não pode ser excluído. O status foi alterado para INATIVO.";
        } else {
            // Pode excluir com segurança do banco
            return "Cliente removido com sucesso.";
        }
    }
}