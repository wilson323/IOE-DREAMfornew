package net.lab1024.sa.base.module.support.datamasking;

import lombok.Getter;

/**
 * 数据脱敏类型枚举
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Getter
public enum DataMaskingTypeEnum {

    /**
     * 默认脱敏
     */
    DEFAULT("默认脱敏", 0),

    /**
     * 中文姓名脱敏
     */
    CHINESE_NAME("中文姓名脱敏", 1),

    /**
     * 手机号脱敏
     */
    PHONE("手机号脱敏", 2),

    /**
     * 邮箱脱敏
     */
    EMAIL("邮箱脱敏", 3),

    /**
     * 身份证号脱敏
     */
    ID_CARD("身份证号脱敏", 4),

    /**
     * 银行卡号脱敏
     */
    BANK_CARD("银行卡号脱敏", 5),

    /**
     * 地址脱敏
     */
    ADDRESS("地址脱敏", 6),

    /**
     * 密码脱敏
     */
    PASSWORD("密码脱敏", 7),

    /**
     * 公司名称脱敏
     */
    COMPANY_NAME("公司名称脱敏", 8),

    /**
     * 客户编号脱敏
     */
    CUSTOMER_NO("客户编号脱敏", 9),

    /**
     * 车牌号脱敏
     */
    LICENSE_PLATE("车牌号脱敏", 10),

    /**
     * 自定义脱敏
     */
    CUSTOM("自定义脱敏", 99);

    private final String desc;
    private final Integer code;

    DataMaskingTypeEnum(String desc, Integer code) {
        this.desc = desc;
        this.code = code;
    }
}