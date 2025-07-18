package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.exception.ProductNotFoundException;
import com.sistema_de_inventarios_v02.exception.DuplicateProductException;
import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public ProductResponseDTO createProduct(CreateProductDTO createProductDTO) {
        if (productRepository.existsByNameIgnoreCase(createProductDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + createProductDTO.getName());
        }

        Product product = convertCreateDTOToEntity(createProductDTO);
        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getAllProductsSummary() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProductsPaginated(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(this::convertToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getProductsWithFilters(String category, String name, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryAndName(category, name, pageable);
        return products.map(this::convertToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        return convertToResponseDTO(product);
    }

    public ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        if (updateProductDTO.getName() != null &&
                !existingProduct.getName().equalsIgnoreCase(updateProductDTO.getName()) &&
                productRepository.existsByNameIgnoreCase(updateProductDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + updateProductDTO.getName());
        }

        updateEntityFromUpdateDTO(existingProduct, updateProductDTO);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToResponseDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsWithLowStock() {
        List<Product> products = productRepository.findProductsWithLowStock();
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsOutOfStock() {
        List<Product> products = productRepository.findProductsOutOfStock();
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO updateProductStock(Long id, StockUpdateDTO stockUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        product.setStock(stockUpdateDTO.getStock());
        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

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