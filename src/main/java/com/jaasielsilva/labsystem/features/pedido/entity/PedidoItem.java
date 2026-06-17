package com.jaasielsilva.labsystem.features.pedido.entity;

import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.exame.entity.Exame;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido_itens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pedido_itens_pedido_exame", columnNames = {"pedido_id", "exame_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exame_id", nullable = false)
    private Exame exame;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
