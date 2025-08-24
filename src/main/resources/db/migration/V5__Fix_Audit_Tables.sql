

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS products_audit;
DROP TABLE IF EXISTS REVINFO;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE REVINFO (
                         REV INT NOT NULL AUTO_INCREMENT,
                         REVTSTMP BIGINT NOT NULL,
                         username VARCHAR(255) NULL,
                         user_id BIGINT NULL,
                         PRIMARY KEY (REV)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE products_audit (
                                id BIGINT NOT NULL,
                                REV INT NOT NULL,
                                REVTYPE TINYINT NULL,
                                name VARCHAR(100) NULL,
                                description VARCHAR(500) NULL,
                                category VARCHAR(70) NULL,
                                price DECIMAL(10,2) NULL,
                                stock INT NULL,
                                minimum_stock INT NULL,
                                PRIMARY KEY (id, REV),
                                INDEX idx_products_audit_rev (REV),
                                CONSTRAINT fk_products_audit_revinfo
                                    FOREIGN KEY (REV) REFERENCES REVINFO (REV)
                                        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_revinfo_timestamp ON REVINFO (REVTSTMP);
CREATE INDEX idx_revinfo_username ON REVINFO (username);
CREATE INDEX idx_products_audit_revtype ON products_audit (REVTYPE);
CREATE INDEX idx_products_audit_id ON products_audit (id);


INSERT INTO REVINFO (REV, REVTSTMP, username, user_id)
VALUES (1, UNIX_TIMESTAMP() * 1000, 'system', NULL);