package net.lab1024.sa.common.auth;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Token解析器测试
 *
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Slf4j
class JwtTokenParserTest {

    private JwtTokenParser jwtTokenParser;

    @BeforeEach
    void setUp() {
        jwtTokenParser = new JwtTokenParser();
    }

    @Test
    void testParseToken_NullToken() {
        log.info("[JWT解析测试] 测试null token");

        // Given
        String token = null;

        // When
        UserContext context = jwtTokenParser.parseToken(token);

        // Then
        assertNull(context);
    }

    @Test
    void testParseToken_EmptyToken() {
        log.info("[JWT解析测试] 测试空token");

        // Given
        String token = "";

        // When
        UserContext context = jwtTokenParser.parseToken(token);

        // Then
        assertNull(context);
    }

    @Test
    void testParseToken_WithBearerPrefix() {
        log.info("[JWT解析测试] 测试带Bearer前缀的token");

        // Given
        String token = "Bearer test_token_value";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock StpUtil.getLoginIdAsString() 返回用户ID字符串
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn("1001");
            Mockito.when(StpUtil.isLogin()).thenReturn(true);

            // When
            UserContext context = jwtTokenParser.parseToken(token);

            // Then
            assertNotNull(context);
            assertEquals(1001L, context.getUserId());
            assertEquals("1001", context.getUserName());

            log.info("[JWT解析测试] 解析成功: userId={}, userName={}",
                     context.getUserId(), context.getUserName());
        }
    }

    @Test
    void testParseToken_WithoutBearerPrefix() {
        log.info("[JWT解析测试] 测试不带Bearer前缀的token");

        // Given
        String token = "test_token_value";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock StpUtil.getLoginIdAsString() 返回用户ID字符串
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn("1002");
            Mockito.when(StpUtil.isLogin()).thenReturn(true);

            // When
            UserContext context = jwtTokenParser.parseToken(token);

            // Then
            assertNotNull(context);
            assertEquals(1002L, context.getUserId());
            assertEquals("1002", context.getUserName());

            log.info("[JWT解析测试] 解析成功: userId={}, userName={}",
                     context.getUserId(), context.getUserName());
        }
    }

    @Test
    void testParseToken_InvalidToken() {
        log.info("[JWT解析测试] 测试无效token");

        // Given
        String token = "Bearer invalid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock返回null表示token无效
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn(null);
            Mockito.when(StpUtil.isLogin()).thenReturn(false);

            // When
            UserContext context = jwtTokenParser.parseToken(token);

            // Then
            assertNull(context);
        }
    }

    @Test
    void testValidateToken_ValidToken() {
        log.info("[JWT解析测试] 测试有效token验证");

        // Given
        String token = "valid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock StpUtil.isLogin() 返回true
            Mockito.when(StpUtil.isLogin()).thenReturn(true);

            // When
            boolean isValid = jwtTokenParser.validateToken(token);

            // Then
            assertTrue(isValid);
        }
    }

    @Test
    void testValidateToken_InvalidToken() {
        log.info("[JWT解析测试] 测试无效token验证");

        // Given
        String token = "invalid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock返回false表示token无效
            Mockito.when(StpUtil.isLogin()).thenReturn(false);

            // When
            boolean isValid = jwtTokenParser.validateToken(token);

            // Then
            assertFalse(isValid);
        }
    }

    @Test
    void testExtractUserId_Success() {
        log.info("[JWT解析测试] 测试提取用户ID成功");

        // Given
        String token = "valid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock StpUtil.getLoginIdAsString() 返回用户ID字符串
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn("1003");
            Mockito.when(StpUtil.isLogin()).thenReturn(true);

            // When
            Long userId = jwtTokenParser.extractUserId(token);

            // Then
            assertNotNull(userId);
            assertEquals(1003L, userId);
        }
    }

    @Test
    void testExtractUserId_Failed() {
        log.info("[JWT解析测试] 测试提取用户ID失败");

        // Given
        String token = "invalid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock返回null
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn(null);
            Mockito.when(StpUtil.isLogin()).thenReturn(false);

            // When
            Long userId = jwtTokenParser.extractUserId(token);

            // Then
            assertNull(userId);
        }
    }

    @Test
    void testExtractUserName_Success() {
        log.info("[JWT解析测试] 测试提取用户名成功");

        // Given
        String token = "valid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock StpUtil.getLoginIdAsString() 返回用户ID字符串
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn("1004");
            Mockito.when(StpUtil.isLogin()).thenReturn(true);

            // When
            String userName = jwtTokenParser.extractUserName(token);

            // Then
            assertNotNull(userName);
            assertEquals("1004", userName);
        }
    }

    @Test
    void testExtractUserName_Failed() {
        log.info("[JWT解析测试] 测试提取用户名失败");

        // Given
        String token = "invalid_token";

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // Mock返回null
            Mockito.when(StpUtil.getLoginIdAsString()).thenReturn(null);
            Mockito.when(StpUtil.isLogin()).thenReturn(false);

            // When
            String userName = jwtTokenParser.extractUserName(token);

            // Then
            assertNull(userName);
        }
    }
}
