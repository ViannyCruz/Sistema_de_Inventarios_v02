package com.sistema_de_inventarios_v02.Controllers.api;

import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.dto.api.StockMovementDTO;
import com.sistema_de_inventarios_v02.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InventoryApiController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryApiController.class);

    @Autowired
    private ProductService productService;

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {
        try {
            logger.info("Creating product via API: {}", createProductDTO.getName());
            ProductResponseDTO createdProduct = productService.createProduct(createProductDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "PRODUCT_CREATION_ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        logger.info("Getting all products via API");
        List<ProductSummaryDTO> products = productService.getAllProductsSummary();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() :
                        Sort.by(sortBy).ascending());

        Page<ProductSummaryDTO> products = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductResponseDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            logger.error("Product not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "PRODUCT_NOT_FOUND", "message", "Producto no encontrado"));
        }
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        try {
            logger.info("Updating product {} via API", id);
            ProductResponseDTO updatedProduct = productService.updateProduct(id, updateProductDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating product {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "PRODUCT_UPDATE_ERROR", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            logger.info("Deleting product {} via API", id);
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Producto eliminado exitosamente",
                    "timestamp", LocalDateTime.now(),
                    "productId", id
            ));
        } catch (Exception e) {
            logger.error("Error deleting product {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "PRODUCT_NOT_FOUND", "message", "Producto no encontrado"));
        }
    }

    @PatchMapping("/products/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductStock(@PathVariable Long id,
                                                @Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        try {
            logger.info("Updating stock for product {} via API", id);
            ProductResponseDTO updatedProduct = productService.updateProductStock(id, stockUpdateDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating stock for product {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "STOCK_UPDATE_ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/stock/movement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> recordStockMovement(@Valid @RequestBody StockMovementDTO movementDTO) {
        try {
            logger.info("Recording stock movement for product {} via API", movementDTO.getProductId());

            ProductResponseDTO product = productService.getProductById(movementDTO.getProductId());

            int newStock = product.getStock();
            if ("ENTRADA".equalsIgnoreCase(movementDTO.getMovementType())) {
                newStock += movementDTO.getQuantity();
            } else if ("SALIDA".equalsIgnoreCase(movementDTO.getMovementType())) {
                newStock -= movementDTO.getQuantity();
                if (newStock < 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "INSUFFICIENT_STOCK",
                                    "message", "Stock insuficiente para la operación"));
                }
            }

            StockUpdateDTO stockUpdate = new StockUpdateDTO();
            stockUpdate.setStock(newStock);
            ProductResponseDTO updatedProduct = productService.updateProductStock(movementDTO.getProductId(), stockUpdate);

            return ResponseEntity.ok(Map.of(
                    "message", "Movimiento de stock registrado exitosamente",
                    "movement", movementDTO,
                    "product", updatedProduct,
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            logger.error("Error recording stock movement", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "STOCK_MOVEMENT_ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/products/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithLowStock() {
        logger.info("Getting products with low stock via API");
        List<ProductResponseDTO> products = productService.getProductsWithLowStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/out-of-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductResponseDTO>> getProductsOutOfStock() {
        logger.info("Getting products out of stock via API");
        List<ProductResponseDTO> products = productService.getProductsOutOfStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<?> searchProductsByName(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_SEARCH", "message", "Nombre de búsqueda requerido"));
        }

        logger.info("Searching products by name: {} via API", name);
        List<ProductSummaryDTO> products = productService.searchProductsByName(name.trim());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/category/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByCategory(@PathVariable String category) {
        logger.info("Getting products by category: {} via API", category);
        List<ProductSummaryDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/price-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<?> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {

        if (minPrice < 0 || maxPrice < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_PRICE_RANGE",
                            "message", "Los precios no pueden ser negativos"));
        }

        if (minPrice > maxPrice) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_PRICE_RANGE",
                            "message", "El precio mínimo no puede ser mayor que el máximo"));
        }

        logger.info("Getting products by price range: {}-{} via API", minPrice, maxPrice);
        List<ProductSummaryDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<String>> getAllCategories() {
        logger.info("Getting all categories via API");
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        logger.info("Getting inventory statistics via API");
        Map<String, Object> stats = productService.getProductStats();

        stats.put("timestamp", LocalDateTime.now());
        stats.put("apiVersion", "1.0");

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        logger.info("Getting dashboard data via API");

        Map<String, Object> stats = productService.getProductStats();
        List<ProductResponseDTO> lowStock = productService.getProductsWithLowStock();
        List<ProductResponseDTO> outOfStock = productService.getProductsOutOfStock();
        List<String> categories = productService.getAllCategories();

        Map<String, Object> dashboard = Map.of(
                "statistics", stats,
                "lowStockProducts", lowStock,
                "outOfStockProducts", outOfStock,
                "categories", categories,
                "alerts", Map.of(
                        "lowStockCount", lowStock.size(),
                        "outOfStockCount", outOfStock.size(),
                        "criticalAlerts", lowStock.size() + outOfStock.size()
                ),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Inventory Management API",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        ));
    }
}