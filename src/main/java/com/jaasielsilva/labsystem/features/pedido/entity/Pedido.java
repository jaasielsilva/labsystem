package com.jaasielsilva.labsystem.features.pedido.entity;

import com.jaasielsilva.labsystem.features.cliente.entity.Cliente;
import com.jaasielsilva.labsystem.features.empresa.entity.Empresa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private PedidoStatus status = PedidoStatus.ABERTO;

    @Column(length = 500)
    private String observacao;

    @Column(name = "motivo_cancelamento", length = 300)
    private String motivoCancelamento;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> itens = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (dataPedido == null) {
            dataPedido = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void replaceItens(List<PedidoItem> novosItens) {
        itens.clear();
        if (novosItens != null) {
            novosItens.forEach(item -> {
                item.setPedido(this);
                itens.add(item);
            });
        }
    }
}
