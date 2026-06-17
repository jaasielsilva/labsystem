package com.jaasielsilva.labsystem.features.resultado.repository;

import com.jaasielsilva.labsystem.features.resultado.entity.Resultado;
import com.jaasielsilva.labsystem.features.resultado.entity.ResultadoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    @EntityGraph(attributePaths = {
            "pedidoItem",
            "pedidoItem.pedido",
            "pedidoItem.pedido.cliente",
            "pedidoItem.exame"
    })
    Optional<Resultado> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = {
            "pedidoItem",
            "pedidoItem.pedido",
            "pedidoItem.pedido.cliente",
            "pedidoItem.exame"
    })
    Page<Resultado> findAllByEmpresaId(Long empresaId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "pedidoItem",
            "pedidoItem.pedido",
            "pedidoItem.pedido.cliente",
            "pedidoItem.exame"
    })
    @Query("""
            SELECT r FROM Resultado r
            JOIN r.pedidoItem pi
            JOIN pi.pedido p
            JOIN p.cliente c
            JOIN pi.exame e
            WHERE r.empresa.id = :empresaId AND (
            LOWER(c.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.nome) LIKE LOWER(CONCAT('%', :term, '%')) OR
            LOWER(e.codigo) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (:digits <> '' AND c.cpf LIKE CONCAT('%', :digits, '%'))
            )
            """)
    Page<Resultado> searchByTermAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            @Param("digits") String digits,
            Pageable pageable
    );

    Optional<Resultado> findByPedidoItemIdAndEmpresaId(Long pedidoItemId, Long empresaId);

    List<Resultado> findAllByPedidoItemPedidoIdAndEmpresaId(Long pedidoId, Long empresaId);

    long countByEmpresaIdAndPedidoItemPedidoIdAndStatusIn(
            Long empresaId,
            Long pedidoId,
            List<ResultadoStatus> statuses
    );

    long countByEmpresaIdAndPedidoItemPedidoId(Long empresaId, Long pedidoId);
}
