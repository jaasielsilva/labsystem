CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    observacao VARCHAR(500),
    motivo_cancelamento VARCHAR(300),
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedidos_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT chk_pedidos_status CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO'))
);

CREATE INDEX idx_pedidos_empresa_id ON pedidos(empresa_id);
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_data_pedido ON pedidos(data_pedido);

CREATE TABLE pedido_itens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    exame_id BIGINT NOT NULL,
    valor_unitario DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_itens_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_itens_exame FOREIGN KEY (exame_id) REFERENCES exames(id),
    CONSTRAINT uk_pedido_itens_pedido_exame UNIQUE (pedido_id, exame_id)
);

CREATE INDEX idx_pedido_itens_pedido_id ON pedido_itens(pedido_id);
CREATE INDEX idx_pedido_itens_exame_id ON pedido_itens(exame_id);
