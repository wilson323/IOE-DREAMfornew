/**
 * 权限管理状态管理模块
 * IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import {
  getPermissionTree,
  getPermissionList,
  getUserPermissionList,
  getPermissionTemplateList,
  getTimeStrategyList,
  getGeoFenceList,
  getDevicePermissionList,
  getUserEffectivePermissions,
  getPermissionOverview,
  getPermissionAuditLogList,
  assignUserPermissions,
  revokeUserPermissions,
  refreshUserPermissionCache
} from '/@/api/system/permission-api';
import {
  SecurityLevel,
  PermissionType,
  PermissionStatus,
  PermissionAssignType,
  TimeStrategyType
} from '/@/types/permission';

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 权限数据
    permissions: [],
    permissionMap: new Map(),
    permissionTree: [],

    // 用户权限数据
    userPermissions: [],
    userPermissionMap: new Map(),
    currentUserPermissions: [],

    // 权限模板数据
    permissionTemplates: [],
    permissionTemplateMap: new Map(),

    // 时间策略数据
    timeStrategies: [],
    timeStrategyMap: new Map(),

    // 地理围栏数据
    geoFences: [],
    geoFenceMap: new Map(),

    // 设备权限数据
    devicePermissions: [],
    devicePermissionMap: new Map(),

    // 当前选中的权限
    selectedPermissions: [],
    selectedUsers: [],

    // 当前用户信息
    currentUser: null,

    // 加载状态
    loading: {
      permissions: false,
      userPermissions: false,
      templates: false,
      timeStrategies: false,
      geoFences: false,
      devicePermissions: false,
      assigning: false
    },

    // 权限统计
    statistics: {
      totalPermissions: 0,
      userPermissionCount: 0,
      rolePermissionCount: 0,
      templateCount: 0,
      expiredCount: 0,
      activeCount: 0,
      securityLevelDistribution: {},
      typeDistribution: {},
      recentAssignments: [],
      auditLogs: []
    },

    // 权限配置
    config: {
      enableInheritance: true,
      enableTimeStrategy: true,
      enableGeoFence: true,
      enableDevicePermission: true,
      enableAuditLog: true,
      enableCache: true,
      cacheTimeout: 300000, // 5分钟
      maxPermissionLevel: SecurityLevel.TOP_SECRET
    },

    // 搜索和过滤
    filters: {
      keyword: '',
      type: null,
      securityLevel: null,
      status: null,
      userId: null,
      departmentId: null,
      positionId: null
    },

    // 分页
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0
    }
  }),

  getters: {
    /**
     * 获取权限树结构
     */
    getPermissionTree: (state) => {
      return state.permissionTree;
    },

    /**
     * 根据ID获取权限
     */
    getPermissionById: (state) => (id) => {
      return state.permissionMap.get(id);
    },

    /**
     * 根据编码获取权限
     */
    getPermissionByCode: (state) => (code) => {
      return Array.from(state.permissionMap.values()).find(p => p.code === code);
    },

    /**
     * 获取用户权限列表
     */
    getUserPermissions: (state) => (userId) => {
      return state.userPermissionMap.get(userId) || [];
    },

    /**
     * 获取用户权限ID列表
     */
    getUserPermissionIds: (state) => (userId) => {
      const permissions = state.userPermissionMap.get(userId) || [];
      return permissions.map(p => p.permissionId);
    },

    /**
     * 检查用户是否有指定权限
     */
    hasUserPermission: (state) => (userId, permissionCode) => {
      const permissions = state.userPermissionMap.get(userId) || [];
      return permissions.some(p =>
        p.permissionCode === permissionCode &&
        p.status === PermissionStatus.ACTIVE
      );
    },

    /**
     * 检查用户是否有指定权限ID
     */
    hasUserPermissionById: (state) => (userId, permissionId) => {
      const permissions = state.userPermissionMap.get(userId) || [];
      return permissions.some(p =>
        p.permissionId === permissionId &&
        p.status === PermissionStatus.ACTIVE
      );
    },

    /**
     * 获取权限模板列表
     */
    getPermissionTemplates: (state) => {
      return state.permissionTemplates;
    },

    /**
     * 根据ID获取权限模板
     */
    getPermissionTemplateById: (state) => (id) => {
      return state.permissionTemplateMap.get(id);
    },

    /**
     * 获取时间策略列表
     */
    getTimeStrategies: (state) => {
      return state.timeStrategies;
    },

    /**
     * 根据ID获取时间策略
     */
    getTimeStrategyById: (state) => (id) => {
      return state.timeStrategyMap.get(id);
    },

    /**
     * 获取地理围栏列表
     */
    getGeoFences: (state) => {
      return state.geoFences;
    },

    /**
     * 根据ID获取地理围栏
     */
    getGeoFenceById: (state) => (id) => {
      return state.geoFenceMap.get(id);
    },

    /**
     * 获取设备权限列表
     */
    getDevicePermissions: (state) => {
      return state.devicePermissions;
    },

    /**
     * 根据用户ID获取设备权限
     */
    getDevicePermissionsByUserId: (state) => (userId) => {
      return state.devicePermissions.filter(dp => dp.userId === userId);
    },

    /**
     * 根据设备ID获取设备权限
     */
    getDevicePermissionsByDeviceId: (state) => (deviceId) => {
      return state.devicePermissions.filter(dp => dp.deviceId === deviceId);
    },

    /**
     * 获取权限统计信息
     */
    getPermissionStatistics: (state) => {
      return state.statistics;
    },

    /**
     * 获取当前加载状态
     */
    getLoadingStatus: (state) => {
      return state.loading;
    },

    /**
     * 获取当前过滤条件
     */
    getCurrentFilters: (state) => {
      return state.filters;
    },

    /**
     * 获取当前分页信息
     */
    getCurrentPagination: (state) => {
      return state.pagination;
    },

    /**
     * 获取已选中的权限
     */
    getSelectedPermissions: (state) => {
      return state.selectedPermissions;
    },

    /**
     * 获取已选中的用户
     */
    getSelectedUsers: (state) => {
      return state.selectedUsers;
    },

    /**
     * 检查是否有权限被选中
     */
    hasSelectedPermissions: (state) => {
      return state.selectedPermissions.length > 0;
    },

    /**
     * 检查是否有用户被选中
     */
    hasSelectedUsers: (state) => {
      return state.selectedUsers.length > 0;
    },

    /**
     * 获取权限选项（用于下拉选择）
     */
    getPermissionOptions: (state) => {
      return state.permissions.map(p => ({
        label: p.name,
        value: p.id,
        key: p.code,
        securityLevel: p.securityLevel,
        type: p.type
      }));
    },

    /**
     * 获取权限模板选项（用于下拉选择）
     */
    getPermissionTemplateOptions: (state) => {
      return state.permissionTemplates.map(t => ({
        label: t.name,
        value: t.id,
        description: t.description,
        securityLevel: t.securityLevel
      }));
    },

    /**
     * 获取时间策略选项（用于下拉选择）
     */
    getTimeStrategyOptions: (state) => {
      return state.timeStrategies.map(s => ({
        label: s.name,
        value: s.id,
        type: s.type,
        description: s.description
      }));
    },

    /**
     * 获取地理围栏选项（用于下拉选择）
     */
    getGeoFenceOptions: (state) => {
      return state.geoFences.map(g => ({
        label: g.name,
        value: g.id,
        type: g.type,
        description: g.description
      }));
    }
  },

  actions: {
    /**
     * 加载权限树
     */
    async loadPermissionTree(params = {}) {
      this.loading.permissions = true;
      try {
        const response = await getPermissionTree(params);
        if (response.success) {
          this.permissionTree = response.data || [];
          this.buildPermissionMap(this.permissionTree);
        }
        return response;
      } catch (error) {
        console.error('加载权限树失败:', error);
        throw error;
      } finally {
        this.loading.permissions = false;
      }
    },

    /**
     * 加载权限列表
     */
    async loadPermissionList(params = {}) {
      this.loading.permissions = true;
      try {
        const response = await getPermissionList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.permissions = data || [];
          this.pagination = { page, pageSize, total };
          this.buildPermissionMap(this.permissions);
        }
        return response;
      } catch (error) {
        console.error('加载权限列表失败:', error);
        throw error;
      } finally {
        this.loading.permissions = false;
      }
    },

    /**
     * 加载用户权限列表
     */
    async loadUserPermissionList(params = {}) {
      this.loading.userPermissions = true;
      try {
        const response = await getUserPermissionList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.userPermissions = data || [];
          this.pagination = { page, pageSize, total };
          this.buildUserPermissionMap(this.userPermissions);
        }
        return response;
      } catch (error) {
        console.error('加载用户权限列表失败:', error);
        throw error;
      } finally {
        this.loading.userPermissions = false;
      }
    },

    /**
     * 加载权限模板列表
     */
    async loadPermissionTemplateList(params = {}) {
      this.loading.templates = true;
      try {
        const response = await getPermissionTemplateList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.permissionTemplates = data || [];
          this.pagination = { page, pageSize, total };
          this.buildPermissionTemplateMap(this.permissionTemplates);
        }
        return response;
      } catch (error) {
        console.error('加载权限模板列表失败:', error);
        throw error;
      } finally {
        this.loading.templates = false;
      }
    },

    /**
     * 加载时间策略列表
     */
    async loadTimeStrategyList(params = {}) {
      this.loading.timeStrategies = true;
      try {
        const response = await getTimeStrategyList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.timeStrategies = data || [];
          this.pagination = { page, pageSize, total };
          this.buildTimeStrategyMap(this.timeStrategies);
        }
        return response;
      } catch (error) {
        console.error('加载时间策略列表失败:', error);
        throw error;
      } finally {
        this.loading.timeStrategies = false;
      }
    },

    /**
     * 加载地理围栏列表
     */
    async loadGeoFenceList(params = {}) {
      this.loading.geoFences = true;
      try {
        const response = await getGeoFenceList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.geoFences = data || [];
          this.pagination = { page, pageSize, total };
          this.buildGeoFenceMap(this.geoFences);
        }
        return response;
      } catch (error) {
        console.error('加载地理围栏列表失败:', error);
        throw error;
      } finally {
        this.loading.geoFences = false;
      }
    },

    /**
     * 加载设备权限列表
     */
    async loadDevicePermissionList(params = {}) {
      this.loading.devicePermissions = true;
      try {
        const response = await getDevicePermissionList({
          ...this.filters,
          ...this.pagination,
          ...params
        });
        if (response.success) {
          const { data, total, page, pageSize } = response.data;
          this.devicePermissions = data || [];
          this.pagination = { page, pageSize, total };
          this.buildDevicePermissionMap(this.devicePermissions);
        }
        return response;
      } catch (error) {
        console.error('加载设备权限列表失败:', error);
        throw error;
      } finally {
        this.loading.devicePermissions = false;
      }
    },

    /**
     * 加载用户有效权限
     */
    async loadUserEffectivePermissions(userId) {
      try {
        const response = await getUserEffectivePermissions(userId);
        if (response.success) {
          this.currentUserPermissions = response.data || [];
        }
        return response;
      } catch (error) {
        console.error('加载用户有效权限失败:', error);
        throw error;
      }
    },

    /**
     * 加载权限统计信息
     */
    async loadPermissionStatistics() {
      try {
        const response = await getPermissionOverview();
        if (response.success) {
          this.statistics = { ...this.statistics, ...response.data };
        }
        return response;
      } catch (error) {
        console.error('加载权限统计信息失败:', error);
        throw error;
      }
    },

    /**
     * 分配用户权限
     */
    async assignUserPermission(params) {
      this.loading.assigning = true;
      try {
        const response = await assignUserPermissions(params);
        if (response.success) {
          // 重新加载用户权限
          if (params.userId) {
            await this.loadUserPermissionList({ userId: params.userId });
          }
          // 刷新用户权限缓存
          await this.refreshUserCache(params.userId);
          // 重新加载统计信息
          await this.loadPermissionStatistics();
        }
        return response;
      } catch (error) {
        console.error('分配用户权限失败:', error);
        throw error;
      } finally {
        this.loading.assigning = false;
      }
    },

    /**
     * 撤销用户权限
     */
    async revokeUserPermission(params) {
      this.loading.assigning = true;
      try {
        const response = await revokeUserPermissions(params);
        if (response.success) {
          // 重新加载用户权限
          if (params.userId) {
            await this.loadUserPermissionList({ userId: params.userId });
          }
          // 刷新用户权限缓存
          await this.refreshUserCache(params.userId);
          // 重新加载统计信息
          await this.loadPermissionStatistics();
        }
        return response;
      } catch (error) {
        console.error('撤销用户权限失败:', error);
        throw error;
      } finally {
        this.loading.assigning = false;
      }
    },

    /**
     * 刷新用户权限缓存
     */
    async refreshUserCache(userId) {
      try {
        const response = await refreshUserPermissionCache(userId);
        return response;
      } catch (error) {
        console.error('刷新用户权限缓存失败:', error);
        throw error;
      }
    },

    /**
     * 构建权限映射表
     */
    buildPermissionMap(permissions) {
      this.permissionMap.clear();
      const buildMap = (items) => {
        items.forEach(item => {
          this.permissionMap.set(item.id, item);
          if (item.children && item.children.length > 0) {
            buildMap(item.children);
          }
        });
      };
      buildMap(permissions);
    },

    /**
     * 构建用户权限映射表
     */
    buildUserPermissionMap(userPermissions) {
      this.userPermissionMap.clear();
      userPermissions.forEach(up => {
        if (!this.userPermissionMap.has(up.userId)) {
          this.userPermissionMap.set(up.userId, []);
        }
        this.userPermissionMap.get(up.userId).push(up);
      });
    },

    /**
     * 构建权限模板映射表
     */
    buildPermissionTemplateMap(templates) {
      this.permissionTemplateMap.clear();
      templates.forEach(template => {
        this.permissionTemplateMap.set(template.id, template);
      });
    },

    /**
     * 构建时间策略映射表
     */
    buildTimeStrategyMap(strategies) {
      this.timeStrategyMap.clear();
      strategies.forEach(strategy => {
        this.timeStrategyMap.set(strategy.id, strategy);
      });
    },

    /**
     * 构建地理围栏映射表
     */
    buildGeoFenceMap(geoFences) {
      this.geoFenceMap.clear();
      geoFences.forEach(geoFence => {
        this.geoFenceMap.set(geoFence.id, geoFence);
      });
    },

    /**
     * 构建设备权限映射表
     */
    buildDevicePermissionMap(devicePermissions) {
      this.devicePermissionMap.clear();
      devicePermissions.forEach(dp => {
        this.devicePermissionMap.set(dp.id, dp);
      });
    },

    /**
     * 设置当前用户
     */
    setCurrentUser(user) {
      this.currentUser = user;
    },

    /**
     * 设置过滤条件
     */
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters };
    },

    /**
     * 重置过滤条件
     */
    resetFilters() {
      this.filters = {
        keyword: '',
        type: null,
        securityLevel: null,
        status: null,
        userId: null,
        departmentId: null,
        positionId: null
      };
    },

    /**
     * 设置分页信息
     */
    setPagination(pagination) {
      this.pagination = { ...this.pagination, ...pagination };
    },

    /**
     * 设置选中的权限
     */
    setSelectedPermissions(permissionIds) {
      this.selectedPermissions = permissionIds || [];
    },

    /**
     * 添加选中的权限
     */
    addSelectedPermission(permissionId) {
      if (!this.selectedPermissions.includes(permissionId)) {
        this.selectedPermissions.push(permissionId);
      }
    },

    /**
     * 移除选中的权限
     */
    removeSelectedPermission(permissionId) {
      const index = this.selectedPermissions.indexOf(permissionId);
      if (index > -1) {
        this.selectedPermissions.splice(index, 1);
      }
    },

    /**
     * 清空选中的权限
     */
    clearSelectedPermissions() {
      this.selectedPermissions = [];
    },

    /**
     * 设置选中的用户
     */
    setSelectedUsers(userIds) {
      this.selectedUsers = userIds || [];
    },

    /**
     * 添加选中的用户
     */
    addSelectedUser(userId) {
      if (!this.selectedUsers.includes(userId)) {
        this.selectedUsers.push(userId);
      }
    },

    /**
     * 移除选中的用户
     */
    removeSelectedUser(userId) {
      const index = this.selectedUsers.indexOf(userId);
      if (index > -1) {
        this.selectedUsers.splice(index, 1);
      }
    },

    /**
     * 清空选中的用户
     */
    clearSelectedUsers() {
      this.selectedUsers = [];
    },

    /**
     * 更新权限配置
     */
    updateConfig(config) {
      this.config = { ...this.config, ...config };
    },

    /**
     * 权限验证（内部方法）
     */
    validatePermission(userId, permissionCode) {
      return this.hasUserPermission(userId, permissionCode);
    },

    /**
     * 批量权限验证
     */
    validatePermissions(userId, permissionCodes) {
      return permissionCodes.map(code => ({
        code,
        hasPermission: this.hasUserPermission(userId, code)
      }));
    },

    /**
     * 根据安全级别过滤权限
     */
    filterPermissionsBySecurityLevel(securityLevel) {
      const levelOrder = [
        SecurityLevel.PUBLIC,
        SecurityLevel.INTERNAL,
        SecurityLevel.CONFIDENTIAL,
        SecurityLevel.SECRET,
        SecurityLevel.TOP_SECRET
      ];
      const maxIndex = levelOrder.indexOf(securityLevel);
      return this.permissions.filter(p => {
        const index = levelOrder.indexOf(p.securityLevel);
        return index <= maxIndex;
      });
    },

    /**
     * 搜索权限
     */
    searchPermissions(keyword, type = null) {
      let filtered = this.permissions;

      if (keyword) {
        filtered = filtered.filter(p =>
          p.name.toLowerCase().includes(keyword.toLowerCase()) ||
          p.code.toLowerCase().includes(keyword.toLowerCase()) ||
          (p.description && p.description.toLowerCase().includes(keyword.toLowerCase()))
        );
      }

      if (type) {
        filtered = filtered.filter(p => p.type === type);
      }

      return filtered;
    },

    /**
     * 检查权限是否过期
     */
    isPermissionExpired(userPermission) {
      if (!userPermission.expireTime) {
        return false;
      }
      return new Date(userPermission.expireTime) < new Date();
    },

    /**
     * 获取权限状态文本
     */
    getPermissionStatusText(status) {
      const statusMap = {
        [PermissionStatus.ACTIVE]: '生效中',
        [PermissionStatus.INACTIVE]: '未生效',
        [PermissionStatus.EXPIRED]: '已过期',
        [PermissionStatus.REVOKED]: '已撤销'
      };
      return statusMap[status] || '未知';
    },

    /**
     * 获取安全级别文本
     */
    getSecurityLevelText(level) {
      const levelMap = {
        [SecurityLevel.PUBLIC]: '公开级',
        [SecurityLevel.INTERNAL]: '内部级',
        [SecurityLevel.CONFIDENTIAL]: '秘密级',
        [SecurityLevel.SECRET]: '机密级',
        [SecurityLevel.TOP_SECRET]: '绝密级'
      };
      return levelMap[level] || '未知';
    },

    /**
     * 获取权限类型文本
     */
    getPermissionTypeText(type) {
      const typeMap = {
        [PermissionType.MENU]: '菜单权限',
        [PermissionType.BUTTON]: '按钮权限',
        [PermissionType.API]: '接口权限',
        [PermissionType.DATA]: '数据权限',
        [PermissionType.DEVICE]: '设备权限',
        [PermissionType.AREA]: '区域权限',
        [PermissionType.TIME]: '时间权限'
      };
      return typeMap[type] || '未知';
    },

    /**
     * 清空数据
     */
    clearData() {
      this.permissions = [];
      this.permissionMap.clear();
      this.permissionTree = [];
      this.userPermissions = [];
      this.userPermissionMap.clear();
      this.currentUserPermissions = [];
      this.permissionTemplates = [];
      this.permissionTemplateMap.clear();
      this.timeStrategies = [];
      this.timeStrategyMap.clear();
      this.geoFences = [];
      this.geoFenceMap.clear();
      this.devicePermissions = [];
      this.devicePermissionMap.clear();
      this.selectedPermissions = [];
      this.selectedUsers = [];
      this.resetFilters();
      this.setPagination({ page: 1, pageSize: 20 });
    }
  }
});