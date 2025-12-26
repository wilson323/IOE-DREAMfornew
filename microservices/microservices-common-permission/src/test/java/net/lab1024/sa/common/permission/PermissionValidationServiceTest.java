package net.lab1024.sa.common.permission;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.annotation.*;
import net.lab1024.sa.common.permission.service.PermissionValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 权限验证服务测试类
 * <p>
 * 测试内容：
 * 1. API级权限验证测试
 * 2. 操作级权限验证测试
 * 3. 数据级权限验证测试
 * 4. 字段级权限验证和脱敏测试
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@SpringBootTest
@DisplayName("四级权限控制测试")
public class PermissionValidationServiceTest {

    private PermissionValidationService permissionValidationService;

    private Set<String> userPermissions;
    private Set<String> userRoles;

    @BeforeEach
    void setUp() {
        permissionValidationService = new PermissionValidationService();

        // 模拟用户权限
        userPermissions = new HashSet<>(Arrays.asList(
                "user:view",
                "user:create",
                "device:view",
                "access:view"
        ));

        // 模拟用户角色
        userRoles = new HashSet<>(Arrays.asList(
                "ROLE_USER",
                "ROLE_OPERATOR"
        ));
    }

    @Test
    @DisplayName("测试API级权限验证 - 有权限")
    void testApiPermissionWithPermission() {
        log.info("[权限测试] 开始测试API级权限验证 - 有权限");

        // 创建注解实例
        ApiPermission annotation = new ApiPermission() {
            @Override
            public String[] value() {
                return new String[]{"user:view"};
            }

            @Override
            public String[] roles() {
                return new String[]{};
            }

            @Override
            public String module() {
                return "user";
            }

            @Override
            public String operation() {
                return "view";
            }

            @Override
            public String[] ipWhitelist() {
                return new String[]{};
            }

            @Override
            public String timeWindow() {
                return "";
            }

            @Override
            public boolean requireAuth() {
                return true;
            }

            @Override
            public boolean rateLimit() {
                return false;
            }

            @Override
            public int rateLimitPerMinute() {
                return 60;
            }

            @Override
            public String description() {
                return "查看用户信息";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ApiPermission.class;
            }
        };

        boolean result = permissionValidationService.validateApiPermission(
                annotation, userPermissions, userRoles);

        log.info("[权限测试] API权限验证结果: {}", result);
        assert result : "用户应有user:view权限";
    }

    @Test
    @DisplayName("测试API级权限验证 - 无权限")
    void testApiPermissionWithoutPermission() {
        log.info("[权限测试] 开始测试API级权限验证 - 无权限");

        ApiPermission annotation = new ApiPermission() {
            @Override
            public String[] value() {
                return new String[]{"user:delete"};
            }

            @Override
            public String[] roles() {
                return new String[]{};
            }

            @Override
            public String module() {
                return "user";
            }

            @Override
            public String operation() {
                return "delete";
            }

            @Override
            public String[] ipWhitelist() {
                return new String[]{};
            }

            @Override
            public String timeWindow() {
                return "";
            }

            @Override
            public boolean requireAuth() {
                return true;
            }

            @Override
            public boolean rateLimit() {
                return false;
            }

            @Override
            public int rateLimitPerMinute() {
                return 60;
            }

            @Override
            public String description() {
                return "删除用户";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ApiPermission.class;
            }
        };

        boolean result = permissionValidationService.validateApiPermission(
                annotation, userPermissions, userRoles);

        log.info("[权限测试] API权限验证结果: {}", result);
        assert !result : "用户应无user:delete权限";
    }

    @Test
    @DisplayName("测试操作级权限验证")
    void testOperationPermission() {
        log.info("[权限测试] 开始测试操作级权限验证");

        OperationPermission annotation = new OperationPermission() {
            @Override
            public OperationPermission.OperationType value() {
                return OperationPermission.OperationType.DELETE;
            }

            @Override
            public String[] permission() {
                return new String[]{"user:delete"};
            }

            @Override
            public String[] roles() {
                return new String[]{"ROLE_ADMIN"};
            }

            @Override
            public String resourceType() {
                return "user";
            }

            @Override
            public boolean requireConfirmation() {
                return true;
            }

            @Override
            public String confirmMessage() {
                return "确定要删除该用户吗？";
            }

            @Override
            public boolean logOperation() {
                return true;
            }

            @Override
            public String description() {
                return "删除用户操作";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return OperationPermission.class;
            }
        };

        boolean result = permissionValidationService.validateOperationPermission(
                annotation, userPermissions, userRoles);

        log.info("[权限测试] 操作权限验证结果: {}", result);
        assert !result : "用户应无删除操作权限";
    }

    @Test
    @DisplayName("测试数据级权限 - 仅本人数据")
    void testDataPermissionUserSelf() {
        log.info("[权限测试] 开始测试数据级权限 - 仅本人数据");

        DataPermission annotation = new DataPermission() {
            @Override
            public DataPermission.DataScopeType value() {
                return DataPermission.DataScopeType.USER_SELF;
            }

            @Override
            public String expression() {
                return "";
            }

            @Override
            public String column() {
                return "create_user_id";
            }

            @Override
            public boolean ignore() {
                return false;
            }

            @Override
            public String description() {
                return "仅本人数据";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return DataPermission.class;
            }
        };

        Long userId = 1001L;
        String sql = permissionValidationService.generateDataPermissionSql(
                annotation, userId, 100L, Arrays.asList(1L, 2L, 3L));

        log.info("[权限测试] 数据权限SQL: {}", sql);
        assert sql != null && !sql.isEmpty() : "应生成数据权限SQL";
        assert sql.contains("create_user_id = ") : "SQL应包含create_user_id条件";
    }

