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
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final DefaultAdminProperties defaultAdminProperties;

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

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "app.default-admin")
    public static class DefaultAdminProperties {
        private String username = "admin";
        private String password = "123456";
    }
}
