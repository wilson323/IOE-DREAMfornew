/**
 * 门禁设备管理前端集成测试
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-17
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { accessDeviceApi } from '/@/api/business/access/device-api';
import { useAccessDeviceStore } from '/@/store/modules/business/access-device';

// Mock API
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

describe('门禁设备管理集成测试', () => {
  let store;
  let pinia;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);
    store = useAccessDeviceStore();
    vi.clearAllMocks();
  });

  afterEach(() => {
    pinia = null;
    store = null;
  });

  describe('状态管理初始化', () => {
    it('应该正确初始化状态', () => {
      expect(store.deviceList).toEqual([]);
      expect(store.deviceLoading).toBe(false);
      expect(store.deviceTotal).toBe(0);
      expect(store.currentDevice).toBe(null);
      expect(store.selectedDeviceId).toBe(null);
      expect(store.deviceTypes).toEqual([]);
      expect(store.deviceStats).toBe(null);
      expect(store.wsConnected).toBe(false);
    });
  });

  describe('设备列表管理', () => {
    it('应该成功获取设备列表', async () => {
      const mockDeviceList = {
        list: [
          {
            deviceId: 1,
            deviceName: '前门读卡器',
            deviceCode: 'FRONT_CARD_001',
            deviceType: 'CARD_READER',
            deviceStatus: 'ONLINE',
            location: '总部大楼1楼',
          },
          {
            deviceId: 2,
            deviceName: '后门人脸识别',
            deviceCode: 'BACK_FACE_001',
            deviceType: 'FACE_RECOGNITION',
            deviceStatus: 'OFFLINE',
            location: '总部大楼1楼',
          },
        ],
        total: 2,
      };

      accessDeviceApi.getDeviceList.mockResolvedValue({
        code: 1,
        data: mockDeviceList,
      });

      const queryParams = { pageNum: 1, pageSize: 20 };
      const result = await store.fetchDeviceList(queryParams);

      expect(accessDeviceApi.getDeviceList).toHaveBeenCalledWith(queryParams);
      expect(store.deviceList).toEqual(mockDeviceList.list);
      expect(store.deviceTotal).toBe(mockDeviceList.total);
      expect(result).toEqual(mockDeviceList);
    });

    it('获取设备列表失败时应该保持空列表', async () => {
      accessDeviceApi.getDeviceList.mockResolvedValue({
        code: 0,
        msg: '获取失败',
      });

      const result = await store.fetchDeviceList();

      expect(store.deviceList).toEqual([]);
      expect(store.deviceTotal).toBe(0);
      expect(result).toBe(null);
    });
  });

  describe('设备CRUD操作', () => {
    it('应该成功添加设备', async () => {
      const newDevice = {
        deviceName: '新设备',
        deviceCode: 'NEW_DEVICE_001',
        deviceType: 'CARD_READER',
        location: '测试位置',
      };

      accessDeviceApi.addDevice.mockResolvedValue({
        code: 1,
        msg: '添加成功',
      });

      store.fetchDeviceList = vi.fn().mockResolvedValue([]);

      const result = await store.addDevice(newDevice);

      expect(accessDeviceApi.addDevice).toHaveBeenCalledWith(newDevice);
      expect(store.fetchDeviceList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功更新设备', async () => {
      const updateDevice = {
        deviceId: 1,
        deviceName: '更新设备',
        deviceCode: 'UPDATE_DEVICE_001',
      };

      accessDeviceApi.updateDevice.mockResolvedValue({
        code: 1,
        msg: '更新成功',
      });

      store.fetchDeviceList = vi.fn().mockResolvedValue([]);

      const result = await store.updateDevice(updateDevice);

      expect(accessDeviceApi.updateDevice).toHaveBeenCalledWith(updateDevice);
      expect(store.fetchDeviceList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功删除设备', async () => {
      const deviceId = 1;

      accessDeviceApi.deleteDevice.mockResolvedValue({
        code: 1,
        msg: '删除成功',
      });

      store.fetchDeviceList = vi.fn().mockResolvedValue([]);

      const result = await store.deleteDevice(deviceId);

      expect(accessDeviceApi.deleteDevice).toHaveBeenCalledWith(deviceId);
      expect(store.fetchDeviceList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功批量删除设备', async () => {
      const deviceIds = [1, 2, 3];

      accessDeviceApi.batchDeleteDevices.mockResolvedValue({
        code: 1,
        msg: '批量删除成功',
      });

      store.fetchDeviceList = vi.fn().mockResolvedValue([]);

      const result = await store.batchDeleteDevices(deviceIds);

      expect(accessDeviceApi.batchDeleteDevices).toHaveBeenCalledWith(deviceIds);
      expect(store.fetchDeviceList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });
  });

  describe('设备控制操作', () => {
    it('应该成功远程开门', async () => {
      const deviceId = 1;

      accessDeviceApi.controlDevice.mockResolvedValue({
        code: 1,
        msg: '开门成功',
      });

      const result = await store.controlDevice(deviceId, 'open');

      expect(accessDeviceApi.controlDevice).toHaveBeenCalledWith(deviceId, 'open');
      expect(result).toBe(true);
    });

    it('应该成功重启设备', async () => {
      const deviceId = 1;

      accessDeviceApi.restartDevice.mockResolvedValue({
        code: 1,
        msg: '重启成功',
      });

      const result = await store.restartDevice(deviceId);

      expect(accessDeviceApi.restartDevice).toHaveBeenCalledWith(deviceId);
      expect(result).toBe(true);
    });

    it('应该成功同步设备时间', async () => {
      const deviceId = 1;

      accessDeviceApi.syncDeviceTime.mockResolvedValue({
        code: 1,
        msg: '同步成功',
      });

      const result = await store.syncDeviceTime(deviceId);

      expect(accessDeviceApi.syncDeviceTime).toHaveBeenCalledWith(deviceId);
      expect(result).toBe(true);
    });
  });

  describe('设备状态和统计', () => {
    it('应该成功获取设备状态', async () => {
      const mockDeviceStatus = {
        deviceId: 1,
        deviceStatus: 'ONLINE',
        lastHeartbeat: '2025-01-17 10:30:00',
        batteryLevel: 85,
        signalStrength: 4,
      };

      accessDeviceApi.getDeviceStatus.mockResolvedValue({
        code: 1,
        data: mockDeviceStatus,
      });

      const result = await store.fetchDeviceStatus(1);

      expect(accessDeviceApi.getDeviceStatus).toHaveBeenCalledWith(1);
      expect(result).toEqual(mockDeviceStatus);
    });

    it('应该成功获取设备统计', async () => {
      const mockDeviceStats = {
        totalDevices: 100,
        onlineDevices: 85,
        offlineDevices: 15,
        faultDevices: 3,
        deviceTypeStats: [
          { deviceType: 'CARD_READER', count: 40 },
          { deviceType: 'FACE_RECOGNITION', count: 30 },
          { deviceType: 'FINGERPRINT', count: 30 },
        ],
      };

      accessDeviceApi.getDeviceStats.mockResolvedValue({
        code: 1,
        data: mockDeviceStats,
      });

      const result = await store.fetchDeviceStats();

      expect(accessDeviceApi.getDeviceStats).toHaveBeenCalled();
      expect(store.deviceStats).toEqual(mockDeviceStats);
      expect(result).toEqual(mockDeviceStats);
    });
  });

  describe('设备类型管理', () => {
    it('应该成功获取设备类型', async () => {
      const mockDeviceTypes = [
        { value: 'CARD_READER', label: '读卡器', description: 'IC/ID卡读卡设备' },
        { value: 'FACE_RECOGNITION', label: '人脸识别', description: '人脸识别设备' },
        { value: 'FINGERPRINT', label: '指纹识别', description: '指纹识别设备' },
      ];

      accessDeviceApi.getDeviceTypes.mockResolvedValue({
        code: 1,
        data: mockDeviceTypes,
      });

      const result = await store.fetchDeviceTypes();

      expect(accessDeviceApi.getDeviceTypes).toHaveBeenCalled();
      expect(store.deviceTypes).toEqual(mockDeviceTypes);
      expect(result).toEqual(mockDeviceTypes);
    });
  });

  describe('设备日志管理', () => {
    it('应该成功获取设备日志', async () => {
      const deviceId = 1;
      const mockDeviceLogs = [
        {
          logId: 1,
          deviceId: 1,
          logType: 'ACCESS',
          logTime: '2025-01-17 10:30:00',
          logContent: '用户张三通过验证',
          userId: 1001,
          userName: '张三',
        },
        {
          logId: 2,
          deviceId: 1,
          logType: 'SYSTEM',
          logTime: '2025-01-17 10:25:00',
          logContent: '设备启动完成',
        },
      ];

      accessDeviceApi.getDeviceLogs.mockResolvedValue({
        code: 1,
        data: mockDeviceLogs,
      });

      const result = await store.fetchDeviceLogs(deviceId);

      expect(accessDeviceApi.getDeviceLogs).toHaveBeenCalledWith(deviceId);
      expect(result).toEqual(mockDeviceLogs);
    });
  });

  describe('Getter计算属性', () => {
    beforeEach(() => {
      store.deviceList = [
        { deviceId: 1, deviceName: '设备1', deviceStatus: 'ONLINE', deviceType: 'CARD_READER' },
        { deviceId: 2, deviceName: '设备2', deviceStatus: 'OFFLINE', deviceType: 'FACE_RECOGNITION' },
        { deviceId: 3, deviceName: '设备3', deviceStatus: 'FAULT', deviceType: 'FINGERPRINT' },
      ];
    });

    it('totalDeviceCount应该返回正确的设备总数', () => {
      expect(store.totalDeviceCount).toBe(3);
    });

    it('onlineDeviceCount应该返回在线设备数量', () => {
      expect(store.onlineDeviceCount).toBe(1);
    });

    it('offlineDeviceCount应该返回离线设备数量', () => {
      expect(store.offlineDeviceCount).toBe(1);
    });

    it('faultDeviceCount应该返回故障设备数量', () => {
      expect(store.faultDeviceCount).toBe(1);
    });

    it('getDeviceById应该根据ID获取设备', () => {
      const device = store.getDeviceById(2);
      expect(device).toEqual({
        deviceId: 2,
        deviceName: '设备2',
        deviceStatus: 'OFFLINE',
        deviceType: 'FACE_RECOGNITION',
      });
    });

    it('getDevicesByType应该根据类型获取设备', () => {
      const cardReaders = store.getDevicesByType('CARD_READER');
      expect(cardReaders).toHaveLength(1);
      expect(cardReaders[0].deviceName).toBe('设备1');
    });
  });

  describe('工具方法', () => {
    it('setCurrentDevice应该正确设置当前设备', () => {
      const device = { deviceId: 1, deviceName: '测试设备' };

      store.setCurrentDevice(device);

      expect(store.currentDevice).toEqual(device);
      expect(store.selectedDeviceId).toBe(1);
    });

    it('setSelectedDeviceId应该正确设置选中设备ID', () => {
      store.deviceList = [
        { deviceId: 1, deviceName: '设备1' },
        { deviceId: 2, deviceName: '设备2' },
      ];

      store.setSelectedDeviceId(2);

      expect(store.selectedDeviceId).toBe(2);
      expect(store.currentDevice).toEqual({ deviceId: 2, deviceName: '设备2' });
    });

    it('clearDeviceData应该清空设备数据', () => {
      store.deviceList = [{ deviceId: 1 }];
      store.deviceTotal = 10;
      store.currentDevice = { deviceId: 1 };
      store.selectedDeviceId = 1;
      store.deviceStats = { totalDevices: 10 };

      store.clearDeviceData();

      expect(store.deviceList).toEqual([]);
      expect(store.deviceTotal).toBe(0);
      expect(store.currentDevice).toBe(null);
      expect(store.selectedDeviceId).toBe(null);
      expect(store.deviceStats).toBe(null);
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

  describe('错误处理', () => {
    it('API调用失败时应该处理异常', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      accessDeviceApi.getDeviceList.mockRejectedValue(new Error('Network error'));

      const result = await store.fetchDeviceList();

      expect(consoleErrorSpy).toHaveBeenCalledWith('获取设备列表失败:', expect.any(Error));
      expect(result).toBe(null);

      consoleErrorSpy.mockRestore();
    });
  });
});