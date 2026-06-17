CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    usuario_email VARCHAR(150),
    perfil VARCHAR(50),

    empresa_id BIGINT NOT NULL,

    action VARCHAR(50),
    entidade VARCHAR(100),
    entidade_id BIGINT,

    detalhes VARCHAR(2000),
    scope VARCHAR(50),

    plataforma_empresa_id BIGINT,
    acting_empresa_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE INDEX idx_audit_empresa_id ON audit_logs(empresa_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);