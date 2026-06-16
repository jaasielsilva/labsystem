package com.jaasielsilva.labsystem.features.empresa.repository;

import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    @Query("""
            SELECT e FROM Empresa e WHERE
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.email) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND e.cnpj LIKE CONCAT('%', :digits, '%'))
            """)
    Page<Empresa> searchByTerm(
            @Param("term") String term,
            @Param("digits") String digits,
            Pageable pageable
    );
}
