/*
 * 门禁设备管理Store
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessDeviceApi } from '/@/api/business/access/device-api';

export const useAccessDeviceStore = defineStore('accessDevice', {
  state: () => ({
    // 设备列表数据
    deviceList: [],
    deviceTotal: 0,
    deviceLoading: false,

    // 分组树数据
    groupTreeData: [],

    // 设备统计
    deviceStats: null,
    statsLoading: false,

    // 当前选中的设备
    currentDevice: null,

    // 实时状态数据
    realTimeStatusMap: new Map(),

    // 查询参数
    queryParams: {
      pageNum: 1,
      pageSize: 20,
    },

    // 选中的设备ID列表
    selectedDeviceIds: [],

    // WebSocket连接状态
    wsConnected: false,

    // 设备类型配置
    deviceTypes: [],
  }),

  getters: {
    /**
     * 在线设备数量
     */
    onlineDeviceCount(state) {
      return state.deviceList.filter(d => d.onlineStatus === 'online').length;
    },

    /**
     * 离线设备数量
     */
    offlineDeviceCount(state) {
      return state.deviceList.filter(d => d.onlineStatus === 'offline').length;
    },

    /**
     * 故障设备数量
     */
    faultDeviceCount(state) {
      return state.deviceList.filter(d => d.status === 'fault').length;
    },

    /**
     * 在线率
     */
    onlineRate(state) {
      if (state.deviceList.length === 0) return 0;
      return Math.round((state.onlineDeviceCount / state.deviceList.length) * 100);
    },

    /**
     * 设备类型统计
     */
    deviceTypeStats(state) {
      const stats = {};
      state.deviceList.forEach(device => {
        const type = device.deviceTypeName || device.deviceType;
        stats[type] = (stats[type] || 0) + 1;
      });
      return stats;
    },

    /**
     * 根据设备ID获取设备信息
     */
    getDeviceById: (state) => (deviceId) => {
      return state.deviceList.find(d => d.deviceId === deviceId);
    },

    /**
     * 获取设备的实时状态
     */
    getDeviceRealTimeStatus: (state) => (deviceId) => {
      return state.realTimeStatusMap.get(deviceId);
    },
  },

  actions: {
    /**
     * 获取设备列表
     */
    async fetchDeviceList(params = {}) {
      this.deviceLoading = true;
      try {
        const queryParams = { ...this.queryParams, ...params };
        const response = await accessDeviceApi.queryDeviceList(queryParams);

        if (response.code === 1) {
          this.deviceList = response.data.list || [];
          this.deviceTotal = response.data.total || 0;
          this.queryParams = queryParams;
          return response.data;
        } else {
          message.error(response.msg || '获取设备列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取设备列表失败:', error);
        message.error('获取设备列表失败');
        return null;
      } finally {
        this.deviceLoading = false;
      }
    },

    /**
     * 获取设备分组树
     */
    async fetchGroupTree() {
      try {
        const response = await accessDeviceApi.getDeviceGroupTree();
        if (response.code === 1) {
          this.groupTreeData = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取设备分组树失败:', error);
        return [];
      }
    },

    /**
     * 获取设备统计
     */
    async fetchDeviceStats() {
      this.statsLoading = true;
      try {
        const response = await accessDeviceApi.getDeviceStats();
        if (response.code === 1) {
          this.deviceStats = response.data;
          return response.data;
        }
      } catch (error) {
        console.error('获取设备统计失败:', error);
        return null;
      } finally {
        this.statsLoading = false;
      }
    },

    /**
     * 获取设备详情
     */
    async fetchDeviceDetail(deviceId) {
      try {
        const response = await accessDeviceApi.getDeviceDetail(deviceId);
        if (response.code === 1) {
          this.currentDevice = response.data;
          return response.data;
        } else {
          message.error(response.msg || '获取设备详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取设备详情失败:', error);
        message.error('获取设备详情失败');
        return null;
      }
    },

    /**
     * 添加设备
     */
    async addDevice(deviceData) {
      try {
        const response = await accessDeviceApi.addDevice(deviceData);
        if (response.code === 1) {
          message.success('添加设备成功');
          await this.fetchDeviceList();
          return true;
        } else {
          message.error(response.msg || '添加设备失败');
          return false;
        }
      } catch (error) {
        console.error('添加设备失败:', error);
        message.error('添加设备失败');
        return false;
      }
    },

    /**
     * 更新设备
     */
    async updateDevice(deviceData) {
      try {
        const response = await accessDeviceApi.updateDevice(deviceData);
        if (response.code === 1) {
          message.success('更新设备成功');
          await this.fetchDeviceList();
          return true;
        } else {
          message.error(response.msg || '更新设备失败');
          return false;
        }
      } catch (error) {
        console.error('更新设备失败:', error);
        message.error('更新设备失败');
        return false;
      }
    },

    /**
     * 删除设备
     */
    async deleteDevice(deviceId) {
      try {
        const response = await accessDeviceApi.deleteDevice(deviceId);
        if (response.code === 1) {
          message.success('删除设备成功');
          await this.fetchDeviceList();
          return true;
        } else {
          message.error(response.msg || '删除设备失败');
          return false;
        }
      } catch (error) {
        console.error('删除设备失败:', error);
        message.error('删除设备失败');
        return false;
      }
    },

    /**
     * 批量删除设备
     */
    async batchDeleteDevices(deviceIds) {
      try {
        const response = await accessDeviceApi.batchDeleteDevices(deviceIds);
        if (response.code === 1) {
          message.success(response.msg || '批量删除成功');
          this.selectedDeviceIds = [];
          await this.fetchDeviceList();
          return true;
        } else {
          message.error(response.msg || '批量删除失败');
          return false;
        }
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
        return false;
      }
    },

    /**
     * 更新设备状态
     */
    async updateDeviceStatus(deviceId, status) {
      try {
        const response = await accessDeviceApi.updateDeviceStatus(deviceId, status);
        if (response.code === 1) {
          message.success('更新设备状态成功');
          await this.fetchDeviceList();
          return true;
        } else {
          message.error(response.msg || '更新设备状态失败');
          return false;
        }
      } catch (error) {
        console.error('更新设备状态失败:', error);
        message.error('更新设备状态失败');
        return false;
      }
    },

    /**
     * 远程开门
     */
    async remoteOpenDoor(deviceId) {
      try {
        const response = await accessDeviceApi.remoteOpenDoor(deviceId);
        if (response.code === 1) {
          message.success('远程开门成功');
          return true;
        } else {
          message.error(response.msg || '远程开门失败');
          return false;
        }
      } catch (error) {
        console.error('远程开门失败:', error);
        message.error('远程开门失败');
        return false;
      }
    },

    /**
     * 重启设备
     */
    async restartDevice(deviceId) {
      try {
        const response = await accessDeviceApi.restartDevice(deviceId);
        if (response.code === 1) {
          message.success('设备重启成功');
          return true;
        } else {
          message.error(response.msg || '设备重启失败');
          return false;
        }
      } catch (error) {
        console.error('设备重启失败:', error);
        message.error('设备重启失败');
        return false;
      }
    },

    /**
     * 同步设备配置
     */
    async syncDeviceConfig(deviceId) {
      try {
        const response = await accessDeviceApi.syncDeviceConfig(deviceId);
        if (response.code === 1) {
          message.success('配置同步成功');
          return true;
        } else {
          message.error(response.msg || '配置同步失败');
          return false;
        }
      } catch (error) {
        console.error('配置同步失败:', error);
        message.error('配置同步失败');
        return false;
      }
    },

    /**
     * 获取设备实时状态
     */
    async fetchRealTimeStatus(deviceIds) {
      try {
        const response = await accessDeviceApi.getDeviceRealTimeStatus(deviceIds);
        if (response.code === 1) {
          const statusMap = new Map();
          response.data.forEach(status => {
            statusMap.set(status.deviceId, status);
          });
          this.realTimeStatusMap = statusMap;
          return response.data;
        }
      } catch (error) {
        console.error('获取实时状态失败:', error);
        return [];
      }
    },

    /**
     * 更新单个设备实时状态
     */
    updateRealTimeStatus(deviceStatus) {
      this.realTimeStatusMap.set(deviceStatus.deviceId, deviceStatus);

      // 同时更新设备列表中的状态
      const device = this.deviceList.find(d => d.deviceId === deviceStatus.deviceId);
      if (device) {
        device.onlineStatus = deviceStatus.onlineStatus;
        device.status = deviceStatus.status;
        device.lastHeartbeatTime = deviceStatus.lastHeartbeatTime;
      }
    },

    /**
     * 设置当前设备
     */
    setCurrentDevice(device) {
      this.currentDevice = device;
    },

    /**
     * 清空设备列表
     */
    clearDeviceList() {
      this.deviceList = [];
      this.deviceTotal = 0;
    },

    /**
     * 设置查询参数
     */
    setQueryParams(params) {
      this.queryParams = { ...this.queryParams, ...params };
    },

    /**
     * 重置查询参数
     */
    resetQueryParams() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 20,
      };
    },

    /**
     * 设置选中的设备ID列表
     */
    setSelectedDeviceIds(deviceIds) {
      this.selectedDeviceIds = deviceIds;
    },

    /**
     * 设置WebSocket连接状态
     */
    setWsConnected(connected) {
      this.wsConnected = connected;
    },

    /**
     * 清空实时状态数据
     */
    clearRealTimeStatus() {
      this.realTimeStatusMap.clear();
    },
  },
});