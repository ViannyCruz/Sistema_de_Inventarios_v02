package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.Controllers.ProductController;
import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("ProductController Tests")
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private CreateProductDTO createProductDTO;
    private UpdateProductDTO updateProductDTO;
    private ProductResponseDTO productResponseDTO;
    private ProductSummaryDTO productSummaryDTO1;
    private ProductSummaryDTO productSummaryDTO2;
    private StockUpdateDTO stockUpdateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // DTO para crear producto
        createProductDTO = new CreateProductDTO(
                "Smartphone",
                "Teléfono inteligente con cámara de 48MP",
                "Electrónicos",
                new BigDecimal("799.99"),
                25,
                10
        );

        // DTO para actualizar producto
        updateProductDTO = new UpdateProductDTO();
        updateProductDTO.setName("Smartphone Actualizado");
        updateProductDTO.setDescription("Teléfono inteligente actualizado");
        updateProductDTO.setPrice(new BigDecimal("849.99"));

        // DTO de respuesta completa
        productResponseDTO = new ProductResponseDTO(
                1L,
                "Smartphone",
                "Teléfono inteligente con cámara de 48MP",
                "Electrónicos",
                new BigDecimal("799.99"),
                25,
                10,
                false, // isLowStock
                false, // isOutOfStock
                "IN_STOCK"
        );

        // DTOs de resumen para listas
        productSummaryDTO1 = new ProductSummaryDTO(
                1L,
                "Smartphone",
                "Electrónicos",
                new BigDecimal("799.99"),
                25,
                "IN_STOCK"
        );

        productSummaryDTO2 = new ProductSummaryDTO(
                2L,
                "Laptop",
                "Electrónicos",
                new BigDecimal("1500.00"),
                10,
                "IN_STOCK"
        );

        // DTO para actualización de stock
        stockUpdateDTO = new StockUpdateDTO(30, "Reposición de inventario");
    }

    @Nested
    @DisplayName("Crear Producto")
    class CreateProductTests {

        @Test
        @DisplayName("Debe crear producto y retornar ProductResponseDTO")
        void createProduct_ShouldReturnCreatedProduct() {
            // Arrange
            when(productService.createProduct(any(CreateProductDTO.class))).thenReturn(productResponseDTO);

            // Act
            ResponseEntity<ProductResponseDTO> response = productController.createProduct(createProductDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(productResponseDTO.getName(), response.getBody().getName());
            assertEquals(productResponseDTO.getCategory(), response.getBody().getCategory());
            assertEquals(productResponseDTO.getStockStatus(), response.getBody().getStockStatus());

            verify(productService, times(1)).createProduct(any(CreateProductDTO.class));
        }
    }

    @Nested
    @DisplayName("Obtener Productos")
    class GetProductsTests {

        @Test
        @DisplayName("Debe obtener todos los productos como ProductSummaryDTO")
        void getAllProducts_ShouldReturnListOfProductSummary() {
            // Arrange
            when(productService.getAllProductsSummary()).thenReturn(List.of(productSummaryDTO1, productSummaryDTO2));

            // Act
            ResponseEntity<List<ProductSummaryDTO>> response = productController.getAllProducts();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals(productSummaryDTO1.getName(), response.getBody().get(0).getName());
            assertEquals(productSummaryDTO2.getName(), response.getBody().get(1).getName());

            verify(productService, times(1)).getAllProductsSummary();
        }

        @Test
        @DisplayName("Debe obtener productos paginados como ProductSummaryDTO")
        void getProductsPaginated_ShouldReturnPagedProductSummary() {
            // Arrange
            Page<ProductSummaryDTO> mockPage = new PageImpl<>(
                    List.of(productSummaryDTO1),
                    PageRequest.of(0, 1),
                    2
            );
            when(productService.getAllProductsPaginated(any())).thenReturn(mockPage);

            // Act
            ResponseEntity<Page<ProductSummaryDTO>> response =
                    productController.getProductsPaginated(0, 1, "name", "asc");

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(productSummaryDTO1.getName(), response.getBody().getContent().get(0).getName());
            assertEquals(2, response.getBody().getTotalElements());
            assertEquals(2, response.getBody().getTotalPages());

            verify(productService, times(1)).getAllProductsPaginated(any());
        }

        @Test
        @DisplayName("Debe obtener producto por ID como ProductResponseDTO")
        void getProductById_ShouldReturnProductResponseDTO_WhenProductExists() {
            // Arrange
            when(productService.getProductById(1L)).thenReturn(productResponseDTO);

            // Act
            ResponseEntity<ProductResponseDTO> response = productController.getProductById(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(productResponseDTO.getId(), response.getBody().getId());
            assertEquals(productResponseDTO.getName(), response.getBody().getName());
            assertEquals(productResponseDTO.getDescription(), response.getBody().getDescription());
            assertEquals(productResponseDTO.isLowStock(), response.getBody().isLowStock());
            assertEquals(productResponseDTO.isOutOfStock(), response.getBody().isOutOfStock());
            assertEquals(productResponseDTO.getStockStatus(), response.getBody().getStockStatus());

            verify(productService, times(1)).getProductById(1L);
        }
    }

    @Nested
    @DisplayName("Actualizar Producto")
    class UpdateProductTests {

        @Test
        @DisplayName("Debe actualizar producto con UpdateProductDTO")
        void updateProduct_ShouldReturnUpdatedProduct_WhenProductExists() {
            // Arrange
            when(productService.updateProduct(1L, updateProductDTO)).thenReturn(productResponseDTO);

            // Act
            ResponseEntity<ProductResponseDTO> response = productController.updateProduct(1L, updateProductDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(productResponseDTO, response.getBody());

            verify(productService, times(1)).updateProduct(1L, updateProductDTO);
        }

        @Test
        @DisplayName("Debe actualizar stock con StockUpdateDTO")
        void updateProductStock_ShouldReturnUpdatedProduct_WhenProductExists() {
            // Arrange
            when(productService.updateProductStock(1L, stockUpdateDTO)).thenReturn(productResponseDTO);

            // Act
            ResponseEntity<ProductResponseDTO> response =
                    productController.updateProductStock(1L, stockUpdateDTO);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(productResponseDTO, response.getBody());

            verify(productService, times(1)).updateProductStock(1L, stockUpdateDTO);
        }
    }

    @Nested
    @DisplayName("Eliminar Producto")
    class DeleteProductTests {

        @Test
        @DisplayName("Debe eliminar producto y retornar mensaje de éxito")
        void deleteProduct_ShouldReturnSuccessMessage_WhenProductExists() {
            // Arrange
            doNothing().when(productService).deleteProduct(1L);

            // Act
            ResponseEntity<Map<String, Object>> response = productController.deleteProduct(1L);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Producto eliminado exitosamente", response.getBody().get("message"));
            assertNotNull(response.getBody().get("timestamp"));
            assertEquals(1L, response.getBody().get("productId"));

            verify(productService, times(1)).deleteProduct(1L);
        }
    }

    @Nested
    @DisplayName("Búsquedas")
    class SearchTests {

        @Test
        @DisplayName("Debe buscar productos por nombre y retornar ProductSummaryDTO")
        void searchProductsByName_ShouldReturnListOfProductSummary_WhenProductsFound() {
            // Arrange
            when(productService.searchProductsByName("Smartphone")).thenReturn(List.of(productSummaryDTO1));

            // Act
            ResponseEntity<List<ProductSummaryDTO>> response =
                    productController.searchProductsByName("Smartphone");

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(productSummaryDTO1.getName(), response.getBody().get(0).getName());

            verify(productService, times(1)).searchProductsByName("Smartphone");
        }

        @Test
        @DisplayName("Debe retornar BadRequest para búsqueda con nombre vacío")
        void searchProductsByName_ShouldReturnBadRequest_WhenNameIsEmpty() {
            // Act
            ResponseEntity<List<ProductSummaryDTO>> response =
                    productController.searchProductsByName("");

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(productService, never()).searchProductsByName(anyString());
        }

        @Test
        @DisplayName("Debe buscar productos por categoría y retornar ProductSummaryDTO")
        void getProductsByCategory_ShouldReturnListOfProductSummary_WhenProductsFound() {
            // Arrange
            when(productService.getProductsByCategory("Electrónicos"))
                    .thenReturn(List.of(productSummaryDTO1, productSummaryDTO2));

            // Act
            ResponseEntity<List<ProductSummaryDTO>> response =
                    productController.getProductsByCategory("Electrónicos");

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("Electrónicos", response.getBody().get(0).getCategory());
            assertEquals("Electrónicos", response.getBody().get(1).getCategory());

            verify(productService, times(1)).getProductsByCategory("Electrónicos");
        }

        @Test
        @DisplayName("Debe buscar productos por rango de precio y retornar ProductSummaryDTO")
        void getProductsByPriceRange_ShouldReturnListOfProductSummary_WhenProductsFound() {
            // Arrange
            when(productService.getProductsByPriceRange(500.0, 1000.0))
                    .thenReturn(List.of(productSummaryDTO1));

            // Act
            ResponseEntity<List<ProductSummaryDTO>> response =
                    productController.getProductsByPriceRange(500.0, 1000.0);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertTrue(response.getBody().get(0).getPrice().doubleValue() >= 500.0);
            assertTrue(response.getBody().get(0).getPrice().doubleValue() <= 1000.0);

            verify(productService, times(1)).getProductsByPriceRange(500.0, 1000.0);
        }

        @Test
        @DisplayName("Debe retornar BadRequest cuando precio mínimo > precio máximo")
        void getProductsByPriceRange_ShouldReturnBadRequest_WhenMinPriceGreaterThanMaxPrice() {
            // Act
            ResponseEntity<List<ProductSummaryDTO>> response =
                    productController.getProductsByPriceRange(1000.0, 500.0);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(productService, never()).getProductsByPriceRange(anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("Debe buscar con filtros avanzados y paginación")
        void getProductsWithFilters_ShouldReturnFilteredAndPagedResults() {
            // Arrange
            Page<ProductSummaryDTO> mockPage = new PageImpl<>(List.of(productSummaryDTO1));
            when(productService.getProductsWithFilters(eq("Electrónicos"), eq("Smart"), any()))
                    .thenReturn(mockPage);

            // Act
            ResponseEntity<Page<ProductSummaryDTO>> response =
                    productController.getProductsWithFilters(0, 10, "name", "asc", "Electrónicos", "Smart");

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());

            verify(productService, times(1)).getProductsWithFilters(eq("Electrónicos"), eq("Smart"), any());
        }
    }

    @Nested
    @DisplayName("Gestión de Stock")
    class StockManagementTests {

        @Test
        @DisplayName("Debe obtener productos con stock bajo como ProductResponseDTO")
        void getProductsWithLowStock_ShouldReturnListOfProductResponse_WhenProductsFound() {
            // Arrange
            ProductResponseDTO lowStockProduct = new ProductResponseDTO(
                    3L, "Producto Bajo", "Descripción", "Categoría",
                    new BigDecimal("100.00"), 2, 5, true, false, "LOW_STOCK"
            );

            when(productService.getProductsWithLowStock()).thenReturn(List.of(lowStockProduct));

            // Act
            ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsWithLowStock();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertTrue(response.getBody().get(0).isLowStock());
            assertEquals("LOW_STOCK", response.getBody().get(0).getStockStatus());

            verify(productService, times(1)).getProductsWithLowStock();
        }

        @Test
        @DisplayName("Debe obtener productos sin stock como ProductResponseDTO")
        void getProductsOutOfStock_ShouldReturnListOfProductResponse_WhenProductsFound() {
            // Arrange
            ProductResponseDTO outOfStockProduct = new ProductResponseDTO(
                    4L, "Producto Agotado", "Descripción", "Categoría",
                    new BigDecimal("100.00"), 0, 5, false, true, "OUT_OF_STOCK"
            );

            when(productService.getProductsOutOfStock()).thenReturn(List.of(outOfStockProduct));

            // Act
            ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsOutOfStock();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertTrue(response.getBody().get(0).isOutOfStock());
            assertEquals("OUT_OF_STOCK", response.getBody().get(0).getStockStatus());

            verify(productService, times(1)).getProductsOutOfStock();
        }
    }

    @Nested
    @DisplayName("Utilidades")
    class UtilityTests {

        @Test
        @DisplayName("Debe obtener todas las categorías")
        void getAllCategories_ShouldReturnListOfCategories_WhenCategoriesFound() {
            // Arrange
            when(productService.getAllCategories()).thenReturn(List.of("Electrónicos", "Hogar"));

            // Act
            ResponseEntity<List<String>> response = productController.getAllCategories();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("Electrónicos", response.getBody().get(0));
            assertEquals("Hogar", response.getBody().get(1));

            verify(productService, times(1)).getAllCategories();
        }

        @Test
        @DisplayName("Debe obtener estadísticas de productos")
        void getProductStats_ShouldReturnProductStatistics() {
            // Arrange
            Map<String, Object> mockStats = Map.of(
                    "totalProducts", 5,
                    "lowStockProducts", 1,
                    "outOfStockProducts", 1,
                    "totalCategories", 2,
                    "inStockProducts", 4
            );
            when(productService.getProductStats()).thenReturn(mockStats);

            // Act
            ResponseEntity<Map<String, Object>> response = productController.getProductStats();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(5, response.getBody().get("totalProducts"));
            assertEquals(1, response.getBody().get("lowStockProducts"));
            assertEquals(1, response.getBody().get("outOfStockProducts"));
            assertEquals(2, response.getBody().get("totalCategories"));
            assertEquals(4, response.getBody().get("inStockProducts"));

            verify(productService, times(1)).getProductStats();
        }

        @Test
        @DisplayName("Debe obtener contadores rápidos")
        void getProductCounts_ShouldReturnProductCounts() {
            // Arrange
            Map<String, Object> mockStats = Map.of(
                    "totalProducts", 5,
                    "lowStockProducts", 1,
                    "outOfStockProducts", 1,
                    "inStockProducts", 4
            );
            when(productService.getProductStats()).thenReturn(mockStats);

            // Act
            ResponseEntity<Map<String, Long>> response = productController.getProductCounts();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(5L, response.getBody().get("total"));
            assertEquals(1L, response.getBody().get("lowStock"));
            assertEquals(1L, response.getBody().get("outOfStock"));
            assertEquals(4L, response.getBody().get("inStock"));

            verify(productService, times(1)).getProductStats();
        }

        @Test
        @DisplayName("Debe responder al health check")
        void healthCheck_ShouldReturnHealthStatus() {
            // Act
            ResponseEntity<Map<String, String>> response = productController.healthCheck();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("UP", response.getBody().get("status"));
            assertEquals("ProductService", response.getBody().get("service"));
            assertNotNull(response.getBody().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("Validaciones de Parámetros")
    class ParameterValidationTests {

        @Test
        @DisplayName("Debe corregir parámetros de paginación inválidos")
        void getProductsPaginated_ShouldCorrectInvalidPaginationParams() {
            // Arrange
            Page<ProductSummaryDTO> mockPage = new PageImpl<>(List.of(productSummaryDTO1));
            when(productService.getAllProductsPaginated(any())).thenReturn(mockPage);

            // Act - página negativa y tamaño excesivo
            ResponseEntity<Page<ProductSummaryDTO>> response =
                    productController.getProductsPaginated(-1, 200, "name", "asc");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Los parámetros deben haberse corregido internamente
            verify(productService, times(1)).getAllProductsPaginated(any());
        }

        @Test
        @DisplayName("Debe manejar dirección de ordenamiento inválida")
        void getProductsPaginated_ShouldHandleInvalidSortDirection() {
            // Arrange
            Page<ProductSummaryDTO> mockPage = new PageImpl<>(List.of(productSummaryDTO1));
            when(productService.getAllProductsPaginated(any())).thenReturn(mockPage);

            // Act
            ResponseEntity<Page<ProductSummaryDTO>> response =
                    productController.getProductsPaginated(0, 10, "name", "invalid");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(productService, times(1)).getAllProductsPaginated(any());
        }

        @Test
        @DisplayName("Debe validar parámetros de filtros avanzados")
        void getProductsWithFilters_ShouldHandleNullFilters() {
            // Arrange
            Page<ProductSummaryDTO> mockPage = new PageImpl<>(List.of(productSummaryDTO1, productSummaryDTO2));
            when(productService.getProductsWithFilters(isNull(), isNull(), any())).thenReturn(mockPage);

            // Act
            ResponseEntity<Page<ProductSummaryDTO>> response =
                    productController.getProductsWithFilters(0, 10, "name", "asc", null, null);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().getContent().size());

            verify(productService, times(1)).getProductsWithFilters(isNull(), isNull(), any());
        }
    }
}