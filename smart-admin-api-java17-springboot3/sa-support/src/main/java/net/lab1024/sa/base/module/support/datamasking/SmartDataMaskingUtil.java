package net.lab1024.sa.base.module.support.datamasking;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏工具类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
public class SmartDataMaskingUtil {

    /**
     * 默认脱敏（保留前3位和后4位）
     */
    private static final int DEFAULT_PREFIX_LENGTH = 3;
    private static final int DEFAULT_SUFFIX_LENGTH = 4;
    private static final String DEFAULT_MASK_CHAR = "*";

    /**
     * 数据脱敏处理
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    public static String dataMasking(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        return dataMasking(value, DataMaskingTypeEnum.DEFAULT);
    }

    /**
     * 根据类型进行数据脱敏
     *
     * @param value 原始值
     * @param typeEnum 脱敏类型
     * @return 脱敏后的值
     */
    public static String dataMasking(Object value, DataMaskingTypeEnum typeEnum) {
        if (value == null) {
            return null;
        }

        String strValue = String.valueOf(value);
        if (StringUtils.isBlank(strValue)) {
            return strValue;
        }

        switch (typeEnum) {
            case CHINESE_NAME:
                return maskChineseName(strValue);
            case ID_CARD:
                return maskIdCard(strValue);
            case PHONE:
                return maskPhone(strValue);
            case EMAIL:
                return maskEmail(strValue);
            case BANK_CARD:
                return maskBankCard(strValue);
            case PASSWORD:
                return maskPassword(strValue);
            case ADDRESS:
                return maskAddress(strValue);
            case COMPANY_NAME:
                return maskCompanyName(strValue);
            case CUSTOMER_NO:
                return maskCustomerNo(strValue);
            case LICENSE_PLATE:
                return maskLicensePlate(strValue);
            case CUSTOM:
                return defaultMask(strValue);
            case DEFAULT:
            default:
                return defaultMask(strValue);
        }
    }

    /**
     * 默认脱敏
     */
    private static String defaultMask(String value) {
        int length = value.length();
        if (length <= DEFAULT_PREFIX_LENGTH + DEFAULT_SUFFIX_LENGTH) {
            return maskAll(value);
        }
        return value.substring(0, DEFAULT_PREFIX_LENGTH) +
                StringUtils.repeat(DEFAULT_MASK_CHAR, length - DEFAULT_PREFIX_LENGTH - DEFAULT_SUFFIX_LENGTH) +
                value.substring(length - DEFAULT_SUFFIX_LENGTH);
    }

    /**
     * 中文姓名脱敏（保留姓氏）
     */
    private static String maskChineseName(String name) {
        if (name.length() <= 1) {
            return maskAll(name);
        }
        return name.charAt(0) + StringUtils.repeat(DEFAULT_MASK_CHAR, name.length() - 1);
    }

    /**
     * 身份证脱敏（保留前6位和后4位）
     */
    private static String maskIdCard(String idCard) {
        if (idCard.length() != 18 && idCard.length() != 15) {
            return maskAll(idCard);
        }
        int prefixLength = idCard.length() == 18 ? 6 : 4;
        return idCard.substring(0, prefixLength) +
                StringUtils.repeat(DEFAULT_MASK_CHAR, idCard.length() - prefixLength - 4) +
                idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏（保留前3位和后4位）
     */
    private static String maskPhone(String phone) {
        if (phone.length() != 11) {
            return maskAll(phone);
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏（保留前2位和@domain部分）
     */
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return maskAll(email);
        }
        return email.substring(0, 2) +
                StringUtils.repeat(DEFAULT_MASK_CHAR, atIndex - 2) +
                email.substring(atIndex);
    }

    /**
     * 银行卡脱敏（保留前6位和后4位）
     */
    private static String maskBankCard(String bankCard) {
        if (bankCard.length() < 10) {
            return maskAll(bankCard);
        }
        return bankCard.substring(0, 6) +
                StringUtils.repeat(DEFAULT_MASK_CHAR, bankCard.length() - 10) +
                bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 密码脱敏（全部掩码）
     */
    private static String maskPassword(String password) {
        return StringUtils.repeat(DEFAULT_MASK_CHAR, 8);
    }

    /**
     * 地址脱敏（保留详细地址到街道级）
     */
    private static String maskAddress(String address) {
        if (address.length() <= 8) {
            return maskAll(address);
        }
        return address.substring(0, 8) + "****";
    }

    /**
     * 公司名称脱敏（保留前2位和后2位）
     */
    private static String maskCompanyName(String companyName) {
        if (companyName.length() <= 4) {
            return maskAll(companyName);
        }
        return companyName.substring(0, 2) +
                StringUtils.repeat(DEFAULT_MASK_CHAR, companyName.length() - 4) +
                companyName.substring(companyName.length() - 2);
    }

    /**
     * 客户编号脱敏（保留前4位）
     */
    private static String maskCustomerNo(String customerNo) {
        if (customerNo.length() <= 4) {
            return maskAll(customerNo);
        }
        return customerNo.substring(0, 4) + "****";
    }

    /**
     * 车牌号脱敏（保留省份简称和数字）
     */
    private static String maskLicensePlate(String licensePlate) {
        if (licensePlate.length() <= 4) {
            return maskAll(licensePlate);
        }
        return licensePlate.substring(0, 2) + "****" + licensePlate.substring(licensePlate.length() - 2);
    }

    /**
     * 全部掩码
     */
    private static String maskAll(String value) {
        return StringUtils.repeat(DEFAULT_MASK_CHAR, value.length());
    }
}