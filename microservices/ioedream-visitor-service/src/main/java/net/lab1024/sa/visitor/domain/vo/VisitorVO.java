package net.lab1024.sa.visitor.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 访客信息VO
 * <p>
 * 用于返回访客的基本信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VisitorVO {

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 身份证号
     */
    private String idNumber;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 被访人ID
     */
    private Long visiteeId;

    /**
     * 被访人姓名
     */
    private String visiteeName;

    /**
     * 访问状态
     * <p>
     * 0-待审核 1-已通过 2-已拒绝 3-已访问 4-已离开
     * </p>
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
