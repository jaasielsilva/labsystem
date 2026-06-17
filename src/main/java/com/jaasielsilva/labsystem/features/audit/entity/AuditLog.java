package com.jaasielsilva.labsystem.features.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_email", length = 150)
    private String usuarioEmail;

    @Column(length = 50)
    private String perfil;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(length = 50)
    private String action;

    @Column(length = 100)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(length = 2000)
    private String detalhes;

    @Column(length = 50)
    private String scope;

    @Column(name = "plataforma_empresa_id")
    private Long plataformaEmpresaId;

    @Column(name = "acting_empresa_id")
    private Long actingEmpresaId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
