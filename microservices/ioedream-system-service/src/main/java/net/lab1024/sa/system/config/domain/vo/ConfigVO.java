package net.lab1024.sa.system.config.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置VO
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
public class ConfigVO {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置键名
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 配置分组名称
     */
    private String configGroupName;

    /**
     * 配置类型(STRING-字符,NUMBER-数字,BOOLEAN-布尔,JSON-JSON对象)
     */
    private String configType;

    /**
     * 配置类型名称
     */
    private String configTypeName;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否系统内置(0-否，1-是)
     */
    private Integer isSystem;

    /**
     * 是否系统内置名称
     */
    private String isSystemName;

    /**
     * 是否加密(0-否，1-是)
     */
    private Integer isEncrypt;

    /**
     * 是否加密名称
     */
    private String isEncryptName;

    /**
     * 是否只读(0-否，1-是)
     */
    private Integer isReadonly;

    /**
     * 是否只读名称
     */
    private String isReadonlyName;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 验证规则
     */
    private String validationRule;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (this.status == null) {
            return null;
        }
        return this.status == 1 ? "启用" : "禁用";
    }

    /**
     * 获取是否系统内置名称
     */
    public String getIsSystemName() {
        if (this.isSystem == null) {
            return null;
        }
        return this.isSystem == 1 ? "系统配置" : "自定义配置";
    }

    /**
     * 获取是否加密名称
     */
    public String getIsEncryptName() {
        if (this.isEncrypt == null) {
            return null;
        }
        return this.isEncrypt == 1 ? "加密" : "明文";
    }

    /**
     * 获取是否只读名称
     */
    public String getIsReadonlyName() {
        if (this.isReadonly == null) {
            return null;
        }
        return this.isReadonly == 1 ? "只读" : "可编辑";
    }

    /**
     * 获取配置类型名称
     */
    public String getConfigTypeName() {
        if (this.configType == null) {
            return null;
        }
        switch (this.configType) {
            case "STRING":
                return "字符";
            case "NUMBER":
                return "数字";
            case "BOOLEAN":
                return "布尔";
            case "JSON":
                return "JSON对象";
            default:
                return this.configType;
        }
    }

    /**
     * 获取配置分组名称
     */
    public String getConfigGroupName() {
        if (this.configGroup == null) {
            return null;
        }
        switch (this.configGroup) {
            case "SYSTEM":
                return "系统配置";
            case "BUSINESS":
                return "业务配置";
            case "SECURITY":
                return "安全配置";
            case "NOTIFICATION":
                return "通知配置";
            case "CACHE":
                return "缓存配置";
            case "DATABASE":
                return "数据库配置";
            default:
                return this.configGroup;
        }
    }
}