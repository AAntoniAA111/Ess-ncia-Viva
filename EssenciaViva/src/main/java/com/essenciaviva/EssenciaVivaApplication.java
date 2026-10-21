package com.essenciaviva;

import com.essenciaviva.model.Cliente;
import com.essenciaviva.model.Usuario;
import com.essenciaviva.repository.ClienteRepository;
import com.essenciaviva.repository.UsuarioRepository;
import com.essenciaviva.security.CryptoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class EssenciaVivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EssenciaVivaApplication.class, args);
    }

    @Bean
    public CommandLineRunner carregarDadosIniciais(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository) {

        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // Criar Utilizador Administrador se a tabela estiver vazia
            if (usuarioRepository.findByLogin("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador do Sistema");
                admin.setLogin("admin");
                admin.setSenhaHash(encoder.encode("admin123"));
                admin.setPerfil("ADMINISTRADOR");
                admin.setStatus("ATIVO");
                usuarioRepository.save(admin);
                System.out.println("✅ [SEED] Usuario 'admin' criado (senha: admin123)");
            }

            // Criar Utilizador Recepcionista se a tabela estiver vazia
            if (usuarioRepository.findByLogin("recep").isEmpty()) {
                Usuario recep = new Usuario();
                recep.setNome("Maria Recepcionista");
                recep.setLogin("recep");
                recep.setSenhaHash(encoder.encode("recep123"));
                recep.setPerfil("RECEPCIONISTA");
                recep.setStatus("ATIVO");
                usuarioRepository.save(recep);
                System.out.println("✅ [SEED] Usuario 'recep' criado (senha: recep123)");
            }

            // Criar Cliente de exemplo cifrado
            if (clienteRepository.count() == 0) {
                try {
                    CryptoService cryptoService = new CryptoService();

                    Cliente clienteSeed = new Cliente();
                    clienteSeed.setNome("Ana Souza");

                    String cpfLimpo = "12345678901";
                    clienteSeed.setCpfHash(cryptoService.calculateCPFHash(cpfLimpo));

                    clienteSeed.setCpfCifrado(cryptoService.encrypt("123.456.789-01"));
                    clienteSeed.setNascimentoCif(cryptoService.encrypt("1995-05-20"));
                    clienteSeed.setTelefoneCif(cryptoService.encrypt("(31) 98765-4321"));
                    clienteSeed.setEmailCif(cryptoService.encrypt("ana.souza@email.com"));
                    clienteSeed.setEnderecoCif(cryptoService.encrypt("Rua das Flores, 123 - Belo Horizonte/MG"));
                    clienteSeed.setObservacoesCif(cryptoService.encrypt("Alergia a produtos com parabenos."));
                    clienteSeed.setStatus("ativo");

                    clienteRepository.save(clienteSeed);
                    System.out.println("✅ [SEED] Cliente 'Ana Souza' criado com sucesso!");
                } catch (Exception e) {
                    System.err.println("⚠️ Configure APP_ENCRYPTION_KEY nas variaveis de ambiente.");
                }
            }
        };
    }
}