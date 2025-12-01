package net.lab1024.sa.base.common.validator.enumeration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 枚举校验注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface CheckEnum {

    /**
     * 错误提示信息
     */
    String message();

    /**
     * 枚举类（需实现 BaseEnum）
     */
    Class<? extends BaseEnum> value();

    /**
     * 是否必填
     */
    boolean required() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


