INSERT INTO empresas (nome, cnpj, email, ativo, created_at)
VALUES ('Laboratório Demo', '00000000000000', 'contato@labsystem.local', TRUE, CURRENT_TIMESTAMP);

SET @demo_empresa_id = LAST_INSERT_ID();

-- usuarios
ALTER TABLE usuarios
    ADD COLUMN empresa_id BIGINT NULL;

UPDATE usuarios SET empresa_id = @demo_empresa_id WHERE empresa_id IS NULL;

ALTER TABLE usuarios
    MODIFY empresa_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_usuarios_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id);

-- clientes
ALTER TABLE clientes
    DROP INDEX cpf,
    DROP INDEX email;

ALTER TABLE clientes
    ADD COLUMN empresa_id BIGINT NULL;

UPDATE clientes SET empresa_id = @demo_empresa_id WHERE empresa_id IS NULL;

ALTER TABLE clientes
    MODIFY empresa_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_clientes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id),
    ADD CONSTRAINT uk_clientes_empresa_cpf UNIQUE (empresa_id, cpf),
    ADD CONSTRAINT uk_clientes_empresa_email UNIQUE (empresa_id, email);

-- exames
ALTER TABLE exames
    DROP INDEX codigo;

ALTER TABLE exames
    ADD COLUMN empresa_id BIGINT NULL;

UPDATE exames SET empresa_id = @demo_empresa_id WHERE empresa_id IS NULL;

ALTER TABLE exames
    MODIFY empresa_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_exames_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id),
    ADD CONSTRAINT uk_exames_empresa_codigo UNIQUE (empresa_id, codigo);
