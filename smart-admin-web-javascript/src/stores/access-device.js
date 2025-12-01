/*
 * 门禁设备管理状态
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessDeviceApi } from '@/api/business/access/device-api';

export const useAccessDeviceStore = defineStore('accessDevice', {
  state: () => ({
    // 设备列表数据
    deviceList: [],
    deviceTotal: 0,
    deviceLoading: false,

    // 设备统计数据
    deviceStats: null,
    statsLoading: false,

    // 设备详情
    deviceDetail: null,
    detailLoading: false,

    // 实时设备状态缓存
    realTimeStatusMap: new Map(),

    // 选中的设备ID列表
    selectedDeviceIds: [],

    // 设备分组数据
    deviceGroups: [],
    groupsLoading: false,

    // 设备类型选项
    deviceTypes: [],
    deviceStatuses: [],

    // 搜索参数
    searchParams: {
      pageNum: 1,
      pageSize: 20,
      deviceName: '',
      deviceType: undefined,
      status: undefined,
      groupId: undefined,
    },

    // 控制操作状态
    controlLoadingMap: new Map(),
  }),

  getters: {
    // 获取在线设备数量
    onlineDeviceCount: (state) => {
      return state.deviceList.filter(device => device.status === 'ONLINE').length;
    },

    // 获取离线设备数量
    offlineDeviceCount: (state) => {
      return state.deviceList.filter(device => device.status === 'OFFLINE').length;
    },

    // 获取故障设备数量
    faultDeviceCount: (state) => {
      return state.deviceList.filter(device => device.status === 'FAULT').length;
    },

    // 获取设备状态分布
    deviceStatusDistribution: (state) => {
      const distribution = {};
      state.deviceList.forEach(device => {
        distribution[device.status] = (distribution[device.status] || 0) + 1;
      });
      return distribution;
    },

    // 获取设备类型分布
    deviceTypeDistribution: (state) => {
      const distribution = {};
      state.deviceList.forEach(device => {
        distribution[device.deviceType] = (distribution[device.deviceType] || 0) + 1;
      });
      return distribution;
    },

    // 根据ID获取设备信息
    getDeviceById: (state) => (deviceId) => {
      return state.deviceList.find(device => device.deviceId === deviceId);
    },

    // 获取设备的实时状态
    getDeviceRealTimeStatus: (state) => (deviceId) => {
      return state.realTimeStatusMap.get(deviceId);
    },

    // 检查设备是否被选中
    isDeviceSelected: (state) => (deviceId) => {
      return state.selectedDeviceIds.includes(deviceId);
    },

    // 获取选中的设备信息
    selectedDevices: (state) => {
      return state.deviceList.filter(device => state.selectedDeviceIds.includes(device.deviceId));
    },
  },

  actions: {
    /**
     * 获取设备列表
     */
    async fetchDeviceList(params = {}) {
      try {
        this.deviceLoading = true;
        const queryParams = { ...this.searchParams, ...params };
        const response = await accessDeviceApi.queryDeviceList(queryParams);

        if (response.code === 200) {
          this.deviceList = response.data.records || [];
          this.deviceTotal = response.data.total || 0;
          this.searchParams = queryParams;
        } else {
          message.error(response.message || '获取设备列表失败');
        }
      } catch (error) {
        console.error('获取设备列表失败:', error);
        message.error('获取设备列表失败');
      } finally {
        this.deviceLoading = false;
      }
    },

    /**
     * 获取设备统计数据
     */
    async fetchDeviceStats() {
      try {
        this.statsLoading = true;
        const response = await accessDeviceApi.getDeviceStats();

        if (response.code === 200) {
          this.deviceStats = response.data;
        } else {
          message.error(response.message || '获取设备统计失败');
        }
      } catch (error) {
        console.error('获取设备统计失败:', error);
        message.error('获取设备统计失败');
      } finally {
        this.statsLoading = false;
      }
    },

    /**
     * 获取设备详情
     */
    async fetchDeviceDetail(deviceId) {
      try {
        this.detailLoading = true;
        const response = await accessDeviceApi.getDeviceDetail(deviceId);

        if (response.code === 200) {
          this.deviceDetail = response.data;
        } else {
          message.error(response.message || '获取设备详情失败');
        }
      } catch (error) {
        console.error('获取设备详情失败:', error);
        message.error('获取设备详情失败');
      } finally {
        this.detailLoading = false;
      }
    },

    /**
     * 添加设备
     */
    async addDevice(deviceData) {
      try {
        const response = await accessDeviceApi.addDevice(deviceData);

        if (response.code === 200) {
          message.success('设备添加成功');
          await this.fetchDeviceList(); // 刷新列表
          return true;
        } else {
          message.error(response.message || '设备添加失败');
          return false;
        }
      } catch (error) {
        console.error('设备添加失败:', error);
        message.error('设备添加失败');
        return false;
      }
    },

    /**
     * 更新设备
     */
    async updateDevice(deviceData) {
      try {
        const response = await accessDeviceApi.updateDevice(deviceData);

        if (response.code === 200) {
          message.success('设备更新成功');
          await this.fetchDeviceList(); // 刷新列表
          return true;
        } else {
          message.error(response.message || '设备更新失败');
          return false;
        }
      } catch (error) {
        console.error('设备更新失败:', error);
        message.error('设备更新失败');
        return false;
      }
    },

    /**
     * 删除设备
     */
    async deleteDevice(deviceId) {
      try {
        const response = await accessDeviceApi.deleteDevice(deviceId);

        if (response.code === 200) {
          message.success('设备删除成功');
          await this.fetchDeviceList(); // 刷新列表
          return true;
        } else {
          message.error(response.message || '设备删除失败');
          return false;
        }
      } catch (error) {
        console.error('设备删除失败:', error);
        message.error('设备删除失败');
        return false;
      }
    },

    /**
     * 更新设备状态
     */
    async updateDeviceStatus(deviceId, status) {
      try {
        const loadingKey = `status-${deviceId}`;
        this.controlLoadingMap.set(loadingKey, true);

        const response = await accessDeviceApi.updateDeviceStatus(deviceId, status);

        if (response.code === 200) {
          message.success('设备状态更新成功');
          // 更新本地状态
          const device = this.deviceList.find(d => d.deviceId === deviceId);
          if (device) {
            device.status = status;
          }
          return true;
        } else {
          message.error(response.message || '设备状态更新失败');
          return false;
        }
      } catch (error) {
        console.error('设备状态更新失败:', error);
        message.error('设备状态更新失败');
        return false;
      } finally {
        this.controlLoadingMap.delete(`status-${deviceId}`);
      }
    },

    /**
     * 远程开门
     */
    async remoteOpenDoor(deviceId) {
      try {
        const loadingKey = `open-${deviceId}`;
        this.controlLoadingMap.set(loadingKey, true);

        const response = await accessDeviceApi.remoteOpenDoor(deviceId);

        if (response.code === 200) {
          message.success('远程开门成功');
          return true;
        } else {
          message.error(response.message || '远程开门失败');
          return false;
        }
      } catch (error) {
        console.error('远程开门失败:', error);
        message.error('远程开门失败');
        return false;
      } finally {
        this.controlLoadingMap.delete(`open-${deviceId}`);
      }
    },

    /**
     * 重启设备
     */
    async restartDevice(deviceId) {
      try {
        const loadingKey = `restart-${deviceId}`;
        this.controlLoadingMap.set(loadingKey, true);

        const response = await accessDeviceApi.restartDevice(deviceId);

        if (response.code === 200) {
          message.success('设备重启成功');
          return true;
        } else {
          message.error(response.message || '设备重启失败');
          return false;
        }
      } catch (error) {
        console.error('设备重启失败:', error);
        message.error('设备重启失败');
        return false;
      } finally {
        this.controlLoadingMap.delete(`restart-${deviceId}`);
      }
    },

    /**
     * 同步设备时间
     */
    async syncDeviceTime(deviceId) {
      try {
        const loadingKey = `syncTime-${deviceId}`;
        this.controlLoadingMap.set(loadingKey, true);

        const response = await accessDeviceApi.syncDeviceTime(deviceId);

        if (response.code === 200) {
          message.success('设备时间同步成功');
          return true;
        } else {
          message.error(response.message || '设备时间同步失败');
          return false;
        }
      } catch (error) {
        console.error('设备时间同步失败:', error);
        message.error('设备时间同步失败');
        return false;
      } finally {
        this.controlLoadingMap.delete(`syncTime-${deviceId}`);
      }
    },

    /**
     * 同步设备配置
     */
    async syncDeviceConfig(deviceId) {
      try {
        const loadingKey = `sync-${deviceId}`;
        this.controlLoadingMap.set(loadingKey, true);

        const response = await accessDeviceApi.syncDeviceConfig(deviceId);

        if (response.code === 200) {
          message.success('设备配置同步成功');
          return true;
        } else {
          message.error(response.message || '设备配置同步失败');
          return false;
        }
      } catch (error) {
        console.error('设备配置同步失败:', error);
        message.error('设备配置同步失败');
        return false;
      } finally {
        this.controlLoadingMap.delete(`sync-${deviceId}`);
      }
    },

    /**
     * 批量更新设备状态
     */
    async batchUpdateDeviceStatus(deviceIds, status) {
      try {
        this.controlLoadingMap.set('batch-update', true);

        const response = await accessDeviceApi.batchUpdateDeviceStatus({
          deviceIds,
          status,
        });

        if (response.code === 200) {
          message.success('批量更新设备状态成功');
          await this.fetchDeviceList(); // 刷新列表
          return true;
        } else {
          message.error(response.message || '批量更新设备状态失败');
          return false;
        }
      } catch (error) {
        console.error('批量更新设备状态失败:', error);
        message.error('批量更新设备状态失败');
        return false;
      } finally {
        this.controlLoadingMap.delete('batch-update');
      }
    },

    /**
     * 获取设备类型选项
     */
    async fetchDeviceTypes() {
      try {
        const response = await accessDeviceApi.getDeviceTypes();

        if (response.code === 200) {
          this.deviceTypes = response.data || [];
        }
      } catch (error) {
        console.error('获取设备类型失败:', error);
      }
    },

    /**
     * 获取设备状态选项
     */
    async fetchDeviceStatuses() {
      try {
        const response = await accessDeviceApi.getDeviceStatuses();

        if (response.code === 200) {
          this.deviceStatuses = response.data || [];
        }
      } catch (error) {
        console.error('获取设备状态选项失败:', error);
      }
    },

    /**
     * 更新设备实时状态
     */
    updateDeviceRealTimeStatus(deviceId, statusData) {
      this.realTimeStatusMap.set(deviceId, {
        ...statusData,
        updateTime: new Date(),
      });

      // 同时更新设备列表中的状态
      const device = this.deviceList.find(d => d.deviceId === deviceId);
      if (device && statusData.status) {
        device.status = statusData.status;
      }
    },

    /**
     * 设置选中的设备
     */
    setSelectedDevices(deviceIds) {
      this.selectedDeviceIds = deviceIds;
    },

    /**
     * 全选/取消全选设备
     */
    toggleSelectAll() {
      if (this.selectedDeviceIds.length === this.deviceList.length) {
        this.selectedDeviceIds = [];
      } else {
        this.selectedDeviceIds = this.deviceList.map(device => device.deviceId);
      }
    },

    /**
     * 重置搜索参数
     */
    resetSearchParams() {
      this.searchParams = {
        pageNum: 1,
        pageSize: 20,
        deviceName: '',
        deviceType: undefined,
        status: undefined,
        groupId: undefined,
      };
      this.selectedDeviceIds = [];
    },

    /**
     * 清除状态数据
     */
    clearState() {
      this.deviceList = [];
      this.deviceTotal = 0;
      this.deviceStats = null;
      this.deviceDetail = null;
      this.selectedDeviceIds = [];
      this.realTimeStatusMap.clear();
      this.controlLoadingMap.clear();
    },
  },
});
