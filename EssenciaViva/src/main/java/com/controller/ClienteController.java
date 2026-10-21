package com.controller;

import com.essenciaviva.dto.ClienteDTO;
import com.essenciaviva.model.Cliente;
import com.essenciaviva.repository.ClienteRepository;
import com.essenciaviva.security.CryptoService;
import com.service.RegrasNegocioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final CryptoService cryptoService = new CryptoService();
    private final RegrasNegocioService regrasNegocioService = new RegrasNegocioService();

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes() throws Exception {
        List<Cliente> clientes = clienteRepository.findAll();
        List<ClienteDTO> resultado = new ArrayList<>();

        for (Cliente c : clientes) {
            ClienteDTO dto = new ClienteDTO();
            dto.setId(c.getId());
            dto.setNome(c.getNome());
            dto.setCpf(cryptoService.decrypt(c.getCpfCifrado())); // Descriptografado na camada de acesso
            dto.setTelefone(cryptoService.decrypt(c.getTelefoneCif()));
            dto.setStatus(c.getStatus());
            resultado.add(dto);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<?> obterDetalhesCliente(@PathVariable Integer id) throws Exception {
        var opt = clienteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Cliente c = opt.get();
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCpf(cryptoService.decrypt(c.getCpfCifrado()));
        dto.setDataNascimento(cryptoService.decrypt(c.getNascimentoCif()));
        dto.setTelefone(cryptoService.decrypt(c.getTelefoneCif()));
        dto.setEmail(cryptoService.decrypt(c.getEmailCif()));
        dto.setEndereco(cryptoService.decrypt(c.getEnderecoCif()));
        dto.setObservacoes(cryptoService.decrypt(c.getObservacoesCif()));
        dto.setStatus(c.getStatus());

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirCliente(@PathVariable Integer id) {
        var opt = clienteRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Cliente c = opt.get();
        // Exemplo: checar agendamentos concluídos
        boolean temAgendamentoConcluido = false; 

        String msg = regrasNegocioService.processarExclusaoCliente(c.getId(), temAgendamentoConcluido);

        if (temAgendamentoConcluido) {
            c.setStatus("inativo");
            clienteRepository.save(c);
        } else {
            clienteRepository.delete(c);
        }

        return ResponseEntity.ok(Map.of("mensagem", msg));
    }
}