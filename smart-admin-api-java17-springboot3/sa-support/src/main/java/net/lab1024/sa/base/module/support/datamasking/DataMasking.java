package net.lab1024.sa.base.module.support.datamasking;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataMasking {

    /**
     * 脱敏类型
     */
    DataMaskingTypeEnum value() default DataMaskingTypeEnum.DEFAULT;

    /**
     * 自定义脱敏函数类（当type为CUSTOM时使用）
     */
    Class<?> customFunction() default DataMaskingFunction.class;
}