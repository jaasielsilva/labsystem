package com.jaasielsilva.labsystem.features.exame.repository;

import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExameRepository extends JpaRepository<Exame, Long> {

    Page<Exame> findAllByEmpresaId(Long empresaId, Pageable pageable);

    Optional<Exame> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByCodigoAndEmpresaId(String codigo, Long empresaId);

    boolean existsByCodigoAndEmpresaIdAndIdNot(String codigo, Long empresaId, Long id);

    @Query("""
            SELECT e FROM Exame e WHERE e.empresa.id = :empresaId AND (
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.categoria) LIKE LOWER(CONCAT('%', :term, '%'))
            )
            """)
    Page<Exame> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            Pageable pageable
    );

    long countByEmpresa_Id(Long empresaId);
}
