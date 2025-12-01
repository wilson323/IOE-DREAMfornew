/**
 * 门禁区域管理前端集成测试
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-17
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import { accessAreaApi } from '/@/api/business/access/area-api';
import { useAccessAreaStore } from '/@/store/modules/business/access-area';

// Mock API
vi.mock('/@/api/business/access/area-api', () => ({
  accessAreaApi: {
    getAreaTree: vi.fn(),
    getAreaList: vi.fn(),
    getAreaOptions: vi.fn(),
    getAreaStats: vi.fn(),
    getAreaDetail: vi.fn(),
    addArea: vi.fn(),
    updateArea: vi.fn(),
    deleteArea: vi.fn(),
    batchDeleteAreas: vi.fn(),
    updateAreaStatus: vi.fn(),
    validateAreaCode: vi.fn(),
    getAreaDevices: vi.fn(),
    getAreaPermissions: vi.fn(),
    updateAreaPermissions: vi.fn(),
  },
}));

// Mock message
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn(),
  },
}));

describe('门禁区域管理集成测试', () => {
  let store;
  let pinia;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);
    store = useAccessAreaStore();

    // 清除所有mock调用记录
    vi.clearAllMocks();
  });

  afterEach(() => {
    pinia = null;
    store = null;
  });

  describe('状态管理初始化', () => {
    it('应该正确初始化状态', () => {
      expect(store.areaTree).toEqual([]);
      expect(store.areaTreeLoading).toBe(false);
      expect(store.areaList).toEqual([]);
      expect(store.areaTotal).toBe(0);
      expect(store.areaLoading).toBe(false);
      expect(store.currentArea).toBe(null);
      expect(store.selectedAreaId).toBe(null);
      expect(store.areaOptions).toEqual([]);
      expect(store.areaStats).toBe(null);
      expect(store.wsConnected).toBe(false);
    });

    it('应该正确初始化区域类型配置', () => {
      expect(store.areaTypes).toEqual([
        { value: 'BUILDING', label: '建筑', color: 'blue' },
        { value: 'FLOOR', label: '楼层', color: 'green' },
        { value: 'ROOM', label: '房间', color: 'orange' },
        { value: 'AREA', label: '区域', color: 'purple' },
      ]);
    });
  });

  describe('区域树形数据管理', () => {
    it('应该成功获取区域树', async () => {
      const mockTreeData = [
        {
          areaId: 1,
          areaName: '总部大楼',
          areaCode: 'HQ_BUILDING',
          areaType: 'BUILDING',
          children: [
            {
              areaId: 2,
              areaName: '1楼',
              areaCode: 'HQ_F1',
              areaType: 'FLOOR',
              children: [],
            },
          ],
        },
      ];

      accessAreaApi.getAreaTree.mockResolvedValue({
        code: 1,
        data: mockTreeData,
        msg: 'success',
      });

      const result = await store.fetchAreaTree();

      expect(accessAreaApi.getAreaTree).toHaveBeenCalledTimes(1);
      expect(store.areaTree).toEqual(mockTreeData);
      expect(store.areaTreeExpandedKeys).toEqual([1, 2]);
      expect(result).toEqual(mockTreeData);
    });

    it('获取区域树失败时应该显示错误消息', async () => {
      const errorMessage = '获取区域树失败';
      accessAreaApi.getAreaTree.mockResolvedValue({
        code: 0,
        msg: errorMessage,
      });

      const result = await store.fetchAreaTree();

      expect(store.areaTree).toEqual([]);
      expect(result).toEqual([]);
    });

    it('网络错误时应该处理异常', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      accessAreaApi.getAreaTree.mockRejectedValue(new Error('Network error'));

      const result = await store.fetchAreaTree();

      expect(consoleErrorSpy).toHaveBeenCalledWith('获取区域树失败:', expect.any(Error));
      expect(store.areaTree).toEqual([]);
      expect(result).toEqual([]);

      consoleErrorSpy.mockRestore();
    });
  });

  describe('区域列表管理', () => {
    it('应该成功获取区域列表', async () => {
      const mockListData = {
        list: [
          {
            areaId: 1,
            areaName: '总部大楼',
            areaCode: 'HQ_BUILDING',
            status: 1,
            deviceCount: 10,
          },
          {
            areaId: 2,
            areaName: '1楼',
            areaCode: 'HQ_F1',
            status: 1,
            deviceCount: 5,
          },
        ],
        total: 2,
      };

      accessAreaApi.getAreaList.mockResolvedValue({
        code: 1,
        data: mockListData,
      });

      const queryParams = { pageNum: 1, pageSize: 20 };
      const result = await store.fetchAreaList(queryParams);

      expect(accessAreaApi.getAreaList).toHaveBeenCalledWith(queryParams);
      expect(store.areaList).toEqual(mockListData.list);
      expect(store.areaTotal).toBe(mockListData.total);
      expect(store.queryParams).toEqual(queryParams);
      expect(result).toEqual(mockListData);
    });
  });

  describe('区域CRUD操作', () => {
    it('应该成功添加区域', async () => {
      const newArea = {
        areaName: '新区域',
        areaCode: 'NEW_AREA',
        areaType: 'AREA',
      };

      accessAreaApi.addArea.mockResolvedValue({
        code: 1,
        msg: '添加成功',
      });

      // Mock fetchAreaTree and fetchAreaList
      store.fetchAreaTree = vi.fn().mockResolvedValue([]);
      store.fetchAreaList = vi.fn().mockResolvedValue([]);

      const result = await store.addArea(newArea);

      expect(accessAreaApi.addArea).toHaveBeenCalledWith(newArea);
      expect(store.fetchAreaTree).toHaveBeenCalledTimes(1);
      expect(store.fetchAreaList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功更新区域', async () => {
      const updateArea = {
        areaId: 1,
        areaName: '更新区域',
        areaCode: 'UPDATE_AREA',
      };

      accessAreaApi.updateArea.mockResolvedValue({
        code: 1,
        msg: '更新成功',
      });

      store.fetchAreaTree = vi.fn().mockResolvedValue([]);
      store.fetchAreaList = vi.fn().mockResolvedValue([]);

      const result = await store.updateArea(updateArea);

      expect(accessAreaApi.updateArea).toHaveBeenCalledWith(updateArea);
      expect(store.fetchAreaTree).toHaveBeenCalledTimes(1);
      expect(store.fetchAreaList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功删除区域', async () => {
      const areaId = 1;

      accessAreaApi.deleteArea.mockResolvedValue({
        code: 1,
        msg: '删除成功',
      });

      store.fetchAreaTree = vi.fn().mockResolvedValue([]);
      store.fetchAreaList = vi.fn().mockResolvedValue([]);

      const result = await store.deleteArea(areaId);

      expect(accessAreaApi.deleteArea).toHaveBeenCalledWith(areaId);
      expect(store.fetchAreaTree).toHaveBeenCalledTimes(1);
      expect(store.fetchAreaList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功批量删除区域', async () => {
      const areaIds = [1, 2, 3];

      accessAreaApi.batchDeleteAreas.mockResolvedValue({
        code: 1,
        msg: '批量删除成功',
      });

      store.fetchAreaTree = vi.fn().mockResolvedValue([]);
      store.fetchAreaList = vi.fn().mockResolvedValue([]);

      const result = await store.batchDeleteAreas(areaIds);

      expect(accessAreaApi.batchDeleteAreas).toHaveBeenCalledWith(areaIds);
      expect(store.fetchAreaTree).toHaveBeenCalledTimes(1);
      expect(store.fetchAreaList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });
  });

  describe('区域验证操作', () => {
    it('应该成功验证区域编码', async () => {
      const areaCode = 'TEST_AREA';
      const excludeId = null;

      accessAreaApi.validateAreaCode.mockResolvedValue({
        code: 1,
        data: { valid: true, message: '编码可用' },
      });

      const result = await store.validateAreaCode(areaCode, excludeId);

      expect(accessAreaApi.validateAreaCode).toHaveBeenCalledWith(areaCode, excludeId);
      expect(result).toEqual({ valid: true, message: '编码可用' });
    });

    it('验证失败时应该返回错误信息', async () => {
      const areaCode = 'EXIST_AREA';

      accessAreaApi.validateAreaCode.mockResolvedValue({
        code: 0,
        msg: '编码已存在',
      });

      const result = await store.validateAreaCode(areaCode);

      expect(result).toEqual({ valid: false, message: '编码已存在' });
    });
  });

  describe('Getter计算属性', () => {
    beforeEach(() => {
      store.areaList = [
        { areaId: 1, areaName: '区域1', status: 1, isDefault: true, deviceCount: 5 },
        { areaId: 2, areaName: '区域2', status: 0, deviceCount: 3 },
        { areaId: 3, areaName: '区域3', status: 1, deviceCount: 7 },
      ];
    });

    it('totalAreaCount应该返回正确的区域总数', () => {
      expect(store.totalAreaCount).toBe(3);
    });

    it('enabledAreaCount应该返回启用的区域数量', () => {
      expect(store.enabledAreaCount).toBe(2);
    });

    it('disabledAreaCount应该返回禁用的区域数量', () => {
      expect(store.disabledAreaCount).toBe(1);
    });

    it('defaultArea应该返回默认区域', () => {
      expect(store.defaultArea).toEqual({
        areaId: 1,
        areaName: '区域1',
        status: 1,
        isDefault: true,
        deviceCount: 5,
      });
    });

    it('rootAreas应该返回根级区域', () => {
      store.areaTree = [
        { areaId: 1, parentAreaId: null, areaName: '根区域1' },
        { areaId: 2, parentAreaId: 1, areaName: '子区域' },
        { areaId: 3, parentAreaId: null, areaName: '根区域2' },
      ];

      expect(store.rootAreas).toHaveLength(2);
      expect(store.rootAreas[0].areaName).toBe('根区域1');
      expect(store.rootAreas[1].areaName).toBe('根区域2');
    });
  });

  describe('区域设备管理', () => {
    it('应该成功获取区域设备', async () => {
      const areaId = 1;
      const mockDevices = [
        { deviceId: 1, deviceName: '设备1', areaId: 1, onlineStatus: true },
        { deviceId: 2, deviceName: '设备2', areaId: 1, onlineStatus: false },
      ];

      accessAreaApi.getAreaDevices.mockResolvedValue({
        code: 1,
        data: mockDevices,
      });

      const result = await store.fetchAreaDevices(areaId);

      expect(accessAreaApi.getAreaDevices).toHaveBeenCalledWith(areaId);
      expect(store.areaDevices).toEqual(mockDevices);
      expect(result).toEqual(mockDevices);
    });
  });

  describe('区域权限管理', () => {
    it('应该成功获取区域权限', async () => {
      const areaId = 1;
      const mockPermissions = [
        { permissionId: 1, permissionName: '进入权限', areaId: 1 },
        { permissionId: 2, permissionName: '离开权限', areaId: 1 },
      ];

      accessAreaApi.getAreaPermissions.mockResolvedValue({
        code: 1,
        data: mockPermissions,
      });

      const result = await store.fetchAreaPermissions(areaId);

      expect(accessAreaApi.getAreaPermissions).toHaveBeenCalledWith(areaId);
      expect(store.areaPermissions).toEqual(mockPermissions);
      expect(result).toEqual(mockPermissions);
    });

    it('应该成功更新区域权限', async () => {
      const areaId = 1;
      const permissions = [
        { permissionId: 1, enabled: true },
        { permissionId: 2, enabled: false },
      ];

      accessAreaApi.updateAreaPermissions.mockResolvedValue({
        code: 1,
        msg: '权限更新成功',
      });

      const result = await store.updateAreaPermissions(areaId, permissions);

      expect(accessAreaApi.updateAreaPermissions).toHaveBeenCalledWith(areaId, permissions);
      expect(store.areaPermissions).toEqual(permissions);
      expect(result).toBe(true);
    });
  });

  describe('工具方法', () => {
    it('setCurrentArea应该正确设置当前区域', () => {
      const area = { areaId: 1, areaName: '测试区域' };

      store.setCurrentArea(area);

      expect(store.currentArea).toEqual(area);
      expect(store.selectedAreaId).toBe(1);
    });

    it('setSelectedAreaId应该正确设置选中区域ID', () => {
      store.areaList = [
        { areaId: 1, areaName: '区域1' },
        { areaId: 2, areaName: '区域2' },
      ];

      store.setSelectedAreaId(2);

      expect(store.selectedAreaId).toBe(2);
      expect(store.currentArea).toEqual({ areaId: 2, areaName: '区域2' });
    });

    it('setQueryParams应该正确设置查询参数', () => {
      const newParams = { pageNum: 2, pageSize: 50 };

      store.setQueryParams(newParams);

      expect(store.queryParams).toEqual({
        pageNum: 1,
        pageSize: 20,
        ...newParams,
      });
    });

    it('resetQueryParams应该重置查询参数', () => {
      store.queryParams = { pageNum: 3, pageSize: 100, keyword: 'test' };

      store.resetQueryParams();

      expect(store.queryParams).toEqual({
        pageNum: 1,
        pageSize: 20,
      });
    });

    it('clearAreaData应该清空区域数据', () => {
      store.areaTree = [{ areaId: 1 }];
      store.areaList = [{ areaId: 1 }];
      store.areaTotal = 10;
      store.currentArea = { areaId: 1 };
      store.selectedAreaId = 1;
      store.areaDevices = [{ deviceId: 1 }];
      store.areaPermissions = [{ permissionId: 1 }];

      store.clearAreaData();

      expect(store.areaTree).toEqual([]);
      expect(store.areaList).toEqual([]);
      expect(store.areaTotal).toBe(0);
      expect(store.currentArea).toBe(null);
      expect(store.selectedAreaId).toBe(null);
      expect(store.areaDevices).toEqual([]);
      expect(store.areaPermissions).toEqual([]);
    });

    it('getAllAreaIds应该递归获取所有区域ID', () => {
      const areas = [
        { areaId: 1, children: [{ areaId: 2, children: [] }] },
        { areaId: 3, children: [] },
      ];

      const ids = store.getAllAreaIds(areas);

      expect(ids).toEqual([1, 2, 3]);
    });
  });

  describe('WebSocket连接管理', () => {
    it('setWsConnected应该正确设置WebSocket连接状态', () => {
      store.setWsConnected(true);
      expect(store.wsConnected).toBe(true);

      store.setWsConnected(false);
      expect(store.wsConnected).toBe(false);
    });
  });
});