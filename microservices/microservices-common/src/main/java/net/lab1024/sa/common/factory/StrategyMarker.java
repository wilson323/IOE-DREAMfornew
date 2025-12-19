package net.lab1024.sa.common.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 策略标记注解
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 用于标记策略实现类，StrategyFactory会自动识别并加载
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：StrategyFactory依赖此注解，需要一起迁移
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrategyMarker {

    /**
     * 策略名称
     * <p>
     * 用于标识策略的唯一名称，通常使用枚举值或字符串标识
     * </p>
     *
     * @return 策略名称
     */
    String name() default "";

    /**
     * 策略类型
     * <p>
     * 用于标识策略的类型，便于分类管理
     * </p>
     *
     * @return 策略类型
     */
    String type() default "";

    /**
     * 策略优先级
     * <p>
     * 数值越大优先级越高，默认100
     * 当多个策略同时匹配时，优先使用优先级高的策略
     * </p>
     *
     * @return 策略优先级
     */
    int priority() default 100;
}

