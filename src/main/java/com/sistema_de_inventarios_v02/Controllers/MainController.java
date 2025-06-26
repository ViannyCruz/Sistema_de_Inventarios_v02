package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/products")
    public String productsView() {
        return "products";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> returnNoFavicon() {
        return ResponseEntity.notFound().build();
    }
}
