package net.lab1024.sa.common.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    private Long userId;
    private String username;
    private String realName;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String avatarUrl;
    private Integer gender;
    private Long departmentId;
    private String departmentName;
    private String position;
    private String employeeNo;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private String remark;
}

