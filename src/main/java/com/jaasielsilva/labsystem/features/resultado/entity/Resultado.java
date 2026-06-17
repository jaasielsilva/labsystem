package com.jaasielsilva.labsystem.features.resultado.entity;

import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import com.jaasielsilva.labsystem.features.pedido.entity.PedidoItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resultados_pedido_item", columnNames = {"pedido_item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_item_id", nullable = false)
    private PedidoItem pedidoItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private ResultadoStatus status = ResultadoStatus.PENDENTE;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String laudo;

    @Column(name = "observacao_interna", length = 500)
    private String observacaoInterna;

    @Column(name = "data_liberacao")
    private LocalDateTime dataLiberacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
