package net.lab1024.sa.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API加密注解
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {

    /**
     * 是否加密响应结果
     *
     * @return true/false
     */
    boolean encryptResponse() default true;

    /**
     * 是否解密请求参数
     *
     * @return true/false
     */
    boolean decryptRequest() default true;

    /**
     * 加密算法类型
     *
     * @return 算法类型
     */
    String algorithm() default "AES";

    /**
     * 是否跳过加密
     *
     * @return true/false
     */
    boolean skip() default false;
}