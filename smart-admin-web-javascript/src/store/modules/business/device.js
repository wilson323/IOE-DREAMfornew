/*
 * 智能视频-设备管理Store
 *
 * @Author:    Claude Code
 * @Date:      2024-11-05
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { deviceApi } from '/@/api/business/smart-video/device-api';

export const useDeviceStore = defineStore('device', {
  state: () => ({
    deviceList: [],
    groupTreeData: [],
    loading: false,
    currentDevice: null,
  }),

  getters: {
    /**
     * 在线设备数量
     */
    onlineDeviceCount(state) {
      return state.deviceList.filter(d => d.status === 'online').length;
    },

    /**
     * 离线设备数量
     */
    offlineDeviceCount(state) {
      return state.deviceList.filter(d => d.status === 'offline').length;
    },

    /**
     * 设备类型统计
     */
    deviceTypeStats(state) {
      const stats = {};
      state.deviceList.forEach(device => {
        stats[device.deviceType] = (stats[device.deviceType] || 0) + 1;
      });
      return stats;
    },
  },

  actions: {
    /**
     * 获取设备列表
     */
    async fetchDeviceList(params = {}, useMock = true) {
      this.loading = true;
      try {
        let response;
        if (useMock) {
          // 开发阶段使用mock数据
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockQueryDeviceList(params);
        } else {
          // 生产环境使用真实API
          response = await deviceApi.queryDeviceList(params);
        }

        if (response.code === 1) {
          this.deviceList = response.data.list;
          return response.data;
        } else {
          message.error(response.msg || '获取数据失败');
          return null;
        }
      } catch (error) {
        console.error('获取设备列表失败:', error);
        message.error('获取数据失败');
        return null;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 获取分组树
     */
    async fetchGroupTree(useMock = true) {
      try {
        let response;
        if (useMock) {
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockGetGroupTree();
        } else {
          response = await deviceApi.getDeviceGroupTree();
        }

        if (response.code === 1) {
          this.groupTreeData = response.data;
          return response.data;
        }
      } catch (error) {
        console.error('获取分组树失败:', error);
        return [];
      }
    },

    /**
     * 添加设备
     */
    async addDevice(deviceData, useMock = true) {
      try {
        let response;
        if (useMock) {
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockAddDevice(deviceData);
        } else {
          response = await deviceApi.addDevice(deviceData);
        }

        if (response.code === 1) {
          message.success('添加设备成功');
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
    async updateDevice(deviceData, useMock = true) {
      try {
        let response;
        if (useMock) {
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockUpdateDevice(deviceData);
        } else {
          response = await deviceApi.updateDevice(deviceData);
        }

        if (response.code === 1) {
          message.success('更新设备成功');
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
    async deleteDevice(deviceId, useMock = true) {
      try {
        let response;
        if (useMock) {
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockDeleteDevice(deviceId);
        } else {
          response = await deviceApi.deleteDevice(deviceId);
        }

        if (response.code === 1) {
          message.success('删除设备成功');
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
    async batchDeleteDevices(deviceIds, useMock = true) {
      try {
        let response;
        if (useMock) {
          const deviceMockData = await import('/@/views/business/smart-video/mock/device-mock-data');
          response = deviceMockData.default.mockBatchDeleteDevice(deviceIds);
        } else {
          response = await deviceApi.batchDeleteDevice(deviceIds);
        }

        if (response.code === 1) {
          message.success(response.msg || '批量删除成功');
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
    },
  },
});
