/**
 * 视频监控API接口
 *
 * @Author:    Claude Code
 * @Date:      2024-11-05
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const monitorApi = {
  // 获取设备树列表
  getDeviceTree: (params) => getRequest('/monitor/device/tree', params),

  // 获取视频流地址
  getVideoStreamUrl: (deviceId, channelId) =>
    getRequest(`/monitor/stream/url/${deviceId}/${channelId}`),

  // 开始播放视频
  startPlay: (params) => postRequest('/monitor/play/start', params),

  // 停止播放视频
  stopPlay: (params) => postRequest('/monitor/play/stop', params),

  // 云台控制
  ptzControl: (params) => postRequest('/monitor/ptz/control', params),

  // 截图
  captureSnapshot: (params) => postRequest('/monitor/capture/snapshot', params),

  // 开始录像
  startRecord: (params) => postRequest('/monitor/record/start', params),

  // 停止录像
  stopRecord: (params) => postRequest('/monitor/record/stop', params),

  // 获取预置位列表
  getPresetList: (deviceId) => getRequest(`/monitor/ptz/preset/${deviceId}`),

  // 调用预置位
  gotoPreset: (params) => postRequest('/monitor/ptz/preset/goto', params),

  // 获取监控场景配置
  getSceneConfig: (sceneId) => getRequest(`/monitor/scene/config/${sceneId}`),

  // 保存监控场景配置
  saveSceneConfig: (params) => postRequest('/monitor/scene/config/save', params),

  // 获取设备统计信息
  getDeviceStatistics: () => getRequest('/monitor/device/statistics'),
};
