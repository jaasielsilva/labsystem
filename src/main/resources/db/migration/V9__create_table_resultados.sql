CREATE TABLE resultados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    pedido_item_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    laudo TEXT,
    observacao_interna VARCHAR(500),
    data_liberacao TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resultados_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_resultados_pedido_item FOREIGN KEY (pedido_item_id) REFERENCES pedido_itens(id) ON DELETE CASCADE,
    CONSTRAINT uk_resultados_pedido_item UNIQUE (pedido_item_id),
    CONSTRAINT chk_resultados_status CHECK (status IN ('PENDENTE', 'EM_ANALISE', 'DISPONIVEL', 'CANCELADO'))
);

CREATE INDEX idx_resultados_empresa_id ON resultados(empresa_id);
CREATE INDEX idx_resultados_status ON resultados(status);

INSERT INTO resultados (empresa_id, pedido_item_id, status, created_at)
SELECT p.empresa_id, pi.id, 'PENDENTE', NOW()
FROM pedido_itens pi
INNER JOIN pedidos p ON p.id = pi.pedido_id
LEFT JOIN resultados r ON r.pedido_item_id = pi.id
WHERE r.id IS NULL;
