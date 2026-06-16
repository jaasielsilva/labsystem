package com.jaasielsilva.labsystem.features.cliente.repository;

import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Page<Cliente> findAllByEmpresaId(Long empresaId, Pageable pageable);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByCpfAndEmpresaId(String cpf, Long empresaId);

    boolean existsByEmailAndEmpresaId(String email, Long empresaId);

    boolean existsByCpfAndEmpresaIdAndIdNot(String cpf, Long empresaId, Long id);

    boolean existsByEmailAndEmpresaIdAndIdNot(String email, Long empresaId, Long id);

    @Query("""
            SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND (
            LOWER(c.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND c.cpf LIKE CONCAT('%', :digits, '%')) OR
            (:digits <> '' AND c.telefone IS NOT NULL AND c.telefone LIKE CONCAT('%', :digits, '%'))
            )
            """)
    Page<Cliente> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            @Param("digits") String digits,
            Pageable pageable
    );

    long countByEmpresa_Id(Long empresaId);
}
