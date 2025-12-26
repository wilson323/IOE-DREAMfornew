/**
 * 本地通知管理
 * 提供告警推送、震动提醒等功能
 */

/**
 * 显示告警通知
 * @param {Object} alarm 告警对象
 * @param {string} alarm.alarmId 告警ID
 * @param {string} alarm.alarmLevel 告警级别
 * @param {string} alarm.message 告警消息
 * @param {string} alarm.deviceName 设备名称
 * @param {string} alarm.alarmTime 告警时间
 */
export function showAlarmNotification(alarm) {
  const { alarmId, alarmLevel, message, deviceName, alarmTime } = alarm;

  // 根据告警级别设置提示音和震动模式
  let sound = 'system';
  let vibrateShort = [200];
  let vibrateLong = [200, 100, 200];

  switch (alarmLevel) {
    case 'HIGH':
      sound = 'alert';
      vibrateShort = vibrateLong;
      break;
    case 'MEDIUM':
      sound = 'beep';
      break;
    case 'LOW':
      sound = 'default';
      break;
  }

  // 创建本地通知
  uni.createPushMessage({
    title: getAlarmTitle(alarmLevel),
    content: `${deviceName || '未知设备'} - ${message || '检测到异常'}`,
    payload: {
      alarmId,
      alarmLevel,
      timestamp: alarmTime || Date.now()
    },
    sound,
    success: () => {
      console.log('[LocalNotification] 通知发送成功:', alarmId);

      // 震动提醒
      if (vibrateShort) {
        uni.vibrateShort && uni.vibrateShort(vibrateShort);
      }
    },
    fail: (error) => {
      console.error('[LocalNotification] 通知发送失败:', error);
    }
  });
}

/**
 * 获取告警标题
 * @param {string} level 告警级别
 * @returns {string} 标题文本
 */
function getAlarmTitle(level) {
  const titleMap = {
    HIGH: '🚨 紧急告警',
    MEDIUM: '⚠️ 告警提醒',
    LOW: 'ℹ️️ 信息提示'
  };
  return titleMap[level] || '告警通知';
}

/**
 * 请求通知权限
 * @returns {Promise<Object>} 权限请求结果
 */
export async function requestNotificationPermission() {
  try {
    const result = await uni.requestPermissions({
      permissions: ['notification', 'vibrate']
    });

    console.log('[LocalNotification] 权限请求结果:', result);

    // 如果权限被拒绝，提示用户
    if (result.notification?.deny || result.vibrate?.deny) {
      uni.showModal({
        title: '权限申请',
        content: '视频监控需要通知和震动权限，以便在发生告警时及时提醒您',
        confirmText: '去设置',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            uni.openSetting();
          }
        }
      });
    }

    return result;
  } catch (error) {
    console.error('[LocalNotification] 请求权限失败:', error);
    return null;
  }
}

/**
 * 检查通知权限
 * @returns {Promise<Object>} 权限状态
 */
export async function checkNotificationPermission() {
  try {
    const result = await uni.getSetting();

    return {
      notification: result.authSetting['notification'],
      vibrate: result.authSetting['vibrate']
    };
  } catch (error) {
    console.error('[LocalNotification] 检查权限失败:', error);
    return {
      notification: false,
      vibrate: false
    };
  }
}

/**
 * 震动提醒
 * @param {string} level 告警级别
 */
export function vibrateAlarm(level) {
  const patternMap = {
    HIGH: [500, 100, 500, 100, 500],      // 紧急: 长震动3次
    MEDIUM: [200, 100, 200],               // 中等: 短震动2次
    LOW: [100]                            // 低级: 短震动1次
  };

  const pattern = patternMap[level] || [100];

  uni.vibrateShort && uni.vibrateShort(pattern);
}

/**
 * 播放提示音
 * @param {string} level 告警级别
 */
export function playAlarmSound(level) {
  const soundMap = {
    HIGH: '/static/audio/alert.mp3',
    MEDIUM: '/static/audio/beep.mp3',
    LOW: '/static/audio/notification.mp3'
  };

  const sound = soundMap[level];

  if (sound) {
    const innerAudioContext = uni.createInnerAudioContext();
    innerAudioContext.src = sound;
    innerAudioContext.play();

    console.log('[LocalNotification] 播放提示音:', sound);
  }
}

/**
 * 创建前台通知（仅App可用）
 * @param {string} title 标题
 * @param {string} content 内容
 * @param {number} progress 进度（可选）
 */
