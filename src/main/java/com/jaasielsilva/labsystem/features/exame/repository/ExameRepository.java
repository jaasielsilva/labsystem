package com.jaasielsilva.labsystem.features.exame.repository;

import com.jaasielsilva.labsystem.features.exame.entity.Exame;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExameRepository extends JpaRepository<Exame, Long> {
    Optional<Exame> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Query("""
            SELECT e FROM Exame e WHERE
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.categoria) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<Exame> searchByTerm(@Param("term") String term, Pageable pageable);
}
