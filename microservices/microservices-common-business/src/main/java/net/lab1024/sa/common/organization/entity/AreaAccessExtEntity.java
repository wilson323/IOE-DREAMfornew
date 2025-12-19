package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域门禁扩展实体类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数量控制在30个以内
 * - 类行数控制在200行以内（理想标准）
 * - 纯数据模型，无业务逻辑方法
 * </p>
 * <p>
 * 业务场景：
 * - 门禁区域扩展配置
 * - 门禁级别管理
 * - 验证方式配置（设备端验证/后台验证）
 * - 门禁模式管理
 * </p>
 * <p>
 * 数据库表：t_access_area_ext
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
@Schema(description = "区域门禁扩展实体")
public class AreaAccessExtEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 扩展ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列ext_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "ext_id", type = IdType.AUTO)
    @Schema(description = "扩展ID", example = "1001")
    private Long id;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    /**
     * 门禁级别
     * 1-普通级别
     * 2-重要级别
     * 3-核心级别
     */
    @TableField("access_level")
    @Schema(description = "门禁级别", example = "1", allowableValues = {"1", "2", "3"})
    private Integer accessLevel;

    /**
     * 门禁模式
     * 描述门禁的工作模式，如：card_only, face_only, card_and_face等
     */
    @Size(max = 50)
    @TableField("access_mode")
    @Schema(description = "门禁模式", example = "card_and_face")
    private String accessMode;

    /**
     * 验证方式（关键字段）
     * <p>
     * 支持的值：
     * - edge: 设备端验证（设备本地完成识别+验证+控制，支持离线）
     * - backend: 后台验证（设备识别，服务器验证权限逻辑，必须在线）
     * - hybrid: 混合验证（根据场景自动选择）
     * </p>
     * <p>
     * 默认值：edge（设备端验证）
     * </p>
     */
    @Size(max = 20)
    @TableField("verification_mode")
    @Schema(description = "验证方式", example = "edge", allowableValues = {"edge", "backend", "hybrid"})
    private String verificationMode;

    /**
     * 设备数量
     * 该区域关联的门禁设备数量
     */
    @TableField("device_count")
    @Schema(description = "设备数量", example = "5")
    private Integer deviceCount;

    /**
     * 扩展配置（JSON格式）
     * <p>
     * 存储额外的配置信息，如：
     * - 反潜配置
     * - 互锁配置
     * - 多人验证配置
     * - 时间段配置
     * </p>
     */
    @TableField("ext_config")
    @Schema(description = "扩展配置（JSON格式）", example = "{\"antiPassback\":true,\"interlock\":false}")
    private String extConfig;

    // ==================== 验证模式常量 ====================

    /**
     * 验证模式常量类
     */
    public static class VerificationMode {
        /**
         * 设备端验证模式
         * 设备本地完成识别+验证+控制，支持离线运行
         */
        public static final String EDGE = "edge";

        /**
         * 后台验证模式
         * 设备识别用户身份，后台验证权限逻辑（反潜、时间段等），必须在线
         */
        public static final String BACKEND = "backend";

        /**
         * 混合验证模式
         * 根据场景自动选择验证模式
         */
        public static final String HYBRID = "hybrid";
    }

    /**
     * 门禁级别常量类
     */
    public static class AccessLevel {
        /**
         * 普通级别
         */
        public static final int NORMAL = 1;

        /**
         * 重要级别
         */
        public static final int IMPORTANT = 2;

        /**
         * 核心级别
         */
        public static final int CORE = 3;
    }
}
