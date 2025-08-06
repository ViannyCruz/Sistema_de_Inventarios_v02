package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductHistoryService {

    private final ProductHistoryRepository productHistoryRepository;

    @Autowired
    public ProductHistoryService(ProductHistoryRepository productHistoryRepository) {
        this.productHistoryRepository = productHistoryRepository;
    }

    public List<Product> getProductHistory(Long productId) {
        return productHistoryRepository.getProductRevisions(productId);
    }

    public Product getProductAtRevision(Long productId, Number revisionId) {
        return productHistoryRepository.getProductAtRevision(productId, revisionId);
    }

    public List<Number> getProductRevisionsList(Long productId) {
        return productHistoryRepository.getRevisions(productId);
    }
}