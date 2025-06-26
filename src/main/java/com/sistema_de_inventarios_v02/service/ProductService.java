package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.dto.ProductDTO;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsByNameIgnoreCase(productDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + productDTO.getName());
        }

        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsWithFilters(String category, String name, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryAndName(category, name, pageable);
        return products.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        return convertToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        if (!existingProduct.getName().equalsIgnoreCase(productDTO.getName()) &&
                productRepository.existsByNameIgnoreCase(productDTO.getName())) {
            throw new DuplicateProductException("Ya existe un producto con el nombre: " + productDTO.getName());
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setMinimumStock(productDTO.getMinimumStock());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsWithLowStock() {
        List<Product> products = productRepository.findProductsWithLowStock();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsOutOfStock() {
        List<Product> products = productRepository.findProductsOutOfStock();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO updateProductStock(Long id, Integer newStock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getMinimumStock()
        );
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return new Product(
                productDTO.getName(),
                productDTO.getDescription(),
                productDTO.getCategory(),
                productDTO.getPrice(),
                productDTO.getStock(),
                productDTO.getMinimumStock()
        );
    }

    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(this::convertToDTO);
    }
}