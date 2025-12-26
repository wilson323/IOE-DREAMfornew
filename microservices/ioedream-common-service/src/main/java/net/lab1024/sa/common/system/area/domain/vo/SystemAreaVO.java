package net.lab1024.sa.common.system.area.domain.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 区域VO（用于接口返回）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class SystemAreaVO {

    private Long areaId;

    private String areaName;

    private String areaCode;

    private String areaType;

    private Long parentId;

    private Integer level;

    private Integer sortOrder;

    private Long managerId;

    private Integer capacity;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<SystemAreaVO> children = new ArrayList<>();
}
