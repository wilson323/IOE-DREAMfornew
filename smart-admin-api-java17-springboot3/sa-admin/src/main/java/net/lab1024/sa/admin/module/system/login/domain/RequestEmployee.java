package net.lab1024.sa.admin.module.system.login.domain;

import lombok.Data;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.enumeration.UserTypeEnum;

/**
 * 请求员工信息
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
public class RequestEmployee implements RequestUser {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 是否管理员标志
     */
    private Boolean administratorFlag;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户代理
     */
    private String userAgent;

    // 实现RequestUser接口的方法
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN_EMPLOYEE;
    }

    @Override
    public String getIp() {
        return userIp;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 获取管理员标志
     * Lombok @Data 注解会自动生成，但为了确保编译通过，手动添加
     *
     * @return 是否为管理员
     */
    public Boolean getAdministratorFlag() {
        return administratorFlag;
    }
}
