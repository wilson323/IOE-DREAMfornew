/*
 * 智能视频-设备管理常量定义
 *
 * @Author:    Claude Code
 * @Date:      2024-11-05
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

/**
 * 设备类型枚举
 */
export const DEVICE_TYPE_ENUM = {
  IPC: {
    value: 'ipc',
    label: '网络摄像机',
    color: 'blue',
  },
  NVR: {
    value: 'nvr',
    label: 'NVR',
    color: 'purple',
  },
  DVR: {
    value: 'dvr',
    label: 'DVR',
    color: 'cyan',
  },
  DECODER: {
    value: 'decoder',
    label: '解码器',
    color: 'green',
  },
};

/**
 * 设备状态枚举
 */
export const DEVICE_STATUS_ENUM = {
  ONLINE: {
    value: 'online',
    label: '在线',
    color: 'success',
  },
  OFFLINE: {
    value: 'offline',
    label: '离线',
    color: 'error',
  },
};

/**
 * 设备厂商枚举
 */
export const DEVICE_MANUFACTURER_ENUM = {
  HIKVISION: {
    value: 'hikvision',
    label: '海康威视',
  },
  DAHUA: {
    value: 'dahua',
    label: '大华',
  },
  UNIVIEW: {
    value: 'uniview',
    label: '宇视',
  },
  TIANDY: {
    value: 'tiandy',
    label: '天地伟业',
  },
};

/**
 * 获取设备类型标签
 */
export function getDeviceTypeLabel(value) {
  const item = Object.values(DEVICE_TYPE_ENUM).find(item => item.value === value);
  return item ? item.label : value;
}

/**
 * 获取设备类型颜色
 */
export function getDeviceTypeColor(value) {
  const item = Object.values(DEVICE_TYPE_ENUM).find(item => item.value === value);
  return item ? item.color : 'default';
}

/**
 * 获取设备状态标签
 */
export function getDeviceStatusLabel(value) {
  const item = Object.values(DEVICE_STATUS_ENUM).find(item => item.value === value);
  return item ? item.label : value;
}

/**
 * 获取设备状态颜色
 */
export function getDeviceStatusColor(value) {
  const item = Object.values(DEVICE_STATUS_ENUM).find(item => item.value === value);
  return item ? item.color : 'default';
}

/**
 * 获取厂商标签
 */
export function getManufacturerLabel(value) {
  const item = Object.values(DEVICE_MANUFACTURER_ENUM).find(item => item.value === value);
  return item ? item.label : value;
}
