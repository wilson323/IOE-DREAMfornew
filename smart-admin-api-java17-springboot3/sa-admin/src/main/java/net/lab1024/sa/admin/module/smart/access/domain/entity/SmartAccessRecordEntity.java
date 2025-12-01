package net.lab1024.sa.admin.module.smart.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁通行记录实体类
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_access_record")
public class SmartAccessRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 通行方式 (CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, PASSWORD-密码, QR-CODE二维码)
     */
    private String accessType;

    /**
     * 通行结果 (SUCCESS-成功, FAILED-失败, REJECTED-拒绝)
     */
    private String accessResult;

    /**
     * 通行时间
     */
    private LocalDateTime accessTime;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 卡片编号
     */
    private String cardNumber;

    /**
     * 人脸特征ID
     */
    private String faceFeatureId;

    /**
     * 指纹特征ID
     */
    private String fingerprintId;

    /**
     * 通行图片路径
     */
    private String accessImagePath;

    /**
     * 通行视频路径
     */
    private String accessVideoPath;

    /**
     * 温度(摄氏度)
     */
    private Double temperature;

    /**
     * 口罩状态 (0-未佩戴, 1-佩戴)
     */
    private Integer maskStatus;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 是否告警 (0-正常, 1-告警)
     */
    private Integer alarmFlag;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 处理状态 (0-未处理, 1-已处理)
     */
    private Integer handleStatus;

    /**
     * 处理人
     */
    private Long handleUserId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 数据来源 (DEVICE-设备, MANUAL-手动, SYNC-同步)
     */
    private String dataSource;

    /**
     * 扩展属性(JSON格式)
     */
    private String extendJson;
}