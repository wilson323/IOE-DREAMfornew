/**
 * 门禁系统前端完整集成测试
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-17
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import { useAccessAreaStore } from '/@/store/modules/business/access-area';
import { useAccessDeviceStore } from '/@/store/modules/business/access-device';
import { useAccessRecordStore } from '/@/store/modules/business/access-record';
import { accessAreaApi } from '/@/api/business/access/area-api';
import { accessDeviceApi } from '/@/api/business/access/device-api';
import { accessRecordApi } from '/@/api/business/access/record-api';

// Mock all APIs
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

vi.mock('/@/api/business/access/device-api', () => ({
  accessDeviceApi: {
    getDeviceList: vi.fn(),
    getDeviceDetail: vi.fn(),
    addDevice: vi.fn(),
    updateDevice: vi.fn(),
    deleteDevice: vi.fn(),
    batchDeleteDevices: vi.fn(),
    updateDeviceStatus: vi.fn(),
    getDeviceTypes: vi.fn(),
    getDeviceStatus: vi.fn(),
    getDeviceStats: vi.fn(),
    controlDevice: vi.fn(),
    getDeviceLogs: vi.fn(),
    syncDeviceTime: vi.fn(),
    restartDevice: vi.fn(),
  },
}));

vi.mock('/@/api/business/access/record-api', () => ({
  accessRecordApi: {
    getRecordList: vi.fn(),
    getRecordDetail: vi.fn(),
    getRecordStats: vi.fn(),
    exportRecords: vi.fn(),
    getHeatmapData: vi.fn(),
    getAccessStats: vi.fn(),
    handleAbnormalRecord: vi.fn(),
    batchProcessAbnormalRecords: vi.fn(),
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

describe('门禁系统前端完整集成测试', () => {
  let areaStore;
  let deviceStore;
  let recordStore;
  let pinia;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);
    areaStore = useAccessAreaStore();
    deviceStore = useAccessDeviceStore();
    recordStore = useAccessRecordStore();
    vi.clearAllMocks();
  });

  afterEach(() => {
    pinia = null;
    areaStore = null;
    deviceStore = null;
    recordStore = null;
  });

  describe('系统初始化集成测试', () => {
    it('所有Store应该正确初始化', () => {
      // 验证区域Store初始化
      expect(areaStore.areaTree).toEqual([]);
      expect(areaStore.areaTreeLoading).toBe(false);
      expect(areaStore.currentArea).toBe(null);

      // 验证设备Store初始化
      expect(deviceStore.deviceList).toEqual([]);
      expect(deviceStore.deviceLoading).toBe(false);
      expect(deviceStore.currentDevice).toBe(null);

      // 验证记录Store初始化
      expect(recordStore.recordList).toEqual([]);
      expect(recordStore.recordLoading).toBe(false);
      expect(recordStore.currentRecord).toBe(null);
    });

    it('WebSocket连接状态应该正确同步', () => {
      // 模拟WebSocket连接
      areaStore.setWsConnected(true);
      deviceStore.setWsConnected(true);
      recordStore.setWsConnected(true);

      expect(areaStore.wsConnected).toBe(true);
      expect(deviceStore.wsConnected).toBe(true);
      expect(recordStore.wsConnected).toBe(true);

      // 模拟WebSocket断开
      areaStore.setWsConnected(false);
      deviceStore.setWsConnected(false);
      recordStore.setWsConnected(false);

      expect(areaStore.wsConnected).toBe(false);
      expect(deviceStore.wsConnected).toBe(false);
      expect(recordStore.wsConnected).toBe(false);
    });
  });

  describe('区域-设备-记录关联测试', () => {
    it('应该正确加载区域及其关联设备', async () => {
      // Mock数据
      const mockAreaTree = [
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

      const mockDevices = [
        {
          deviceId: 1,
          deviceName: '前门读卡器',
          deviceCode: 'FRONT_CARD_001',
          areaId: 2,
          deviceType: 'CARD_READER',
          deviceStatus: 'ONLINE',
        },
        {
          deviceId: 2,
          deviceName: '后门人脸识别',
          deviceCode: 'BACK_FACE_001',
          areaId: 2,
          deviceType: 'FACE_RECOGNITION',
          deviceStatus: 'OFFLINE',
        },
      ];

      // 设置Mock返回值
      accessAreaApi.getAreaTree.mockResolvedValue({
        code: 1,
        data: mockAreaTree,
      });

      accessAreaApi.getAreaDevices.mockResolvedValue({
        code: 1,
        data: mockDevices,
      });

      // 执行操作
      await areaStore.fetchAreaTree();
      await areaStore.fetchAreaDevices(2);

      // 验证结果
      expect(areaStore.areaTree).toEqual(mockAreaTree);
      expect(areaStore.areaDevices).toEqual(mockDevices);
      expect(areaStore.getAreaDevices(2)).toEqual(mockDevices);
    });

    it('应该正确处理设备产生的通行记录', async () => {
      // Mock设备数据
      const mockDevices = [
        {
          deviceId: 1,
          deviceName: '前门读卡器',
          deviceCode: 'FRONT_CARD_001',
          location: '总部大楼1楼',
        },
      ];

      // Mock记录数据
      const mockRecords = [
        {
          recordId: 1,
          userName: '张三',
          deviceName: '前门读卡器',
          deviceCode: 'FRONT_CARD_001',
          location: '总部大楼1楼',
          accessTime: '2025-01-17 09:00:00',
          accessResult: 'success',
        },
        {
          recordId: 2,
          userName: '李四',
          deviceName: '前门读卡器',
          deviceCode: 'FRONT_CARD_001',
          location: '总部大楼1楼',
          accessTime: '2025-01-17 09:05:00',
          accessResult: 'success',
        },
      ];

      accessDeviceApi.getDeviceList.mockResolvedValue({
        code: 1,
        data: { list: mockDevices, total: 1 },
      });

      accessRecordApi.getRecordList.mockResolvedValue({
        code: 1,
        data: { list: mockRecords, total: 2 },
      });

      // 执行操作
      await deviceStore.fetchDeviceList();
      await recordStore.fetchRecordList({ deviceId: 1 });

      // 验证设备和记录关联
      expect(deviceStore.deviceList).toEqual(mockDevices);
      expect(recordStore.recordList).toEqual(mockRecords);
      expect(recordStore.recordList.every(record => record.deviceCode === 'FRONT_CARD_001')).toBe(true);
    });
  });

  describe('数据一致性测试', () => {
    it('删除区域时应该同步清理相关数据', async () => {
      // 设置初始数据
      areaStore.areaList = [
        { areaId: 1, areaName: '测试区域', status: 1 },
      ];
      areaStore.currentArea = { areaId: 1, areaName: '测试区域' };
      areaStore.selectedAreaId = 1;

      deviceStore.deviceList = [
        { deviceId: 1, deviceName: '测试设备', areaId: 1 },
      ];

      // Mock删除操作
      accessAreaApi.deleteArea.mockResolvedValue({ code: 1 });
      areaStore.fetchAreaList = vi.fn().mockResolvedValue([]);
      deviceStore.fetchDeviceList = vi.fn().mockResolvedValue([]);

      // 执行删除
      await areaStore.deleteArea(1);

      // 验证数据清理
      expect(areaStore.currentArea).toBe(null);
      expect(areaStore.selectedAreaId).toBe(null);
    });

    it('设备状态变更应该影响记录统计', async () => {
      // 设置设备数据
      const mockDevices = [
        { deviceId: 1, deviceName: '设备1', deviceStatus: 'ONLINE' },
        { deviceId: 2, deviceName: '设备2', deviceStatus: 'OFFLINE' },
        { deviceId: 3, deviceName: '设备3', deviceStatus: 'FAULT' },
      ];

      deviceStore.deviceList = mockDevices;

      // 设置记录数据
      const mockRecords = [
        { recordId: 1, deviceName: '设备1', accessResult: 'success' },
        { recordId: 2, deviceName: '设备2', accessResult: 'failed' },
        { recordId: 3, deviceName: '设备3', accessResult: 'failed' },
      ];

      recordStore.recordList = mockRecords;

      // 验证统计数据
      expect(deviceStore.totalDeviceCount).toBe(3);
      expect(deviceStore.onlineDeviceCount).toBe(1);
      expect(deviceStore.offlineDeviceCount).toBe(1);
      expect(deviceStore.faultDeviceCount).toBe(1);

      expect(recordStore.totalRecordCount).toBe(3);
      expect(recordStore.successRecordCount).toBe(1);
      expect(recordStore.failedRecordCount).toBe(2);
    });
  });

  describe('业务流程集成测试', () => {
    it('应该完整执行区域管理流程', async () => {
      // 1. 添加区域
      const newArea = {
        areaName: '新区域',
        areaCode: 'NEW_AREA',
        areaType: 'AREA',
      };

      accessAreaApi.addArea.mockResolvedValue({ code: 1 });
      accessAreaApi.getAreaTree.mockResolvedValue({
        code: 1,
        data: [{ areaId: 1, ...newArea }],
      });
      accessAreaApi.getAreaList.mockResolvedValue({
        code: 1,
        data: { list: [{ areaId: 1, ...newArea }], total: 1 },
      });

      const addResult = await areaStore.addArea(newArea);
      expect(addResult).toBe(true);

      // 2. 获取区域详情
      accessAreaApi.getAreaDetail.mockResolvedValue({
        code: 1,
        data: { areaId: 1, ...newArea },
      });

      const detailResult = await areaStore.fetchAreaDetail(1);
      expect(detailResult.areaId).toBe(1);

      // 3. 更新区域
      const updateArea = { areaId: 1, areaName: '更新区域' };
      accessAreaApi.updateArea.mockResolvedValue({ code: 1 });

      const updateResult = await areaStore.updateArea(updateArea);
      expect(updateResult).toBe(true);

      // 4. 删除区域
      accessAreaApi.deleteArea.mockResolvedValue({ code: 1 });

      const deleteResult = await areaStore.deleteArea(1);
      expect(deleteResult).toBe(true);
    });

    it('应该完整执行设备管理流程', async () => {
      // 1. 添加设备
      const newDevice = {
        deviceName: '新设备',
        deviceCode: 'NEW_DEVICE_001',
        deviceType: 'CARD_READER',
      };

      accessDeviceApi.addDevice.mockResolvedValue({ code: 1 });
      accessDeviceApi.getDeviceList.mockResolvedValue({
        code: 1,
        data: { list: [{ deviceId: 1, ...newDevice }], total: 1 },
      });

      const addResult = await deviceStore.addDevice(newDevice);
      expect(addResult).toBe(true);

      // 2. 设备远程控制
      accessDeviceApi.controlDevice.mockResolvedValue({ code: 1 });

      const controlResult = await deviceStore.controlDevice(1, 'open');
      expect(controlResult).toBe(true);

      // 3. 获取设备状态
      accessDeviceApi.getDeviceStatus.mockResolvedValue({
        code: 1,
        data: { deviceId: 1, deviceStatus: 'ONLINE' },
      });

      const statusResult = await deviceStore.fetchDeviceStatus(1);
      expect(statusResult.deviceStatus).toBe('ONLINE');

      // 4. 删除设备
      accessDeviceApi.deleteDevice.mockResolvedValue({ code: 1 });

      const deleteResult = await deviceStore.deleteDevice(1);
      expect(deleteResult).toBe(true);
    });

    it('应该完整执行记录管理流程', async () => {
      // 1. 获取记录列表
      const mockRecords = [
        {
          recordId: 1,
          userName: '张三',
          accessResult: 'success',
          accessTime: '2025-01-17 09:00:00',
        },
        {
          recordId: 2,
          userName: '李四',
          accessResult: 'failed',
          accessTime: '2025-01-17 09:05:00',
        },
      ];

      accessRecordApi.getRecordList.mockResolvedValue({
        code: 1,
        data: { list: mockRecords, total: 2 },
      });

      await recordStore.fetchRecordList();
      expect(recordStore.recordList).toHaveLength(2);

      // 2. 获取记录统计
      const mockStats = {
        totalCount: 100,
        successCount: 95,
        failedCount: 5,
        successRate: 95.0,
      };

      accessRecordApi.getRecordStats.mockResolvedValue({
        code: 1,
        data: mockStats,
      });

      const statsResult = await recordStore.fetchRecordStats();
      expect(statsResult.totalCount).toBe(100);
      expect(statsResult.successRate).toBe(95.0);

      // 3. 处理异常记录
      const processResult = await recordStore.handleAbnormalRecord({
        recordId: 2,
        action: 'resolve',
        remark: '处理异常记录',
      });

      expect(processResult).toBe(true);

      // 4. 导出记录
      accessRecordApi.exportRecords.mockResolvedValue({
        code: 1,
        data: {
          downloadUrl: 'https://example.com/export.xlsx',
          fileName: 'records.xlsx',
        },
      });

      const exportResult = await recordStore.exportRecords({ format: 'excel' });
      expect(exportResult.fileName).toBe('records.xlsx');
    });
  });

  describe('性能和错误处理测试', () => {
    it('应该正确处理并发请求', async () => {
      // 并发获取区域列表和设备列表
      const areaPromise = areaStore.fetchAreaList();
      const devicePromise = deviceStore.fetchDeviceList();
      const recordPromise = recordStore.fetchRecordList();

      accessAreaApi.getAreaList.mockResolvedValue({
        code: 1,
        data: { list: [], total: 0 },
      });

      accessDeviceApi.getDeviceList.mockResolvedValue({
        code: 1,
        data: { list: [], total: 0 },
      });

      accessRecordApi.getRecordList.mockResolvedValue({
        code: 1,
        data: { list: [], total: 0 },
      });

      await Promise.all([areaPromise, devicePromise, recordPromise]);

      expect(areaStore.areaLoading).toBe(false);
      expect(deviceStore.deviceLoading).toBe(false);
      expect(recordStore.recordLoading).toBe(false);
    });

    it('应该正确处理API错误', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      // Mock API错误
      accessAreaApi.getAreaList.mockRejectedValue(new Error('Network error'));
      accessDeviceApi.getDeviceList.mockRejectedValue(new Error('Network error'));
      accessRecordApi.getRecordList.mockRejectedValue(new Error('Network error'));

      const areaResult = await areaStore.fetchAreaList();
      const deviceResult = await deviceStore.fetchDeviceList();
      const recordResult = await recordStore.fetchRecordList();

      expect(areaResult).toBe(null);
      expect(deviceResult).toBe(null);
      expect(recordResult).toBe(null);
      expect(consoleErrorSpy).toHaveBeenCalledTimes(3);

      consoleErrorSpy.mockRestore();
    });

    it('应该正确清理状态', () => {
      // 设置测试数据
      areaStore.areaList = [{ areaId: 1 }];
      areaStore.currentArea = { areaId: 1 };
      areaStore.selectedAreaId = 1;

      deviceStore.deviceList = [{ deviceId: 1 }];
      deviceStore.currentDevice = { deviceId: 1 };
      deviceStore.selectedDeviceId = 1;

      recordStore.recordList = [{ recordId: 1 }];
      recordStore.currentRecord = { recordId: 1 };
      recordStore.selectedRecordId = 1;

      // 清理所有状态
      areaStore.clearAreaData();
      deviceStore.clearDeviceData();
      recordStore.clearRecordData();

      // 验证清理结果
      expect(areaStore.areaList).toEqual([]);
      expect(areaStore.currentArea).toBe(null);
      expect(areaStore.selectedAreaId).toBe(null);

      expect(deviceStore.deviceList).toEqual([]);
      expect(deviceStore.currentDevice).toBe(null);
      expect(deviceStore.selectedDeviceId).toBe(null);

      expect(recordStore.recordList).toEqual([]);
      expect(recordStore.currentRecord).toBe(null);
      expect(recordStore.selectedRecordId).toBe(null);
    });
  });

  describe('数据验证和业务规则测试', () => {
    it('应该正确验证区域编码唯一性', async () => {
      // Mock验证成功
      accessAreaApi.validateAreaCode.mockResolvedValue({
        code: 1,
        data: { valid: true, message: '编码可用' },
      });

      const validResult = await areaStore.validateAreaCode('NEW_AREA');
      expect(validResult.valid).toBe(true);

      // Mock验证失败
      accessAreaApi.validateAreaCode.mockResolvedValue({
        code: 0,
        msg: '编码已存在',
      });

      const invalidResult = await areaStore.validateAreaCode('EXIST_AREA');
      expect(invalidResult.valid).toBe(false);
      expect(invalidResult.message).toBe('编码已存在');
    });

    it('应该正确计算统计数据', () => {
      // 设置测试数据
      areaStore.areaList = [
        { areaId: 1, status: 1, isDefault: true },
        { areaId: 2, status: 1 },
        { areaId: 3, status: 0 },
      ];

      deviceStore.deviceList = [
        { deviceId: 1, deviceStatus: 'ONLINE' },
        { deviceId: 2, deviceStatus: 'OFFLINE' },
        { deviceId: 3, deviceStatus: 'FAULT' },
      ];

      recordStore.recordList = [
        { recordId: 1, accessResult: 'success' },
        { recordId: 2, accessResult: 'success' },
        { recordId: 3, accessResult: 'failed' },
      ];

      // 验证区域统计
      expect(areaStore.totalAreaCount).toBe(3);
      expect(areaStore.enabledAreaCount).toBe(2);
      expect(areaStore.disabledAreaCount).toBe(1);
      expect(areaStore.defaultArea.areaId).toBe(1);

      // 验证设备统计
      expect(deviceStore.totalDeviceCount).toBe(3);
      expect(deviceStore.onlineDeviceCount).toBe(1);
      expect(deviceStore.offlineDeviceCount).toBe(1);
      expect(deviceStore.faultDeviceCount).toBe(1);

      // 验证记录统计
      expect(recordStore.totalRecordCount).toBe(3);
      expect(recordStore.successRecordCount).toBe(2);
      expect(recordStore.failedRecordCount).toBe(1);
      expect(recordStore.successRate).toBe(66.67);
    });
  });
});