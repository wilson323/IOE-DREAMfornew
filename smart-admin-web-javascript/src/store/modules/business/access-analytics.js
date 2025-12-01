/*
 * 门禁管理-数据分析报表状态管理
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { reactive, ref } from 'vue';
import { analyticsApi } from '/@/api/business/access/analytics-api';
import { message } from 'ant-design-vue';

export const useAccessAnalyticsStore = defineStore('access-analytics', () => {
  // 状态定义
  const state = reactive({
    // 加载状态
    loading: {
      traffic: false,
      device: false,
      permission: false,
      security: false,
      dashboard: false
    },

    // 通行分析数据
    trafficData: {
      heatmapData: [],
      areaStats: [],
      personTypeStats: [],
      abnormalTrend: [],
      trafficTrend: [],
      peakHourAnalysis: []
    },

    // 设备分析数据
    deviceData: {
      onlineRate: null,
      failureRate: null,
      usageRanking: [],
      maintenanceReminders: [],
      performanceMetrics: [],
      healthScores: []
    },

    // 权限分析数据
    permissionData: {
      applicationTrend: [],
      usageFrequency: [],
      expiringPermissions: [],
      typeDistribution: [],
      efficiencyAnalysis: []
    },

    // 安防分析数据
    securityData: {
      eventTrend: [],
      highRiskAreas: [],
      levelDistribution: [],
      alarmEfficiency: [],
      eventTypeStats: [],
      postureScore: null
    },

    // 仪表盘数据
    dashboardData: {
      overview: null,
      kpiMetrics: [],
      realTimeUpdates: [],
      systemHealth: null
    },

    // 查询参数缓存
    queryCache: {
      traffic: null,
      device: null,
      permission: null,
      security: null,
      dashboard: null
    },

    // 导出任务状态
    exportTasks: new Map(),

    // 实时更新配置
    realTimeConfig: {
      enabled: false,
      interval: 30000, // 30秒
      lastUpdateTime: null
    }
  });

  // 定时器引用
  const realTimeTimer = ref(null);

  // 通行分析相关操作
  const trafficActions = {
    /**
     * 获取24小时通行热力图数据
     */
    async get24HourHeatmapData(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.get24HourHeatmapData(params);
        state.trafficData.heatmapData = response.data || [];
        state.queryCache.traffic = params;
        return response.data;
      } catch (error) {
        console.error('获取24小时通行热力图数据失败:', error);
        message.error('获取24小时通行热力图数据失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    },

    /**
     * 获取按区域统计的通行数据
     */
    async getAreaTrafficStats(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.getAreaTrafficStats(params);
        state.trafficData.areaStats = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取区域通行统计数据失败:', error);
        message.error('获取区域通行统计数据失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    },

    /**
     * 获取按人员类型统计的通行数据
     */
    async getPersonTypeStats(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.getPersonTypeStats(params);
        state.trafficData.personTypeStats = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取人员类型统计数据失败:', error);
        message.error('获取人员类型统计数据失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    },

    /**
     * 获取异常通行趋势数据
     */
    async getAbnormalTrendData(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.getAbnormalTrendData(params);
        state.trafficData.abnormalTrend = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取异常通行趋势数据失败:', error);
        message.error('获取异常通行趋势数据失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    },

    /**
     * 获取通行流量趋势数据
     */
    async getTrafficTrendData(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.getTrafficTrendData(params);
        state.trafficData.trafficTrend = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取通行流量趋势数据失败:', error);
        message.error('获取通行流量趋势数据失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    },

    /**
     * 获取通行高峰时段分析
     */
    async getPeakHourAnalysis(params) {
      state.loading.traffic = true;
      try {
        const response = await analyticsApi.accessAnalytics.getPeakHourAnalysis(params);
        state.trafficData.peakHourAnalysis = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取通行高峰时段分析失败:', error);
        message.error('获取通行高峰时段分析失败');
        throw error;
      } finally {
        state.loading.traffic = false;
      }
    }
  };

  // 设备分析相关操作
  const deviceActions = {
    /**
     * 获取设备在线率统计
     */
    async getDeviceOnlineRate(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getDeviceOnlineRate(params);
        state.deviceData.onlineRate = response.data;
        return response.data;
      } catch (error) {
        console.error('获取设备在线率统计失败:', error);
        message.error('获取设备在线率统计失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    },

    /**
     * 获取设备故障率分析
     */
    async getDeviceFailureRate(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getDeviceFailureRate(params);
        state.deviceData.failureRate = response.data;
        return response.data;
      } catch (error) {
        console.error('获取设备故障率分析失败:', error);
        message.error('获取设备故障率分析失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    },

    /**
     * 获取设备使用频率排行
     */
    async getDeviceUsageRanking(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getDeviceUsageRanking(params);
        state.deviceData.usageRanking = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取设备使用频率排行失败:', error);
        message.error('获取设备使用频率排行失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    },

    /**
     * 获取维修保养提醒
     */
    async getMaintenanceReminders(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getMaintenanceReminders(params);
        state.deviceData.maintenanceReminders = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取维修保养提醒失败:', error);
        message.error('获取维修保养提醒失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    },

    /**
     * 获取设备性能指标
     */
    async getDevicePerformanceMetrics(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getDevicePerformanceMetrics(params);
        state.deviceData.performanceMetrics = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取设备性能指标失败:', error);
        message.error('获取设备性能指标失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    },

    /**
     * 获取设备健康度评分
     */
    async getDeviceHealthScore(params) {
      state.loading.device = true;
      try {
        const response = await analyticsApi.deviceAnalytics.getDeviceHealthScore(params);
        state.deviceData.healthScores = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取设备健康度评分失败:', error);
        message.error('获取设备健康度评分失败');
        throw error;
      } finally {
        state.loading.device = false;
      }
    }
  };

  // 权限分析相关操作
  const permissionActions = {
    /**
     * 获取权限申请趋势
     */
    async getPermissionApplicationTrend(params) {
      state.loading.permission = true;
      try {
        const response = await analyticsApi.permissionAnalytics.getPermissionApplicationTrend(params);
        state.permissionData.applicationTrend = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取权限申请趋势失败:', error);
        message.error('获取权限申请趋势失败');
        throw error;
      } finally {
        state.loading.permission = false;
      }
    },

    /**
     * 获取权限使用频率统计
     */
    async getPermissionUsageFrequency(params) {
      state.loading.permission = true;
      try {
        const response = await analyticsApi.permissionAnalytics.getPermissionUsageFrequency(params);
        state.permissionData.usageFrequency = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取权限使用频率统计失败:', error);
        message.error('获取权限使用频率统计失败');
        throw error;
      } finally {
        state.loading.permission = false;
      }
    },

    /**
     * 获取过期权限预警
     */
    async getExpiringPermissions(params) {
      state.loading.permission = true;
      try {
        const response = await analyticsApi.permissionAnalytics.getExpiringPermissions(params);
        state.permissionData.expiringPermissions = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取过期权限预警失败:', error);
        message.error('获取过期权限预警失败');
        throw error;
      } finally {
        state.loading.permission = false;
      }
    },

    /**
     * 获取权限类型分布
     */
    async getPermissionTypeDistribution(params) {
      state.loading.permission = true;
      try {
        const response = await analyticsApi.permissionAnalytics.getPermissionTypeDistribution(params);
        state.permissionData.typeDistribution = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取权限类型分布失败:', error);
        message.error('获取权限类型分布失败');
        throw error;
      } finally {
        state.loading.permission = false;
      }
    },

    /**
     * 获取权限使用效率分析
     */
    async getPermissionEfficiencyAnalysis(params) {
      state.loading.permission = true;
      try {
        const response = await analyticsApi.permissionAnalytics.getPermissionEfficiencyAnalysis(params);
        state.permissionData.efficiencyAnalysis = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取权限使用效率分析失败:', error);
        message.error('获取权限使用效率分析失败');
        throw error;
      } finally {
        state.loading.permission = false;
      }
    }
  };

  // 安防分析相关操作
  const securityActions = {
    /**
     * 获取安防事件趋势图
     */
    async getSecurityEventTrend(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getSecurityEventTrend(params);
        state.securityData.eventTrend = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取安防事件趋势图失败:', error);
        message.error('获取安防事件趋势图失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    },

    /**
     * 获取高风险区域识别
     */
    async getHighRiskAreas(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getHighRiskAreas(params);
        state.securityData.highRiskAreas = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取高风险区域识别失败:', error);
        message.error('获取高风险区域识别失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    },

    /**
     * 获取安防级别分布
     */
    async getSecurityLevelDistribution(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getSecurityLevelDistribution(params);
        state.securityData.levelDistribution = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取安防级别分布失败:', error);
        message.error('获取安防级别分布失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    },

    /**
     * 获取告警处理效率
     */
    async getAlarmHandlingEfficiency(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getAlarmHandlingEfficiency(params);
        state.securityData.alarmEfficiency = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取告警处理效率失败:', error);
        message.error('获取告警处理效率失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    },

    /**
     * 获取安防事件类型统计
     */
    async getSecurityEventTypeStats(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getSecurityEventTypeStats(params);
        state.securityData.eventTypeStats = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取安防事件类型统计失败:', error);
        message.error('获取安防事件类型统计失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    },

    /**
     * 获取实时安防态势评分
     */
    async getSecurityPostureScore(params) {
      state.loading.security = true;
      try {
        const response = await analyticsApi.securityAnalytics.getSecurityPostureScore(params);
        state.securityData.postureScore = response.data;
        return response.data;
      } catch (error) {
        console.error('获取实时安防态势评分失败:', error);
        message.error('获取实时安防态势评分失败');
        throw error;
      } finally {
        state.loading.security = false;
      }
    }
  };

  // 仪表盘相关操作
  const dashboardActions = {
    /**
     * 获取仪表盘概览数据
     */
    async getDashboardOverview(params) {
      state.loading.dashboard = true;
      try {
        const response = await analyticsApi.dashboardAnalytics.getDashboardOverview(params);
        state.dashboardData.overview = response.data;
        return response.data;
      } catch (error) {
        console.error('获取仪表盘概览数据失败:', error);
        message.error('获取仪表盘概览数据失败');
        throw error;
      } finally {
        state.loading.dashboard = false;
      }
    },

    /**
     * 获取KPI指标数据
     */
    async getKpiMetrics(params) {
      state.loading.dashboard = true;
      try {
        const response = await analyticsApi.dashboardAnalytics.getKpiMetrics(params);
        state.dashboardData.kpiMetrics = response.data || [];
        return response.data;
      } catch (error) {
        console.error('获取KPI指标数据失败:', error);
        message.error('获取KPI指标数据失败');
        throw error;
      } finally {
        state.loading.dashboard = false;
      }
    },

    /**
     * 获取实时数据更新
     */
    async getRealTimeUpdates(params) {
      try {
        const response = await analyticsApi.dashboardAnalytics.getRealTimeUpdates(params);
        state.dashboardData.realTimeUpdates = response.data || [];
        state.realTimeConfig.lastUpdateTime = new Date().toISOString();
        return response.data;
      } catch (error) {
        console.error('获取实时数据更新失败:', error);
        message.error('获取实时数据更新失败');
        throw error;
      }
    },

    /**
     * 获取系统健康度评分
     */
    async getSystemHealthScore(params) {
      state.loading.dashboard = true;
      try {
        const response = await analyticsApi.dashboardAnalytics.getSystemHealthScore(params);
        state.dashboardData.systemHealth = response.data;
        return response.data;
      } catch (error) {
        console.error('获取系统健康度评分失败:', error);
        message.error('获取系统健康度评分失败');
        throw error;
      } finally {
        state.loading.dashboard = false;
      }
    }
  };

  // 数据导出相关操作
  const exportActions = {
    /**
     * 导出通行数据分析报表
     */
    async exportTrafficAnalytics(params) {
      try {
        const response = await analyticsApi.exportAnalytics.exportTrafficAnalytics(params);
        const exportId = response.data?.exportId;
        if (exportId) {
          state.exportTasks.set(exportId, {
            status: 'PENDING',
            progress: 0,
            startTime: new Date().toISOString()
          });
        }
        return response.data;
      } catch (error) {
        console.error('导出通行数据分析报表失败:', error);
        message.error('导出通行数据分析报表失败');
        throw error;
      }
    },

    /**
     * 导出设备运行分析报表
     */
    async exportDeviceAnalytics(params) {
      try {
        const response = await analyticsApi.exportAnalytics.exportDeviceAnalytics(params);
        const exportId = response.data?.exportId;
        if (exportId) {
          state.exportTasks.set(exportId, {
            status: 'PENDING',
            progress: 0,
            startTime: new Date().toISOString()
          });
        }
        return response.data;
      } catch (error) {
        console.error('导出设备运行分析报表失败:', error);
        message.error('导出设备运行分析报表失败');
        throw error;
      }
    },

    /**
     * 导出权限使用分析报表
     */
    async exportPermissionAnalytics(params) {
      try {
        const response = await analyticsApi.exportAnalytics.exportPermissionAnalytics(params);
        const exportId = response.data?.exportId;
        if (exportId) {
          state.exportTasks.set(exportId, {
            status: 'PENDING',
            progress: 0,
            startTime: new Date().toISOString()
          });
        }
        return response.data;
      } catch (error) {
        console.error('导出权限使用分析报表失败:', error);
        message.error('导出权限使用分析报表失败');
        throw error;
      }
    },

    /**
     * 导出安防态势分析报表
     */
    async exportSecurityAnalytics(params) {
      try {
        const response = await analyticsApi.exportAnalytics.exportSecurityAnalytics(params);
        const exportId = response.data?.exportId;
        if (exportId) {
          state.exportTasks.set(exportId, {
            status: 'PENDING',
            progress: 0,
            startTime: new Date().toISOString()
          });
        }
        return response.data;
      } catch (error) {
        console.error('导出安防态势分析报表失败:', error);
        message.error('导出安防态势分析报表失败');
        throw error;
      }
    },

    /**
     * 生成自定义报表
     */
    async generateCustomReport(params) {
      try {
        const response = await analyticsApi.exportAnalytics.generateCustomReport(params);
        const exportId = response.data?.exportId;
        if (exportId) {
          state.exportTasks.set(exportId, {
            status: 'PENDING',
            progress: 0,
            startTime: new Date().toISOString()
          });
        }
        return response.data;
      } catch (error) {
        console.error('生成自定义报表失败:', error);
        message.error('生成自定义报表失败');
        throw error;
      }
    },

    /**
     * 更新导出任务状态
     */
    updateExportTaskStatus(exportId, status) {
      const task = state.exportTasks.get(exportId);
      if (task) {
        task.status = status;
        if (status === 'COMPLETED') {
          task.endTime = new Date().toISOString();
          task.progress = 100;
        } else if (status === 'FAILED') {
          task.endTime = new Date().toISOString();
        }
      }
    },

    /**
     * 移除导出任务
     */
    removeExportTask(exportId) {
      state.exportTasks.delete(exportId);
    },

    /**
     * 清空所有导出任务
     */
    clearExportTasks() {
      state.exportTasks.clear();
    }
  };

  // 实时数据更新相关操作
  const realTimeActions = {
    /**
     * 启动实时数据更新
     */
    startRealTimeUpdates(interval = 30000) {
      if (realTimeTimer.value) {
        clearInterval(realTimeTimer.value);
      }

      state.realTimeConfig.enabled = true;
      state.realTimeConfig.interval = interval;

      realTimeTimer.value = setInterval(async () => {
        try {
          // 获取实时更新数据
          await dashboardActions.getRealTimeUpdates({});
          console.log('实时数据更新完成');
        } catch (error) {
          console.error('实时数据更新失败:', error);
        }
      }, interval);

      message.success('实时数据更新已启动');
    },

    /**
     * 停止实时数据更新
     */
    stopRealTimeUpdates() {
      if (realTimeTimer.value) {
        clearInterval(realTimeTimer.value);
        realTimeTimer.value = null;
      }

      state.realTimeConfig.enabled = false;
      message.info('实时数据更新已停止');
    },

    /**
     * 切换实时数据更新状态
     */
    toggleRealTimeUpdates() {
      if (state.realTimeConfig.enabled) {
        realTimeActions.stopRealTimeUpdates();
      } else {
        realTimeActions.startRealTimeUpdates(state.realTimeConfig.interval);
      }
    }
  };

  // 数据清理相关操作
  const cleanupActions = {
    /**
     * 清空所有数据
     */
    clearAllData() {
      state.trafficData = {
        heatmapData: [],
        areaStats: [],
        personTypeStats: [],
        abnormalTrend: [],
        trafficTrend: [],
        peakHourAnalysis: []
      };

      state.deviceData = {
        onlineRate: null,
        failureRate: null,
        usageRanking: [],
        maintenanceReminders: [],
        performanceMetrics: [],
        healthScores: []
      };

      state.permissionData = {
        applicationTrend: [],
        usageFrequency: [],
        expiringPermissions: [],
        typeDistribution: [],
        efficiencyAnalysis: []
      };

      state.securityData = {
        eventTrend: [],
        highRiskAreas: [],
        levelDistribution: [],
        alarmEfficiency: [],
        eventTypeStats: [],
        postureScore: null
      };

      state.dashboardData = {
        overview: null,
        kpiMetrics: [],
        realTimeUpdates: [],
        systemHealth: null
      };

      state.queryCache = {
        traffic: null,
        device: null,
        permission: null,
        security: null,
        dashboard: null
      };

      exportActions.clearExportTasks();
    },

    /**
     * 重置加载状态
     */
    resetLoadingStates() {
      Object.keys(state.loading).forEach(key => {
        state.loading[key] = false;
      });
    }
  };

  return {
    // 状态
    state,

    // 通行分析
    ...trafficActions,

    // 设备分析
    ...deviceActions,

    // 权限分析
    ...permissionActions,

    // 安防分析
    ...securityActions,

    // 仪表盘
    ...dashboardActions,

    // 数据导出
    ...exportActions,

    // 实时更新
    ...realTimeActions,

    // 数据清理
    ...cleanupActions
  };
});