export function createForegroundNotification(title, content, progress = null) {
  // #ifdef APP-PLUS
  try {
    const main = plus.android.runtimeMainActivity();
    const Notification = plus.android.import('android.app.Notification');
    const Context = plus.android.import('android.content.Context');
    const NotificationManager = plus.android.import('android.app.NotificationManager');
    const PendingIntent = plus.android.import('android.app.PendingIntent');
    const Intent = plus.android.import('android.content.Intent');

    const notificationManager = main.getSystemService(Context.NOTIFICATION_SERVICE);

    const builder = new Notification.Builder(main);

    builder
      .setContentTitle(title)
      .setContentText(content)
      .setSmallIcon(plus.android.R.drawable.ic_notification_overlay)
      .setAutoCancel(true);

    if (progress !== null) {
      builder.setProgress(100, progress, false);
    }

    notificationManager.notify(1, builder.build());

    console.log('[LocalNotification] 前台通知创建成功');
  } catch (error) {
    console.error('[LocalNotification] 创建前台通知失败:', error);
  }
  // #endif
}

/**
 * 清除通知
 */
export function clearNotification() {
  // #ifdef APP-PLUS
  try {
    const main = plus.android.runtimeMainActivity();
    const Context = plus.android.import('android.content.Context');
    const NotificationManager = plus.android.import('android.app.NotificationManager');

    const notificationManager = main.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();

    console.log('[LocalNotification] 清除通知成功');
  } catch (error) {
    console.error('[LocalNotification] 清除通知失败:', error);
  }
  // #endif
}

/**
 * 显示设备离线通知
 * @param {Object} device 设备对象
 */
export function showDeviceOfflineNotification(device) {
  uni.createPushMessage({
    title: '设备离线提醒',
    content: `${device.deviceName || device.deviceCode} 已离线`,
    payload: {
      type: 'device_offline',
      deviceId: device.deviceId,
      timestamp: Date.now()
    },
    success: () => {
      uni.vibrateShort && uni.vibrateShort([100]);
    }
  });
}

/**
 * 显示录像下载完成通知
 * @param {string} fileName 文件名
 * @param {string} filePath 文件路径
 */
export function showDownloadCompleteNotification(fileName, filePath) {
  uni.createPushMessage({
    title: '录像下载完成',
    content: fileName,
    payload: {
      type: 'download_complete',
      filePath,
      timestamp: Date.now()
    }
  });
}

/**
 * 批量显示告警通知
 * @param {Array} alarms 告警列表
 * @param {number} maxCount 最大显示数量，默认5条
 */
export function showBatchAlarmNotifications(alarms, maxCount = 5) {
  const count = Math.min(alarms.length, maxCount);

  for (let i = 0; i < count; i++) {
    // 延迟发送，避免通知堆积
    setTimeout(() => {
      showAlarmNotification(alarms[i]);
    }, i * 300);
  }

  console.log(`[LocalNotification] 批量发送${count}条告警通知`);
}

/**
 * 通知管理器
 */
export class NotificationManager {
  constructor() {
    this.enabled = true;
    this.queue = [];
    this.isProcessing = false;
  }

  /**
   * 启用通知
   */
  enable() {
    this.enabled = true;
    console.log('[NotificationManager] 通知已启用');
  }

  /**
   * 禁用通知
   */
  disable() {
    this.enabled = false;
    console.log('[NotificationManager] 通知已禁用');
  }

  /**
   * 添加通知到队列
   */
  async enqueue(alarm) {
    if (!this.enabled) {
      console.log('[NotificationManager] 通知已禁用，忽略告警');
      return;
    }

    this.queue.push(alarm);
    await this.processQueue();
  }

  /**
   * 处理通知队列
   */
  async processQueue() {
    if (this.isProcessing || this.queue.length === 0) {
      return;
    }

    this.isProcessing = true;

    while (this.queue.length > 0) {
      const alarm = this.queue.shift();
      await this.showNotification(alarm);

      // 间隔500ms，避免通知过快
      await new Promise(resolve => setTimeout(resolve, 500));
    }

    this.isProcessing = false;
  }

  /**
   * 显示通知
   */
  async showNotification(alarm) {
    try {
      await requestNotificationPermission();
      showAlarmNotification(alarm);
    } catch (error) {
      console.error('[NotificationManager] 显示通知失败:', error);
    }
  }

  /**
   * 清空队列
   */
  clearQueue() {
    this.queue = [];
    console.log('[NotificationManager] 清空通知队列');
  }
}

// 导出单例
export const notificationManager = new NotificationManager();

export default {
  showAlarmNotification,
  requestNotificationPermission,
  checkNotificationPermission,
  vibrateAlarm,
  playAlarmSound,
  createForegroundNotification,
  clearNotification,
  showDeviceOfflineNotification,
  showDownloadCompleteNotification,
  showBatchAlarmNotifications,
  NotificationManager,
  notificationManager
};
