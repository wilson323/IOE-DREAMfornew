/*
 * 账户安全结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.result;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 账户安全结果
 * 封装账户安全检查的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */




@Schema(description = "账户安全结果")
public class AccountSecurityResult {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "安全等级")
    private String securityLevel;

    @Schema(description = "安全得分(0-100)")
    private Integer securityScore;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "是否存在风险")
    private Boolean hasRisk;

    @Schema(description = "风险详情")
    private List<RiskDetail> riskDetails;

    @Schema(description = "安全建议")
    private List<String> securitySuggestions;

    @Schema(description = "账户状态")
    private String accountStatus;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "异常登录次数")
    private Integer abnormalLoginCount;

    @Schema(description = "密码强度等级")
    private String passwordStrength;

    @Schema(description = "多因子认证状态")
    private Boolean mfaEnabled;

    @Schema(description = "安全设置统计")
    private SecuritySettings securitySettings;

    @Schema(description = "检查项目结果")
    private Map<String, CheckItemResult> checkResults;

    /**
     * 风险详情
     */
    
    
    
    
    public static class RiskDetail {

        @Schema(description = "风险类型")
        private String riskType;

        @Schema(description = "风险级别")
        private String riskLevel;

        @Schema(description = "风险描述")
        private String description;

        @Schema(description = "发现时间")
        private LocalDateTime detectedTime;

        @Schema(description = "处理状态")
        private String status;

        @Schema(description = "建议措施")
        private String suggestion;
    }

    /**
     * 安全设置
     */
    
    
    
    
    public static class SecuritySettings {

        @Schema(description = "密码长度要求")
        private Integer passwordMinLength;

        @Schema(description = "密码复杂度要求")
        private Boolean passwordComplexityRequired;

        @Schema(description = "登录失败锁定阈值")
        private Integer loginFailureThreshold;

        @Schema(description = "会话超时时间(分钟)")
        private Integer sessionTimeoutMinutes;

        @Schema(description = "强制双因子认证")
        private Boolean forceMfa;

        @Schema(description = "异常登录监控")
        private Boolean abnormalLoginMonitor;

        @Schema(description = "密码定期更换")
        private Boolean passwordRegularChange;

        @Schema(description = "密码更换周期(天)")
        private Integer passwordChangeDays;
    }

    /**
     * 检查项目结果
     */
    
    
    
    
    public static class CheckItemResult {

        @Schema(description = "检查项名称")
        private String checkName;

        @Schema(description = "是否通过")
        private Boolean passed;

        @Schema(description = "检查结果")
        private String result;

        @Schema(description = "得分")
        private Integer score;

        @Schema(description = "检查时间")
        private LocalDateTime checkTime;

        @Schema(description = "详细信息")
        private String details;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建安全账户结果
     */
    public static AccountSecurityResult createSecure(Long userId, String username) {
        return AccountSecurityResult.builder()
                .userId(userId)
                .username(username)
                .checkTime(LocalDateTime.now())
                .securityLevel("HIGH")
                .securityScore(95)
                .riskLevel("LOW")
                .hasRisk(false)
                .accountStatus("ACTIVE")
                .passwordStrength("STRONG")
                .mfaEnabled(true)
                .abnormalLoginCount(0)
                .securitySuggestions(List.of("账户安全状态良好", "建议继续保持当前安全设置"))
                .build();
    }

    /**
     * 创建风险账户结果
     */
    public static AccountSecurityResult createRisk(Long userId, String username, List<RiskDetail> risks) {
        return AccountSecurityResult.builder()
                .userId(userId)
                .username(username)
                .checkTime(LocalDateTime.now())
                .securityLevel("LOW")
                .securityScore(35)
                .riskLevel("HIGH")
                .hasRisk(true)
                .accountStatus("SUSPENDED")
                .passwordStrength("WEAK")
                .mfaEnabled(false)
                .abnormalLoginCount(risks != null ? risks.size() : 0)
                .riskDetails(risks)
                .securitySuggestions(List.of("立即修改密码", "启用双因子认证", "检查异常登录记录", "联系管理员"))
                .build();
    }

    /**
     * 创建中等安全账户结果
     */
    public static AccountSecurityResult createMediumSecurity(Long userId, String username) {
        return AccountSecurityResult.builder()
                .userId(userId)
                .username(username)
                .checkTime(LocalDateTime.now())
                .securityLevel("MEDIUM")
                .securityScore(70)
                .riskLevel("MEDIUM")
                .hasRisk(false)
                .accountStatus("ACTIVE")
                .passwordStrength("MEDIUM")
                .mfaEnabled(false)
                .abnormalLoginCount(0)
                .securitySuggestions(List.of("建议启用双因子认证", "考虑增强密码强度", "定期检查账户活动"))
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为安全账户
     */
    public boolean isSecure() {
        return Boolean.TRUE.equals(hasRisk) == false &&
                securityScore != null && securityScore >= 80 &&
                "HIGH".equals(securityLevel);
    }

    /**
     * 判断是否需要立即处理
     */
    public boolean needsImmediateAction() {
        return Boolean.TRUE.equals(hasRisk) &&
                ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel));
    }

    /**
     * 获取安全状态描述
     */

    /**
     * 计算安全等级改进建议
     */

    /**
     * 添加风险详情
     */
    public void addRiskDetail(String riskType, String riskLevel, String description) {
        RiskDetail risk = RiskDetail.builder()
                .riskType(riskType)
                .riskLevel(riskLevel)
                .description(description)
                .detectedTime(LocalDateTime.now())
                .status("PENDING")
                .build();

        if (this.riskDetails == null) {
            this.riskDetails = List.of(risk);
        } else {
            this.riskDetails.add(risk);
        }
    }

    /**
     * 更新安全得分
     */
    public void updateSecurityScore() {
        int baseScore = 100;

        // 根据风险项扣分
        if (riskDetails != null) {
            for (RiskDetail risk : riskDetails) {
                switch (risk.getRiskLevel()) {
                    case "CRITICAL":
                        baseScore -= 30;
                        break;
                    case "HIGH":
                        baseScore -= 20;
                        break;
                    case "MEDIUM":
                        baseScore -= 10;
                        break;
                    case "LOW":
                        baseScore -= 5;
                        break;
                }
            }
        }

        // 根据安全设置加分
        if (Boolean.TRUE.equals(mfaEnabled)) {
            baseScore += 10;
        }
        if ("STRONG".equals(passwordStrength)) {
            baseScore += 10;
        }

        this.securityScore = Math.max(0, Math.min(100, baseScore));

        // 更新安全等级
        if (this.securityScore >= 90) {
            this.securityLevel = "HIGH";
        } else if (this.securityScore >= 70) {
            this.securityLevel = "MEDIUM";
        } else {
            this.securityLevel = "LOW";
        }
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)












    public Boolean isHasRisk() {
        return hasRisk;
    }


















    public Boolean isMfaEnabled() {
        return mfaEnabled;
    }

    // ==================== 简单工厂方法 ====================

    /**
     * 创建成功结果
     */
    public static AccountSecurityResult success(String message) {
        AccountSecurityResult result = new AccountSecurityResult();
        result.securityLevel = "HIGH";
        result.securityScore = 100;
        result.riskLevel = "LOW";
        result.hasRisk = false;
        result.accountStatus = "SUCCESS";
        result.checkTime = LocalDateTime.now();
        // 添加message字段支持（如果没有的话需要扩展）
        return result;
    }

    /**
     * 创建失败结果
     */
    public static AccountSecurityResult failure(String message) {
        AccountSecurityResult result = new AccountSecurityResult();
        result.securityLevel = "LOW";
        result.securityScore = 0;
        result.riskLevel = "HIGH";
        result.hasRisk = true;
        result.accountStatus = "FAILED";
        result.checkTime = LocalDateTime.now();
        // 添加message字段支持（如果没有的话需要扩展）
        return result;
    }

    /**
     * 判断是否成功（兼容内部类的isSuccess方法）
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(accountStatus) && Boolean.TRUE.equals(hasRisk) == false;
    }

    /**
     * 获取消息（兼容内部类的getMessage方法）
     * 这里返回账户状态作为消息
     */
    public String getMessage() {
        return accountStatus != null ? accountStatus : "操作完成";
    }






}
