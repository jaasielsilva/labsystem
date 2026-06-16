package com.jaasielsilva.labsystem.features.exame.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exames")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAmostra tipoAmostra;

    @Column(name = "prazo_dias", nullable = false)
    private Integer prazoDias;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TipoAmostra {
        SANGUE, URINA, FEZES, OUTRO
    }
}
