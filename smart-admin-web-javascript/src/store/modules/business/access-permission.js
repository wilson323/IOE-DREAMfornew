/*
 * 门禁权限管理Store
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessPermissionApi } from '/@/api/business/access/permission-api';

export const useAccessPermissionStore = defineStore('accessPermission', {
  state: () => ({
    // 权限列表数据
    permissionList: [],
    permissionTotal: 0,
    permissionLoading: false,

    // 权限模板列表
    templateList: [],

    // 时间策略列表
    strategyList: [],

    // 权限统计
    permissionStats: null,
    statsLoading: false,

    // 当前选中的权限
    currentPermission: null,

    // 查询参数
    queryParams: {
      pageNum: 1,
      pageSize: 20,
    },

    // 选中的权限ID列表
    selectedPermissionIds: [],

    // 即将过期的权限
    expiringPermissions: [],

    // 用户权限缓存
    userPermissionCache: new Map(),
  }),

  getters: {
    /**
     * 有效权限数量
     */
    activePermissionCount(state) {
      return state.permissionList.filter(p => p.status === 'active').length;
    },

    /**
     * 临时权限数量
     */
    temporaryPermissionCount(state) {
      return state.permissionList.filter(p => p.isTemporary).length;
    },

    /**
     * 过期权限数量
     */
    expiredPermissionCount(state) {
      return state.permissionList.filter(p => p.status === 'expired').length;
    },

    /**
     * 权限类型统计
     */
    permissionTypeStats(state) {
      const stats = {};
      state.permissionList.forEach(permission => {
        const type = permission.permissionType;
        stats[type] = (stats[type] || 0) + 1;
      });
      return stats;
    },

    /**
     * 根据用户ID获取权限列表
     */
    getPermissionsByUserId: (state) => (userId) => {
      return state.permissionList.filter(p => p.userId === userId);
    },

    /**
     * 获取用户权限缓存
     */
    getUserPermissionCache: (state) => (userId) => {
      return state.userPermissionCache.get(userId);
    },
  },

  actions: {
    /**
     * 获取权限列表
     */
    async fetchPermissionList(params = {}) {
      this.permissionLoading = true;
      try {
        const queryParams = { ...this.queryParams, ...params };
        const response = await accessPermissionApi.queryPermissionList(queryParams);

        if (response.code === 1) {
          this.permissionList = response.data.list || [];
          this.permissionTotal = response.data.total || 0;
          this.queryParams = queryParams;
          return response.data;
        } else {
          message.error(response.msg || '获取权限列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取权限列表失败:', error);
        message.error('获取权限列表失败');
        return null;
      } finally {
        this.permissionLoading = false;
      }
    },

    /**
     * 获取用户权限
     */
    async fetchUserPermissions(userId) {
      try {
        const response = await accessPermissionApi.getUserPermissions(userId);
        if (response.code === 1) {
          // 缓存用户权限
          this.userPermissionCache.set(userId, response.data);
          return response.data;
        }
      } catch (error) {
        console.error('获取用户权限失败:', error);
        return [];
      }
    },

    /**
     * 分配权限
     */
    async assignPermission(permissionData) {
      try {
        const response = await accessPermissionApi.assignPermission(permissionData);
        if (response.code === 1) {
          message.success('权限分配成功');
          await this.fetchPermissionList();

          // 清除相关用户缓存
          this.clearUserPermissionCache(permissionData.userId);

          return true;
        } else {
          message.error(response.msg || '权限分配失败');
          return false;
        }
      } catch (error) {
        console.error('权限分配失败:', error);
        message.error('权限分配失败');
        return false;
      }
    },

    /**
     * 批量分配权限
     */
    async batchAssignPermissions(params) {
      try {
        const response = await accessPermissionApi.batchAssignPermissions(params);
        if (response.code === 1) {
          message.success('批量分配权限成功');
          await this.fetchPermissionList();

          // 清除相关用户缓存
          if (params.userIds) {
            params.userIds.forEach(userId => {
              this.clearUserPermissionCache(userId);
            });
          }

          return true;
        } else {
          message.error(response.msg || '批量分配权限失败');
          return false;
        }
      } catch (error) {
        console.error('批量分配权限失败:', error);
        message.error('批量分配权限失败');
        return false;
      }
    },

    /**
     * 撤销权限
     */
    async revokePermission(permissionId) {
      try {
        const response = await accessPermissionApi.revokePermission(permissionId);
        if (response.code === 1) {
          message.success('权限撤销成功');
          await this.fetchPermissionList();
          return true;
        } else {
          message.error(response.msg || '权限撤销失败');
          return false;
        }
      } catch (error) {
        console.error('权限撤销失败:', error);
        message.error('权限撤销失败');
        return false;
      }
    },

    /**
     * 批量撤销权限
     */
    async batchRevokePermissions(permissionIds) {
      try {
        const response = await accessPermissionApi.batchRevokePermissions(permissionIds);
        if (response.code === 1) {
          message.success('批量撤销权限成功');
          this.selectedPermissionIds = [];
          await this.fetchPermissionList();
          return true;
        } else {
          message.error(response.msg || '批量撤销权限失败');
          return false;
        }
      } catch (error) {
        console.error('批量撤销权限失败:', error);
        message.error('批量撤销权限失败');
        return false;
      }
    },

    /**
     * 创建临时权限
     */
    async createTemporaryPermission(permissionData) {
      try {
        const response = await accessPermissionApi.createTemporaryPermission(permissionData);
        if (response.code === 1) {
          message.success('临时权限创建成功');
          await this.fetchPermissionList();

          // 清除相关用户缓存
          this.clearUserPermissionCache(permissionData.userId);

          return true;
        } else {
          message.error(response.msg || '临时权限创建失败');
          return false;
        }
      } catch (error) {
        console.error('临时权限创建失败:', error);
        message.error('临时权限创建失败');
        return false;
      }
    },

    /**
     * 获取权限模板列表
     */
    async fetchPermissionTemplates() {
      try {
        const response = await accessPermissionApi.getPermissionTemplates();
        if (response.code === 1) {
          this.templateList = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取权限模板失败:', error);
        return [];
      }
    },

    /**
     * 创建权限模板
     */
    async createPermissionTemplate(templateData) {
      try {
        const response = await accessPermissionApi.createPermissionTemplate(templateData);
        if (response.code === 1) {
          message.success('权限模板创建成功');
          await this.fetchPermissionTemplates();
          return true;
        } else {
          message.error(response.msg || '权限模板创建失败');
          return false;
        }
      } catch (error) {
        console.error('权限模板创建失败:', error);
        message.error('权限模板创建失败');
        return false;
      }
    },

    /**
     * 应用权限模板
     */
    async applyPermissionTemplate(params) {
      try {
        const response = await accessPermissionApi.applyPermissionTemplate(params);
        if (response.code === 1) {
          message.success('权限模板应用成功');
          await this.fetchPermissionList();

          // 清除相关用户缓存
          if (params.userIds) {
            params.userIds.forEach(userId => {
              this.clearUserPermissionCache(userId);
            });
          }

          return true;
        } else {
          message.error(response.msg || '权限模板应用失败');
          return false;
        }
      } catch (error) {
        console.error('权限模板应用失败:', error);
        message.error('权限模板应用失败');
        return false;
      }
    },

    /**
     * 获取时间策略列表
     */
    async fetchTimeStrategies() {
      try {
        const response = await accessPermissionApi.getTimeStrategies();
        if (response.code === 1) {
          this.strategyList = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取时间策略失败:', error);
        return [];
      }
    },

    /**
     * 获取权限统计
     */
    async fetchPermissionStats(params = {}) {
      this.statsLoading = true;
      try {
        const response = await accessPermissionApi.getPermissionStats(params);
        if (response.code === 1) {
          this.permissionStats = response.data;
          return response.data;
        }
      } catch (error) {
        console.error('获取权限统计失败:', error);
        return null;
      } finally {
        this.statsLoading = false;
      }
    },

    /**
     * 获取即将过期的权限
     */
    async fetchExpiringPermissions(days = 7) {
      try {
        const response = await accessPermissionApi.getExpiringPermissions(days);
        if (response.code === 1) {
          this.expiringPermissions = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取即将过期权限失败:', error);
        return [];
      }
    },

    /**
     * 权限验证
     */
    async validatePermission(params) {
      try {
        const response = await accessPermissionApi.validatePermission(params);
        if (response.code === 1) {
          return response.data;
        }
      } catch (error) {
        console.error('权限验证失败:', error);
        return null;
      }
    },

    /**
     * 设置当前权限
     */
    setCurrentPermission(permission) {
      this.currentPermission = permission;
    },

    /**
     * 清空权限列表
     */
    clearPermissionList() {
      this.permissionList = [];
      this.permissionTotal = 0;
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
     * 设置选中的权限ID列表
     */
    setSelectedPermissionIds(permissionIds) {
      this.selectedPermissionIds = permissionIds;
    },

    /**
     * 清除用户权限缓存
     */
    clearUserPermissionCache(userId) {
      if (userId) {
        this.userPermissionCache.delete(userId);
      } else {
        this.userPermissionCache.clear();
      }
    },

    /**
     * 更新权限状态
     */
    updatePermissionStatus(permissionId, status) {
      const permission = this.permissionList.find(p => p.permissionId === permissionId);
      if (permission) {
        permission.status = status;
      }
    },

    /**
     * 批量更新权限状态
     */
    batchUpdatePermissionStatus(permissionIds, status) {
      this.permissionList.forEach(permission => {
        if (permissionIds.includes(permission.permissionId)) {
          permission.status = status;
        }
      });
    },
  },
});