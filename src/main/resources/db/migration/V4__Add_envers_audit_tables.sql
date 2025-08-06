-- Drop existing tables if they exist (to fix naming issues)
DROP TABLE IF EXISTS products_audit;
DROP TABLE IF EXISTS REVINFO;

-- Tabla de información de revisión con información del usuario
-- Note: Hibernate expects specific column names and types
CREATE TABLE REVINFO (
                         REV INTEGER NOT NULL AUTO_INCREMENT,
                         REVTSTMP BIGINT NOT NULL,
                         username VARCHAR(255),
                         user_id BIGINT,
                         PRIMARY KEY (REV)
);

-- Tabla de auditoría para productos
-- Note: This should match your Product entity structure exactly
CREATE TABLE products_audit (
                                id BIGINT NOT NULL,
                                REV INTEGER NOT NULL,
                                REVTYPE TINYINT,
                                name VARCHAR(100),
                                description VARCHAR(500),
                                category VARCHAR(70),
                                price DECIMAL(10,2),
                                stock INT,
                                minimum_stock INT,
                                created_at TIMESTAMP,
                                updated_at TIMESTAMP,
                                PRIMARY KEY (id, REV),
                                CONSTRAINT FK_products_audit_rev FOREIGN KEY (REV) REFERENCES REVINFO(REV)
);

-- Índices para mejorar el rendimiento de consultas de auditoría
CREATE INDEX idx_products_audit_rev ON products_audit(REV);
CREATE INDEX idx_products_audit_revtype ON products_audit(REVTYPE);
CREATE INDEX idx_products_audit_id ON products_audit(id);
CREATE INDEX idx_revinfo_username ON REVINFO(username);
CREATE INDEX idx_revinfo_user_id ON REVINFO(user_id);