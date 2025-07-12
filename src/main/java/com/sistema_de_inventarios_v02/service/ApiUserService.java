package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.model.ApiUser;
import com.sistema_de_inventarios_v02.repository.ApiUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApiUserService {

    @Autowired
    private ApiUserRepository apiUserRepository;

    public ApiUser save(ApiUser user) {
        return apiUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public ApiUser findByUsername(String username) {
        return apiUserRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return apiUserRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public ApiUser findById(Long id) {
        return apiUserRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ApiUser> findAll() {
        return apiUserRepository.findAll();
    }

    public void deleteById(Long id) {
        apiUserRepository.deleteById(id);
    }

    public ApiUser updateUser(Long id, ApiUser updatedUser) {
        ApiUser existingUser = findById(id);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEnabled(updatedUser.isEnabled());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(updatedUser.getPassword());
            }
            return apiUserRepository.save(existingUser);
        }
        return null;
    }
}
