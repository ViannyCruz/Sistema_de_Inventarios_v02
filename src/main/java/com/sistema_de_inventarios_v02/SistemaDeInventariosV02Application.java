package com.sistema_de_inventarios_v02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class SistemaDeInventariosV02Application {
    final static Logger logger = LoggerFactory.getLogger(SistemaDeInventariosV02Application.class);

    public static void main(String[] args) {
        SpringApplication.run(SistemaDeInventariosV02Application.class, args);
    }
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        logger.info("Hello endpoint was called");
        return ResponseEntity.ok().body("Hello, World!");
    }
}
