package net.lab1024.sa.common.security.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户实体类
 * <p>
 * 系统用户账户实体，用于身份认证和权限管理
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 完整的用户账户字段
 * </p>
 * <p>
 * 业务场景：
 * - 用户登录认证
 * - 账户状态管理
 * - 登录安全控制（锁定、失败次数）
 * - 权限和角色关联
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    /**
     * 用户ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列user_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一，用于登录）
     */
    private String username;

    /**
     * 密码（加密存储）
     * <p>
     * 使用BCrypt加密，不存储明文密码
     * </p>
     */
    private String password;

    /**
     * 用户状态
     * <p>
     * 1-正常
     * 2-禁用
     * 3-锁定
     * </p>
     */
    private Integer status;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录失败次数
     * <p>
     * 用于防暴力破解，达到阈值后锁定账户
     * </p>
     */
    private Integer loginFailCount;

    /**
     * 账户是否锁定
     * <p>
     * 0-未锁定
     * 1-已锁定
     * </p>
     */
    private Integer accountLocked;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 解锁时间
     */
    private LocalDateTime unlockTime;

    /**
     * 备注
     */
    private String remark;
}
