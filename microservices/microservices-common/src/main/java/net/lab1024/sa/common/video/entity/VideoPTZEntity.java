package net.lab1024.sa.common.video.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 视频云台控制实体类
 * <p>
 * PTZ云台控制管理实体，支持上下左右移动、变倍、预置位等控制功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_ptz")
public class VideoPTZEntity extends BaseEntity {

    /**
     * PTZ控制ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long ptzId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 通道号
     */
    @TableField("channel_no")
    private Integer channelNo;

    /**
     * 控制用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 控制类型：1-云台移动 2-变倍控制 3-预置位 4-巡航 5-扫描 6-守望
     */
    @TableField("control_type")
    private Integer controlType;

    /**
     * 控制命令：UP-上移 DOWN-下移 LEFT-左移 RIGHT-右移
     * ZOOM_IN-变倍放大 ZOOM_OUT-变倍缩小
     * FOCUS_NEAR-聚焦远 FOCUS_FAR-聚焦近
     * IRIS_OPEN-光圈开 IRIS_CLOSE-光圈关
     */
    @TableField("control_command")
    private String controlCommand;

    /**
     * 水平角度（0-360度）
     */
    @TableField("pan_angle")
    private Double panAngle;

    /**
     * 垂直角度（-90到90度）
     */
    @TableField("tilt_angle")
    private Double tiltAngle;

    /**
     * 变倍倍数（1-32倍）
     */
    @TableField("zoom_ratio")
    private Double zoomRatio;

    /**
     * 聚焦值（0-1000）
     */
    @TableField("focus_value")
    private Integer focusValue;

    /**
     * 光圈值（0-100）
     */
    @TableField("iris_value")
    private Integer irisValue;

    /**
     * 移动速度：1-很慢 2-慢 3-正常 4-快 5-很快
     */
    @TableField("move_speed")
    private Integer moveSpeed;

    /**
     * 预置位编号
     */
    @TableField("preset_number")
    private Integer presetNumber;

    /**
     * 预置位名称
     */
    @TableField("preset_name")
    private String presetName;

    /**
     * 巡航路径ID
     */
    @TableField("cruise_path_id")
    private String cruisePathId;

    /**
     * 巡航路径名称
     */
    @TableField("cruise_path_name")
    private String cruisePathName;

    /**
     * 扫描模式：1-水平扫描 2-垂直扫描 3-矩形扫描
     */
    @TableField("scan_mode")
    private Integer scanMode;

    /**
     * 扫描起点水平角度
     */
    @TableField("scan_start_pan")
    private Double scanStartPan;

    /**
     * 扫描终点水平角度
     */
    @TableField("scan_end_pan")
    private Double scanEndPan;

    /**
     * 扫描起点垂直角度
     */
    @TableField("scan_start_tilt")
    private Double scanStartTilt;

    /**
     * 扫描终点垂直角度
     */
    @TableField("scan_end_tilt")
    private Double scanEndTilt;

    /**
     * 守望区域坐标（JSON格式：x1,y1,x2,y2）
     */
    @TableField("guard_area")
    private String guardArea;

    /**
     * 是否启用守望：0-禁用 1-启用
     */
    @TableField("guard_enabled")
    private Integer guardEnabled;

    /**
     * 控制状态：1-执行中 2-完成 3-失败 4-超时
     */
    @TableField("control_status")
    private Integer controlStatus;

    /**
     * 控制时间
     */
    @TableField("control_time")
    private LocalDateTime controlTime;

    /**
     * 执行开始时间
     */
    @TableField("execute_start_time")
    private LocalDateTime executeStartTime;

    /**
     * 执行完成时间
     */
    @TableField("execute_end_time")
    private LocalDateTime executeEndTime;

    /**
     * 执行时长（毫秒）
     */
    @TableField("execute_duration")
    private Long executeDuration;

    /**
     * 控制结果：1-成功 2-部分成功 3-失败
     */
    @TableField("control_result")
    private Integer controlResult;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 是否自动校准：0-禁用 1-启用
     */
    @TableField("auto_calibration")
    private Integer autoCalibration;

    /**
     * 校准间隔（秒）
     */
    @TableField("calibration_interval")
    private Integer calibrationInterval;

    /**
     * 上次校准时间
     */
    @TableField("last_calibration_time")
    private LocalDateTime lastCalibrationTime;

    /**
     * 翻转模式：1-正常 2-翻转 3-镜像
     */
    @TableField("flip_mode")
    private Integer flipMode;

    /**
     * 是否支持3D定位：0-不支持 1-支持
     */
    @TableField("d3d_positioning")
    private Integer d3dPositioning;

    /**
     * 3D定位坐标（JSON格式：x,y,z）
     */
    @TableField("d3d_coordinates")
    private String d3dCoordinates;

    /**
     * 权限级别：1-查看 2-控制 3-管理
     */
    @TableField("permission_level")
    private Integer permissionLevel;

    /**
     * 控制来源：1-本地 2-远程 3-自动化
     */
    @TableField("control_source")
    private Integer controlSource;

    /**
     * 控制标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 控制备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}