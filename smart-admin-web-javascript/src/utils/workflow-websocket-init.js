/**
 * 工作流WebSocket初始化工具
 * 用于在应用启动时自动初始化WebSocket连接
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { message } from 'ant-design-vue';
import { initWorkflowWebSocketStomp, getWorkflowWebSocketStomp } from './workflow-websocket-stomp';
import { initWorkflowWebSocket, getWorkflowWebSocket } from './workflow-websocket';
import { useWorkflowStore } from '/@/store/modules/business/workflow';

/**
 * 初始化工作流WebSocket连接
 * 优先尝试STOMP协议，失败则降级到原生WebSocket
 * 
 * @param {String} wsUrl - WebSocket服务器地址
 * @param {String} token - 认证令牌
 * @param {Boolean} useStomp - 是否使用STOMP协议（默认true）
 */
export function initWorkflowWebSocketConnection(wsUrl, token, useStomp = true) {
  if (!wsUrl || !token) {
    console.warn('WebSocket初始化参数不完整，跳过初始化');
    return;
  }

  const workflowStore = useWorkflowStore();

  try {
    if (useStomp) {
      // 尝试使用STOMP协议
      initWorkflowWebSocketStomp(wsUrl, token);
      const ws = getWorkflowWebSocketStomp();

      // 监听连接成功
      ws.on('connected', () => {
        console.log('工作流WebSocket连接已建立（STOMP协议）');
        if (workflowStore.setWebSocketConnected) {
          workflowStore.setWebSocketConnected(true);
        } else {
          workflowStore.wsConnected = true;
          workflowStore.webSocketConnected = true;
        }
      });

      // 监听新任务通知
      ws.on('new-task', (data) => {
        console.log('收到新任务通知:', data);
        message.info(`您有新的待办任务：${data.taskName || '新任务'}`);
        
        // 刷新待办任务列表
        workflowStore.fetchPendingTaskList({
          pageNum: 1,
          pageSize: 20,
        });
      });

      // 监听任务状态变更
      ws.on('task-status-changed', (data) => {
        console.log('任务状态已变更:', data);
        // 刷新任务列表
        workflowStore.fetchPendingTaskList({
          pageNum: 1,
          pageSize: 20,
        });
      });

      // 监听流程实例状态变更
      ws.on('instance-status-changed', (data) => {
        console.log('流程实例状态已变更:', data);
        // 刷新流程实例列表
        workflowStore.fetchInstanceList({
          pageNum: 1,
          pageSize: 20,
        });
      });

      // 监听连接错误
      ws.on('error', (error) => {
        console.error('WebSocket连接错误:', error);
        if (workflowStore.setWebSocketConnected) {
          workflowStore.setWebSocketConnected(false);
        } else {
          workflowStore.wsConnected = false;
          workflowStore.webSocketConnected = false;
        }
      });

      // 监听连接断开
      ws.on('disconnected', () => {
        console.log('WebSocket连接已断开');
        if (workflowStore.setWebSocketConnected) {
          workflowStore.setWebSocketConnected(false);
        } else {
          workflowStore.wsConnected = false;
          workflowStore.webSocketConnected = false;
        }
      });

    } else {
      // 使用原生WebSocket
      initWorkflowWebSocket(wsUrl, token);
      const ws = getWorkflowWebSocket();

      // 监听新任务通知
      ws.on('NEW_TASK', (data) => {
        console.log('收到新任务通知:', data);
        message.info(`您有新的待办任务：${data.taskName || '新任务'}`);
        workflowStore.fetchPendingTaskList({
          pageNum: 1,
          pageSize: 20,
        });
      });

      // 监听任务状态变更
      ws.on('TASK_STATUS_CHANGED', (data) => {
        console.log('任务状态已变更:', data);
        workflowStore.fetchPendingTaskList({
          pageNum: 1,
          pageSize: 20,
        });
      });

      // 监听连接错误
      ws.on('error', (error) => {
        console.error('WebSocket连接错误:', error);
        if (workflowStore.setWebSocketConnected) {
          workflowStore.setWebSocketConnected(false);
        } else {
          workflowStore.wsConnected = false;
          workflowStore.webSocketConnected = false;
        }
      });
    }
  } catch (err) {
    console.error('初始化工作流WebSocket连接失败:', err);
    if (workflowStore.setWebSocketConnected) {
      workflowStore.setWebSocketConnected(false);
    } else {
      workflowStore.wsConnected = false;
      workflowStore.webSocketConnected = false;
    }
  }
}

/**
 * 关闭工作流WebSocket连接
 */
export function closeWorkflowWebSocketConnection() {
  try {
    const wsStomp = getWorkflowWebSocketStomp();
    if (wsStomp && wsStomp.isConnected()) {
      wsStomp.disconnect();
    }
  } catch (err) {
    console.error('关闭STOMP WebSocket连接失败:', err);
  }

  try {
    const ws = getWorkflowWebSocket();
    if (ws && ws.isConnected()) {
      ws.disconnect();
    }
  } catch (err) {
    console.error('关闭原生WebSocket连接失败:', err);
  }

  const workflowStore = useWorkflowStore();
  if (workflowStore.setWebSocketConnected) {
    workflowStore.setWebSocketConnected(false);
  } else {
    workflowStore.wsConnected = false;
    workflowStore.webSocketConnected = false;
  }
}

/**
 * 重新连接工作流WebSocket
 * 
 * @param {String} wsUrl - WebSocket服务器地址
 * @param {String} token - 认证令牌
 * @param {Boolean} useStomp - 是否使用STOMP协议
 */
export function reconnectWorkflowWebSocket(wsUrl, token, useStomp = true) {
  closeWorkflowWebSocketConnection();
  setTimeout(() => {
    initWorkflowWebSocketConnection(wsUrl, token, useStomp);
  }, 1000);
}

