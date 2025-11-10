/*
 * 解码器管理模块模拟数据
 *
 * @Author:    Claude Code
 * @Date:      2025-11-06
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 模拟解码器列表数据
let mockDecoderList = [
  {
    id: 1,
    decoderName: '解码器-001',
    decoderCode: 'DEC001',
    manufacturer: 'hikvision',
    model: 'DS-6901UDI',
    ipAddress: '192.168.1.201',
    port: 8000,
    status: 1, // 1-在线, 0-离线
    totalChannels: 16,
    usedChannels: 8,
    firmwareVersion: 'V4.0.0',
    cpuUsage: 45,
    memoryUsage: 62,
    temperature: 42,
    registerTime: '2024-01-10 09:30:00',
    lastHeartbeat: '2025-11-06 14:35:20',
    description: '一号楼监控解码器',
  },
  {
    id: 2,
    decoderName: '解码器-002',
    decoderCode: 'DEC002',
    manufacturer: 'dahua',
    model: 'DH-NVD1205DH',
    ipAddress: '192.168.1.202',
    port: 8000,
    status: 1,
    totalChannels: 16,
    usedChannels: 12,
    firmwareVersion: 'V3.5.2',
    cpuUsage: 68,
    memoryUsage: 75,
    temperature: 48,
    registerTime: '2024-01-12 10:00:00',
    lastHeartbeat: '2025-11-06 14:35:18',
    description: '二号楼监控解码器',
  },
  {
    id: 3,
    decoderName: '解码器-003',
    decoderCode: 'DEC003',
    manufacturer: 'uniview',
    model: 'NVD2116-4K',
    ipAddress: '192.168.1.203',
    port: 8000,
    status: 0,
    totalChannels: 16,
    usedChannels: 0,
    firmwareVersion: 'V2.0.5',
    cpuUsage: 0,
    memoryUsage: 0,
    temperature: 0,
    registerTime: '2024-01-15 11:20:00',
    lastHeartbeat: '2025-11-06 10:20:15',
    description: '停车场解码器（离线维护中）',
  },
  {
    id: 4,
    decoderName: '解码器-004',
    decoderCode: 'DEC004',
    manufacturer: 'hikvision',
    model: 'DS-6901UDI',
    ipAddress: '192.168.1.204',
    port: 8000,
    status: 1,
    totalChannels: 16,
    usedChannels: 16,
    firmwareVersion: 'V4.0.0',
    cpuUsage: 85,
    memoryUsage: 88,
    temperature: 55,
    registerTime: '2024-01-18 14:30:00',
    lastHeartbeat: '2025-11-06 14:35:25',
    description: '生产区域解码器',
  },
  {
    id: 5,
    decoderName: '解码器-005',
    decoderCode: 'DEC005',
    manufacturer: 'dahua',
    model: 'DH-NVD1205DH',
    ipAddress: '192.168.1.205',
    port: 8000,
    status: 1,
    totalChannels: 16,
    usedChannels: 6,
    firmwareVersion: 'V3.5.2',
    cpuUsage: 38,
    memoryUsage: 52,
    temperature: 40,
    registerTime: '2024-01-20 09:00:00',
    lastHeartbeat: '2025-11-06 14:35:22',
    description: '公共区域解码器',
  },
];

// 模拟解码器通道数据
const mockChannelData = {
  1: [
    { channelId: 1, channelName: '通道1', deviceName: '前门摄像头-001', status: 1, resolution: '1920x1080' },
    { channelId: 2, channelName: '通道2', deviceName: '大厅摄像头-002', status: 1, resolution: '1920x1080' },
    { channelId: 3, channelName: '通道3', deviceName: '走廊摄像头-003', status: 1, resolution: '1920x1080' },
    { channelId: 4, channelName: '通道4', deviceName: '会议室摄像头-001', status: 1, resolution: '1920x1080' },
    { channelId: 5, channelName: '通道5', deviceName: '财务室摄像头-001', status: 1, resolution: '1920x1080' },
    { channelId: 6, channelName: '通道6', deviceName: '会议室摄像头-002', status: 1, resolution: '2688x1520' },
    { channelId: 7, channelName: '通道7', deviceName: '档案室摄像头-001', status: 1, resolution: '1920x1080' },
    { channelId: 8, channelName: '通道8', deviceName: '总经理办公室', status: 1, resolution: '1920x1080' },
  ],
  2: [
    { channelId: 1, channelName: '通道1', deviceName: '停车场摄像头-01', status: 1, resolution: '1920x1080' },
    { channelId: 2, channelName: '通道2', deviceName: '停车场摄像头-02', status: 1, resolution: '1920x1080' },
    { channelId: 3, channelName: '通道3', deviceName: '停车场摄像头-03', status: 1, resolution: '1920x1080' },
    { channelId: 4, channelName: '通道4', deviceName: '停车场摄像头-04', status: 1, resolution: '1920x1080' },
    { channelId: 5, channelName: '通道5', deviceName: '出入口摄像头-01', status: 1, resolution: '2688x1520' },
    { channelId: 6, channelName: '通道6', deviceName: '出入口摄像头-02', status: 1, resolution: '2688x1520' },
    { channelId: 7, channelName: '通道7', deviceName: '道闸监控-01', status: 1, resolution: '1920x1080' },
    { channelId: 8, channelName: '通道8', deviceName: '道闸监控-02', status: 1, resolution: '1920x1080' },
    { channelId: 9, channelName: '通道9', deviceName: '地下车库-A01', status: 1, resolution: '1920x1080' },
    { channelId: 10, channelName: '通道10', deviceName: '地下车库-A02', status: 1, resolution: '1920x1080' },
    { channelId: 11, channelName: '通道11', deviceName: '地下车库-B01', status: 1, resolution: '1920x1080' },
    { channelId: 12, channelName: '通道12', deviceName: '地下车库-B02', status: 1, resolution: '1920x1080' },
  ],
  3: [],
  4: Array.from({ length: 16 }, (_, i) => ({
    channelId: i + 1,
    channelName: `通道${i + 1}`,
    deviceName: `生产线摄像头-${String(i + 1).padStart(3, '0')}`,
    status: 1,
    resolution: '1920x1080',
  })),
  5: [
    { channelId: 1, channelName: '通道1', deviceName: '电梯厅摄像头-01', status: 1, resolution: '1920x1080' },
    { channelId: 2, channelName: '通道2', deviceName: '电梯厅摄像头-02', status: 1, resolution: '1920x1080' },
    { channelId: 3, channelName: '通道3', deviceName: '休息区摄像头-01', status: 1, resolution: '1920x1080' },
    { channelId: 4, channelName: '通道4', deviceName: '休息区摄像头-02', status: 1, resolution: '1920x1080' },
    { channelId: 5, channelName: '通道5', deviceName: '消防通道-01', status: 1, resolution: '1920x1080' },
    { channelId: 6, channelName: '通道6', deviceName: '消防通道-02', status: 1, resolution: '1920x1080' },
  ],
};

// 计算统计信息
function calculateStatistics(list) {
  const totalCount = list.length;
  const onlineCount = list.filter(d => d.status === 1).length;
  const offlineCount = totalCount - onlineCount;
  const totalChannels = list.reduce((sum, d) => sum + d.totalChannels, 0);
  const usedChannels = list.reduce((sum, d) => sum + d.usedChannels, 0);
  const usageRate = totalChannels > 0 ? Math.round((usedChannels / totalChannels) * 100) : 0;

  const onlineDecoders = list.filter(d => d.status === 1);
  const averageCpuUsage = onlineDecoders.length > 0
    ? Math.round(onlineDecoders.reduce((sum, d) => sum + d.cpuUsage, 0) / onlineDecoders.length)
    : 0;
  const averageMemoryUsage = onlineDecoders.length > 0
    ? Math.round(onlineDecoders.reduce((sum, d) => sum + d.memoryUsage, 0) / onlineDecoders.length)
    : 0;

  return {
    totalCount,
    onlineCount,
    offlineCount,
    totalChannels,
    usedChannels,
    usageRate,
    averageCpuUsage,
    averageMemoryUsage,
  };
}

// 模拟查询解码器列表
function mockQueryDecoderList(params) {
  let list = [...mockDecoderList];

  // 筛选
  if (params.decoderName) {
    list = list.filter(
      decoder =>
        decoder.decoderName.includes(params.decoderName) ||
        decoder.decoderCode.includes(params.decoderName)
    );
  }

  if (params.status !== undefined && params.status !== '') {
    list = list.filter(decoder => decoder.status === params.status);
  }

  if (params.manufacturer) {
    list = list.filter(decoder => decoder.manufacturer === params.manufacturer);
  }

  // 计算统计信息
  const statistics = calculateStatistics(mockDecoderList);

  // 分页
  const pageNum = params.pageNum || 1;
  const pageSize = params.pageSize || 20;
  const start = (pageNum - 1) * pageSize;
  const end = start + pageSize;
  const pageList = list.slice(start, end);

  return {
    code: 1,
    msg: '查询成功',
    data: {
      list: pageList,
      total: list.length,
      pageNum,
      pageSize,
      statistics,
    },
  };
}

// 模拟添加解码器
function mockAddDecoder(params) {
  const newDecoder = {
    id: mockDecoderList.length + 1,
    ...params,
    status: 0, // 新增的默认离线
    totalChannels: params.totalChannels || 16,
    usedChannels: 0,
    cpuUsage: 0,
    memoryUsage: 0,
    temperature: 0,
    registerTime: new Date().toLocaleString('zh-CN'),
    lastHeartbeat: new Date().toLocaleString('zh-CN'),
  };

  mockDecoderList.push(newDecoder);

  return {
    code: 1,
    msg: '添加成功',
    data: newDecoder,
  };
}

// 模拟更新解码器
function mockUpdateDecoder(params) {
  const index = mockDecoderList.findIndex(decoder => decoder.id === params.id);

  if (index === -1) {
    return {
      code: 0,
      msg: '解码器不存在',
    };
  }

  mockDecoderList[index] = {
    ...mockDecoderList[index],
    ...params,
  };

  return {
    code: 1,
    msg: '更新成功',
    data: mockDecoderList[index],
  };
}

// 模拟删除解码器
function mockDeleteDecoder(id) {
  const index = mockDecoderList.findIndex(decoder => decoder.id === id);

  if (index === -1) {
    return {
      code: 0,
      msg: '解码器不存在',
    };
  }

  mockDecoderList.splice(index, 1);

  return {
    code: 1,
    msg: '删除成功',
  };
}

// 模拟测试连接
function mockTestConnection(params) {
  // 模拟随机成功或失败
  const isSuccess = Math.random() > 0.2; // 80% 成功率

  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: isSuccess ? 1 : 0,
        msg: isSuccess ? '连接成功' : '连接失败：无法访问设备',
        data: isSuccess
          ? {
              responseTime: Math.floor(Math.random() * 100) + 50,
              firmwareVersion: 'V4.0.0',
              totalChannels: 16,
            }
          : null,
      });
    }, 1000);
  });
}

// 模拟重启解码器
function mockRestartDecoder(id) {
  const decoder = mockDecoderList.find(d => d.id === id);

  if (!decoder) {
    return Promise.resolve({
      code: 0,
      msg: '解码器不存在',
    });
  }

  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟重启后状态变化
      decoder.status = 1;
      decoder.cpuUsage = Math.floor(Math.random() * 30) + 10;
      decoder.memoryUsage = Math.floor(Math.random() * 40) + 20;
      decoder.lastHeartbeat = new Date().toLocaleString('zh-CN');

      resolve({
        code: 1,
        msg: '重启成功',
        data: decoder,
      });
    }, 2000);
  });
}

// 模拟获取解码器统计信息
function mockGetDecoderStatistics() {
  const statistics = calculateStatistics(mockDecoderList);

  return {
    code: 1,
    msg: '获取成功',
    data: statistics,
  };
}

// 获取解码器通道信息
function getDecoderChannels(decoderId) {
  return mockChannelData[decoderId] || [];
}

export default {
  mockQueryDecoderList,
  mockAddDecoder,
  mockUpdateDecoder,
  mockDeleteDecoder,
  mockTestConnection,
  mockRestartDecoder,
  mockGetDecoderStatistics,
  getDecoderChannels,
  mockDecoderList,
};
