package com.example.money_lover.repository;

import com.example.money_lover.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    // JpaRepository<Role, String>: String ở đây là kiểu dữ liệu của khoá chính (@Id String name)
}