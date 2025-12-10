package net.lab1024.sa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.util.AESUtil;

/**
 * 工具类配置
 * <p>
 * 符合CLAUDE.md规范 - 将纯Java工具类注册为Spring Bean
 * </p>
 * <p>
 * 包含的工具类：
 * - AESUtil: AES加密解密工具
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class UtilConfig {

    /**
     * AES加密工具Bean
     * <p>
     * 企业级特性：
     * - AES-128-ECB加密算法
     * - Base64编码输出
     * - 支持从环境变量获取密钥
     * - 生产环境必须配置CONFIG_ENCRYPT_KEY
     * </p>
     *
     * @return AES工具实例
     */
    @Bean
    public AESUtil aesUtil() {
        log.info("[AESUtil] 初始化企业级AES加密工具");
        AESUtil aesUtil = new AESUtil();
        log.info("[AESUtil] 企业级AES加密工具初始化完成");
        log.info("[AESUtil] 功能支持: AES-128-ECB加密、Base64编码、环境变量密钥配置");
        log.info("[AESUtil] 注意: 生产环境请配置CONFIG_ENCRYPT_KEY环境变量");
        return aesUtil;
    }
}
