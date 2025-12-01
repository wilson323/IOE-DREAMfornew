package net.lab1024.sa.base.module.support.datamasking;

/**
 * 数据脱敏函数接口
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@FunctionalInterface
public interface DataMaskingFunction {

    /**
     * 执行脱敏操作
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    Object apply(Object value);
}