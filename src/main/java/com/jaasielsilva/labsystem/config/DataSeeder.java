package com.jaasielsilva.labsystem.config;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ExameRepository exameRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Empresa plataforma = resolvePlatformEmpresa();
        Empresa laboratorio = resolveDemoLaboratorio();
        seedSuperAdmin(plataforma);
        seedUsuarios(laboratorio);
        seedExames(laboratorio);
    }

    private void seedSuperAdmin(Empresa plataforma) {
        if (usuarioRepository.existsByEmail("super@labsystem.local")) {
            return;
        }

        log.info("Criando super administrador da plataforma para empresaId={}", plataforma.getId());

        usuarioRepository.save(Usuario.builder()
                .nome("Super Administrador")
                .email("super@labsystem.local")
                .senhaHash(passwordEncoder.encode("super123"))
                .perfil(Perfil.SUPER_ADMIN)
                .ativo(true)
                .empresa(plataforma)
                .build());
    }

    private void seedUsuarios(Empresa empresa) {
        if (usuarioRepository.existsByEmail("admin@labsystem.local")) {
            return;
        }

        log.info("Criando usuários padrão de desenvolvimento para empresaId={}", empresa.getId());

        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email("admin@labsystem.local")
                .senhaHash(passwordEncoder.encode("admin123"))
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .empresa(empresa)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Operador")
                .email("operador@labsystem.local")
                .senhaHash(passwordEncoder.encode("operador123"))
                .perfil(Perfil.OPERADOR)
                .ativo(true)
                .empresa(empresa)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Visualizador")
                .email("visualizador@labsystem.local")
                .senhaHash(passwordEncoder.encode("visualizador123"))
                .perfil(Perfil.VISUALIZADOR)
                .ativo(true)
                .empresa(empresa)
                .build());
    }

    private void seedExames(Empresa empresa) {
        if (exameRepository.count() > 0) {
            return;
        }

        log.info("Criando catálogo de exames de desenvolvimento para empresaId={}", empresa.getId());

        exameRepository.save(Exame.builder()
                .codigo("HEMO001")
                .nome("Hemograma Completo")
                .descricao("Contagem de células sanguíneas e avaliação morfológica.")
                .categoria("Hematologia")
                .tipoAmostra(Exame.TipoAmostra.SANGUE)
                .prazoDias(1)
                .valor(new BigDecimal("45.00"))
                .ativo(true)
                .empresa(empresa)
                .build());

        exameRepository.save(Exame.builder()
                .codigo("GLIC001")
                .nome("Glicose em Jejum")
                .descricao("Dosagem de glicose plasmática em jejum.")
                .categoria("Bioquímica")
                .tipoAmostra(Exame.TipoAmostra.SANGUE)
                .prazoDias(1)
                .valor(new BigDecimal("25.00"))
                .ativo(true)
                .empresa(empresa)
                .build());

        exameRepository.save(Exame.builder()
                .codigo("URIN001")
                .nome("Urina Tipo I")
                .descricao("Exame físico, químico e sedimentoscópico da urina.")
                .categoria("Urinálise")
                .tipoAmostra(Exame.TipoAmostra.URINA)
                .prazoDias(2)
                .valor(new BigDecimal("30.00"))
                .ativo(true)
                .empresa(empresa)
                .build());
    }

    private Empresa resolvePlatformEmpresa() {
        return empresaRepository.findByTipo(TipoEmpresa.PLATAFORMA)
                .orElseGet(() -> {
                    log.info("Criando empresa plataforma de desenvolvimento");
                    return empresaRepository.save(Empresa.builder()
                            .nome("Labsystem Plataforma")
                            .cnpj("00000000000001")
                            .email("plataforma@labsystem.local")
                            .tipo(TipoEmpresa.PLATAFORMA)
                            .ativo(true)
                            .build());
                });
    }

    private Empresa resolveDemoLaboratorio() {
        return empresaRepository.findAll().stream()
                .filter(empresa -> empresa.getTipo() == TipoEmpresa.LABORATORIO)
                .findFirst()
                .orElseGet(() -> {
                    log.info("Criando laboratório padrão de desenvolvimento");
                    return empresaRepository.save(Empresa.builder()
                            .nome("Laboratório Demo")
                            .cnpj("00000000000000")
                            .email("contato@labsystem.local")
                            .tipo(TipoEmpresa.LABORATORIO)
                            .ativo(true)
                            .build());
                });
    }
}
