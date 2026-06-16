package com.jaasielsilva.labsystem.features.platform.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import com.jaasielsilva.labsystem.features.auth.repository.UsuarioRepository;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaRequest;
import com.jaasielsilva.labsystem.features.empresa.dto.EmpresaResponse;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import com.jaasielsilva.labsystem.features.empresa.mapper.EmpresaMapper;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import com.jaasielsilva.labsystem.features.exame.repository.ExameRepository;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingRequest;
import com.jaasielsilva.labsystem.features.platform.dto.LaboratorioOnboardingResponse;
import com.jaasielsilva.labsystem.features.platform.dto.PrimeiroAdminRequest;
import com.jaasielsilva.labsystem.features.platform.service.PlatformEmpresaService;
import com.jaasielsilva.labsystem.features.usuario.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformEmpresaServiceImpl implements PlatformEmpresaService {

    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ExameRepository exameRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> findAll(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            log.info("Buscando laboratórios paginados (escopo plataforma)");
            return repository.findAllByTipo(TipoEmpresa.LABORATORIO, pageable).map(mapper::toResponse);
        }

        String term = search.trim();
        String digits = term.replaceAll("\\D", "");
        log.info("Buscando laboratórios com filtro (escopo plataforma)");
        return repository.searchByTermAndTipo(TipoEmpresa.LABORATORIO, term, digits, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponse findById(Long id) {
        log.info("Buscando laboratório por id={}", id);
        Empresa empresa = findLaboratorio(id);
        return mapper.toResponse(empresa);
    }

    @Override
    @Transactional
    public EmpresaResponse create(EmpresaRequest request) {
        log.info("Criando novo laboratório");
        return mapper.toResponse(saveNewLaboratorio(request));
    }

    @Override
    @Transactional
    public LaboratorioOnboardingResponse onboard(LaboratorioOnboardingRequest request) {
        log.info("Onboarding de novo laboratório com administrador");

        PrimeiroAdminRequest admin = request.admin();
        if (usuarioRepository.existsByEmail(admin.email())) {
            throw new BusinessException("E-mail do administrador já cadastrado.");
        }

        Empresa laboratorio = saveNewLaboratorio(request.laboratorio());

        Usuario adminUsuario = Usuario.builder()
                .nome(admin.nome())
                .email(admin.email())
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .empresa(laboratorio)
                .senhaHash(passwordEncoder.encode(admin.senha()))
                .build();

        Usuario savedAdmin = usuarioRepository.save(adminUsuario);

        return new LaboratorioOnboardingResponse(
                mapper.toResponse(laboratorio),
                usuarioMapper.toResponse(savedAdmin)
        );
    }

    private Empresa saveNewLaboratorio(EmpresaRequest request) {
        if (repository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado no sistema.");
        }

        Empresa empresa = mapper.toEntity(request);
        empresa.setTipo(TipoEmpresa.LABORATORIO);
        return repository.save(empresa);
    }

    @Override
    @Transactional
    public EmpresaResponse update(Long id, EmpresaRequest request) {
        log.info("Atualizando laboratório id={}", id);

        Empresa empresa = findLaboratorio(id);

        if (repository.existsByCnpjAndIdNot(request.cnpj(), id)) {
            throw new BusinessException("CNPJ já cadastrado por outro laboratório.");
        }

        mapper.updateEntity(request, empresa);
        empresa.setTipo(TipoEmpresa.LABORATORIO);
        Empresa updated = repository.save(empresa);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deletando laboratório id={}", id);

        Empresa empresa = findLaboratorio(id);

        if (repository.countByTipo(TipoEmpresa.LABORATORIO) <= 1) {
            throw new BusinessException("Não é possível remover o único laboratório do sistema.");
        }

        if (usuarioRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover laboratório com usuários vinculados.");
        }
        if (clienteRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover laboratório com clientes vinculados.");
        }
        if (exameRepository.countByEmpresa_Id(id) > 0) {
            throw new BusinessException("Não é possível remover laboratório com exames vinculados.");
        }

        repository.delete(empresa);
    }

    private Empresa findLaboratorio(Long id) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratório não encontrado com ID: " + id));

        if (empresa.getTipo() != TipoEmpresa.LABORATORIO) {
            throw new ResourceNotFoundException("Laboratório não encontrado com ID: " + id);
        }

        return empresa;
    }
}
