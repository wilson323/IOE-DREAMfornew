/**
 * 访客通知推送工具
 * 提供预约、审批、签到签退等场景的通知功能
 */

// ==================== 通知权限管理 ====================

/**
 * 请求通知权限
 * @returns {Promise<Object>} 权限请求结果
 */
export async function requestNotificationPermission() {
  try {
    const result = await uni.requestPermissions({
      permissions: ['notification', 'vibrate']
    });

    console.log('[VisitorNotification] 权限请求结果:', result);

    // 如果权限被拒绝，提示用户
    if (result.notification?.deny || result.vibrate?.deny) {
      uni.showModal({
        title: '权限申请',
        content: '访客管理需要通知和震动权限，以便及时提醒您预约审批和访客动态',
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
    console.error('[VisitorNotification] 请求权限失败:', error);
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
    console.error('[VisitorNotification] 检查权限失败:', error);
    return {
      notification: false,
      vibrate: false
    };
  }
}

// ==================== 预约通知 ====================

/**
 * 发送预约创建通知
 * @param {Object} appointment 预约信息
 * @param {string} appointment.visitorName 访客姓名
 * @param {string} appointment.visiteeName 被访人姓名
 * @param {string} appointment.appointmentTime 预约时间
 * @param {number} appointment.appointmentId 预约ID
 */
export function sendAppointmentCreatedNotification(appointment) {
  const { visitorName, visiteeName, appointmentTime, appointmentId } = appointment;

  uni.createPushMessage({
    title: '📅 新的访客预约',
    content: `${visitorName} 预约了 ${appointmentTime} 拜访${visiteeName}`,
    payload: {
      type: 'APPOINTMENT_CREATED',
      appointmentId,
      timestamp: Date.now()
    },
    sound: 'system',
    success: () => {
      console.log('[VisitorNotification] 预约创建通知发送成功:', appointmentId);
      // 震动提醒
      uni.vibrateShort && uni.vibrateShort([100]);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 预约创建通知发送失败:', error);
    }
  });
}

/**
 * 发送预约审批通过通知
 * @param {Object} appointment 预约信息
 * @param {string} appointment.visitorName 访客姓名
 * @param {string} appointment.appointmentTime 预约时间
 * @param {string} appointment.qrCode 二维码
 */
export function sendAppointmentApprovedNotification(appointment) {
  const { visitorName, appointmentTime, appointmentId, qrCode } = appointment;

  uni.createPushMessage({
    title: '✅ 预约已通过',
    content: `您${appointmentTime}的访客预约已通过审批`,
    payload: {
      type: 'APPOINTMENT_APPROVED',
      appointmentId,
      qrCode,
      timestamp: Date.now()
    },
    sound: 'beep',
    success: () => {
      console.log('[VisitorNotification] 预约审批通过通知发送成功:', appointmentId);
      uni.vibrateShort && uni.vibrateShort([200, 100, 200]);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 预约审批通过通知发送失败:', error);
    }
  });
}

/**
 * 发送预约审批拒绝通知
 * @param {Object} appointment 预约信息
 * @param {string} appointment.visitorName 访客姓名
 * @param {string} appointment.rejectionReason 拒绝原因
 */
export function sendAppointmentRejectedNotification(appointment) {
  const { visitorName, rejectionReason, appointmentId } = appointment;

  uni.createPushMessage({
    title: '❌ 预约已拒绝',
    content: rejectionReason || '您的访客预约未通过审批',
    payload: {
      type: 'APPOINTMENT_REJECTED',
      appointmentId,
      timestamp: Date.now()
    },
    sound: 'default',
    success: () => {
      console.log('[VisitorNotification] 预约审批拒绝通知发送成功:', appointmentId);
      uni.vibrateShort && uni.vibrateShort([100]);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 预约审批拒绝通知发送失败:', error);
    }
  });
}

/**
 * 发送预约取消通知
 * @param {Object} appointment 预约信息
 * @param {string} appointment.visitorName 访客姓名
 * @param {string} appointment.appointmentTime 预约时间
 */
export function sendAppointmentCancelledNotification(appointment) {
  const { visitorName, appointmentTime, appointmentId } = appointment;

  uni.createPushMessage({
    title: '🚫 预约已取消',
    content: `${visitorName}的访客预约已取消`,
    payload: {
      type: 'APPOINTMENT_CANCELLED',
      appointmentId,
      timestamp: Date.now()
    },
    success: () => {
      console.log('[VisitorNotification] 预约取消通知发送成功:', appointmentId);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 预约取消通知发送失败:', error);
    }
  });
}

// ==================== 访客到达/离开通知 ====================

/**
 * 发送访客到达通知
 * @param {Object} registration 登记信息
 * @param {string} registration.visitorName 访客姓名
 * @param {string} registration.visiteeName 被访人姓名
 * @param {string} registration.checkInTime 签到时间
 * @param {string} registration.photoUrl 照片URL
 */
export function sendVisitorArrivedNotification(registration) {
  const { visitorName, visiteeName, checkInTime, registrationId, photoUrl } = registration;

  uni.createPushMessage({
    title: '🚪 访客已到达',
    content: `${visitorName}已到达，正在等候${visiteeName}`,
    payload: {
      type: 'VISITOR_ARRIVED',
      registrationId,
      photoUrl,
      timestamp: Date.now()
    },
    sound: 'beep',
    success: () => {
      console.log('[VisitorNotification] 访客到达通知发送成功:', registrationId);
      // 连续震动3次
      uni.vibrateShort && uni.vibrateShort([200, 100, 200, 100, 200]);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 访客到达通知发送失败:', error);
    }
  });
}

/**
 * 发送访客离开通知
 * @param {Object} registration 登记信息
 * @param {string} registration.visitorName 访客姓名
 * @param {string} registration.checkOutTime 签退时间
 */
export function sendVisitorDepartedNotification(registration) {
  const { visitorName, checkOutTime, registrationId } = registration;

  uni.createPushMessage({
    title: '👋 访客已离开',
    content: `${visitorName}已完成访问，于${checkOutTime}离开`,
    payload: {
      type: 'VISITOR_DEPARTED',
      registrationId,
      timestamp: Date.now()
    },
    success: () => {
      console.log('[VisitorNotification] 访客离开通知发送成功:', registrationId);
      uni.vibrateShort && uni.vibrateShort([100]);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 访客离开通知发送失败:', error);
    }
  });
}

// ==================== 物品寄存通知 ====================

/**
 * 发送物品寄存通知
 * @param {Object} item 物品信息
 * @param {string} item.itemName 物品名称
 * @param {string} item.logisticsNo 物流单号
 */
export function sendItemDepositedNotification(item) {
  const { itemName, logisticsNo, registrationId } = item;

  uni.createPushMessage({
    title: '📦 物品已寄存',
    content: `访客物品"${itemName}"已寄存，单号：${logisticsNo}`,
    payload: {
      type: 'ITEM_DEPOSITED',
      logisticsNo,
      registrationId,
      timestamp: Date.now()
    },
    success: () => {
      console.log('[VisitorNotification] 物品寄存通知发送成功:', logisticsNo);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 物品寄存通知发送失败:', error);
    }
  });
}

/**
 * 发送物品领取通知
 * @param {Object} item 物品信息
 * @param {string} item.itemName 物品名称
 * @param {string} item.logisticsNo 物流单号
 */
export function sendItemPickedUpNotification(item) {
  const { itemName, logisticsNo, registrationId } = item;

  uni.createPushMessage({
    title: '✅ 物品已领取',
    content: `访客物品"${itemName}"已被领取，单号：${logisticsNo}`,
    payload: {
      type: 'ITEM_PICKED_UP',
      logisticsNo,
      registrationId,
      timestamp: Date.now()
    },
    success: () => {
      console.log('[VisitorNotification] 物品领取通知发送成功:', logisticsNo);
    },
    fail: (error) => {
      console.error('[VisitorNotification] 物品领取通知发送失败:', error);
    }
  });
}

// ==================== WebSocket实时通知 ====================

/**
 * WebSocket连接管理器
 */
export class VisitorWebSocketManager {
  constructor() {
    this.ws = null;
    this.url = '';
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.listeners = {};
    this.isConnected = false;
    this.subscriptions = new Set();
  }

  /**
   * 连接WebSocket
   * @param {string} wsUrl WebSocket地址
   * @param {number} userId 用户ID
   */
  connect(wsUrl, userId) {
    this.url = `${wsUrl}?userId=${userId}`;

    this.ws = uni.connectSocket({
      url: this.url,
      success: () => {
        console.log('[VisitorWS] 连接成功');
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.onOpen();
      },
      fail: (error) => {
        console.error('[VisitorWS] 连接失败:', error);
        this.isConnected = false;
        this.onError(error);
        this.reconnect();
      }
    });

    // 监听消息
    uni.onSocketMessage((res) => {
      try {
        const data = JSON.parse(res.data);
        this.onMessage(data);
      } catch (error) {
        console.error('[VisitorWS] 消息解析失败:', error);
      }
    });

    // 监听关闭
    uni.onSocketClose(() => {
      console.log('[VisitorWS] 连接关闭');
      this.isConnected = false;
      this.onClose();
      this.reconnect();
    });
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`[VisitorWS] 重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

      setTimeout(() => {
        const userId = this.getUserIdFromUrl();
        if (userId) {
          this.connect(this.getWsUrlFromUrl(), userId);
        }
      }, this.reconnectInterval);
    } else {
      console.error('[VisitorWS] 达到最大重连次数，停止重连');
    }
  }

  /**
   * 订阅访客相关主题
   * @param {string} topic 主题名称（APPOINTMENT, APPROVAL, VISITOR等）
   */
  subscribe(topic) {
    if (this.isConnected) {
      this.subscriptions.add(topic);
      this.send({
        type: 'SUBSCRIBE',
        topic: topic
      });
      console.log('[VisitorWS] 订阅主题:', topic);
    }
  }

  /**
   * 取消订阅
   * @param {string} topic 主题名称
   */
  unsubscribe(topic) {
    if (this.isConnected) {
      this.subscriptions.delete(topic);
      this.send({
        type: 'UNSUBSCRIBE',
        topic: topic
      });
      console.log('[VisitorWS] 取消订阅主题:', topic);
    }
  }

  /**
   * 发送消息
   * @param {Object} data 消息数据
   */
  send(data) {
    if (this.isConnected) {
      uni.sendSocketMessage({
        data: JSON.stringify(data),
        success: () => {
          console.log('[VisitorWS] 消息发送成功:', data);
        },
        fail: (error) => {
          console.error('[VisitorWS] 消息发送失败:', error);
        }
      });
    } else {
      console.warn('[VisitorWS] 未连接，无法发送消息');
    }
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.ws) {
      uni.closeSocket();
      this.ws = null;
      this.isConnected = false;
      this.subscriptions.clear();
    }
  }

  /**
   * 添加消息监听器
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(callback);
  }

  /**
   * 移除消息监听器
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  off(event, callback) {
    if (this.listeners[event]) {
      const index = this.listeners[event].indexOf(callback);
      if (index > -1) {
        this.listeners[event].splice(index, 1);
      }
    }
  }

  /**
   * 连接成功回调
   */
  onOpen() {
    this.emit('connected');

    // 自动重新订阅之前的主题
    this.subscriptions.forEach(topic => {
      this.subscribe(topic);
    });
  }

  /**
   * 连接关闭回调
   */
  onClose() {
    this.emit('disconnected');
  }

  /**
   * 错误回调
   * @param {Object} error 错误信息
   */
  onError(error) {
    this.emit('error', error);
  }

  /**
   * 消息处理
   * @param {Object} data 消息数据
   */
  onMessage(data) {
    console.log('[VisitorWS] 收到消息:', data);

    // 根据消息类型分发到对应监听器
    if (data.type && this.listeners[data.type]) {
      this.listeners[data.type].forEach(callback => {
        callback(data.data);
      });
    }

    // 通用消息监听器
    if (this.listeners['message']) {
      this.listeners['message'].forEach(callback => {
        callback(data);
      });
    }

    // 自动发送本地通知
    this.handleAutoNotification(data);
  }

  /**
   * 自动发送本地通知
   * @param {Object} data 消息数据
   */
  handleAutoNotification(data) {
    switch (data.type) {
      case 'APPOINTMENT_CREATED':
        sendAppointmentCreatedNotification(data.data);
        break;
      case 'APPOINTMENT_APPROVED':
        sendAppointmentApprovedNotification(data.data);
        break;
      case 'APPOINTMENT_REJECTED':
        sendAppointmentRejectedNotification(data.data);
        break;
      case 'APPOINTMENT_CANCELLED':
        sendAppointmentCancelledNotification(data.data);
        break;
      case 'VISITOR_ARRIVED':
        sendVisitorArrivedNotification(data.data);
        break;
      case 'VISITOR_DEPARTED':
        sendVisitorDepartedNotification(data.data);
        break;
      case 'ITEM_DEPOSITED':
        sendItemDepositedNotification(data.data);
        break;
      case 'ITEM_PICKED_UP':
        sendItemPickedUpNotification(data.data);
        break;
    }
  }

  /**
   * 触发事件
   * @param {string} event 事件名称
   * @param {*} data 事件数据
   */
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        callback(data);
      });
    }
  }

  /**
   * 从URL中提取用户ID
   */
  getUserIdFromUrl() {
    const match = this.url.match(/userId=(\d+)/);
    return match ? match[1] : null;
  }

  /**
   * 从URL中提取WebSocket地址
   */
  getWsUrlFromUrl() {
    return this.url.split('?')[0];
  }
}

// 导出WebSocket实例
export const visitorWSManager = new VisitorWebSocketManager();

// ==================== 导出 ====================

export default {
  // 权限管理
  requestNotificationPermission,
  checkNotificationPermission,

  // 预约通知
  sendAppointmentCreatedNotification,
  sendAppointmentApprovedNotification,
  sendAppointmentRejectedNotification,
  sendAppointmentCancelledNotification,

  // 访客动态通知
  sendVisitorArrivedNotification,
  sendVisitorDepartedNotification,

  // 物品通知
  sendItemDepositedNotification,
  sendItemPickedUpNotification,

  // WebSocket
  VisitorWebSocketManager,
  visitorWSManager
};
