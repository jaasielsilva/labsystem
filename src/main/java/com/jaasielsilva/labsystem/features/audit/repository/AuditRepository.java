package com.jaasielsilva.labsystem.features.audit.repository;

import com.jaasielsilva.labsystem.features.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByEmpresaId(Long empresaId, Pageable pageable);

    Optional<AuditLog> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.empresaId = :empresaId AND (
                LOWER(a.usuarioEmail) LIKE LOWER(CONCAT('%', :term, '%')) OR
                LOWER(a.action) LIKE LOWER(CONCAT('%', :term, '%')) OR
                LOWER(a.entidade) LIKE LOWER(CONCAT('%', :term, '%')) OR
                LOWER(a.detalhes) LIKE LOWER(CONCAT('%', :term, '%'))
            )
            """)
    Page<AuditLog> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            Pageable pageable
    );
}
