package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.Controllers.ProductController;
import com.sistema_de_inventarios_v02.dto.ProductDTO;
import com.sistema_de_inventarios_v02.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDTO testProductDTO_01;
    private ProductDTO testProductDTO_02;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar datos de prueba
        testProductDTO_01 = new ProductDTO();
        testProductDTO_01.setName("Smartphone");
        testProductDTO_01.setDescription("Teléfono inteligente con cámara de 48MP");
        testProductDTO_01.setCategory("Electrónicos");
        testProductDTO_01.setPrice(new BigDecimal("799.99"));
        testProductDTO_01.setStock(25);
        testProductDTO_01.setMinimumStock(10);

        testProductDTO_02 = new ProductDTO();
        testProductDTO_02.setName("Laptop");
        testProductDTO_02.setDescription("Laptop de alta gama");
        testProductDTO_02.setCategory("Electrónicos");
        testProductDTO_02.setPrice(new BigDecimal("1500.00"));
        testProductDTO_02.setStock(10);
        testProductDTO_02.setMinimumStock(5);
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        // Configurar el mock
        // Cuando alguien llame a createProduct en productService con cualquier ProductDTO, se devolvera el testProductDTO que configuramos en setUp
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(testProductDTO_01);

        // Ejecutar el metodo del controlador
        ResponseEntity<ProductDTO> response = productController.createProduct(testProductDTO_01);

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProductDTO_01.getName(), response.getBody().getName());
        assertEquals(testProductDTO_01.getCategory(), response.getBody().getCategory());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).createProduct(any(ProductDTO.class));
    }


    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        // Configurar el mock
        when(productService.getAllProducts()).thenReturn(List.of(testProductDTO_01, testProductDTO_02));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().get(0).getName());
        assertEquals(testProductDTO_02.getName(), response.getBody().get(1).getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getAllProducts();
    }

    @Test
    void getProductPaginated_ShouldReturnOneProductPerPage_WhenPageSizeIs1() {
        // Configurar mock para pagina 0 (size=1, total=2)
        Page<ProductDTO> mockPage = new PageImpl<>(
                List.of(testProductDTO_01),
                PageRequest.of(0, 1),
                2
        );
        when(productService.getAllProductsPaginated(any())).thenReturn(mockPage);

        ResponseEntity<Page<ProductDTO>> response = productController.getProductsPaginated(0, 1);

        // Verificar solo el contenido de la pagina actual (1 elemento)
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().getContent().getFirst().getName());

        // Verificar metadatos de paginación
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getTotalPages());
    }


    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Configurar el mock
        when(productService.getProductById(testProductDTO_01.getId())).thenReturn(testProductDTO_01);

        // Ejecutar el metodo del controlador
        ResponseEntity<ProductDTO> response = productController.getProductById(testProductDTO_01.getId());

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProductDTO_01, response.getBody());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getProductById(testProductDTO_01.getId());
    }


    @Test
    void updateProduct_ShouldReturnUpdatedProduct_WhenProductExists() {
        // Configurar el mock
        when(productService.updateProduct(testProductDTO_01.getId(), testProductDTO_01)).thenReturn(testProductDTO_01);

        // Ejecutar el metodo del controlador
        ResponseEntity<ProductDTO> response = productController.updateProduct(testProductDTO_01.getId(), testProductDTO_01);

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProductDTO_01, response.getBody());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).updateProduct(testProductDTO_01.getId(), testProductDTO_01);
    }

    @Test
    void deleteProduct_shouldReturnSuccessMessage_WhenProductExists() {
        // Configurar el mock
        Mockito.doNothing().when(productService).deleteProduct(testProductDTO_01.getId());

        // Ejecutar el metodo del controlador
        ResponseEntity<Map<String, String>> response = productController.deleteProduct(testProductDTO_01.getId());

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Producto eliminado exitosamente", response.getBody().get("message"));

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).deleteProduct(testProductDTO_01.getId());
    }


    @Test
    void searchProductsByName_ShouldReturnListOfProducts_WhenProductsFound() {
        // Configurar el mock
        when(productService.searchProductsByName("Smartphone")).thenReturn(List.of(testProductDTO_01));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.searchProductsByName("Smartphone");

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().getFirst().getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).searchProductsByName("Smartphone");
    }

    @Test
    void getProductsByCategory_ShouldReturnListOfProducts_WhenProductsFound() {
        // Configurar el mock
        when(productService.getProductsByCategory("Electrónicos")).thenReturn(List.of(testProductDTO_01, testProductDTO_02));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.getProductsByCategory("Electrónicos");

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().get(0).getName());
        assertEquals(testProductDTO_02.getName(), response.getBody().get(1).getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getProductsByCategory("Electrónicos");
    }

    @Test
    void getProductsWithLowStock_ShouldReturnListOfProducts_WhenProductsFound() {
        // Configurar el mock
        when(productService.getProductsWithLowStock()).thenReturn(List.of(testProductDTO_01));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.getProductsWithLowStock();

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().getFirst().getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getProductsWithLowStock();
    }

    @Test
    void getProductsOutOfStock_ShouldReturnListOfProducts_WhenProductsFound() {
        // Configurar el mock
        when(productService.getProductsOutOfStock()).thenReturn(List.of(testProductDTO_02));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.getProductsOutOfStock();

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductDTO_02.getName(), response.getBody().getFirst().getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getProductsOutOfStock();
    }

    // todo: poner en front end
    @Test
    void updateProductStock_ShouldReturnUpdatedProduct_WhenProductExists() {
        // Configurar el mock
        when(productService.updateProductStock(testProductDTO_01.getId(), 30)).thenReturn(testProductDTO_01);

        // Ejecutar el metodo del controlador
        ResponseEntity<ProductDTO> response = productController.updateProductStock(testProductDTO_01.getId(), Map.of("stock", 30));

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProductDTO_01, response.getBody());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).updateProductStock(testProductDTO_01.getId(), 30);
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories_WhenCategoriesFound() {
        // Configurar el mock
        when(productService.getAllCategories()).thenReturn(List.of("Electrónicos", "Hogar"));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<String>> response = productController.getAllCategories();

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Electrónicos", response.getBody().get(0));
        assertEquals("Hogar", response.getBody().get(1));

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getAllCategories();
    }

    @Test
    void getProductsByPriceRange_ShouldReturnListOfProducts_WhenProductsFound() {
        // Configurar el mock
        when(productService.getProductsByPriceRange(500.00, 1000.00)).thenReturn(List.of(testProductDTO_01));

        // Ejecutar el metodo del controlador
        ResponseEntity<List<ProductDTO>> response = productController.getProductsByPriceRange(500.00, 1000.00);

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductDTO_01.getName(), response.getBody().getFirst().getName());

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getProductsByPriceRange(500.00, 1000.00);
    }

    @Test
    void getProductStats_ShouldReturnProductStatistics() {
        // Configurar el mock
        when(productService.getAllProducts()).thenReturn(List.of(testProductDTO_01, testProductDTO_02));
        when(productService.getProductsWithLowStock()).thenReturn(List.of(testProductDTO_01));
        when(productService.getProductsOutOfStock()).thenReturn(List.of(testProductDTO_02));
        when(productService.getAllCategories()).thenReturn(List.of("Electrónicos", "Hogar"));

        // Ejecutar el metodo del controlador
        ResponseEntity<Map<String, Object>> response = productController.getProductStats();

        // Verificar los resultados
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("totalProducts"));
        assertEquals(1, response.getBody().get("lowStockProducts"));
        assertEquals(1, response.getBody().get("outOfStockProducts"));
        assertEquals(2, response.getBody().get("totalCategories"));

        // Verificar que el servicio fue llamado
        Mockito.verify(productService).getAllProducts();
        Mockito.verify(productService).getProductsWithLowStock();
        Mockito.verify(productService).getProductsOutOfStock();
        Mockito.verify(productService).getAllCategories();
    }
}