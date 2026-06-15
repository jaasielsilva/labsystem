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
    Optional<Cliente> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            SELECT c FROM Cliente c WHERE
            LOWER(c.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND c.cpf LIKE CONCAT('%', :digits, '%')) OR
            (:digits <> '' AND c.telefone IS NOT NULL AND c.telefone LIKE CONCAT('%', :digits, '%'))
            """)
    Page<Cliente> searchByTerm(@Param("term") String term, @Param("digits") String digits, Pageable pageable);
}
