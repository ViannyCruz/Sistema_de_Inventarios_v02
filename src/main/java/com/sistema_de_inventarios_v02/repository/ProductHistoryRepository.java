package com.sistema_de_inventarios_v02.repository;

import com.sistema_de_inventarios_v02.model.Product;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ProductHistoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Product> getProductRevisions(Long productId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .add(AuditEntity.id().eq(productId))
                .addOrder(AuditEntity.revisionNumber().desc());
        
        return query.getResultList();
    }

    public Product getProductAtRevision(Long productId, Number revisionId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(Product.class, productId, revisionId);
    }

    public List<Number> getRevisions(Long productId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.getRevisions(Product.class, productId);
    }
}