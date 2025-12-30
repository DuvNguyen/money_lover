package com.example.money_lover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class MoneyLoverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyLoverApplication.class, args);
	}

	/**
     * Bean này chỉ chạy khi ở profile 'dev'.
     * Nó sẽ XÓA SẠCH Database sau đó tạo lại từ đầu mỗi khi chạy App.
     */
    @Bean
    @Profile("dev") // Quan trọng: Chỉ áp dụng cho môi trường Dev, không dùng cho Prod!
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();   // 1. Xóa sạch bảng và dữ liệu cũ
            flyway.migrate(); // 2. Chạy lại V1 (Tạo bảng), V2 (Seed data)
        };
    }
}
