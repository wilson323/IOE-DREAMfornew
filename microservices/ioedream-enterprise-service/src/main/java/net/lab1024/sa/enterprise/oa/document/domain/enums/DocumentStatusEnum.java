package net.lab1024.sa.enterprise.oa.document.domain.enums;

import lombok.Getter;
import net.lab1024.sa.common.enumeration.BaseEnum;

/**
 * 文档状态枚举
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Getter
public enum DocumentStatusEnum implements BaseEnum {

    DRAFT("DRAFT", "草稿", "文档编辑中，未发布"),
    PUBLISHED("PUBLISHED", "已发布", "文档已发布，可正常访问"),
    ARCHIVED("ARCHIVED", "已归档", "文档已归档，只读访问"),
    LOCKED("LOCKED", "已锁定", "文档被锁定，无法编辑"),
    DELETED("DELETED", "已删除", "文档已删除，逻辑删除状态");

    private final String value;
    private final String description;
    private final String remark;

    DocumentStatusEnum(String value, String description, String remark) {
        this.value = value;
        this.description = description;
        this.remark = remark;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举
     */
    public static DocumentStatusEnum getByCode(String code) {
        for (DocumentStatusEnum status : values()) {
            if (status.value.equals(code)) {
                return status;
            }
        }
        return DRAFT;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return description;
    }

}
