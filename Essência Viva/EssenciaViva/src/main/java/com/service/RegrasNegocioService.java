package com.essenciaviva.service;

import java.time.LocalDateTime;
import java.util.List;

public class RegrasNegocioService {

    /**
     * Valida se o profissional já possui algum agendamento no mesmo período de horário.
     */
    public void validarSobreposicaoHorario(int profissionalId, LocalDateTime inicio, LocalDateTime fim, List<Agendamento> agendamentosExistentes) {
        for (Agendamento ag : agendamentosExistentes) {
            if (ag.getProfissionalId() == profissionalId && !"CANCELADO".equalsIgnoreCase(ag.getSituacao())) {
                // Checa colisão entre intervalos: (InicioA < FimB) E (FimA > InicioB)
                boolean sobrepoe = inicio.isBefore(ag.getDataHoraFim()) && fim.isAfter(ag.getDataHoraInicio());
                if (sobrepoe) {
                    throw new IllegalArgumentException("O profissional selecionado já possui um atendimento agendado neste horário!");
                }
            }
        }
    }

    /**
     * Regra de exclusão/inativação de clientes.
     */
    public String processarExclusaoCliente(int clienteId, boolean possuiAgendamentosConcluidos) {
        if (possuiAgendamentosConcluidos) {
            // Em vez de deletar fisicamente, inativa para manter o histórico de dados
            return "O cliente possui agendamentos concluídos e não pode ser excluído. O status foi alterado para INATIVO.";
        } else {
            // Pode excluir com segurança do banco
            return "Cliente removido com sucesso.";
        }
    }
}