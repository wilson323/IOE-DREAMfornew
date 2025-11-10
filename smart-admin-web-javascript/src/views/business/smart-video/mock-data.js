/*
 * 智能视频模块模拟数据
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 设备类型映射
export const DEVICE_TYPE_MAP = {
  ipc: '网络摄像机',
  nvr: 'NVR',
  dvr: 'DVR',
  decoder: '解码器',
};

// 设备状态映射
export const DEVICE_STATUS_MAP = {
  online: '在线',
  offline: '离线',
  maintenance: '维护中',
  error: '故障',
};

// 模拟设备数据
export const mockDevices = [
  {
    id: 1,
    deviceName: '前门摄像头-001',
    deviceCode: 'CAM001',
    deviceType: 'ipc',
    ipAddress: '192.168.1.101',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 10:30',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-1f',
    location: '前门-入口',
    username: 'admin',
    password: '******',
    description: '主入口监控摄像头，24小时不间断监控',
    manufacturer: '海康威视',
    model: 'DS-2CD3326DWDA',
    firmwareVersion: 'V5.5.0',
    serialNumber: 'DS-2CD3326DWDA20231201AACH123456789',
    macAddress: '00:12:34:56:78:9A',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 2,
    deviceName: '大厅摄像头-002',
    deviceCode: 'CAM002',
    deviceType: 'ipc',
    ipAddress: '192.168.1.102',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 10:32',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-1f',
    location: '大厅中央',
    username: 'admin',
    password: '******',
    description: '大厅全景监控摄像头',
    manufacturer: '大华',
    model: 'DH-IPC-HFW5442E-ZE',
    firmwareVersion: 'V2.800.0000000.0.R',
    serialNumber: 'DH-IPC-HFW5442E-ZE20231115BB987654321',
    macAddress: '00:12:34:56:78:9B',
    resolution: '2688*1520',
    frameRate: 30,
  },
  {
    id: 3,
    deviceName: '走廊摄像头-003',
    deviceCode: 'CAM003',
    deviceType: 'ipc',
    ipAddress: '192.168.1.103',
    port: 8000,
    status: 'offline',
    registerTime: '2024-01-15 10:35',
    lastHeartbeat: '2024-01-30 12:20',
    groupId: 'office-1f',
    location: '一楼走廊',
    username: 'admin',
    password: '******',
    description: '一楼走廊通道监控',
    manufacturer: '海康威视',
    model: 'DS-2CD3326DWDA',
    firmwareVersion: 'V5.5.0',
    serialNumber: 'DS-2CD3326DWDA20231202AACH123456790',
    macAddress: '00:12:34:56:78:9C',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 4,
    deviceName: '会议室摄像头-001',
    deviceCode: 'CAM004',
    deviceType: 'ipc',
    ipAddress: '192.168.1.104',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 11:00',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-1f',
    location: '一楼会议室',
    username: 'admin',
    password: '******',
    description: '一楼会议室内部监控',
    manufacturer: '宇视',
    model: 'IPC3224ER3-DUPF40',
    firmwareVersion: 'V2.0.0',
    serialNumber: 'IPC3224ER3-DUPF4020231203UNV123456789',
    macAddress: '00:12:34:56:78:9D',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 5,
    deviceName: '财务室摄像头-001',
    deviceCode: 'CAM005',
    deviceType: 'ipc',
    ipAddress: '192.168.1.105',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 11:30',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-2f',
    location: '二楼财务室',
    username: 'admin',
    password: '******',
    description: '财务室重点监控区域',
    manufacturer: '海康威视',
    model: 'DS-2CD3326DWDA',
    firmwareVersion: 'V5.5.0',
    serialNumber: 'DS-2CD3326DWDA20231204AACH123456791',
    macAddress: '00:12:34:56:78:9E',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 6,
    deviceName: '会议室摄像头-002',
    deviceCode: 'CAM006',
    deviceType: 'ipc',
    ipAddress: '192.168.1.106',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 12:00',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-2f',
    location: '二楼大会议室',
    username: 'admin',
    password: '******',
    description: '二楼大会议室全景监控',
    manufacturer: '大华',
    model: 'DH-IPC-HFW5442E-ZE',
    firmwareVersion: 'V2.800.0000000.0.R',
    serialNumber: 'DH-IPC-HFW5442E-ZE20231116BB987654322',
    macAddress: '00:12:34:56:78:9F',
    resolution: '2688*1520',
    frameRate: 30,
  },
  {
    id: 7,
    deviceName: '档案室摄像头-001',
    deviceCode: 'CAM007',
    deviceType: 'ipc',
    ipAddress: '192.168.1.107',
    port: 8000,
    status: 'maintenance',
    registerTime: '2024-01-15 12:30',
    lastHeartbeat: '2024-01-30 13:45',
    groupId: 'office-2f',
    location: '二楼档案室',
    username: 'admin',
    password: '******',
    description: '档案室重要区域监控',
    manufacturer: '宇视',
    model: 'IPC3224ER3-DUPF40',
    firmwareVersion: 'V2.0.0',
    serialNumber: 'IPC3224ER3-DUPF4020231204UNV123456790',
    macAddress: '00:12:34:56:78:A0',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 8,
    deviceName: 'NVR存储设备-01',
    deviceCode: 'NVR001',
    deviceType: 'nvr',
    ipAddress: '192.168.1.200',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-14 16:20',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'office-3f',
    location: '三楼机房',
    username: 'admin',
    password: '******',
    description: '16路NVR存储设备，负责存储所有摄像头录像',
    manufacturer: '海康威视',
    model: 'DS-7816N-K2/16P',
    firmwareVersion: 'V4.30.005_210121',
    serialNumber: 'DS-7816N-K2-16P20231210AACH123456789',
    macAddress: '00:12:34:56:78:A1',
    channelCount: 16,
    hddCapacity: '8TB',
  },
  {
    id: 9,
    deviceName: '停车场摄像头-01',
    deviceCode: 'CAM008',
    deviceType: 'ipc',
    ipAddress: '192.168.1.108',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 13:00',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'parking',
    location: '停车场入口',
    username: 'admin',
    password: '******',
    description: '停车场入口车牌识别摄像头',
    manufacturer: '海康威视',
    model: 'DS-2CD3326DWDA',
    firmwareVersion: 'V5.5.0',
    serialNumber: 'DS-2CD3326DWDA20231205AACH123456792',
    macAddress: '00:12:34:56:78:A2',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 10,
    deviceName: '车间摄像头-001',
    deviceCode: 'CAM009',
    deviceType: 'ipc',
    ipAddress: '192.168.1.109',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 13:30',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'production-workshop',
    location: '生产线A',
    username: 'admin',
    password: '******',
    description: '生产线A区域监控',
    manufacturer: '大华',
    model: 'DH-IPC-HFW5442E-ZE',
    firmwareVersion: 'V2.800.0000000.0.R',
    serialNumber: 'DH-IPC-HFW5442E-ZE20231117BB987654323',
    macAddress: '00:12:34:56:78:A3',
    resolution: '2688*1520',
    frameRate: 30,
  },
  {
    id: 11,
    deviceName: '车间摄像头-002',
    deviceCode: 'CAM010',
    deviceType: 'ipc',
    ipAddress: '192.168.1.110',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 14:00',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'production-workshop',
    location: '生产线B',
    username: 'admin',
    password: '******',
    description: '生产线B区域监控',
    manufacturer: '宇视',
    model: 'IPC3224ER3-DUPF40',
    firmwareVersion: 'V2.0.0',
    serialNumber: 'IPC3224ER3-DUPF4020231205UNV123456791',
    macAddress: '00:12:34:56:78:A4',
    resolution: '1920*1080',
    frameRate: 25,
  },
  {
    id: 12,
    deviceName: '周界摄像头-001',
    deviceCode: 'CAM011',
    deviceType: 'ipc',
    ipAddress: '192.168.1.111',
    port: 8000,
    status: 'online',
    registerTime: '2024-01-15 14:30',
    lastHeartbeat: '2024-01-30 14:35',
    groupId: 'security',
    location: '东侧围墙',
    username: 'admin',
    password: '******',
    description: '东侧围墙周界防范监控',
    manufacturer: '海康威视',
    model: 'DS-2CD3326DWDA',
    firmwareVersion: 'V5.5.0',
    serialNumber: 'DS-2CD3326DWDA20231206AACH123456793',
    macAddress: '00:12:34:56:78:A5',
    resolution: '1920*1080',
    frameRate: 25,
  },
];

// 模拟分组数据
export const mockGroups = [
  {
    id: 'root',
    name: '全部设备',
    parentId: null,
    description: '所有设备的根分组',
    deviceCount: 12,
    onlineCount: 11,
    children: [
      {
        id: 'office',
        name: '办公区域',
        parentId: 'root',
        description: '办公楼内部设备',
        deviceCount: 7,
        onlineCount: 6,
        children: [
          {
            id: 'office-1f',
            name: '一楼',
            parentId: 'office',
            description: '办公楼一楼设备',
            deviceCount: 4,
            onlineCount: 3,
          },
          {
            id: 'office-2f',
            name: '二楼',
            parentId: 'office',
            description: '办公楼二楼设备',
            deviceCount: 3,
            onlineCount: 2,
          },
          {
            id: 'office-3f',
            name: '三楼',
            parentId: 'office',
            description: '办公楼三楼设备',
            deviceCount: 1,
            onlineCount: 1,
          },
        ],
      },
      {
        id: 'production',
        name: '生产区域',
        parentId: 'root',
        description: '生产车间相关设备',
        deviceCount: 2,
        onlineCount: 2,
        children: [
          {
            id: 'production-workshop',
            name: '车间',
            parentId: 'production',
            description: '生产线车间设备',
            deviceCount: 2,
            onlineCount: 2,
          },
          {
            id: 'production-warehouse',
            name: '仓库',
            parentId: 'production',
            description: '原材料仓库设备',
            deviceCount: 0,
            onlineCount: 0,
          },
        ],
      },
      {
        id: 'parking',
        name: '停车区域',
        parentId: 'root',
        description: '停车场监控设备',
        deviceCount: 1,
        onlineCount: 1,
      },
      {
        id: 'public',
        name: '公共区域',
        parentId: 'root',
        description: '公共活动区域设备',
        deviceCount: 0,
        onlineCount: 0,
      },
      {
        id: 'security',
        name: '周界防范',
        parentId: 'root',
        description: '周界安全防范设备',
        deviceCount: 1,
        onlineCount: 1,
      },
    ],
  },
];

// 获取设备统计数据
export const getDeviceStats = () => {
  const total = mockDevices.length;
  const online = mockDevices.filter(d => d.status === 'online').length;
  const offline = mockDevices.filter(d => d.status === 'offline').length;
  const maintenance = mockDevices.filter(d => d.status === 'maintenance').length;
  const error = mockDevices.filter(d => d.status === 'error').length;

  const typeStats = {};
  mockDevices.forEach(device => {
    typeStats[device.deviceType] = (typeStats[device.deviceType] || 0) + 1;
  });

  return {
    total,
    online,
    offline,
    maintenance,
    error,
    onlineRate: total > 0 ? ((online / total) * 100).toFixed(1) : 0,
    typeStats,
  };
};

// 根据分组ID获取设备列表
export const getDevicesByGroup = (groupId) => {
  if (groupId === 'root') {
    return mockDevices;
  }
  return mockDevices.filter(device => device.groupId === groupId);
};

// 根据设备ID获取设备信息
export const getDeviceById = (deviceId) => {
  return mockDevices.find(device => device.id === deviceId);
};

// 根据分组ID获取分组信息
export const getGroupById = (groupId, groups = mockGroups) => {
  for (const group of groups) {
    if (group.id === groupId) {
      return group;
    }
    if (group.children) {
      const found = getGroupById(groupId, group.children);
      if (found) return found;
    }
  }
  return null;
};

// 搜索设备
export const searchDevices = (keyword, filters = {}) => {
  let results = [...mockDevices];

  // 关键词搜索
  if (keyword) {
    const lowerKeyword = keyword.toLowerCase();
    results = results.filter(device =>
      device.deviceName.toLowerCase().includes(lowerKeyword) ||
      device.deviceCode.toLowerCase().includes(lowerKeyword) ||
      device.ipAddress.includes(lowerKeyword) ||
      device.location.toLowerCase().includes(lowerKeyword)
    );
  }

  // 状态过滤
  if (filters.status) {
    results = results.filter(device => device.status === filters.status);
  }

  // 设备类型过滤
  if (filters.deviceType) {
    results = results.filter(device => device.deviceType === filters.deviceType);
  }

  // 分组过滤
  if (filters.groupId && filters.groupId !== 'root') {
    results = results.filter(device => device.groupId === filters.groupId);
  }

  return results;
};

// 生成设备编码
export const generateDeviceCode = (deviceType) => {
  const typePrefix = {
    ipc: 'CAM',
    nvr: 'NVR',
    dvr: 'DVR',
    decoder: 'DEC',
  }[deviceType] || 'DEV';

  const existingCodes = mockDevices
    .filter(d => d.deviceCode.startsWith(typePrefix))
    .map(d => parseInt(d.deviceCode.replace(typePrefix, '')))
    .filter(n => !isNaN(n))
    .sort((a, b) => a - b);

  const nextNumber = existingCodes.length > 0 ? existingCodes[existingCodes.length - 1] + 1 : 1;
  return `${typePrefix}${String(nextNumber).padStart(3, '0')}`;
};