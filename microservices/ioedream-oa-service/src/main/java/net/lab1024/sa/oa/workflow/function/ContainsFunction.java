package net.lab1024.sa.oa.workflow.function;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;


import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

/**
 * 字符串包含函数
 * <p>
 * 工作流表达式引擎自定义函数，用于检查字符串是否包含指定子字符串
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class ContainsFunction extends AbstractFunction {


    @Override
    public String getName() {
        return "contains";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            // 获取参数
            String str = arg1.getValue(env).toString();
            String substr = arg2.getValue(env).toString();

            boolean contains = str.contains(substr);

            log.debug("[字符串包含检查] 字符串: {}, 子字符串: {}, 结果: {}", str, substr, contains);

            return AviatorBoolean.valueOf(contains);

        } catch (Exception e) {
            log.error("[字符串包含检查] 执行异常: {}", e.getMessage(), e);
            return AviatorBoolean.FALSE;
        }
    }
}
