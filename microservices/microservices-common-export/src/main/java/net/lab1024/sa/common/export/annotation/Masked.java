package net.lab1024.sa.common.export.annotation;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 * <p>
 * 用于标记Excel导出时需要脱敏的字段
 * </p>
 * <p>
 * 使用方式：在Excel模型的字段上添加此注解
 * </p>
 * <pre>
 * &#64;Data
 * public class UserExportVO {
 *
 *     &#64;ExcelProperty("用户ID")
 *     private Long userId;
 *
 *     &#64;ExcelProperty("手机号")
 *     &#64;Masked(MaskType.PHONE)  // 导出时自动脱敏
 *     private String phone;
 *
 *     &#64;ExcelProperty("身份证号")
 *     &#64;Masked(MaskType.ID_CARD)  // 导出时自动脱敏
 *     private String idCard;
 * }
 * </pre>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Masked {

    /**
     * 脱敏类型
     */
    MaskType value() default MaskType.PHONE;

    /**
     * 脱敏类型枚举
     */
    enum MaskType {
        /** 手机号 */
        PHONE,
        /** 固定电话 */
        FIXED_PHONE,
        /** 身份证号 */
        ID_CARD,
        /** 银行卡号 */
        BANK_CARD,
        /** 邮箱 */
        EMAIL,
        /** 中文姓名 */
        CHINESE_NAME,
        /** 车牌号 */
        LICENSE_PLATE,
        /** IP地址 */
        IP_ADDRESS,
        /** 地址 */
        ADDRESS,
        /** 密码 */
        PASSWORD
    }
}
