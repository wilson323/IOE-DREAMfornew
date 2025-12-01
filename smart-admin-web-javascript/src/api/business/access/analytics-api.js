/*
 * 门禁管理-数据分析报表API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const analyticsApi = {
  /**
   * 通行数据分析API
   */
  accessAnalytics: {
    /**
     * 获取24小时通行热力图数据
     */
    get24HourHeatmapData: (params) => {
      return postRequest('/access/analytics/traffic/24h-heatmap', params);
    },

    /**
     * 获取按区域统计的通行数据
     */
    getAreaTrafficStats: (params) => {
      return postRequest('/access/analytics/traffic/area-stats', params);
    },

    /**
     * 获取按人员类型统计的通行数据
     */
    getPersonTypeStats: (params) => {
      return postRequest('/access/analytics/traffic/person-type-stats', params);
    },

    /**
     * 获取异常通行趋势数据
     */
    getAbnormalTrendData: (params) => {
      return postRequest('/access/analytics/traffic/abnormal-trend', params);
    },

    /**
     * 获取通行流量趋势数据
     */
    getTrafficTrendData: (params) => {
      return postRequest('/access/analytics/traffic/trend', params);
    },

    /**
     * 获取通行高峰时段分析
     */
    getPeakHourAnalysis: (params) => {
      return postRequest('/access/analytics/traffic/peak-analysis', params);
    }
  },

  /**
   * 设备运行分析API
   */
  deviceAnalytics: {
    /**
     * 获取设备在线率统计
     */
    getDeviceOnlineRate: (params) => {
      return postRequest('/access/analytics/device/online-rate', params);
    },

    /**
     * 获取设备故障率分析
     */
    getDeviceFailureRate: (params) => {
      return postRequest('/access/analytics/device/failure-rate', params);
    },

    /**
     * 获取设备使用频率排行
     */
    getDeviceUsageRanking: (params) => {
      return postRequest('/access/analytics/device/usage-ranking', params);
    },

    /**
     * 获取维修保养提醒
     */
    getMaintenanceReminders: (params) => {
      return postRequest('/access/analytics/device/maintenance', params);
    },

    /**
     * 获取设备性能指标
     */
    getDevicePerformanceMetrics: (params) => {
      return postRequest('/access/analytics/device/performance', params);
    },

    /**
     * 获取设备健康度评分
     */
    getDeviceHealthScore: (params) => {
      return postRequest('/access/analytics/device/health-score', params);
    }
  },

  /**
   * 权限使用分析API
   */
  permissionAnalytics: {
    /**
     * 获取权限申请趋势
     */
    getPermissionApplicationTrend: (params) => {
      return postRequest('/access/analytics/permission/application-trend', params);
    },

    /**
     * 获取权限使用频率统计
     */
    getPermissionUsageFrequency: (params) => {
      return postRequest('/access/analytics/permission/usage-frequency', params);
    },

    /**
     * 获取过期权限预警
     */
    getExpiringPermissions: (params) => {
      return postRequest('/access/analytics/permission/expiring', params);
    },

    /**
     * 获取权限类型分布
     */
    getPermissionTypeDistribution: (params) => {
      return postRequest('/access/analytics/permission/type-distribution', params);
    },

    /**
     * 获取权限使用效率分析
     */
    getPermissionEfficiencyAnalysis: (params) => {
      return postRequest('/access/analytics/permission/efficiency', params);
    }
  },

  /**
   * 安防态势分析API
   */
  securityAnalytics: {
    /**
     * 获取安防事件趋势图
     */
    getSecurityEventTrend: (params) => {
      return postRequest('/access/analytics/security/event-trend', params);
    },

    /**
     * 获取高风险区域识别
     */
    getHighRiskAreas: (params) => {
      return postRequest('/access/analytics/security/high-risk-areas', params);
    },

    /**
     * 获取安防级别分布
     */
    getSecurityLevelDistribution: (params) => {
      return postRequest('/access/analytics/security/level-distribution', params);
    },

    /**
     * 获取告警处理效率
     */
    getAlarmHandlingEfficiency: (params) => {
      return postRequest('/access/analytics/security/alarm-efficiency', params);
    },

    /**
     * 获取安防事件类型统计
     */
    getSecurityEventTypeStats: (params) => {
      return postRequest('/access/analytics/security/event-type-stats', params);
    },

    /**
     * 获取实时安防态势评分
     */
    getSecurityPostureScore: (params) => {
      return postRequest('/access/analytics/security/posture-score', params);
    }
  },

  /**
   * 综合仪表盘API
   */
  dashboardAnalytics: {
    /**
     * 获取仪表盘概览数据
     */
    getDashboardOverview: (params) => {
      return postRequest('/access/analytics/dashboard/overview', params);
    },

    /**
     * 获取KPI指标数据
     */
    getKpiMetrics: (params) => {
      return postRequest('/access/analytics/dashboard/kpi', params);
    },

    /**
     * 获取实时数据更新
     */
    getRealTimeUpdates: (params) => {
      return postRequest('/access/analytics/dashboard/realtime', params);
    },

    /**
     * 获取系统健康度评分
     */
    getSystemHealthScore: (params) => {
      return postRequest('/access/analytics/dashboard/system-health', params);
    }
  },

  /**
   * 数据导出API
   */
  exportAnalytics: {
    /**
     * 导出通行数据分析报表
     */
    exportTrafficAnalytics: (params) => {
      return postRequest('/access/analytics/export/traffic', params);
    },

    /**
     * 导出设备运行分析报表
     */
    exportDeviceAnalytics: (params) => {
      return postRequest('/access/analytics/export/device', params);
    },

    /**
     * 导出权限使用分析报表
     */
    exportPermissionAnalytics: (params) => {
      return postRequest('/access/analytics/export/permission', params);
    },

    /**
     * 导出安防态势分析报表
     */
    exportSecurityAnalytics: (params) => {
      return postRequest('/access/analytics/export/security', params);
    },

    /**
     * 导出综合仪表盘报表
     */
    exportDashboardAnalytics: (params) => {
      return postRequest('/access/analytics/export/dashboard', params);
    },

    /**
     * 生成自定义报表
     */
    generateCustomReport: (params) => {
      return postRequest('/access/analytics/export/custom', params);
    }
  },

  /**
   * 通用分析API
   */
  commonAnalytics: {
    /**
     * 获取时间范围数据
     */
    getTimeRangeData: (params) => {
      return postRequest('/access/analytics/common/time-range', params);
    },

    /**
     * 获取对比分析数据
     */
    getComparisonData: (params) => {
      return postRequest('/access/analytics/common/comparison', params);
    },

    /**
     * 获取预测分析数据
     */
    getForecastData: (params) => {
      return postRequest('/access/analytics/common/forecast', params);
    },

    /**
     * 获取数据质量报告
     */
    getDataQualityReport: (params) => {
      return postRequest('/access/analytics/common/data-quality', params);
    }
  }
};