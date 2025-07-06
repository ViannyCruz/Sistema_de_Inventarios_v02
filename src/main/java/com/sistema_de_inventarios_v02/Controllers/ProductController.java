package com.sistema_de_inventarios_v02.Controllers;

import com.sistema_de_inventarios_v02.dto.CreateProductDTO;
import com.sistema_de_inventarios_v02.dto.UpdateProductDTO;
import com.sistema_de_inventarios_v02.dto.ProductResponseDTO;
import com.sistema_de_inventarios_v02.dto.ProductSummaryDTO;
import com.sistema_de_inventarios_v02.dto.StockUpdateDTO;
import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.service.ProductHistoryService;
import com.sistema_de_inventarios_v02.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated // Necesario para validaciones en parámetros de query
public class ProductController {

    private final ProductService productService;
    private final ProductHistoryService productHistoryService;

    @Autowired
    public ProductController(ProductService productService, ProductHistoryService productHistoryService) {
        this.productService = productService;
        this.productHistoryService = productHistoryService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {
        ProductResponseDTO createdProduct = productService.createProduct(createProductDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        List<ProductSummaryDTO> products = productService.getAllProductsSummary();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Validar parámetros de paginación
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() :
                        Sort.by(sortBy).ascending());

        Page<ProductSummaryDTO> products = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, updateProductDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of(
                "message", "Producto eliminado exitosamente",
                "timestamp", System.currentTimeMillis(),
                "productId", id
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchProductsByName(
            @RequestParam String name) {

        // Validar que el nombre no esté vacío
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductSummaryDTO> products = productService.searchProductsByName(name.trim());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByCategory(@PathVariable String category) {
        List<ProductSummaryDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithLowStock() {
        List<ProductResponseDTO> products = productService.getProductsWithLowStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductResponseDTO>> getProductsOutOfStock() {
        List<ProductResponseDTO> products = productService.getProductsOutOfStock();
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProductStock(@PathVariable Long id,
                                                                 @Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        ProductResponseDTO updatedProduct = productService.updateProductStock(id, stockUpdateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByPriceRange(
            @RequestParam
            @DecimalMin(value = "0.0", message = "El precio mínimo debe ser mayor o igual a cero")
            Double minPrice,
            @RequestParam
            @DecimalMin(value = "0.0", message = "El precio máximo debe ser mayor o igual a cero")
            Double maxPrice) {

        // Validar que el rango de precios sea lógico
        if (minPrice > maxPrice) {
            return ResponseEntity.badRequest()
                    .header("Error-Message", "El precio mínimo no puede ser mayor que el precio máximo")
                    .build();
        }

        List<ProductSummaryDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    // NUEVOS ENDPOINTS ÚTILES

    @GetMapping("/filters")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsWithFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {

        // Validar parámetros de paginación
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() :
                        Sort.by(sortBy).ascending());

        Page<ProductSummaryDTO> products = productService.getProductsWithFilters(category, name, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getProductCounts() {
        Map<String, Object> stats = productService.getProductStats();

        Map<String, Long> counts = Map.of(
                "total", ((Integer) stats.get("totalProducts")).longValue(),
                "lowStock", ((Integer) stats.get("lowStockProducts")).longValue(),
                "outOfStock", ((Integer) stats.get("outOfStockProducts")).longValue(),
                "inStock", ((Integer) stats.get("inStockProducts")).longValue()
        );

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ProductService",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }




    // AUDITORIA
    @GetMapping("/{id}/history")
    public ResponseEntity<List<Product>> getProductHistory(@PathVariable Long id) {
        List<Product> history = productHistoryService.getProductHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/history/{revisionId}")
    public ResponseEntity<Product> getProductAtRevision(
            @PathVariable Long id,
            @PathVariable Long revisionId) {
        Product product = productHistoryService.getProductAtRevision(id, revisionId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<Number>> getProductRevisions(@PathVariable Long id) {
        List<Number> revisions = productHistoryService.getProductRevisionsList(id);
        return ResponseEntity.ok(revisions);
    }
}