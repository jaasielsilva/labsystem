package com.jaasielsilva.labsystem.features.cliente.service.impl;

import com.jaasielsilva.labsystem.exception.BusinessException;
import com.jaasielsilva.labsystem.exception.ResourceNotFoundException;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteRequest;
import com.jaasielsilva.labsystem.features.cliente.dto.ClienteResponse;
import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.cliente.mapper.ClienteMapper;
import com.jaasielsilva.labsystem.features.cliente.repository.ClienteRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteServiceImpl service;

    private Cliente cliente;
    private ClienteRequest clienteRequest;
    private ClienteResponse clienteResponse;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .nome("Jaasiel Silva")
                .cpf("12345678901")
                .email("jaasiel@test.com")
                .telefone("11988887777")
                .dataNascimento(LocalDate.of(1995, 5, 20))
                .ativo(true)
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
        
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        Page<ClienteResponse> result = service.findAll(pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(clienteResponse.nome(), result.getContent().get(0).nome());
        verify(repository, times(1)).findAll(pageable);
        verify(repository, never()).searchByTerm(any(), any(), any());
    }

    @Test
    void findAll_WithSearch_ShouldUseRepositorySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(Collections.singletonList(cliente));

        when(repository.searchByTerm("Jaasiel", "", pageable)).thenReturn(page);
        when(mapper.toResponse(any(Cliente.class))).thenReturn(clienteResponse);

        Page<ClienteResponse> result = service.findAll(pageable, "Jaasiel");

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).searchByTerm("Jaasiel", "", pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void findById_WhenExists_ShouldReturnCliente() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(clienteResponse.nome(), result.nome());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
        verify(repository, times(1)).findById(1L);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void create_WhenCpfAndEmailAreUnique_ShouldCreateCliente() {
        when(repository.existsByCpf(clienteRequest.cpf())).thenReturn(false);
        when(repository.existsByEmail(clienteRequest.email())).thenReturn(false);
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
        when(repository.existsByCpf(clienteRequest.cpf())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(clienteRequest));
        assertEquals("CPF já cadastrado no sistema.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WhenEmailExists_ShouldThrowBusinessException() {
        when(repository.existsByCpf(clienteRequest.cpf())).thenReturn(false);
        when(repository.existsByEmail(clienteRequest.email())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(clienteRequest));
        assertEquals("E-mail já cadastrado no sistema.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenExistsAndDataIsUnique_ShouldUpdateCliente() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndIdNot(clienteRequest.cpf(), 1L)).thenReturn(false);
        when(repository.existsByEmailAndIdNot(clienteRequest.email(), 1L)).thenReturn(false);
        when(repository.save(cliente)).thenReturn(cliente);
        when(mapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = service.update(1L, clienteRequest);

        assertNotNull(result);
        verify(mapper, times(1)).updateEntity(clienteRequest, cliente);
        verify(repository, times(1)).save(cliente);
    }

    @Test
    void update_WhenCpfExistsForAnotherCliente_ShouldThrowBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndIdNot(clienteRequest.cpf(), 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(1L, clienteRequest));
        assertEquals("CPF já cadastrado por outro cliente.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenEmailExistsForAnotherCliente_ShouldThrowBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.existsByCpfAndIdNot(clienteRequest.cpf(), 1L)).thenReturn(false);
        when(repository.existsByEmailAndIdNot(clienteRequest.email(), 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(1L, clienteRequest));
        assertEquals("E-mail já cadastrado por outro cliente.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(repository).delete(cliente);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository, times(1)).delete(cliente);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
        verify(repository, never()).delete(any());
    }
}
