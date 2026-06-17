package com.jaasielsilva.labsystem.features.pedido.repository;

import com.jaasielsilva.labsystem.features.pedido.entity.Pedido;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"cliente", "itens"})
    Page<Pedido> findAllByEmpresaId(Long empresaId, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.exame"})
    Optional<Pedido> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "itens"})
    @Query("""
            SELECT p FROM Pedido p
            JOIN p.cliente c
            WHERE p.empresa.id = :empresaId AND (
            LOWER(c.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND c.cpf LIKE CONCAT('%', :digits, '%'))
            )
            """)
    Page<Pedido> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            @Param("digits") String digits,
            Pageable pageable
    );

    Page<Pedido> findAllByEmpresaIdAndStatus(Long empresaId, PedidoStatus status, Pageable pageable);

    long countByEmpresa_Id(Long empresaId);
}
