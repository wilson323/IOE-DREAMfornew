package net.lab1024.sa.base.module.area.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 人员区域统计视图对象
 * 用于展示人员区域关联的统计信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "人员区域统计视图对象")
public class PersonAreaStatisticsVO {

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 人员类型
     * EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-外包
     */
    @Schema(description = "人员类型", example = "EMPLOYEE")
    private String personType;

    /**
     * 人员类型描述
     */
    @Schema(description = "人员类型描述", example = "员工")
    private String personTypeDesc;

    /**
     * 关联总数
     */
    @Schema(description = "关联总数", example = "5")
    private Integer totalRelations;

    /**
     * 有效关联数
     */
    @Schema(description = "有效关联数", example = "4")
    private Integer activeRelations;

    /**
     * 过期关联数
     */
    @Schema(description = "过期关联数", example = "1")
    private Integer expiredRelations;

    /**
     * 待生效关联数
     */
    @Schema(description = "待生效关联数", example = "0")
    private Integer pendingRelations;

    /**
     * 区域权限统计
     */
    @Schema(description = "区域权限统计")
    private AreaPermissionStatistics areaStats;

    /**
     * 设备权限统计
     */
    @Schema(description = "设备权限统计")
    private DevicePermissionStatistics deviceStats;

    /**
     * 按关联类型统计
     */
    @Schema(description = "按关联类型统计")
    private List<RelationTypeStats> relationTypeStats;

    /**
     * 按区域级别统计
     */
    @Schema(description = "按区域级别统计")
    private List<AreaLevelStats> areaLevelStats;

    /**
     * 最近活动统计
     */
    @Schema(description = "最近活动统计")
    private RecentActivityStats recentStats;

    /**
     * 区域权限统计内部类
     */
    @Data
    @Schema(description = "区域权限统计")
    public static class AreaPermissionStatistics {

        /**
         * 有权限的区域总数
         */
        @Schema(description = "有权限的区域总数", example = "8")
        private Integer totalAreas;

        /**
         * 办公区域数量
         */
        @Schema(description = "办公区域数量", example = "3")
        private Integer officeAreas;

        /**
         * 生产区域数量
         */
        @Schema(description = "生产区域数量", example = "2")
        private Integer productionAreas;

        /**
         * 特殊区域数量
         */
        @Schema(description = "特殊区域数量", example = "1")
        private Integer specialAreas;

        /**
         * 其他区域数量
         */
        @Schema(description = "其他区域数量", example = "2")
        private Integer otherAreas;

        /**
         * 最大区域级别
         */
        @Schema(description = "最大区域级别", example = "3")
        private Integer maxAreaLevel;

        /**
         * 权限覆盖的楼栋数
         */
        @Schema(description = "权限覆盖的楼栋数", example = "2")
        private Integer buildingCount;
    }

    /**
     * 设备权限统计内部类
     */
    @Data
    @Schema(description = "设备权限统计")
    public static class DevicePermissionStatistics {

        /**
         * 可访问的设备总数
         */
        @Schema(description = "可访问的设备总数", example = "25")
        private Integer totalDevices;

        /**
         * 门禁设备数量
         */
        @Schema(description = "门禁设备数量", example = "12")
        private Integer accessDevices;

        /**
         * 考勤设备数量
         */
        @Schema(description = "考勤设备数量", example = "8")
        private Integer attendanceDevices;

        /**
         * 消费设备数量
         */
        @Schema(description = "消费设备数量", example = "3")
        private Integer consumeDevices;

        /**
         * 视频设备数量
         */
        @Schema(description = "视频设备数量", example = "2")
        private Integer videoDevices;

        /**
         * 在线设备数量
         */
        @Schema(description = "在线设备数量", example = "23")
        private Integer onlineDevices;

        /**
         * 离线设备数量
         */
        @Schema(description = "离线设备数量", example = "2")
        private Integer offlineDevices;
    }

    /**
     * 关联类型统计内部类
     */
    @Data
    @Schema(description = "关联类型统计")
    public static class RelationTypeStats {

        /**
         * 关联类型
         */
        @Schema(description = "关联类型", example = "PRIMARY")
        private String relationType;

        /**
         * 关联类型描述
         */
        @Schema(description = "关联类型描述", example = "主要关联")
        private String relationTypeDesc;

        /**
         * 关联数量
         */
        @Schema(description = "关联数量", example = "3")
        private Integer count;

        /**
         * 占比
         */
        @Schema(description = "占比", example = "60.0")
        private Double percentage;
    }

    /**
     * 区域级别统计内部类
     */
    @Data
    @Schema(description = "区域级别统计")
    public static class AreaLevelStats {

        /**
         * 区域级别
         * 1-根区域，2-子区域，3-孙区域
         */
        @Schema(description = "区域级别", example = "2")
        private Integer areaLevel;

        /**
         * 区域级别描述
         */
        @Schema(description = "区域级别描述", example = "子区域")
        private String areaLevelDesc;

        /**
         * 该级别区域数量
         */
        @Schema(description = "该级别区域数量", example = "5")
        private Integer count;

        /**
         * 占比
         */
        @Schema(description = "占比", example = "62.5")
        private Double percentage;
    }

    /**
     * 最近活动统计内部类
     */
    @Data
    @Schema(description = "最近活动统计")
    public static class RecentActivityStats {

        /**
         * 最近7天新增关联数
         */
        @Schema(description = "最近7天新增关联数", example = "2")
        private Integer recentAddedCount;

        /**
         * 最近7天过期关联数
         */
        @Schema(description = "最近7天过期关联数", example = "1")
        private Integer recentExpiredCount;

        /**
         * 最近30天内访问的区域数
         */
        @Schema(description = "最近30天内访问的区域数", example = "6")
        private Integer recentAccessedAreas;

        /**
         * 最近访问时间
         */
        @Schema(description = "最近访问时间", example = "2025-11-24 09:15:30")
        private String lastAccessTime;

        /**
         * 最近访问的区域
         */
        @Schema(description = "最近访问的区域", example = "办公区A")
        private String lastAccessedArea;
    }

    /**
     * 获取有效关联率
     */
    public Double getActiveRelationRate() {
        if (totalRelations == null || totalRelations == 0) {
            return 0.0;
        }
        return (activeRelations.doubleValue() / totalRelations) * 100;
    }

    /**
     * 获取过期关联率
     */
    public Double getExpiredRelationRate() {
        if (totalRelations == null || totalRelations == 0) {
            return 0.0;
        }
        return (expiredRelations.doubleValue() / totalRelations) * 100;
    }

    /**
     * 是否有活跃关联
     */
    public boolean hasActiveRelations() {
        return activeRelations != null && activeRelations > 0;
    }

    /**
     * 是否有过期关联
     */
    public boolean hasExpiredRelations() {
        return expiredRelations != null && expiredRelations > 0;
    }
}