package com.jaasielsilva.labsystem.features.empresa.repository;

import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.empresa.entity.TipoEmpresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    Page<Empresa> findAllByTipo(TipoEmpresa tipo, Pageable pageable);

    @Query("""
            SELECT e FROM Empresa e WHERE e.tipo = :tipo AND (
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.email) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND e.cnpj LIKE CONCAT('%', :digits, '%'))
            )
            """)
    Page<Empresa> searchByTermAndTipo(
            @Param("tipo") TipoEmpresa tipo,
            @Param("term") String term,
            @Param("digits") String digits,
            Pageable pageable
    );

    boolean existsByTipo(TipoEmpresa tipo);

    Optional<Empresa> findByTipo(TipoEmpresa tipo);

    long countByTipo(TipoEmpresa tipo);
}
