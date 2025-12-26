package net.lab1024.sa.common.permission.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.annotation.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 权限验证服务
 * <p>
 * 功能：统一处理四级权限控制验证
 * 1. API级权限验证 - @ApiPermission
 * 2. 操作级权限验证 - @OperationPermission
 * 3. 数据级权限验证 - @DataPermission
 * 4. 字段级权限验证 - @FieldPermission
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class PermissionValidationService {

    /**
     * 验证API级权限
     *
     * @param annotation API权限注解
     * @param userPermissions 用户权限列表
     * @param userRoles 用户角色列表
     * @return 是否有权限
     */
    public boolean validateApiPermission(ApiPermission annotation,
                                         Set<String> userPermissions,
                                         Set<String> userRoles) {
        if (annotation == null) {
            return true;
        }

        // 检查权限代码
        String[] requiredPermissions = annotation.value();
        if (requiredPermissions != null && requiredPermissions.length > 0) {
            if (userPermissions == null || userPermissions.isEmpty()) {
                log.warn("[API权限] 用户权限列表为空，拒绝访问");
                return false;
            }

            boolean hasPermission = false;
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                log.warn("[API权限] 用户缺少所需权限: required={}", Arrays.toString(requiredPermissions));
                return false;
            }
        }

        // 检查角色代码
        String[] requiredRoles = annotation.roles();
        if (requiredRoles != null && requiredRoles.length > 0) {
            if (userRoles == null || userRoles.isEmpty()) {
                log.warn("[API权限] 用户角色列表为空，拒绝访问");
                return false;
            }

            boolean hasRole = false;
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    hasRole = true;
                    break;
                }
            }

            if (!hasRole) {
                log.warn("[API权限] 用户缺少所需角色: required={}", Arrays.toString(requiredRoles));
                return false;
            }
        }

        log.debug("[API权限] API权限验证通过: module={}, operation={}",
                annotation.module(), annotation.operation());
        return true;
    }

    /**
     * 验证操作级权限
     *
     * @param annotation 操作权限注解
     * @param userPermissions 用户权限列表
     * @param userRoles 用户角色列表
     * @return 是否有权限
     */
    public boolean validateOperationPermission(OperationPermission annotation,
                                              Set<String> userPermissions,
                                              Set<String> userRoles) {
        if (annotation == null) {
            return true;
        }

        // 检查权限代码
        String[] requiredPermissions = annotation.permission();
        if (requiredPermissions != null && requiredPermissions.length > 0) {
            if (userPermissions == null || userPermissions.isEmpty()) {
                log.warn("[操作权限] 用户权限列表为空，拒绝操作");
                return false;
            }

            boolean hasPermission = false;
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                log.warn("[操作权限] 用户缺少所需权限: operation={}, required={}",
                        annotation.value(), Arrays.toString(requiredPermissions));
                return false;
            }
        }

        // 检查角色代码
        String[] requiredRoles = annotation.roles();
        if (requiredRoles != null && requiredRoles.length > 0) {
            if (userRoles == null || userRoles.isEmpty()) {
                log.warn("[操作权限] 用户角色列表为空，拒绝操作");
                return false;
            }

            boolean hasRole = false;
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    hasRole = true;
                    break;
                }
            }

            if (!hasRole) {
                log.warn("[操作权限] 用户缺少所需角色: operation={}, required={}",
                        annotation.value(), Arrays.toString(requiredRoles));
                return false;
            }
        }

        log.debug("[操作权限] 操作权限验证通过: operation={}, resource={}",
                annotation.value(), annotation.resourceType());
        return true;
    }

    /**
     * 验证数据级权限
     *
     * @param annotation 数据权限注解
     * @param userId 当前用户ID
     * @param userDeptId 用户部门ID
     * @param userAreaIds 用户区域ID列表
     * @return 数据权限SQL片段
     */
    public String generateDataPermissionSql(DataPermission annotation,
                                           Long userId,
                                           Long userDeptId,
                                           List<Long> userAreaIds) {
        if (annotation == null || annotation.ignore()) {
            return null;
        }

        DataPermission.DataScopeType scopeType = annotation.value();
        String column = annotation.column();

        StringBuilder sql = new StringBuilder();

        switch (scopeType) {
            case USER_SELF:
                // 仅本人数据
                sql.append(" AND ").append(column).append(" = ").append(userId);
                break;

            case USER_DEPT:
                // 本部门数据
                sql.append(" AND ").append(column).append(" = ").append(userDeptId);
                break;

            case USER_DEPT_AND_CHILD:
                // 本部门及子部门数据
                sql.append(" AND ").append(column).append(" IN (")
                   .append("SELECT dept_id FROM t_common_department ")
                   .append("WHERE FIND_IN_SET(?, ancestors)")
                   .append(")");
                break;

            case USER_AREA:
                // 本区域数据
                if (userAreaIds != null && !userAreaIds.isEmpty()) {
                    sql.append(" AND ").append(column).append(" IN (")
                       .append(String.join(",", Collections.nCopies(userAreaIds.size(), "?")))
                       .append(")");
                }
                break;

            case USER_AREA_AND_CHILD:
                // 本区域及子区域数据
                if (userAreaIds != null && !userAreaIds.isEmpty()) {
                    sql.append(" AND ").append(column).append(" IN (")
                       .append("SELECT area_id FROM t_common_area ")
                       .append("WHERE FIND_IN_SET((SELECT area_id FROM t_user_area WHERE user_id = ? LIMIT 1), ancestors)")
                       .append(")");
                }
                break;

            case USER_ALL:
                // 全部数据（不添加WHERE条件）
                break;

            case CUSTOM:
                // 自定义数据范围（使用SpEL表达式）
                log.warn("[数据权限] 自定义数据范围暂未实现: expression={}", annotation.expression());
                break;

            default:
                log.warn("[数据权限] 未知的数据权限类型: {}", scopeType);
                break;
        }

        String result = sql.toString();
        log.debug("[数据权限] 生成数据权限SQL: type={}, column={}, sql={}",
                scopeType, column, result);

        return result.isEmpty() ? null : result;
    }

    /**
     * 验证字段级权限并脱敏
     *
     * @param field 字段对象
     * @param fieldValue 字段值
     * @param userPermissions 用户权限列表
     * @param userRoles 用户角色列表
     * @return 脱敏后的字段值
     */
    public Object validateAndMaskField(Field field,
                                      Object fieldValue,
                                      Set<String> userPermissions,
                                      Set<String> userRoles) {
        // 获取字段上的@FieldPermission注解
        FieldPermission annotation = field.getAnnotation(FieldPermission.class);
        if (annotation == null) {
            return fieldValue;
        }

        // 检查权限
        boolean hasPermission = checkFieldPermission(annotation, userPermissions, userRoles);

        if (hasPermission) {
            // 有权限，返回原始值
            return fieldValue;
        } else {
            // 无权限，执行脱敏
            Object maskedValue = net.lab1024.sa.common.permission.util.FieldPermissionUtils.maskField(
                    fieldValue,
                    annotation.maskStrategy(),
                    annotation.replaceText(),
                    annotation.partialPercent()
            );

            log.debug("[字段权限] 字段脱敏: field={}, strategy={}, maskedValue={}",
                    field.getName(), annotation.maskStrategy(), maskedValue);

            return maskedValue;
        }
    }

    /**
     * 检查字段权限
     */
    private boolean checkFieldPermission(FieldPermission annotation,
                                        Set<String> userPermissions,
                                        Set<String> userRoles) {
        // 检查权限代码
        String[] requiredPermissions = annotation.value();
        if (requiredPermissions != null && requiredPermissions.length > 0) {
            if (userPermissions != null && !userPermissions.isEmpty()) {
                for (String permission : requiredPermissions) {
                    if (userPermissions.contains(permission)) {
                        return true;
                    }
                }
            }
        }

        // 检查角色代码
        String[] requiredRoles = annotation.roles();
        if (requiredRoles != null && requiredRoles.length > 0) {
            if (userRoles != null && !userRoles.isEmpty()) {
                for (String role : requiredRoles) {
                    if (userRoles.contains(role)) {
                        return true;
                    }
                }
            }
        }

        // 既没有权限要求也没有角色要求，默认允许访问
        if ((requiredPermissions == null || requiredPermissions.length == 0) &&
            (requiredRoles == null || requiredRoles.length == 0)) {
            return true;
        }

        return false;
    }
}
