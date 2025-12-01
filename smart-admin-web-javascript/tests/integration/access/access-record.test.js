/**
 * 门禁记录管理前端集成测试
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-17
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { accessRecordApi } from '/@/api/business/access/record-api';
import { useAccessRecordStore } from '/@/store/modules/business/access-record';

// Mock API
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

describe('门禁记录管理集成测试', () => {
  let store;
  let pinia;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);
    store = useAccessRecordStore();
    vi.clearAllMocks();
  });

  afterEach(() => {
    pinia = null;
    store = null;
  });

  describe('状态管理初始化', () => {
    it('应该正确初始化状态', () => {
      expect(store.recordList).toEqual([]);
      expect(store.recordLoading).toBe(false);
      expect(store.recordTotal).toBe(0);
      expect(store.currentRecord).toBe(null);
      expect(store.selectedRecordId).toBe(null);
      expect(store.recordStats).toBe(null);
      expect(store.abnormalRecords).toEqual([]);
      expect(store.heatmapData).toBe(null);
      expect(store.wsConnected).toBe(false);
    });

    it('应该正确初始化记录类型配置', () => {
      expect(store.recordTypes).toEqual([
        { value: 'in', label: '进入', color: 'green' },
        { value: 'out', label: '离开', color: 'blue' },
        { value: 'pass', label: '通过', color: 'orange' },
      ]);
    });

    it('应该正确初始化通行结果配置', () => {
      expect(store.accessResults).toEqual([
        { value: 'success', label: '成功', color: 'green' },
        { value: 'failed', label: '失败', color: 'red' },
        { value: 'denied', label: '拒绝', color: 'orange' },
        { value: 'timeout', label: '超时', color: 'default' },
      ]);
    });
  });

  describe('记录列表管理', () => {
    it('应该成功获取记录列表', async () => {
      const mockRecordList = {
        list: [
          {
            recordId: 1,
            userName: '张三',
            userCardNumber: '100001',
            deviceName: '前门读卡器',
            location: '总部大楼1楼',
            accessTime: '2025-01-17 09:00:00',
            accessType: 'in',
            accessResult: 'success',
            verificationMethod: 'card_only',
          },
          {
            recordId: 2,
            userName: '李四',
            userCardNumber: '100002',
            deviceName: '后门人脸识别',
            location: '总部大楼2楼',
            accessTime: '2025-01-17 09:15:00',
            accessType: 'out',
            accessResult: 'failed',
            verificationMethod: 'face_only',
          },
        ],
        total: 2,
      };

      accessRecordApi.getRecordList.mockResolvedValue({
        code: 1,
        data: mockRecordList,
      });

      const queryParams = { pageNum: 1, pageSize: 20 };
      const result = await store.fetchRecordList(queryParams);

      expect(accessRecordApi.getRecordList).toHaveBeenCalledWith(queryParams);
      expect(store.recordList).toEqual(mockRecordList.list);
      expect(store.recordTotal).toBe(mockRecordList.total);
      expect(store.queryParams).toEqual(queryParams);
      expect(result).toEqual(mockRecordList);
    });

    it('获取记录列表失败时应该保持空列表', async () => {
      accessRecordApi.getRecordList.mockResolvedValue({
        code: 0,
        msg: '获取失败',
      });

      const result = await store.fetchRecordList();

      expect(store.recordList).toEqual([]);
      expect(store.recordTotal).toBe(0);
      expect(result).toBe(null);
    });
  });

  describe('记录详情管理', () => {
    it('应该成功获取记录详情', async () => {
      const mockRecordDetail = {
        recordId: 1,
        userName: '张三',
        userId: 1001,
        userCardNumber: '100001',
        deviceName: '前门读卡器',
        deviceCode: 'FRONT_CARD_001',
        location: '总部大楼1楼',
        accessTime: '2025-01-17 09:00:00',
        accessType: 'in',
        accessResult: 'success',
        verificationMethod: 'card_only',
        verificationSuccess: true,
        verificationDuration: 150,
        verificationScore: 95,
        photoUrl: 'https://example.com/photo1.jpg',
        departmentName: '技术部',
        position: '软件工程师',
        phoneNumber: '13800138001',
        email: 'zhangsan@example.com',
        validityPeriod: '2025-12-31',
        deviceType: 'CARD_READER',
        deviceIp: '192.168.1.100',
        deviceOnlineStatus: true,
        isAbnormal: false,
        temperature: 36.5,
        maskDetected: true,
        todayAccessCount: 3,
        duration: 30,
        dataSource: '设备采集',
        syncStatus: 'synced',
      };

      accessRecordApi.getRecordDetail.mockResolvedValue({
        code: 1,
        data: mockRecordDetail,
      });

      const result = await store.fetchRecordDetail(1);

      expect(accessRecordApi.getRecordDetail).toHaveBeenCalledWith(1);
      expect(store.currentRecord).toEqual(mockRecordDetail);
      expect(result).toEqual(mockRecordDetail);
    });

    it('获取记录详情失败时应该清空当前记录', async () => {
      accessRecordApi.getRecordDetail.mockResolvedValue({
        code: 0,
        msg: '记录不存在',
      });

      const result = await store.fetchRecordDetail(999);

      expect(store.currentRecord).toBe(null);
      expect(result).toBe(null);
    });
  });

  describe('记录统计管理', () => {
    it('应该成功获取记录统计', async () => {
      const mockRecordStats = {
        totalCount: 1000,
        successCount: 950,
        failedCount: 30,
        deniedCount: 15,
        timeoutCount: 5,
        successRate: 95.0,
        abnormalCount: 10,
        activeUsers: 200,
        todayCount: 150,
        peakHour: '09:00',
        avgVerificationTime: 180,
        deviceStats: [
          { deviceName: '前门读卡器', totalCount: 500, successCount: 480 },
          { deviceName: '后门人脸识别', totalCount: 300, successCount: 285 },
          { deviceName: '侧门指纹识别', totalCount: 200, successCount: 185 },
        ],
        userStats: [
          { userName: '张三', totalCount: 25, successCount: 24 },
          { userName: '李四', totalCount: 20, successCount: 19 },
        ],
        verificationData: [
          { method: 'card_only', count: 600 },
          { method: 'face_only', count: 250 },
          { method: 'fingerprint_only', count: 150 },
        ],
        trendData: [
          { time: '2025-01-17 08:00', successCount: 45, failedCount: 2 },
          { time: '2025-01-17 09:00', successCount: 120, failedCount: 8 },
          { time: '2025-01-17 10:00', successCount: 85, failedCount: 3 },
        ],
        hourlyData: [
          { hour: '08:00', count: 50 },
          { hour: '09:00', count: 130 },
          { hour: '10:00', count: 90 },
          { hour: '11:00', count: 75 },
        ],
      };

      accessRecordApi.getRecordStats.mockResolvedValue({
        code: 1,
        data: mockRecordStats,
      });

      const result = await store.fetchRecordStats();

      expect(accessRecordApi.getRecordStats).toHaveBeenCalled();
      expect(store.recordStats).toEqual(mockRecordStats);
      expect(result).toEqual(mockRecordStats);
    });
  });

  describe('异常记录处理', () => {
    it('应该成功处理异常记录', async () => {
      const processData = {
        recordId: 1,
        action: 'resolve',
        result: 'verified',
        remark: '卡片已更新，验证通过',
        processTime: '2025-01-17 10:30:00',
        notifications: ['user', 'manager'],
      };

      accessRecordApi.handleAbnormalRecord.mockResolvedValue({
        code: 1,
        msg: '处理成功',
      });

      store.fetchRecordList = vi.fn().mockResolvedValue([]);

      const result = await store.handleAbnormalRecord(processData);

      expect(accessRecordApi.handleAbnormalRecord).toHaveBeenCalledWith(processData);
      expect(store.fetchRecordList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });

    it('应该成功批量处理异常记录', async () => {
      const batchProcessData = {
        recordIds: [1, 2, 3],
        action: 'resolve',
        result: 'verified',
        remark: '批量处理验证通过',
      };

      accessRecordApi.batchProcessAbnormalRecords.mockResolvedValue({
        code: 1,
        msg: '批量处理成功',
      });

      store.fetchRecordList = vi.fn().mockResolvedValue([]);

      const result = await store.batchProcessAbnormalRecords(batchProcessData);

      expect(accessRecordApi.batchProcessAbnormalRecords).toHaveBeenCalledWith(batchProcessData);
      expect(store.fetchRecordList).toHaveBeenCalledTimes(1);
      expect(result).toBe(true);
    });
  });

  describe('热力图数据管理', () => {
    it('应该成功获取热力图数据', async () => {
      const mockHeatmapData = {
        statistics: {
          totalCount: 1500,
          peakTime: '09:00',
          activeLocation: '总部大楼1楼',
          avgDensity: 25.5,
          coverageRate: 85.2,
          activeDays: 20,
        },
        heatmapData: {
          xAxis: ['08:00', '09:00', '10:00', '11:00', '12:00'],
          yAxis: ['1楼', '2楼', '3楼', '4楼'],
          maxValue: 100,
          data: [
            [0, 0, 15], [1, 0, 45], [2, 0, 85], [3, 0, 25], [4, 0, 10],
            [0, 1, 8], [1, 1, 35], [2, 1, 65], [3, 1, 18], [4, 1, 5],
          ],
        },
        hotspotData: [
          {
            rank: 1,
            location: '总部大楼1楼前门',
            count: 450,
            density: 90,
            peakHour: '09:00',
            mainUsers: '张三, 李四, 王五',
          },
          {
            rank: 2,
            location: '总部大楼2楼后门',
            count: 320,
            density: 75,
            peakHour: '12:00',
            mainUsers: '赵六, 钱七',
          },
        ],
        trendData: [
          { time: '2025-01-01', count: 800 },
          { time: '2025-01-02', count: 950 },
          { time: '2025-01-03', count: 1100 },
        ],
        comparisonData: {
          categories: ['周一', '周二', '周三', '周四', '周五'],
          series: [
            { name: '本周', data: [800, 950, 1100, 900, 1200] },
            { name: '上周', data: [750, 900, 1000, 850, 1100] },
          ],
        },
        distributionData: [
          { name: '工作日', value: 4200 },
          { name: '周末', value: 800 },
          { name: '节假日', value: 300 },
        ],
      };

      accessRecordApi.getHeatmapData.mockResolvedValue({
        code: 1,
        data: mockHeatmapData,
      });

      const params = { type: 'time_location', granularity: 'hour' };
      const result = await store.fetchHeatmapData(params);

      expect(accessRecordApi.getHeatmapData).toHaveBeenCalledWith(params);
      expect(store.heatmapData).toEqual(mockHeatmapData);
      expect(result).toEqual(mockHeatmapData);
    });
  });

  describe('记录导出功能', () => {
    it('应该成功导出记录', async () => {
      const exportParams = {
        format: 'excel',
        startTime: '2025-01-01 00:00:00',
        endTime: '2025-01-17 23:59:59',
      };

      const mockExportResult = {
        code: 1,
        data: {
          downloadUrl: 'https://example.com/export/records_20250117.xlsx',
          fileName: '通行记录_20250117.xlsx',
        },
      };

      accessRecordApi.exportRecords.mockResolvedValue(mockExportResult);

      const result = await store.exportRecords(exportParams);

      expect(accessRecordApi.exportRecords).toHaveBeenCalledWith(exportParams);
      expect(result).toEqual(mockExportResult.data);
    });
  });

  describe('Getter计算属性', () => {
    beforeEach(() => {
      store.recordList = [
        { recordId: 1, accessResult: 'success', accessType: 'in', verificationMethod: 'card_only' },
        { recordId: 2, accessResult: 'failed', accessType: 'out', verificationMethod: 'face_only' },
        { recordId: 3, accessResult: 'success', accessType: 'in', verificationMethod: 'fingerprint_only' },
        { recordId: 4, accessResult: 'denied', accessType: 'pass', verificationMethod: 'card_password' },
      ];
    });

    it('totalRecordCount应该返回正确的记录总数', () => {
      expect(store.totalRecordCount).toBe(4);
    });

    it('successRecordCount应该返回成功记录数量', () => {
      expect(store.successRecordCount).toBe(2);
    });

    it('failedRecordCount应该返回失败记录数量', () => {
      expect(store.failedRecordCount).toBe(1);
    });

    it('deniedRecordCount应该返回拒绝记录数量', () => {
      expect(store.deniedRecordCount).toBe(1);
    });

    it('successRate应该返回正确的成功率', () => {
      expect(store.successRate).toBe(50); // 2/4 * 100
    });

    it('getRecordById应该根据ID获取记录', () => {
      const record = store.getRecordById(3);
      expect(record).toEqual({
        recordId: 3,
        accessResult: 'success',
        accessType: 'in',
        verificationMethod: 'fingerprint_only',
      });
    });

    it('getRecordsByType应该根据类型获取记录', () => {
      const inRecords = store.getRecordsByType('in');
      expect(inRecords).toHaveLength(2);
      expect(inRecords[0].accessType).toBe('in');
    });

    it('getRecordsByResult应该根据结果获取记录', () => {
      const successRecords = store.getRecordsByResult('success');
      expect(successRecords).toHaveLength(2);
      expect(successRecords[0].accessResult).toBe('success');
    });
  });

  describe('工具方法', () => {
    it('setCurrentRecord应该正确设置当前记录', () => {
      const record = { recordId: 1, userName: '测试用户' };

      store.setCurrentRecord(record);

      expect(store.currentRecord).toEqual(record);
      expect(store.selectedRecordId).toBe(1);
    });

    it('setSelectedRecordId应该正确设置选中记录ID', () => {
      store.recordList = [
        { recordId: 1, userName: '用户1' },
        { recordId: 2, userName: '用户2' },
      ];

      store.setSelectedRecordId(2);

      expect(store.selectedRecordId).toBe(2);
      expect(store.currentRecord).toEqual({ recordId: 2, userName: '用户2' });
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

    it('clearRecordData应该清空记录数据', () => {
      store.recordList = [{ recordId: 1 }];
      store.recordTotal = 100;
      store.currentRecord = { recordId: 1 };
      store.selectedRecordId = 1;
      store.recordStats = { totalCount: 100 };
      store.heatmapData = { data: [] };

      store.clearRecordData();

      expect(store.recordList).toEqual([]);
      expect(store.recordTotal).toBe(0);
      expect(store.currentRecord).toBe(null);
      expect(store.selectedRecordId).toBe(null);
      expect(store.recordStats).toBe(null);
      expect(store.heatmapData).toBe(null);
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
      accessRecordApi.getRecordList.mockRejectedValue(new Error('Network error'));

      const result = await store.fetchRecordList();

      expect(consoleErrorSpy).toHaveBeenCalledWith('获取记录列表失败:', expect.any(Error));
      expect(result).toBe(null);

      consoleErrorSpy.mockRestore();
    });
  });
});