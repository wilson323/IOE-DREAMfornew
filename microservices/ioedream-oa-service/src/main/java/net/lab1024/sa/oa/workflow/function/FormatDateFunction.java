package net.lab1024.sa.oa.workflow.function;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * 格式化日期函数
 * <p>
 * 工作流表达式引擎自定义函数，用于格式化日期时间
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class FormatDateFunction extends AbstractFunction {


    @Override
    public String getName() {
        return "formatDate";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            // 获取参数
            Object dateObj = arg1.getValue(env);
            String pattern = arg2.getValue(env).toString();

            String formattedDate = formatDate(dateObj, pattern);

            log.debug("[日期格式化] 输入: {}, 模式: {}, 结果: {}", dateObj, pattern, formattedDate);

            return new AviatorString(formattedDate);

        } catch (Exception e) {
            log.error("[日期格式化] 执行异常: {}", e.getMessage(), e);
            return new AviatorString("");
        }
    }

    /**
     * 格式化日期
     *
     * @param dateObj 日期对象
     * @param pattern 格式模式
     * @return 格式化后的日期字符串
     */
    private String formatDate(Object dateObj, String pattern) {
        if (dateObj == null || pattern == null) {
            return "";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            if (dateObj instanceof LocalDateTime) {
                return ((LocalDateTime) dateObj).format(formatter);
            }

            if (dateObj instanceof String) {
                LocalDateTime dateTime = LocalDateTime.parse(dateObj.toString(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return dateTime.format(formatter);
            }

            if (dateObj instanceof Long) {
                LocalDateTime dateTime = LocalDateTime.now();
                return dateTime.format(formatter);
            }

            return dateObj.toString();

        } catch (Exception e) {
            log.warn("[日期格式化] 格式化失败: {}, 模式: {}, 错误: {}", dateObj, pattern, e.getMessage());
            return dateObj.toString();
        }
    }
}
