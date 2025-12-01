package net.lab1024.sa.base.module.support.apiencrypt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API解密注解
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDecrypt {

    /**
     * 是否需要解密
     *
     * @return true/false
     */
    boolean value() default true;

    /**
     * 解密算法类型
     *
     * @return 算法类型
     */
    String algorithm() default "AES";
}