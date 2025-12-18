package net.lab1024.sa.oa.workflow.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * 判断为空函数
 * <p>
 * 工作流表达式引擎自定义函数，用于检查对象是否为空
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class IsEmptyFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "isEmpty";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            Object value = arg1.getValue(env);
            boolean isEmpty = isEmpty(value);

            log.debug("[空值检查] 对象: {}, 结果: {}", value, isEmpty);

            return AviatorBoolean.valueOf(isEmpty);

        } catch (Exception e) {
            log.error("[空值检查] 执行异常: {}", e.getMessage(), e);
            return AviatorBoolean.TRUE;
        }
    }

    /**
     * 判断对象是否为空
     *
     * @param value 要检查的对象
     * @return 是否为空
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }

        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }

        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }

        if (value.getClass().isArray()) {
            return ((Object[]) value).length == 0;
        }

        return false;
    }
}



