package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.exception.ProductNotFoundException;
import com.sistema_de_inventarios_v02.exception.DuplicateProductException;
import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // CREAR PRODUCTO
    public ProductResponseDTO createProduct(CreateProductDTO createProductDTO) {
        if (productRepository.existsByNameIgnoreCase(createProductDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + createProductDTO.getName());
        }

        Product product = convertCreateDTOToEntity(createProductDTO);
        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);
    }

    // OBTENER TODOS LOS PRODUCTOS (SUMMARY)
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getAllProductsSummary() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // OBTENER TODOS LOS PRODUCTOS PAGINADOS (SUMMARY)
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProductsPaginated(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(this::convertToSummaryDTO);
    }

    // OBTENER PRODUCTOS CON FILTROS
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getProductsWithFilters(String category, String name, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryAndName(category, name, pageable);
        return products.map(this::convertToSummaryDTO);
    }

    // OBTENER PRODUCTO POR ID (COMPLETO)
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        return convertToResponseDTO(product);
    }

    // ACTUALIZAR PRODUCTO
    public ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        // Verificar nombre duplicado solo si se está cambiando
        if (updateProductDTO.getName() != null &&
                !existingProduct.getName().equalsIgnoreCase(updateProductDTO.getName()) &&
                productRepository.existsByNameIgnoreCase(updateProductDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + updateProductDTO.getName());
        }

        updateEntityFromUpdateDTO(existingProduct, updateProductDTO);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToResponseDTO(updatedProduct);
    }

    // ELIMINAR PRODUCTO
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // BUSCAR PRODUCTOS POR NOMBRE
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // OBTENER PRODUCTOS POR CATEGORÍA
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // OBTENER PRODUCTOS CON STOCK BAJO
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsWithLowStock() {
        List<Product> products = productRepository.findProductsWithLowStock();
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // OBTENER PRODUCTOS SIN STOCK
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsOutOfStock() {
        List<Product> products = productRepository.findProductsOutOfStock();
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ACTUALIZAR STOCK DE PRODUCTO
    public ProductResponseDTO updateProductStock(Long id, StockUpdateDTO stockUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        product.setStock(stockUpdateDTO.getStock());
        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }

    // OBTENER TODAS LAS CATEGORÍAS
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    // OBTENER PRODUCTOS POR RANGO DE PRECIO
    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    // OBTENER ESTADÍSTICAS DE PRODUCTOS
    @Transactional(readOnly = true)
    public Map<String, Object> getProductStats() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> lowStockProducts = productRepository.findProductsWithLowStock();
        List<Product> outOfStockProducts = productRepository.findProductsOutOfStock();
        List<String> categories = productRepository.findAllCategories();

        return Map.of(
                "totalProducts", allProducts.size(),
                "lowStockProducts", lowStockProducts.size(),
                "outOfStockProducts", outOfStockProducts.size(),
                "totalCategories", categories.size(),
                "inStockProducts", allProducts.size() - outOfStockProducts.size()
        );
    }

    // ===== MÉTODOS DE CONVERSIÓN =====

    // Convertir CreateProductDTO a Entity
    private Product convertCreateDTOToEntity(CreateProductDTO createDTO) {
        return new Product(
                createDTO.getName(),
                createDTO.getDescription(),
                createDTO.getCategory(),
                createDTO.getPrice(),
                createDTO.getStock(),
                createDTO.getMinimumStock()
        );
    }

    // Actualizar Entity desde UpdateProductDTO
    private void updateEntityFromUpdateDTO(Product product, UpdateProductDTO updateDTO) {
        if (updateDTO.getName() != null) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCategory() != null) {
            product.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
        if (updateDTO.getMinimumStock() != null) {
            product.setMinimumStock(updateDTO.getMinimumStock());
        }
    }

    // Convertir Entity a ProductResponseDTO
    private ProductResponseDTO convertToResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getMinimumStock(),
                product.isLowStock(),
                product.isOutOfStock(),
                calculateStockStatus(product)
        );
    }

    // Convertir Entity a ProductSummaryDTO
    private ProductSummaryDTO convertToSummaryDTO(Product product) {
        return new ProductSummaryDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                calculateStockStatus(product)
        );
    }

    // Calcular estado del stock
    private String calculateStockStatus(Product product) {
        if (product.isOutOfStock()) {
            return "OUT_OF_STOCK";
        }
        if (product.isLowStock()) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }
}