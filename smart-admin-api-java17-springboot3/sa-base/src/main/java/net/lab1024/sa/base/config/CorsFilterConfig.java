package net.lab1024.sa.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2021/11/15 20:38
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
@Configuration
public class CorsFilterConfig {

    @Value("${access-control-allow-origin}")
    private String accessControlAllowOrigin;
    
    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter () {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许凭证（cookies、authorization headers等）
        config.setAllowCredentials(true);

        // 设置访问源地址 - 支持localhost和127.0.0.1
        config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedOrigin("http://127.0.0.1:8081");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:3000");

        // 如果配置了通配符，也添加
        if ("*".equals(accessControlAllowOrigin)) {
            config.addAllowedOrigin("*");
        } else {
            try {
                config.addAllowedOriginPattern(accessControlAllowOrigin);
            } catch (Exception e) {
                // 如果模式解析失败，使用通配符
                config.addAllowedOrigin("*");
            }
        }

        // 设置访问源请求头
        config.addAllowedHeader("*");

        // 设置访问源请求方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");

        // 预检请求缓存时间
        config.setMaxAge(3600L);

        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}