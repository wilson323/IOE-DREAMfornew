package net.lab1024.sa.common.permission.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.annotation.FieldPermission;
import org.springframework.stereotype.Component;

/**
 * 字段权限工具类
 * <p>
 * 功能：处理字段级权限控制的脱敏操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class FieldPermissionUtils {

    /**
     * 对字段值进行脱敏处理
     *
     * @param fieldValue 原始字段值
     * @param strategy   脱敏策略
     * @param replaceText 替换文本
     * @param partialPercent 部分显示比例
     * @return 脱敏后的值
     */
    public static Object maskField(Object fieldValue, FieldPermission.MaskStrategy strategy,
                                   String replaceText, int partialPercent) {
        if (fieldValue == null) {
            return null;
        }

        String valueStr = fieldValue.toString();

        switch (strategy) {
            case HIDDEN:
                return null;

            case PHONE:
                return maskPhone(valueStr);

            case EMAIL:
                return maskEmail(valueStr);

            case ID_CARD:
                return maskIdCard(valueStr);

            case BANK_CARD:
                return maskBankCard(valueStr);

            case ADDRESS:
                return maskAddress(valueStr);

            case NAME:
                return maskName(valueStr);

            case PARTIAL:
                return maskPartial(valueStr, partialPercent);

            case REPLACE:
                return replaceText != null && !replaceText.isEmpty() ? replaceText : "***";

            default:
                return valueStr;
        }
    }

    /**
     * 手机号脱敏 - 显示前3后4位
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏 - 显示前2后@域名
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return "**@" + domain;
        }
        return username.substring(0, 2) + "****@" + domain;
    }

    /**
     * 身份证号脱敏 - 显示前6后4位
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return "***";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡号脱敏 - 显示前4后4位
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return "***";
        }
        return bankCard.substring(0, 4) + "********" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 地址脱敏 - 显示前6个字符
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address != null ? address : "***";
        }
        return address.substring(0, 6) + "****";
    }

    /**
     * 姓名脱敏 - 显示姓氏
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        if (name.length() == 1) {
            return "*";
        }
        return name.substring(0, 1) + "**";
    }

    /**
     * 部分脱敏 - 显示前N%
     */
    public static String maskPartial(String value, int percent) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 限制百分比范围
        int safePercent = Math.max(0, Math.min(100, percent));
        int showLength = value.length() * safePercent / 100;

        if (showLength <= 0) {
            return "***";
        }

        if (showLength >= value.length()) {
            return value;
        }

        return value.substring(0, showLength) + "****";
    }

    /**
     * 检查用户是否有权限访问字段
     *
     * @param userPermissions 用户权限列表
     * @param requiredPermissions 所需权限
     * @return 是否有权限
     */
    public static boolean hasFieldPermission(java.util.Set<String> userPermissions, String[] requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }

        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        for (String permission : requiredPermissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查用户是否有角色访问字段
     *
     * @param userRoles 用户角色列表
     * @param requiredRoles 所需角色
     * @return 是否有角色
     */
    public static boolean hasFieldRole(java.util.Set<String> userRoles, String[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true;
        }

        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        for (String role : requiredRoles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }

        return false;
    }
}
