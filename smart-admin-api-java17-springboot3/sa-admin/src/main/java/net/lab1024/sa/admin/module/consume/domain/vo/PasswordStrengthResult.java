/*
 * 密码强度检查结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 密码强度检查结果
 * 封装密码强度分析和建议信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordStrengthResult {

    /**
     * 是否通过强度检查
     */
    private boolean passed;

    /**
     * 强度等级（WEAK/MEDIUM/STRONG/VERY_STRONG）
     */
    private String strengthLevel;

    /**
     * 强度分数（0-100）
     */
    private Integer score;

    /**
     * 长度分数
     */
    private Integer lengthScore;

    /**
     * 复杂度分数
     */
    private Integer complexityScore;

    /**
     * 唯一性分数
     */
    private Integer uniquenessScore;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 强度描述
     */
    private String description;

    /**
     * 安全建议
     */
    private List<String> suggestions;

    /**
     * 是否包含敏感信息
     */
    private Boolean containsSensitiveInfo;

    /**
     * 是否为常见弱密码
     */
    private Boolean isCommonPassword;

    /**
     * 检查详情
     */
    private List<String> checkDetails;

    /**
     * 创建成功的强度检查结果
     */
    public static PasswordStrengthResult success(String strengthLevel, Integer score) {
        return PasswordStrengthResult.builder()
                .passed(true)
                .strengthLevel(strengthLevel)
                .score(score)
                .message("密码强度检查通过")
                .build();
    }

    /**
     * 创建失败的强度检查结果
     */
    public static PasswordStrengthResult failure(String errorCode, String message) {
        return PasswordStrengthResult.builder()
                .passed(false)
                .strengthLevel("WEAK")
                .score(0)
                .message(message)
                .build();
    }

    /**
     * 获取强度等级描述
     */
    public String getStrengthLevelDescription() {
        if (strengthLevel == null) {
            return "未知";
        }
        switch (strengthLevel) {
            case "VERY_STRONG":
                return "非常强";
            case "STRONG":
                return "强";
            case "MEDIUM":
                return "中等";
            case "WEAK":
                return "弱";
            default:
                return strengthLevel;
        }
    }

    /**
     * 获取安全建议
     */
    public String getSecuritySuggestion() {
        if (passed) {
            if ("VERY_STRONG".equals(strengthLevel)) {
                return "密码强度很好，安全性高";
            } else if ("STRONG".equals(strengthLevel)) {
                return "密码强度良好，建议定期更换";
            } else if ("MEDIUM".equals(strengthLevel)) {
                return "密码强度一般，建议增加复杂度";
            }
        } else {
            if (score < 30) {
                return "密码强度过低，建议重新设置";
            } else if (score < 60) {
                return "密码强度较弱，建议增加长度和复杂度";
            }
        }
        return "请参考详细建议提高密码强度";
    }

    /**
     * 是否需要建议
     */
    public boolean needsSuggestion() {
        return !passed || score < 80;
    }

    /**
     * 是否为强密码
     */
    public boolean isStrongPassword() {
        return passed && ("STRONG".equals(strengthLevel) || "VERY_STRONG".equals(strengthLevel));
    }

    /**
     * 是否需要立即改进
     */
    public boolean needsImmediateImprovement() {
        return !passed || "WEAK".equals(strengthLevel);
    }
}