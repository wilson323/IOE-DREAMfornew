package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 区域设备关联实体类
 * <p>
 * 核心概念：区域与设备的双向关联，串联各个业务场景
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 门禁区域设备关联（门禁机、读卡器）
 * - 考勤区域设备关联（考勤机、指纹机）
 * - 消费区域设备关联（POS机、消费机）
 * - 视频区域设备关联（摄像头、录像机）
 * - 访客区域设备关联（访客机、登记终端）
 * </p>
 * <p>
 * 设计原则：
 * - 一个设备可以属于多个区域（支持跨区域服务）
 * - 一个区域可以有多个设备（设备集群）
 * - 设备在区域中有特定的业务属性
 * - 支持设备在区域中的动态部署和移除
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area_device_relation")
@Schema(description = "区域设备关联实体")
public class AreaDeviceEntity extends BaseEntity {

    /**
     * 关联ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列relation_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * 注意：此实体类主键为String类型，使用雪花算法生成
     * </p>
     */
    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    @Schema(description = "关联ID", example = "1001")
    private String id;

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    @TableField("area_id")
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @TableField("device_code")
    @Schema(description = "设备编码", example = "CAMERA_001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @TableField("device_name")
    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    /**
     * 设备类型
     * <p>
     * 1-门禁设备（门禁机、读卡器）
     * 2-考勤设备（考勤机、指纹机）
     * 3-消费设备（POS机、消费机）
     * 4-视频设备（摄像头、录像机）
     * 5-访客设备（访客机、登记终端）
     * 6-报警设备（报警器、传感器）
     * 7-显示设备（LED屏、信息屏）
     * 8-网络设备（交换机、路由器）
     * </p>
     */
    @NotNull(message = "设备类型不能为空")
    @TableField("device_type")
    @Schema(description = "设备类型", example = "4")
    private Integer deviceType;

    /**
     * 设备子类型
     * <p>
     * 门禁设备：11-门禁控制器 12-读卡器 13-生物识别设备
     * 考勤设备：21-考勤机 22-指纹机 23-人脸识别设备
     * 消费设备：31-POS机 32-消费机 33-充值机
     * 视频设备：41-网络摄像头 42-模拟摄像头 43-录像机
     * </p>
     */
    @TableField("device_sub_type")
    @Schema(description = "设备子类型", example = "41")
    private Integer deviceSubType;

    /**
     * 业务模块标识
     * <p>
     * access-门禁管理
     * attendance-考勤管理
     * consume-消费管理
     * visitor-访客管理
     * video-视频监控
     * oa-办公协同
     * device_comm-设备通讯
     * </p>
     */
    @NotBlank(message = "业务模块不能为空")
    @TableField("business_module")
    @Schema(description = "业务模块", example = "access")
    private String businessModule;

    /**
     * 设备在区域中的位置描述
     */
    @Size(max = 200, message = "位置描述长度不能超过200个字符")
    @TableField("location_desc")
    @Schema(description = "位置描述", example = "主入口左侧")
    private String locationDesc;

    /**
     * 设备在区域中的安装位置
     * <p>
     * JSON格式：{"floor": "1F", "building": "A栋", "room": "101", "coordinates": {"x": 100, "y": 200}}
     * </p>
     */
    @TableField("install_location")
    @Schema(description = "安装位置", example = "{\"floor\": \"1F\", \"x\": 100, \"y\": 200}")
    private String installLocation;

    /**
     * 设备在区域中的业务属性
     * <p>
     * JSON格式：{"priority": 1, "role": "main", "config": {...}}
     * </p>
     */
    @TableField("business_attributes")
    @Schema(description = "业务属性", example = "{\"priority\": 1, \"role\": \"main\"}")
    private String businessAttributes;

    /**
     * 设备关联状态
     * <p>
     * 1-正常（设备正常工作）
     * 2-维护（设备维护中）
     * 3-故障（设备故障）
     * 4-离线（设备离线）
     * 5-停用（设备已停用）
     * </p>
     */
    @NotNull(message = "关联状态不能为空")
    @TableField("relation_status")
    @Schema(description = "关联状态", example = "1")
    private Integer relationStatus;

    /**
     * 设备在区域中的优先级
     * <p>
     * 1-主设备（主要业务设备）
     * 2-辅助设备（辅助业务设备）
     * 3-备用设备（备用业务设备）
     * 9-测试设备（测试用途）
     * </p>
     */
    @TableField("priority")
    @Schema(description = "优先级", example = "1")
    private Integer priority;

    /**
     * 是否启用
     */
    @TableField("enabled")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-12-08T10:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    @Schema(description = "失效时间", example = "2026-12-08T10:00:00")
    private LocalDateTime expireTime;

    /**
     * 用户同步状态
     * <p>
     * 0-未同步
     * 1-同步中
     * 2-同步成功
     * 3-同步失败
     * </p>
     */
    @TableField("user_sync_status")
    @Schema(description = "用户同步状态", example = "0")
    private Integer userSyncStatus;

    /**
     * 最后用户同步时间
     */
    @TableField("last_user_sync_time")
    @Schema(description = "最后用户同步时间", example = "2025-12-08T10:00:00")
    private LocalDateTime lastUserSyncTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    @Schema(description = "备注", example = "主要出入口监控设备")
    private String remark;

    /**
     * 获取关联ID（兼容getRelationId()调用）
     */
    public String getRelationId() {
        return this.id;
    }

    /**
     * 设置关联ID（兼容setRelationId()调用）
     */
    public void setRelationId(String relationId) {
        this.id = relationId;
    }
}
