package com.essenciaviva.controller;

import com.essenciaviva.dto.LoginRequestDTO;
import com.essenciaviva.model.Sessao;
import com.essenciaviva.model.Usuario;
import com.essenciaviva.repository.SessaoRepository;
import com.essenciaviva.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UsuarioRepository usuarioRepository, SessaoRepository sessaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO req) {
        var usuarioOpt = usuarioRepository.findByLogin(req.getUsuario());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", "Usuário ou senha incorretos."));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Conta temporariamente bloqueada por muitas tentativas. Tente mais tarde."));
        }

        if (!encoder.matches(req.getSenha(), usuario.getSenhaHash())) {
            int falhas = usuario.getTentativasFalhas() + 1;
            usuario.setTentativasFalhas(falhas);
            if (falhas >= 3) {
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", "Usuário ou senha incorretos."));
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        String token = UUID.randomUUID().toString();
        Sessao sessao = new Sessao();
        sessao.setUsuario(usuario);
        sessao.setTokenHash(token); // Armazenado como hash/token de sessão
        sessao.setDataCriacao(LocalDateTime.now());
        sessao.setUltimaAtividade(LocalDateTime.now());
        sessao.setStatus("ATIVA");
        sessaoRepository.save(sessao);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "nomeUsuario", usuario.getNome(),
                "perfil", usuario.getPerfil()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            var sessaoOpt = sessaoRepository.findByTokenHashAndStatus(token, "ATIVA");
            sessaoOpt.ifPresent(sessao -> {
                sessao.setStatus("ENCERRADA");
                sessaoRepository.save(sessao);
            });
        }
        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada com sucesso."));
    }
}