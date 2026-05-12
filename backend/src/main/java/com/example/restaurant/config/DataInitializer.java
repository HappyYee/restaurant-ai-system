package com.example.restaurant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.entity.AdminUser;
import com.example.restaurant.mapper.AdminUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final DefaultAdminProperties defaultAdminProperties;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner schemaCompatibilityRunner() {
        return args -> {
            ensureColumn("user", "member_level", "VARCHAR(20) NOT NULL DEFAULT '普通会员' COMMENT '会员等级'");
            ensureColumn("user", "points", "INT NOT NULL DEFAULT 0 COMMENT '会员积分'");
            ensureColumn("user", "total_spent", "DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费'");
            ensureColumn("user", "member_since", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入会时间'");
            ensureColumn("orders", "original_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员价餐品原始合计'");
            ensureColumn("orders", "points_used", "INT NOT NULL DEFAULT 0 COMMENT '本单使用积分'");
            ensureColumn("orders", "points_discount", "DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额'");
            ensureColumn("orders", "points_earned", "INT NOT NULL DEFAULT 0 COMMENT '本单获得积分'");
        };
    }

    @Bean
    public CommandLineRunner defaultAdminRunner() {
        return args -> {
            Long count = adminUserMapper.selectCount(
                    new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, defaultAdminProperties.getUsername())
            );
            if (count == 0) {
                AdminUser adminUser = new AdminUser();
                adminUser.setUsername(defaultAdminProperties.getUsername());
                adminUser.setPasswordHash(passwordEncoder.encode(defaultAdminProperties.getPassword()));
                adminUser.setStatus(1);
                adminUserMapper.insert(adminUser);
            }
        };
    }

    private void ensureColumn(String tableName, String columnName, String columnDefinition) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Long.class, tableName, columnName);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDefinition);
        }
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "app.default-admin")
    public static class DefaultAdminProperties {
        private String username = "admin";
        private String password = "123456";
    }
}
