package net.lab1024.sa.base.module.support.operationlog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志娉ㄨВ
 * 
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 操作鎻忚堪
     */
    String operationDesc() default "";
}
