package com.jaasielsilva.labsystem.config;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        log.info("Criando usuários padrão de desenvolvimento");

        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email("admin@labsystem.local")
                .senhaHash(passwordEncoder.encode("admin123"))
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Operador")
                .email("operador@labsystem.local")
                .senhaHash(passwordEncoder.encode("operador123"))
                .perfil(Perfil.OPERADOR)
                .ativo(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Visualizador")
                .email("visualizador@labsystem.local")
                .senhaHash(passwordEncoder.encode("visualizador123"))
                .perfil(Perfil.VISUALIZADOR)
                .ativo(true)
                .build());
    }
}
