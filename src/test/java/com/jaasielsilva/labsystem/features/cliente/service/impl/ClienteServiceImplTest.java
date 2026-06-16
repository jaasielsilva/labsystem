package com.jaasielsilva.labsystem.features.cliente.service.impl;

import com.jaasielsilva.labsystem.common.TenantContext;
import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.mapper.ClienteMapper;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private ClienteServiceImpl service;

    private Cliente cliente;
    private ClienteRequest clienteRequest;
    private ClienteResponse clienteResponse;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(EMPRESA_ID)
                .nome("Laboratório Demo")
                .cnpj("00000000000000")
                .email("contato@labsystem.local")
                .ativo(true)
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nome("Jaasiel Silva")
                .cpf("12345678901")
                .email("jaasiel@test.com")
                .telefone("11988887777")
                .dataNascimento(LocalDate.of(1995, 5, 20))
                .ativo(true)
                .empresa(empresa)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        clienteRequest = new ClienteRequest(
                "Jaasiel Silva",
                "12345678901",
                "jaasiel@test.com",
                "11988887777",
                LocalDate.of(1995, 5, 20)
        );

        clienteResponse = new ClienteResponse(
                1L,
                "Jaasiel Silva",
                "12345678901",
                "jaasiel@test.com",
                "11988887777",
                LocalDate.of(1995, 5, 20),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void findAll_ShouldReturnPagedClientes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(Collections.singletonList(cliente));

        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findAllByEmpresaId(EMPRESA_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        Page<ClienteResponse> result = service.findAll(pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(clienteResponse.nome(), result.getContent().get(0).nome());
        verify(repository, times(1)).findAllByEmpresaId(EMPRESA_ID, pageable);
        verify(repository, never()).searchByTermAndEmpresaId(any(), any(), any(), any());
    }

    @Test
    void findAll_WithSearch_ShouldUseRepositorySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(Collections.singletonList(cliente));

        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.searchByTermAndEmpresaId(EMPRESA_ID, "Jaasiel", "", pageable)).thenReturn(page);
        when(mapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        Page<ClienteResponse> result = service.findAll(pageable, "Jaasiel");

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).searchByTermAndEmpresaId(EMPRESA_ID, "Jaasiel", "", pageable);
        verify(repository, never()).findAllByEmpresaId(any(), any(Pageable.class));
    }

    @Test
    void findById_WhenExists_ShouldReturnCliente() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        when(mapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(clienteResponse.nome(), result.nome());
        verify(repository, times(1)).findByIdAndEmpresaId(1L, EMPRESA_ID);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        verify(repository, times(1)).findByIdAndEmpresaId(1L, EMPRESA_ID);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void create_WhenCpfAndEmailAreUnique_ShouldCreateCliente() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByCpfAndEmpresaId(clienteRequest.cpf(), EMPRESA_ID)).thenReturn(false);
        when(repository.existsByEmailAndEmpresaId(clienteRequest.email(), EMPRESA_ID)).thenReturn(false);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa);
        when(mapper.toEntity(clienteRequest)).thenReturn(cliente);
        when(repository.save(cliente)).thenReturn(cliente);
        when(mapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = service.create(clienteRequest);

        assertNotNull(result);
        assertEquals(clienteResponse.cpf(), result.cpf());
        verify(repository, times(1)).save(cliente);
    }

    @Test
    void create_WhenCpfExists_ShouldThrowBusinessException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByCpfAndEmpresaId(clienteRequest.cpf(), EMPRESA_ID)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(clienteRequest));
        assertEquals("CPF já cadastrado no sistema.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WhenEmailExists_ShouldThrowBusinessException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.existsByCpfAndEmpresaId(clienteRequest.cpf(), EMPRESA_ID)).thenReturn(false);
        when(repository.existsByEmailAndEmpresaId(clienteRequest.email(), EMPRESA_ID)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(clienteRequest));
        assertEquals("E-mail já cadastrado no sistema.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenExistsAndDataIsUnique_ShouldUpdateCliente() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndEmpresaIdAndIdNot(clienteRequest.cpf(), EMPRESA_ID, 1L)).thenReturn(false);
        when(repository.existsByEmailAndEmpresaIdAndIdNot(clienteRequest.email(), EMPRESA_ID, 1L)).thenReturn(false);
        when(repository.save(cliente)).thenReturn(cliente);
        when(mapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = service.update(1L, clienteRequest);

        assertNotNull(result);
        verify(mapper, times(1)).updateEntity(clienteRequest, cliente);
        verify(repository, times(1)).save(cliente);
    }

    @Test
    void update_WhenCpfExistsForAnotherCliente_ShouldThrowBusinessException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndEmpresaIdAndIdNot(clienteRequest.cpf(), EMPRESA_ID, 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(1L, clienteRequest));
        assertEquals("CPF já cadastrado por outro cliente.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenEmailExistsForAnotherCliente_ShouldThrowBusinessException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndEmpresaIdAndIdNot(clienteRequest.cpf(), EMPRESA_ID, 1L)).thenReturn(false);
        when(repository.existsByEmailAndEmpresaIdAndIdNot(clienteRequest.email(), EMPRESA_ID, 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(1L, clienteRequest));
        assertEquals("E-mail já cadastrado por outro cliente.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(cliente));
        doNothing().when(repository).delete(cliente);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository, times(1)).delete(cliente);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(tenantContext.requireEmpresaId()).thenReturn(EMPRESA_ID);
        when(repository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
        verify(repository, never()).delete(any());
    }
}
