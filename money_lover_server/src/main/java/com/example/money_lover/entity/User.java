package com.example.money_lover.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    String password;

    @Column(name = "full_name")
    String fullName;

    // Map với cột created_at trong DB
    @Column(name = "created_at")
    LocalDateTime createdAt;

    // Map với cột updated_at trong DB
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // --- CẤU HÌNH MANY-TO-MANY ---
    @ManyToMany(fetch = FetchType.EAGER) 
    // fetch = EAGER: Lấy User -> lấy luôn Role (Rất quan trọng cho Security)
    @JoinTable(
            name = "user_roles",                           // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "user_id"),   // Khóa liên kết với bảng User này
            inverseJoinColumns = @JoinColumn(name = "role_name") // Khóa liên kết với bảng Role kia
    )
    Set<Role> roles;

    // --- TỰ ĐỘNG CẬP NHẬT THỜI GIAN ---
    
    @PrePersist // Chạy trước khi lưu vào DB (Insert)
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Chạy trước khi cập nhật DB (Update)
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}