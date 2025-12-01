package {package}.domain.entity;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * {entity_comment}
 * <p>
 * {business_description}
 * 通过扩展表机制支持不同业务模块的特定需求
 *
 * @author {author}
 * @since {create_date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_{table_name}")
public class {EntityName}Entity extends BaseEntity {

    /**
     * {entity_comment}ID（主键）
     */
    @TableId("{entity_id_field}")
    private Long {entityId};

    /**
     * {entity_comment}编码（系统唯一）
     */
    @NotBlank(message = "{entity_comment}编码不能为空")
    @Size(max = 32, message = "{entity_comment}编码长度不能超过32个字符")
    @TableField("{entity_code_field}")
    private String {entityCode};

    /**
     * {entity_comment}名称
     */
    @NotBlank(message = "{entity_comment}名称不能为空")
    @Size(max = 100, message = "{entity_comment}名称长度不能超过100个字符")
    @TableField("{entity_name_field}")
    private String {entityName};

    /**
     * {entity_comment}类型{type_description}
     */
    @NotNull(message = "{entity_comment}类型不能为空")
    @TableField("{entity_type_field}")
    private Integer {entityType};

    // ==================== 层级结构字段（如需要） ====================

    /**
     * 上级ID（0表示根节点）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 层级路径（用逗号分隔的ID链，如：0,1,2,3）
     */
    @TableField("path")
    private String path;

    /**
     * 层级深度（根节点为0）
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序号（同层级排序）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    // ==================== 状态字段 ====================

    /**
     * 状态（0:停用 1:正常 2:维护中）
     */
    @TableField("status")
    private Integer status;

    // ==================== 位置信息字段（如需要） ====================

    /**
     * 经度坐标
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度坐标
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 面积（平方米）
     */
    @TableField("area_size")
    private BigDecimal areaSize;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    // ==================== 描述字段 ====================

    /**
     * {entity_comment}描述
     */
    @Size(max = 500, message = "{entity_comment}描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    // ==================== 非数据库字段（用于业务逻辑展示） ====================

    /**
     * 子节点列表（非数据库字段，用于树形结构展示）
     */
    @TableField(exist = false)
    private List<{EntityName}Entity> children;

    /**
     * 子节点数量（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Integer childrenCount;

    /**
     * 是否有子节点（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Boolean hasChildren;

    /**
     * 父级名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 类型名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String {entityType}Name;

    /**
     * 包含自身和所有子节点的ID列表（非数据库字段，用于权限判断）
     */
    @TableField(exist = false)
    private List<Long> selfAndAllChildrenIds;

    // ==================== 业务方法 ====================

    /**
     * 是否为根节点
     *
     * @return true 如果是根节点
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 是否有子节点
     *
     * @return true 如果有子节点
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 获取完整路径名称（如：园区A/建筑B/楼层1）
     *
     * @return 完整路径名称
     */
    public String getFullPathName() {
        if (isRoot()) {
            return {entityName};
        }

        // 如果有父级信息，则构建完整路径
        if (parentName != null && !parentName.isEmpty()) {
            return parentName + "/" + {entityName};
        }

        return {entityName};
    }

    /**
     * 是否启用
     *
     * @return true 如果启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 是否在维护中
     *
     * @return true 如果在维护中
     */
    public boolean isMaintenance() {
        return Integer.valueOf(2).equals(status);
    }

    /**
     * 获取层级深度
     *
     * @return 层级深度
     */
    public int getDepth() {
        return this.path != null ? this.path.split(",").length : 1;
    }

    /**
     * 获取完整的ID路径（包含自身和所有父级ID）
     *
     * @return ID路径数组
     */
    public Long[] getPathIds() {
        if (path == null || path.isEmpty()) {
            return new Long[]{this.{entityId}};
        }
        String[] ids = path.split(",");
        Long[] result = new Long[ids.length + 1];
        for (int i = 0; i < ids.length; i++) {
            result[i] = Long.parseLong(ids[i]);
        }
        result[ids.length] = this.{entityId};
        return result;
    }

    // ==================== 枚举定义（如需要） ====================

    /**
     * {entity_comment}类型枚举
     */
    public enum {EntityName}Type {{
        TYPE_1(1, "类型1"),
        TYPE_2(2, "类型2"),
        TYPE_3(3, "类型3"),
        TYPE_4(4, "类型4"),
        TYPE_5(5, "类型5"),
        TYPE_6(6, "其他");

        private final Integer code;
        private final String desc;

        {EntityName}Type(Integer code, String desc) {{
            this.code = code;
            this.desc = desc;
        }}

        public Integer getCode() {{
            return code;
        }}

        public String getDesc() {{
            return desc;
        }}

        /**
         * 根据编码获取枚举
         */
        public static {EntityName}Type getByCode(Integer code) {{
            for ({EntityName}Type type : values()) {{
                if (type.getCode().equals(code)) {{
                    return type;
                }}
            }}
            return null;
        }}
    }}

    /**
     * {entity_comment}状态枚举
     */
    public enum {EntityName}Status {{
        DISABLED(0, "停用"),
        ENABLED(1, "正常"),
        MAINTENANCE(2, "维护中");

        private final Integer code;
        private final String desc;

        {EntityName}Status(Integer code, String desc) {{
            this.code = code;
            this.desc = desc;
        }}

        public Integer getCode() {{
            return code;
        }}

        public String getDesc() {{
            return desc;
        }}

        /**
         * 根据编码获取枚举
         */
        public static {EntityName}Status getByCode(Integer code) {{
            for ({EntityName}Status status : values()) {{
                if (status.getCode().equals(code)) {{
                    return status;
                }}
            }}
            return null;
        }}
    }}
}