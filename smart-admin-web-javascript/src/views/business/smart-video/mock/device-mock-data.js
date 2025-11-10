/*
 * 设备管理模块模拟数据
 *
 * @Author:    Claude Code
 * @Date:      2025-11-06
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 模拟设备列表数据
const mockDeviceList = [
  {
    id: 1,
    deviceName: '前门摄像头-001',
    deviceCode: 'CAM001',
    deviceType: 'ipc',
    manufacturer: 'hikvision',
    ipAddress: '192.168.1.101',
    port: 8000,
    status: 'online',
    groupName: '一号楼',
    registerTime: '2024-01-15 10:30:00',
    lastHeartbeat: '2025-11-06 14:35:20',
  },
  {
    id: 2,
    deviceName: '大厅摄像头-002',
    deviceCode: 'CAM002',
    deviceType: 'ipc',
    manufacturer: 'dahua',
    ipAddress: '192.168.1.102',
    port: 8000,
    status: 'online',
    groupName: '一号楼',
    registerTime: '2024-01-15 10:32:00',
    lastHeartbeat: '2025-11-06 14:35:18',
  },
  {
    id: 3,
    deviceName: '走廊摄像头-003',
    deviceCode: 'CAM003',
    deviceType: 'ipc',
    manufacturer: 'hikvision',
    ipAddress: '192.168.1.103',
    port: 8000,
    status: 'offline',
    groupName: '一号楼',
    registerTime: '2024-01-15 10:35:00',
    lastHeartbeat: '2025-11-06 12:20:15',
  },
  {
    id: 4,
    deviceName: '会议室摄像头-001',
    deviceCode: 'CAM004',
    deviceType: 'ipc',
    manufacturer: 'uniview',
    ipAddress: '192.168.1.104',
    port: 8000,
    status: 'online',
    groupName: '一号楼',
    registerTime: '2024-01-15 11:00:00',
    lastHeartbeat: '2025-11-06 14:35:22',
  },
  {
    id: 5,
    deviceName: '财务室摄像头-001',
    deviceCode: 'CAM005',
    deviceType: 'ipc',
    manufacturer: 'hikvision',
    ipAddress: '192.168.1.105',
    port: 8000,
    status: 'online',
    groupName: '停车场',
    registerTime: '2024-01-15 11:30:00',
    lastHeartbeat: '2025-11-06 14:35:25',
  },
  {
    id: 6,
    deviceName: 'NVR存储设备-01',
    deviceCode: 'NVR001',
    deviceType: 'nvr',
    manufacturer: 'hikvision',
    ipAddress: '192.168.1.200',
    port: 8000,
    status: 'online',
    groupName: '机房',
    registerTime: '2024-01-14 16:20:00',
    lastHeartbeat: '2025-11-06 14:35:30',
  },
  {
    id: 7,
    deviceName: '停车场摄像头-01',
    deviceCode: 'CAM006',
    deviceType: 'ipc',
    manufacturer: 'dahua',
    ipAddress: '192.168.1.106',
    port: 8000,
    status: 'online',
    groupName: '停车场',
    registerTime: '2024-01-16 09:00:00',
    lastHeartbeat: '2025-11-06 14:35:28',
  },
  {
    id: 8,
    deviceName: '车间摄像头-001',
    deviceCode: 'CAM007',
    deviceType: 'ipc',
    manufacturer: 'hikvision',
    ipAddress: '192.168.1.107',
    port: 8000,
    status: 'online',
    groupName: '生产区域',
    registerTime: '2024-01-16 10:00:00',
    lastHeartbeat: '2025-11-06 14:35:32',
  },
];

// 模拟分组树数据
export const mockGroupTreeData = [
  {
    id: 'root',
    name: '全部设备',
    children: [
      {
        id: 'office',
        name: '办公区域',
        children: [
          { id: 'office-1f', name: '一号楼' },
          { id: 'office-2f', name: '二楼' },
          { id: 'office-3f', name: '三楼' },
        ],
      },
      {
        id: 'production',
        name: '生产区域',
        children: [
          { id: 'production-workshop', name: '车间' },
          { id: 'production-warehouse', name: '仓库' },
        ],
      },
      {
        id: 'parking',
        name: '停车区域',
      },
      {
        id: 'public',
        name: '公共区域',
      },
    ],
  },
];

// 模拟查询设备列表
function mockQueryDeviceList(params) {
  let list = [...mockDeviceList];

  // 筛选
  if (params.deviceName) {
    list = list.filter(
      (device) =>
        device.deviceName.includes(params.deviceName) ||
        device.deviceCode.includes(params.deviceName)
    );
  }

  if (params.status) {
    list = list.filter((device) => device.status === params.status);
  }

  if (params.deviceType) {
    list = list.filter((device) => device.deviceType === params.deviceType);
  }

  // 分页
  const start = (params.pageNum - 1) * params.pageSize;
  const end = start + params.pageSize;
  const pageList = list.slice(start, end);

  return {
    code: 1,
    msg: '查询成功',
    data: {
      list: pageList,
      total: list.length,
      pageNum: params.pageNum,
      pageSize: params.pageSize,
    },
  };
}

// 模拟删除设备
function mockDeleteDevice(id) {
  const index = mockDeviceList.findIndex((device) => device.id === id);
  if (index > -1) {
    mockDeviceList.splice(index, 1);
    return {
      code: 1,
      msg: '删除成功',
    };
  }
  return {
    code: 0,
    msg: '设备不存在',
  };
}

// 模拟批量删除设备
function mockBatchDeleteDevice(ids) {
  ids.forEach((id) => {
    const index = mockDeviceList.findIndex((device) => device.id === id);
    if (index > -1) {
      mockDeviceList.splice(index, 1);
    }
  });

  return {
    code: 1,
    msg: `成功删除 ${ids.length} 个设备`,
  };
}

export default {
  mockQueryDeviceList,
  mockDeleteDevice,
  mockBatchDeleteDevice,
  mockDeviceList,
};
