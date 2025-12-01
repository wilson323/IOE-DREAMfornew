package net.lab1024.sa.base.module.support.datamasking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏占位注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DataMasking {
    DataMaskingTypeEnum value() default DataMaskingTypeEnum.DEFAULT;
}