    @Test
    @DisplayName("测试数据级权限 - 全部数据")
    void testDataPermissionUserAll() {
        log.info("[权限测试] 开始测试数据级权限 - 全部数据");

        DataPermission annotation = new DataPermission() {
            @Override
            public DataPermission.DataScopeType value() {
                return DataPermission.DataScopeType.USER_ALL;
            }

            @Override
            public String expression() {
                return "";
            }

            @Override
            public String column() {
                return "create_user_id";
            }

            @Override
            public boolean ignore() {
                return false;
            }

            @Override
            public String description() {
                return "全部数据";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return DataPermission.class;
            }
        };

        String sql = permissionValidationService.generateDataPermissionSql(
                annotation, 1001L, 100L, Arrays.asList(1L, 2L, 3L));

        log.info("[权限测试] 数据权限SQL: {}", sql);
        // 全部数据不需要添加WHERE条件
        assert sql == null || sql.isEmpty() : "全部数据不应生成WHERE条件";
    }

    @Test
    @DisplayName("测试字段级权限 - 手机号脱敏")
    void testFieldPermissionPhoneMask() throws NoSuchFieldException {
        log.info("[权限测试] 开始测试字段级权限 - 手机号脱敏");

        // 创建测试类
        class TestUser {
            @FieldPermission(
                    field = "phone",
                    value = {"user:view_phone"},
                    maskStrategy = FieldPermission.MaskStrategy.PHONE
            )
            public String phone;

            public TestUser(String phone) {
                this.phone = phone;
            }
        }

        TestUser testUser = new TestUser("13800138000");
        Field phoneField = TestUser.class.getDeclaredField("phone");

        // 用户无查看手机号权限
        Set<String> limitedPermissions = new HashSet<>(Arrays.asList("user:view"));

        Object maskedValue = permissionValidationService.validateAndMaskField(
                phoneField, testUser.phone, limitedPermissions, userRoles);

        log.info("[权限测试] 手机号脱敏结果: {} -> {}", testUser.phone, maskedValue);
        assert maskedValue != null : "脱敏值不应为null";
        assert maskedValue.toString().contains("****") : "应包含脱敏符号";
        assert !maskedValue.equals(testUser.phone) : "脱敏后值应与原值不同";
    }

    @Test
    @DisplayName("测试字段级权限 - 姓名脱敏")
    void testFieldPermissionNameMask() throws NoSuchFieldException {
        log.info("[权限测试] 开始测试字段级权限 - 姓名脱敏");

        class TestUser {
            @FieldPermission(
                    field = "username",
                    value = {"user:view_name"},
                    maskStrategy = FieldPermission.MaskStrategy.NAME
            )
            public String username;

            public TestUser(String username) {
                this.username = username;
            }
        }

        TestUser testUser = new TestUser("张三");
        Field usernameField = TestUser.class.getDeclaredField("username");

        // 用户无查看姓名权限
        Set<String> limitedPermissions = new HashSet<>(Arrays.asList("user:view"));

        Object maskedValue = permissionValidationService.validateAndMaskField(
                usernameField, testUser.username, limitedPermissions, userRoles);

        log.info("[权限测试] 姓名脱敏结果: {} -> {}", testUser.username, maskedValue);
        assert maskedValue != null : "脱敏值不应为null";
        assert "**".equals(maskedValue) : "姓名应脱敏为**";
    }

    @Test
    @DisplayName("测试字段级权限 - 完全隐藏")
    void testFieldPermissionHidden() throws NoSuchFieldException {
        log.info("[权限测试] 开始测试字段级权限 - 完全隐藏");

        class TestUser {
            @FieldPermission(
                    field = "idCard",
                    value = {"user:view_idcard"},
                    maskStrategy = FieldPermission.MaskStrategy.HIDDEN
            )
            public String idCard;

            public TestUser(String idCard) {
                this.idCard = idCard;
            }
        }

        TestUser testUser = new TestUser("110101199001011234");
        Field idCardField = TestUser.class.getDeclaredField("idCard");

        // 用户无查看身份证号权限
        Set<String> limitedPermissions = new HashSet<>(Arrays.asList("user:view"));

        Object maskedValue = permissionValidationService.validateAndMaskField(
                idCardField, testUser.idCard, limitedPermissions, userRoles);

        log.info("[权限测试] 身份证号脱敏结果: {} -> {}", testUser.idCard, maskedValue);
        assert maskedValue == null : "完全隐藏策略应返回null";
    }

    @Test
    @DisplayName("测试字段级权限 - 有权限查看")
    void testFieldPermissionWithPermission() throws NoSuchFieldException {
        log.info("[权限测试] 开始测试字段级权限 - 有权限查看");

        class TestUser {
            @FieldPermission(
                    field = "email",
                    value = {"user:view_email"},
                    maskStrategy = FieldPermission.MaskStrategy.EMAIL
            )
            public String email;

            public TestUser(String email) {
                this.email = email;
            }
        }

        TestUser testUser = new TestUser("admin@example.com");
        Field emailField = TestUser.class.getDeclaredField("email");

        // 用户有查看邮箱权限
        Set<String> fullPermissions = new HashSet<>(Arrays.asList("user:view", "user:view_email"));

        Object maskedValue = permissionValidationService.validateAndMaskField(
                emailField, testUser.email, fullPermissions, userRoles);

        log.info("[权限测试] 邮箱查看结果: {} -> {}", testUser.email, maskedValue);
        assert maskedValue != null : "有权限查看时不应为null";
        assert maskedValue.equals(testUser.email) : "有权限时应返回原始值";
    }
}
