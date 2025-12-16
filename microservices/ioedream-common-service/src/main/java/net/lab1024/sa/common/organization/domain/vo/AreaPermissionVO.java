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
}

