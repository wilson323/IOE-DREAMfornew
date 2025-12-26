package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.*;

/**
 * 数据级权限控制注解
 * <p>
 * 功能：控制用户对数据的访问范围（基于组织架构、区域等）
 * 使用场景：
 * 1. 仅查看本人数据（USER_SELF）
 * 2. 查看本部门数据（USER_DEPT）
 * 3. 查看本部门及子部门数据（USER_DEPT_AND_CHILD）
 * 4. 查看本区域数据（USER_AREA）
 * 5. 查看所有数据（USER_ALL，仅管理员）
 * 6. 自定义数据范围（CUSTOM）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限类型
     */
    DataScopeType value() default DataScopeType.USER_SELF;

    /**
     * 自定义权限表达式
     * <p>当type=CUSTOM时，使用SpEL表达式定义数据范围</p>
     * <p>示例：#userId == @dataPermissionService.getDataOwnerId(#dataId)</p>
     */
    String expression() default "";

    /**
     * 数据权限类型枚举
     */
    enum DataScopeType {
        /**
         * 仅本人数据
         * <p>SQL示例：WHERE create_user_id = #{currentUserId}</p>
         */
        USER_SELF,

        /**
         * 本部门数据
         * <p>SQL示例：WHERE dept_id IN (SELECT dept_id FROM t_user WHERE user_id = #{currentUserId})</p>
         */
        USER_DEPT,

        /**
         * 本部门及子部门数据
         * <p>SQL示例：WHERE dept_id IN (SELECT dept_id FROM t_department WHERE find_in_set(#{deptId}, ancestors))</p>
         */
        USER_DEPT_AND_CHILD,

        /**
         * 本区域数据
         * <p>SQL示例：WHERE area_id IN (SELECT area_id FROM t_user_area WHERE user_id = #{currentUserId})</p>
         */
        USER_AREA,

        /**
         * 本区域及子区域数据
         * <p>SQL示例：WHERE area_id IN (SELECT area_id FROM t_area WHERE find_in_set(#{areaId}, ancestors))</p>
         */
        USER_AREA_AND_CHILD,

        /**
         * 全部数据（仅管理员）
         */
        USER_ALL,

        /**
         * 自定义数据范围
         * <p>使用expression定义的SpEL表达式</p>
         */
        CUSTOM
    }

    /**
     * 数据权限列名
     * <p>用于SQL拼接的数据列名，如：create_user_id, dept_id, area_id</p>
     */
    String column() default "create_user_id";

    /**
     * 是否忽略数据权限
     * <p>true=不应用数据权限过滤，false=应用数据权限过滤</p>
     */
    boolean ignore() default false;

    /**
     * 描述信息
     */
    String description() default "";
}
