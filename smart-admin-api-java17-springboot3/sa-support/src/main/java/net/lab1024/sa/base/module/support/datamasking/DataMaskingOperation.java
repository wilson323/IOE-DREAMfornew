package net.lab1024.sa.base.module.support.datamasking;

import lombok.Getter;

/**
 * 数据脱敏操作
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Getter
public class DataMaskingOperation {

    /**
     * 脱敏类型
     */
    private DataMaskingTypeEnum type;

    /**
     * 脱敏函数
     */
    private DataMaskingFunction function;

    public DataMaskingOperation(DataMaskingTypeEnum type, DataMaskingFunction function) {
        this.type = type;
        this.function = function;
    }

    /**
     * 执行脱敏
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    public Object mask(Object value) {
        if (value == null || function == null) {
            return value;
        }
        return function.apply(value);
    }
}