package net.lab1024.sa.common.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT Token解析器持有者
 *
 * <p>用于在静态工具类中访问Spring管理的JwtTokenParser Bean</p>
 *
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Component
@Slf4j
public class JwtTokenParserHolder {

    private static JwtTokenParser jwtTokenParser;

    /**
     * 构造函数注入JwtTokenParser
     *
     * @param parser JWT Token解析器
     */
    public JwtTokenParserHolder(JwtTokenParser parser) {
        JwtTokenParserHolder.jwtTokenParser = parser;
        log.info("[JWT解析器] JwtTokenParser已初始化");
    }

    /**
     * 获取JwtTokenParser实例
     *
     * @return JwtTokenParser实例
     */
    public static JwtTokenParser getParser() {
        if (jwtTokenParser == null) {
            log.warn("[JWT解析器] JwtTokenParser未初始化，请确保Spring容器已启动");
            return null;
        }
        return jwtTokenParser;
    }

    /**
     * 检查JwtTokenParser是否已初始化
     *
     * @return true-已初始化，false-未初始化
     */
    public static boolean isInitialized() {
        return jwtTokenParser != null;
    }
}
