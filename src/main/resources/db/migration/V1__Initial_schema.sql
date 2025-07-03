CREATE TABLE products (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          name VARCHAR(100) NOT NULL,
                          description VARCHAR(500),
                          category VARCHAR(70) NOT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          stock INT NOT NULL,
                          minimum_stock INT,
                          PRIMARY KEY (id)
);