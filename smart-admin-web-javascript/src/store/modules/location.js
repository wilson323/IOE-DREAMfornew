import { defineStore } from 'pinia';

/**
 * 位置管理状态存储
 */
export const locationStore = defineStore('location', {
  state: () => ({
    // 位置历史数据
    locationHistory: [],

    // 当前选中的位置
    currentLocation: null,

    // 地理围栏列表
    geoFences: [],

    // 实时追踪的用户/设备列表
    trackingTargets: [],

    // 地图配置
    mapConfig: {
      center: [39.9042, 116.4074], // 默认北京坐标
      zoom: 12,
      mapType: 'standard'
    },

    // 热力图数据
    heatmapData: [],

    // 轨迹数据
    trajectoryData: [],

    // 位置搜索结果
    searchResults: [],

    // 加载状态
    loading: {
      locationHistory: false,
      geoFences: false,
      heatmap: false,
      trajectory: false
    },

    // 错误信息
    error: null,

    // WebSocket连接状态
    wsConnected: false,

    // 实时位置数据
    realtimePositions: new Map(),

    // 位置统计数据
    statistics: {
      todayUpdates: 0,
      activeUsers: 0,
      totalDistance: 0,
      avgAccuracy: 0
    },

    // 筛选条件
    filters: {
      targetType: 'user', // 'user' | 'device'
      targetId: null,
      positioningMethod: null,
      timeRange: null,
      areaIds: []
    }
  }),

  getters: {
    // 获取最新的位置数据
    latestLocation: (state) => {
      return state.locationHistory.length > 0 ? state.locationHistory[0] : null;
    },

    // 获取今天的位置数据
    todayLocations: (state) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      return state.locationHistory.filter(location => {
        return new Date(location.positioningTime) >= today;
      });
    },

    // 获取活跃的地理围栏
    activeGeoFences: (state) => {
      return state.geoFences.filter(fence => fence.status === 'ACTIVE');
    },

    // 按用户ID分组的位置数据
    locationsByUser: (state) => {
      const grouped = {};
      state.locationHistory.forEach(location => {
        if (!grouped[location.userId]) {
          grouped[location.userId] = [];
        }
        grouped[location.userId].push(location);
      });
      return grouped;
    },

    // 按设备ID分组的位置数据
    locationsByDevice: (state) => {
      const grouped = {};
      state.locationHistory.forEach(location => {
        if (location.deviceId) {
          if (!grouped[location.deviceId]) {
            grouped[location.deviceId] = [];
          }
          grouped[location.deviceId].push(location);
        }
      });
      return grouped;
    },

    // 获取特定用户/设备的最新位置
    getLatestPositionByTarget: (state) => (targetId, targetType = 'user') => {
      const positions = targetType === 'user'
        ? state.locationHistory.filter(loc => loc.userId == targetId)
        : state.locationHistory.filter(loc => loc.deviceId == targetId);

      if (positions.length === 0) return null;

      return positions.sort((a, b) =>
        new Date(b.positioningTime) - new Date(a.positioningTime)
      )[0];
    },

    // 计算总距离
    totalDistance: (state) => {
      if (state.locationHistory.length < 2) return 0;

      let distance = 0;
      for (let i = 1; i < state.locationHistory.length; i++) {
        const prev = state.locationHistory[i - 1];
        const curr = state.locationHistory[i];
        distance += calculateDistance(prev, curr);
      }
      return distance;
    },

    // 平均定位精度
    averageAccuracy: (state) => {
      if (state.locationHistory.length === 0) return 0;

      const totalAccuracy = state.locationHistory.reduce((sum, loc) =>
        sum + (loc.accuracy || 0), 0
      );
      return totalAccuracy / state.locationHistory.length;
    },

    // 获取当前位置边界
    getBounds: (state) => {
      if (state.locationHistory.length === 0) return null;

      const lats = state.locationHistory.map(loc => loc.latitude);
      const lngs = state.locationHistory.map(loc => loc.longitude);

      return {
        minLat: Math.min(...lats),
        maxLat: Math.max(...lats),
        minLng: Math.min(...lngs),
        maxLng: Math.max(...lngs)
      };
    }
  },

  actions: {
    // 设置位置历史数据
    setLocationHistory(history) {
      this.locationHistory = history;
      this.updateStatistics();
    },

    // 添加位置数据
    addLocation(location) {
      this.locationHistory.unshift(location);

      // 限制历史记录数量，避免内存溢出
      if (this.locationHistory.length > 10000) {
        this.locationHistory = this.locationHistory.slice(0, 10000);
      }

      this.updateStatistics();
    },

    // 设置当前选中位置
    setCurrentLocation(location) {
      this.currentLocation = location;
    },

    // 设置地理围栏
    setGeoFences(fences) {
      this.geoFences = fences;
    },

    // 添加地理围栏
    addGeoFence(fence) {
      this.geoFences.push(fence);
    },

    // 更新地理围栏
    updateGeoFence(fenceId, updates) {
      const index = this.geoFences.findIndex(f => f.fenceId === fenceId);
      if (index !== -1) {
        this.geoFences[index] = { ...this.geoFences[index], ...updates };
      }
    },

    // 删除地理围栏
    removeGeoFence(fenceId) {
      const index = this.geoFences.findIndex(f => f.fenceId === fenceId);
      if (index !== -1) {
        this.geoFences.splice(index, 1);
      }
    },

    // 设置地图配置
    setMapConfig(config) {
      this.mapConfig = { ...this.mapConfig, ...config };
    },

    // 设置热力图数据
    setHeatmapData(data) {
      this.heatmapData = data;
    },

    // 设置轨迹数据
    setTrajectoryData(data) {
      this.trajectoryData = data;
    },

    // 设置搜索结果
    setSearchResults(results) {
      this.searchResults = results;
    },

    // 设置加载状态
    setLoading(type, status) {
      this.loading[type] = status;
    },

    // 设置错误信息
    setError(error) {
      this.error = error;
    },

    // 清除错误信息
    clearError() {
      this.error = null;
    },

    // 设置WebSocket连接状态
    setWsConnected(connected) {
      this.wsConnected = connected;
    },

    // 添加实时位置
    addRealtimePosition(targetId, position) {
      this.realtimePositions.set(targetId, position);
    },

    // 移除实时位置
    removeRealtimePosition(targetId) {
      this.realtimePositions.delete(targetId);
    },

    // 添加追踪目标
    addTrackingTarget(target) {
      if (!this.trackingTargets.find(t => t.id === target.id)) {
        this.trackingTargets.push(target);
      }
    },

    // 移除追踪目标
    removeTrackingTarget(targetId) {
      const index = this.trackingTargets.findIndex(t => t.id === targetId);
      if (index !== -1) {
        this.trackingTargets.splice(index, 1);
      }
      this.removeRealtimePosition(targetId);
    },

    // 设置筛选条件
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters };
    },

    // 重置筛选条件
    resetFilters() {
      this.filters = {
        targetType: 'user',
        targetId: null,
        positioningMethod: null,
        timeRange: null,
        areaIds: []
      };
    },

    // 更新统计数据
    updateStatistics() {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const todayLocations = this.locationHistory.filter(location => {
        return new Date(location.positioningTime) >= today;
      });

      // 今日更新次数
      this.statistics.todayUpdates = todayLocations.length;

      // 活跃用户数
      const activeUsers = new Set(todayLocations.map(loc => loc.userId));
      this.statistics.activeUsers = activeUsers.size;

      // 总距离
      this.statistics.totalDistance = this.totalDistance;

      // 平均精度
      this.statistics.avgAccuracy = this.averageAccuracy;
    },

    // 清理数据
    clearData() {
      this.locationHistory = [];
      this.currentLocation = null;
      this.trajectoryData = [];
      this.searchResults = [];
      this.realtimePositions.clear();
      this.trackingTargets = [];
      this.heatmapData = [];
    },

    // 导出数据
    exportData(format = 'json') {
      const data = {
        locationHistory: this.locationHistory,
        geoFences: this.geoFences,
        statistics: this.statistics,
        exportTime: new Date().toISOString()
      };

      switch (format) {
        case 'json':
          return JSON.stringify(data, null, 2);
        case 'csv':
          return this.convertToCSV(data.locationHistory);
        default:
          return data;
      }
    },

    // 转换为CSV格式
    convertToCSV(data) {
      if (!data.length) return '';

      const headers = [
        '用户ID', '设备ID', '纬度', '经度', '精度', '定位方式',
        '定位时间', '地址', '速度', '方向角'
      ];

      const rows = data.map(item => [
        item.userId || '',
        item.deviceId || '',
        item.latitude || '',
        item.longitude || '',
        item.accuracy || '',
        item.positioningMethod || '',
        item.positioningTime || '',
        item.address || '',
        item.speed || '',
        item.bearing || ''
      ]);

      return [headers, ...rows].map(row => row.join(',')).join('\n');
    }
  }
});

// 辅助函数：计算两点间距离（米）
function calculateDistance(point1, point2) {
  if (!point1 || !point2 || !point1.latitude || !point1.longitude ||
      !point2.latitude || !point2.longitude) {
    return 0;
  }

  const R = 6371; // 地球半径（公里）
  const lat1 = point1.latitude * Math.PI / 180;
  const lat2 = point2.latitude * Math.PI / 180;
  const deltaLat = (point2.latitude - point1.latitude) * Math.PI / 180;
  const deltaLng = (point2.longitude - point1.longitude) * Math.PI / 180;

  const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c * 1000; // 返回米
}

export default locationStore;