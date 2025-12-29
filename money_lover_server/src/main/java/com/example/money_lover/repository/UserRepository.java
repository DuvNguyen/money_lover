package com.example.money_lover.repository;

import com.example.money_lover.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Spring Data JPA sẽ tự động generate câu lệnh SQL từ tên hàm
    boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);
}