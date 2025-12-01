package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客记录实体
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的字段定义
 * - 清晰的注释说明
 * - 标准的命名规范
 * - 审计字段完整性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@TableName("t_visitor_record")
public class VisitorRecordEntity {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 预约ID
     */
    private Long reservationId;

    /**
     * 预约编号
     */
    private String reservationCode;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 访客手机号
     */
    private String visitorPhone;

    /**
     * 访客身份证号
     */
    private String visitorIdCard;

    /**
     * 访客照片
     */
    private String visitorPhoto;

    /**
     * 访问区域ID
     */
    private Long visitAreaId;

    /**
     * 访问区域名称
     */
    private String visitAreaName;

    /**
     * 访问设备ID
     */
    private Long deviceId;

    /**
     * 访问设备名称
     */
    private String deviceName;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 通行方式：1-二维码，2-人脸识别，3-身份证，4-人工登记，5-NFC，6-指纹
     */
    private Integer accessMethod;

    /**
     * 通行结果：0-成功，1-失败，2-异常，3-超时
     */
    private Integer accessResult;

    /**
     * 通行结果描述
     */
    private String accessResultDesc;

    /**
     * 接待人用户ID
     */
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    private String hostUserName;

    /**
     * 接待部门
     */
    private String hostDepartment;

    /**
     * 进入时间
     */
    private LocalDateTime entryTime;

    /**
     * 离开时间
     */
    private LocalDateTime exitTime;

    /**
     * 停留时长（分钟）
     */
    private Integer stayDuration;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 访问备注
     */
    private String visitRemarks;

    /**
     * 体温检测结果
     */
    private Double bodyTemperature;

    /**
     * 健康码状态：0-绿码，1-黄码，2-红码
     */
    private Integer healthCodeStatus;

    /**
     * 安检状态：0-未安检，1-已通过，2-未通过
     */
    private Integer securityCheckStatus;

    /**
     * 安检备注
     */
    private String securityCheckRemarks;

    /**
     * 随行人员数量
     */
    private Integer companionCount;

    /**
     * 随行人员信息（JSON格式）
     */
    private String companionInfo;

    /**
     * 物品登记信息（JSON格式）
     */
    private String baggageInfo;

    /**
     * 车辆信息（JSON格式）
     */
    private String vehicleInfo;

    /**
     * 访客类型：1-普通访客，2-VIP访客，3-临时访客，4-长期访客
     */
    private Integer visitorType;

    /**
     * 紧急程度：1-普通，2-紧急，3-非常紧急
     */
    private Integer urgencyLevel;

    /**
     * 访问类型：1-商务访问，2-技术交流，3-培训学习，4-其他
     */
    private Integer visitType;

    /**
     * 通行权限级别：1-普通，2-受限，3-高级
     */
    private Integer accessLevel;

    /**
     * 验证方式：1-人工验证，2-系统自动验证，3-远程验证
     */
    private Integer verificationMethod;

    /**
     * 验证人员ID
     */
    private Long verifierId;

    /**
     * 验证人员姓名
     */
    private String verifierName;

    /**
     * 验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 验证结果描述
     */
    private String verifyResult;

    /**
     * 异常类型：1-身份异常，2-时间异常，3-区域异常，4-设备异常，5-其他
     */
    private Integer exceptionType;

    /**
     * 异常描述
     */
    private String exceptionDesc;

    /**
     * 处理状态：0-未处理，1-已处理，2-处理中
     */
    private Integer handleStatus;

    /**
     * 处理人员ID
     */
    private Long handlerId;

    /**
     * 处理人员姓名
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 图片证据（JSON格式）
     */
    private String imageEvidence;

    /**
     * 视频证据路径
     */
    private String videoEvidence;

    /**
     * 访客评价：1-很差，2-较差，3-一般，4-满意，5-非常满意
     */
    private Integer visitorRating;

    /**
     * 访客反馈
     */
    private String visitorFeedback;

    /**
     * 反馈时间
     */
    private LocalDateTime feedbackTime;

    /**
     * 系统备注
     */
    private String systemRemarks;

    /**
     * 数据来源：1-门禁系统，2-人工录入，3-访客系统，4-第三方系统
     */
    private Integer dataSource;

    /**
     * 同步状态：0-未同步，1-已同步，2-同步失败
     */
    private Integer syncStatus;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 地理位置
     */
    private String location;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备序列号
     */
    private String deviceSerial;

    /**
     * 访客签名图片
     */
    private String visitorSignature;

    /**
     * 接待人签名图片
     */
    private String hostSignature;

    /**
     * 扩展属性（JSON格式）
     */
    private String extendProperties;

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
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer deletedFlag;
}