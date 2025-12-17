package net.lab1024.sa.common.auth.config;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * 生产环境JWT密钥Fail-Fast校验
 * <p>
 * 目标：避免生产环境因缺失/弱密钥导致的安全事故与不可控行为。
 * </p>
 */
@Configuration
@Profile("prod")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class JwtSecretFailFastValidator {

    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    public ApplicationRunner jwtSecretFailFast(Environment environment) {
        return args -> {
            String secret = environment.getProperty("security.jwt.secret");
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("缺少必需配置：security.jwt.secret（生产环境必须显式配置强密钥）");
            }

            int bytes = secret.getBytes(StandardCharsets.UTF_8).length;
            if (bytes < MIN_SECRET_BYTES) {
                throw new IllegalStateException("JWT密钥长度不足（至少需要 " + MIN_SECRET_BYTES + " 字节）：security.jwt.secret");
            }
        };
    }
}

