package net.lab1024.sa.common.organization.domain.vo;

import lombok.Data;

/**
 * 区域权限VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class AreaPermissionVO {

    private String permissionId;

    private String userName;

    private String userCode;

    private String roleName;

    private String roleCode;

    private String permissionLevel;

    private Integer status;

    private String startTime;

    private String endTime;

    private String createTime;

    // 手动添加setter方法以确保编译通过
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}

