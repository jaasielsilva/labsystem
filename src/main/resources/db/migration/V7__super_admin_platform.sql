ALTER TABLE empresas
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'LABORATORIO';

INSERT INTO empresas (nome, cnpj, email, ativo, tipo, created_at)
SELECT 'Labsystem Plataforma', '00000000000001', 'plataforma@labsystem.local', TRUE, 'PLATAFORMA', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE tipo = 'PLATAFORMA');
