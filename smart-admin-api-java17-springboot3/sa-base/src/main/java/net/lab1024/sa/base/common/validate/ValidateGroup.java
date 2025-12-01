package net.lab1024.sa.base.common.validate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证组
 * 严格遵循repowiki规范：定义分组验证的标识符
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "验证组")
public class ValidateGroup {

    /**
     * 创建验证组
     */
    public static interface Create {}

    /**
     * 更新验证组
     */
    public static interface Update {}

    /**
     * 删除验证组
     */
    public static interface Delete {}

    /**
     * 查询验证组
     */
    public static interface Query {}

    /**
     * 业务验证组
     */
    public static interface Business {}

    /**
     * 系统验证组
     */
    public static interface System {}

    /**
     * 用户相关验证组
     */
    public static interface User {}

    /**
     * 设备相关验证组
     */
    public static interface Device {}

    /**
     * 消费相关验证组
     */
    public static interface Consume {}

    /**
     * 财务相关验证组
     */
    public static interface Finance {}
}