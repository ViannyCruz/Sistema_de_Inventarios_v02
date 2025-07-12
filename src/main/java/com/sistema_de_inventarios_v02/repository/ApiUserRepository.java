package com.sistema_de_inventarios_v02.repository;

import com.sistema_de_inventarios_v02.model.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {
    Optional<ApiUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
