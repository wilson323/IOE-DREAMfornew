package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账户查询Form
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "账户查询Form")
public class AccountQueryForm {

    /**
     * 账户ID
     */
    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号", example = "ACC001")
    private String accountNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /**
     * 账户状态
     */
    @Schema(description = "账户状态", allowableValues = {"0", "1"}, example = "1")
    private Integer status;

    /**
     * 账户类型
     */
    @Schema(description = "账户类型", allowableValues = {"STAFF", "STUDENT", "VISITOR"}, example = "STAFF")
    private String accountType;

    /**
     * 余额最小值
     */
    @Schema(description = "余额最小值", example = "0.00")
    private Double balanceMin;

    /**
     * 余额最大值
     */
    @Schema(description = "余额最大值", example = "1000.00")
    private Double balanceMax;

    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始", example = "2025-01-01T00:00:00")
    private LocalDateTime createTimeStart;

    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束", example = "2025-12-31T23:59:59")
    private LocalDateTime createTimeEnd;

    /**
     * 更新时间开始
     */
    @Schema(description = "更新时间开始", example = "2025-01-01T00:00:00")
    private LocalDateTime updateTimeStart;

    /**
     * 更新时间结束
     */
    @Schema(description = "更新时间结束", example = "2025-12-31T23:59:59")
    private LocalDateTime updateTimeEnd;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "总部大楼")
    private String areaName;

    /**
     * 卡号
     */
    @Schema(description = "卡号", example = "1234567890")
    private String cardNo;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime")
    private String orderBy;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式", allowableValues = {"ASC", "DESC"}, example = "DESC")
    private String orderDirection;

    /**
     * 模糊查询关键字
     */
    @Schema(description = "模糊查询关键字", example = "张")
    private String keyword;
}


