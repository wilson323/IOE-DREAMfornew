package net.lab1024.sa.common.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 轻量级验证配置
 * <p>
 * 避免过度复杂的验证逻辑，只提供核心功能：
 * - JSR-380验证支持
 * - 方法参数验证
 * - 自定义验证器
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring配置类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "validation.enabled", havingValue = "true", matchIfMissing = true)
public class LightValidationConfiguration {

    /**
     * 方法验证后处理器
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * 验证器
     */
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    /**
     * 轻量级验证工具类
     */
    public static class LightValidationUtil {

        private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        /**
         * 验证对象
         */
        public static <T> void validate(T object) {
            var violations = validator.validate(object);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((s1, s2) -> s1 + "; " + s2)
                        .orElse("验证失败");
                throw new IllegalArgumentException(message);
            }
        }

        /**
         * 验证对象并返回第一个错误消息
         */
        public static <T> String getFirstErrorMessage(T object) {
            var violations = validator.validate(object);
            return violations.isEmpty() ? null : violations.iterator().next().getMessage();
        }
    }
}
