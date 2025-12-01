/*
 * 门禁区域管理Store
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessAreaApi } from '/@/api/business/access/area-api';

export const useAccessAreaStore = defineStore('accessArea', {
  state: () => ({
    // 区域树形数据
    areaTree: [],
    areaTreeLoading: false,
    areaTreeExpandedKeys: [],

    // 区域列表数据
    areaList: [],
    areaTotal: 0,
    areaLoading: false,

    // 当前选中的区域
    currentArea: null,
    selectedAreaId: null,

    // 区域选项数据（用于下拉选择）
    areaOptions: [],
    areaOptionsLoading: false,

    // 区域统计信息
    areaStats: null,
    statsLoading: false,

    // 查询参数
    queryParams: {
      pageNum: 1,
      pageSize: 20,
    },

    // 区域设备数据
    areaDevices: [],
    devicesLoading: false,

    // 区域权限配置
    areaPermissions: [],
    permissionsLoading: false,

    // WebSocket连接状态
    wsConnected: false,

    // 区域类型配置
    areaTypes: [
      { value: 'BUILDING', label: '建筑', color: 'blue' },
      { value: 'FLOOR', label: '楼层', color: 'green' },
      { value: 'ROOM', label: '房间', color: 'orange' },
      { value: 'AREA', label: '区域', color: 'purple' },
    ],

    // 权限类型配置
    permissionTypes: [
      { value: 'entrance', label: '进入权限' },
      { value: 'exit', label: '离开权限' },
      { visitor: { value: 'visitor', label: '访客权限' },
      { value: 'emergency', label: '紧急权限' },
    ],

    // 时间限制配置
    timeRestrictions: [
      { value: 'full_day', label: '全天' },
      { value: 'work_day', label: '工作日' },
      { value: 'custom', label: '自定义' },
    ],
  }),

  getters: {
    /**
     * 获取区域总数
     */
    totalAreaCount(state) {
      return state.areaList.length;
    },

    /**
     * 获取启用区域数量
     */
    enabledAreaCount(state) {
      return state.areaList.filter(area => area.status === 1).length;
    },

    /**
     * 获取禁用区域数量
     */
    disabledAreaCount(state) {
      return state.areaList.filter(area => area.status === 0).length;
    },

    /**
     * 获取默认区域
     */
    defaultArea(state) {
      return state.areaList.find(area => area.isDefault);
    },

    /**
     * 获取根级区域
     */
    rootAreas(state) {
      return state.areaTree.filter(area => !area.parentAreaId);
    },

    /**
     * 获取区域类型统计
     */
    areaTypeStats(state) {
      const stats = {};
      state.areaTypes.forEach(type => {
        stats[type.value] = {
          count: 0,
          label: type.label,
          color: type.color,
        };
      });

      state.areaList.forEach(area => {
        if (stats[area.areaType]) {
          stats[area.area.value].count++;
        }
      });

      return stats;
    },

    /**
     * 获取设备数量统计
     */
    deviceCountStats(state) {
      const stats = {
        total: 0,
        online: 0,
        offline: 0,
        fault: 0,
      };

      state.areaList.forEach(area => {
        stats.total += area.deviceCount || 0;
        if (area.onlineDeviceCount) stats.online += area.onlineDeviceCount;
        if (area.offlineDeviceCount) stats.offline += area.offlineDeviceCount;
        if (area.faultDeviceCount) stats.fault += area.faultDeviceCount;
      });

      return stats;
    },

    /**
     * 根据区域ID获取区域信息
     */
    getAreaById: (state) => (areaId) => {
      return state.areaList.find(area => area.areaId === areaId);
    },

    /**
     * 根据区域ID获取子区域
     */
    getSubAreas: (state) => (areaId) => {
      return state.areaList.filter(area => area.parentAreaId === areaId);
    },

    /**
     * 获取区域树形路径
     */
    getAreaPath: (state) => (areaId) => {
      const path = [];
      let currentArea = state.getAreaById(areaId);

      while (currentArea) {
        path.unshift(currentArea.areaName);
        if (currentArea.parentAreaId) {
          currentArea = state.getAreaById(currentArea.parentAreaId);
        } else {
          break;
        }
      }

      return path.join(' / ');
    },

    /**
     * 检查区域是否有子区域
     */
    hasChildren: (state) => (areaId) => {
      return state.areaList.some(area => area.parentAreaId === areaId);
    },

    /**
     * 获取区域的设备列表
     */
    getAreaDevices: (state) => (areaId) => {
      return state.areaDevices.filter(device => device.areaId === areaId);
    },

    /**
     * 获取在线设备比例
     */
    getOnlineDeviceRatio: (state) => (areaId) => {
      const devices = state.getAreaDevices(areaId);
      if (devices.length === 0) return 0;
      const onlineDevices = devices.filter(device => device.onlineStatus);
      return Math.round((onlineDevices.length / devices.length) * 100);
    },
  },

  actions: {
    /**
     * 获取区域树形结构
     */
    async fetchAreaTree() {
      this.areaTreeLoading = true;
      try {
        const response = await accessAreaApi.getAreaTree();
        if (response.code === 1) {
          this.areaTree = response.data || [];
          this.areaTreeExpandedKeys = this.getAllAreaIds(response.data || []);
          return response.data;
        } else {
          message.error(response.msg || '获取区域树失败');
          return [];
        }
      } catch (error) {
        console.error('获取区域树失败:', error);
        message.error('获取区域树失败');
        return [];
      } finally {
        this.areaTreeLoading = false;
      }
    },

    /**
     * 获取区域列表
     */
    async fetchAreaList(params = {}) {
      this.areaLoading = true;
      try {
        const queryParams = { ...this.queryParams, ...params };
        const response = await accessAreaApi.getAreaList(queryParams);

        if (response.code === 1) {
          this.areaList = response.data.list || [];
          this.areaTotal = response.data.total || 0;
          this.queryParams = queryParams;
          return response.data;
        } else {
          message.error(response.msg || '获取区域列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取区域列表失败:', error);
        message.error('获取区域列表失败');
        return null;
      } finally {
        this.areaLoading = false;
      }
    },

    /**
     * 获取区域选项
     */
    async fetchAreaOptions() {
      this.areaOptionsLoading = true;
      try {
        const response = await accessAreaApi.getAreaOptions();
        if (response.code === 1) {
          this.areaOptions = response.data || [];
          return response.data;
        } else {
          message.error(response.msg || '获取区域选项失败');
          return [];
        }
      } catch (error) {
        console.error('获取区域选项失败:', error);
        return [];
      } finally {
        this.areaOptionsLoading = false;
      }
    },

    /**
     * 获取区域统计
     */
    async fetchAreaStats() {
      this.statsLoading = true;
      try {
        const response = await accessAreaApi.getAreaStats();
        if (response.code === 1) {
          this.areaStats = response.data;
          return response.data;
        } else {
          message.error(response.msg || '获取区域统计失败');
          return null;
        }
      } catch (error) {
        console.error('获取区域统计失败:', error);
        return null;
      } finally {
        this.statsLoading = false;
      }
    },

    /**
     * 获取区域详情
     */
    async fetchAreaDetail(areaId) {
      try {
        const response = await accessAreaApi.getAreaDetail(areaId);
        if (response.code === 1) {
          this.currentArea = response.data;
          return response.data;
        } else {
          message.error(response.msg || '获取区域详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取区域详情失败:', error);
        message.error('获取区域详情失败');
        return null;
      }
    },

    /**
     * 添加区域
     */
    async addArea(areaData) {
      try {
        const response = await accessAreaApi.addArea(areaData);
        if (response.code === 1) {
          message.success('添加区域成功');
          await this.fetchAreaTree();
          await this.fetchAreaList();
          return true;
        } else {
          message.error(response.msg || '添加区域失败');
          return false;
        }
      } catch (error) {
        console.error('添加区域失败:', error);
        message.error('添加区域失败');
        return false;
      }
    },

    /**
     * 更新区域
     */
    async updateArea(areaData) {
      try {
        const response = await accessAreaApi.updateArea(areaData);
        if (response.code === 1) {
          message.success('更新区域成功');
          await this.fetchAreaTree();
          await this.fetchAreaList();
          return true;
        } else {
          message.error(response.msg || '更新区域失败');
          return false;
        }
      } catch (error) {
        console.error('更新区域失败:', error);
        message.error('更新区域失败');
        return false;
      }
    },

    /**
     * 删除区域
     */
    async deleteArea(areaId) {
      try {
        const response = await accessAreaApi.deleteArea(areaId);
        if (response.code === 1) {
          message.success('删除区域成功');
          await this.fetchAreaTree();
          await this.fetchAreaList();
          return true;
        } else {
          message.error(response.msg || '删除区域失败');
          return false;
        }
      } catch (error) {
        console.error('删除区域失败:', error);
        message.error('删除区域失败');
        return false;
      }
    },

    /**
     * 批量删除区域
     */
    async batchDeleteAreas(areaIds) {
      try {
        const response = await accessAreaApi.batchDeleteAreas(areaIds);
        if (response.code === 1) {
          message.success('批量删除区域成功');
          await this.fetchAreaTree();
          await this.fetchAreaList();
          return true;
        } else {
          message.error(response.msg || '批量删除区域失败');
          return false;
        }
      } catch (error) {
        console.error('批量删除区域失败:', error);
        message.error('批量删除区域失败');
        return false;
      }
    },

    /**
     * 更新区域状态
     */
    async updateAreaStatus(areaId, status) {
      try {
        const response = await accessAreaApi.updateAreaStatus(areaId, status);
        if (response.code === 1) {
          message.success(status === 1 ? '启用区域成功' : '禁用区域成功');
          await this.fetchAreaTree();
          await this.fetchAreaList();
          return true;
        } else {
          message.error(response.msg || '更新区域状态失败');
          return false;
        }
      } catch (error) {
        console.error('更新区域状态失败:', error);
        message.error('更新区域状态失败');
        return false;
      }
    },

    /**
     * 验证区域编码
     */
    async validateAreaCode(areaCode, excludeId = null) {
      try {
        const response = await accessAreaApi.validateAreaCode(areaCode, excludeId);
        if (response.code === 1) {
          return response.data;
        } else {
          return { valid: false, message: response.msg || '区域编码验证失败' };
        }
      } catch (error) {
        console.error('验证区域编码失败:', error);
        return { valid: false, message: '验证区域编码失败' };
      }
    },

    /**
     * 获取区域设备
     */
    async fetchAreaDevices(areaId) {
      this.devicesLoading = true;
      try {
        const response = await accessAreaApi.getAreaDevices(areaId);
        if (response.code === 1) {
          // 更新设备列表到全局状态
          const deviceStore = useAccessDeviceStore();
          if (deviceStore) {
            deviceStore.deviceList = [
              ...deviceStore.deviceList.filter(d => d.areaId !== areaId),
              ...(response.data || [])
            ];
          }

          this.areaDevices = response.data || [];
          return response.data;
        } else {
          message.error(response.msg || '获取区域设备失败');
          return [];
        }
      } catch (error) {
        console.error('获取区域设备失败:', error);
        message.error('获取区域设备失败');
        return [];
      } finally {
        this.devicesLoading = false;
      }
    },

    /**
     * 获取区域权限配置
     */
    async fetchAreaPermissions(areaId) {
      this.permissionsLoading = true;
      try {
        const response = await accessAreaApi.getAreaPermissions(areaId);
        if (response.code === 1) {
          this.areaPermissions = response.data || [];
          return response.data;
        } else {
          message.error(response.msg || '获取区域权限失败');
          return [];
        }
      } catch (error) {
        console.error('获取区域权限失败:', error);
        return [];
      } finally {
        this.permissionsLoading = false;
      }
    },

    /**
     * 更新区域权限配置
     */
    async updateAreaPermissions(areaId, permissions) {
      try {
        const response = await accessAreaApi.updateAreaPermissions(areaId, permissions);
        if (response.code === 1) {
          message.success('更新区域权限成功');
          this.areaPermissions = permissions;
          return true;
        } else {
          message.error(response.msg || '更新区域权限失败');
          return false;
        }
      } catch (error) {
        console.error('更新区域权限失败:', error);
        message.error('更新区域权限失败');
        return false;
      }
    },

    /**
     * 设置当前区域
     */
    setCurrentArea(area) {
      this.currentArea = area;
      if (area) {
        this.selectedAreaId = area.areaId;
      } else {
        this.selectedAreaId = null;
      }
    },

    /**
     * 设置选中的区域ID
     */
    setSelectedAreaId(areaId) {
      this.selectedAreaId = areaId;
      const area = this.getAreaById(areaId);
      this.setCurrentArea(area);
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
     * 清空区域数据
     */
    clearAreaData() {
      this.areaTree = [];
      this.areaList = [];
      this.areaTotal = 0;
      this.currentArea = null;
      this.selectedAreaId = null;
      this.areaDevices = [];
      this.areaPermissions = [];
    },

    /**
     * 设置展开的节点
     */
    setExpandedKeys(expandedKeys) {
      this.areaTreeExpandedKeys = expandedKeys;
    },

    /**
     * 清空设备数据
     */
    clearDeviceData() {
      this.areaDevices = [];
    },

    /**
     * 设置WebSocket连接状态
     */
    setWsConnected(connected) {
      this.wsConnected = connected;
    },

    /**
     * 获取所有区域ID
     */
    getAllAreaIdList(areas) {
      const ids = [];
      areas.forEach(area => {
        ids.push(area.areaId);
        if (area.children && area.children.length > 0) {
          ids.push(...this.getAllAreaIdList(area.children));
        }
      });
      return ids;
    },

    /**
     * 递归获取所有区域ID
     */
    getAllAreaIds(areas) {
      return this.getAllAreaIdList(areas);
    },
  },
});