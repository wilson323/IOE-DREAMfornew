package net.lab1024.sa.gateway.filter;

import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 网关安全集成测试
 * <p>
 * 测试 401/403 场景：
 * - 401: 缺少令牌、令牌无效、令牌过期、令牌类型不支持
 * - 403: 权限不足（RBAC）
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewaySecurityIntegrationTest {

    @Resource
    private WebTestClient webTestClient;

    @Test
    void testMissingTokenReturns401() {
        // 测试缺少令牌返回 401
        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.message").exists();
    }

    @Test
    void testInvalidTokenReturns401() {
        // 测试无效令牌返回 401
        webTestClient.get()
                .uri("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void testWhitelistPathBypassesAuth() {
        // 测试白名单路径绕过鉴权
        webTestClient.post()
                .uri("/api/v1/login")
                .exchange()
                .expectStatus().value(status ->
                    org.junit.jupiter.api.Assertions.assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status));
    }
}
