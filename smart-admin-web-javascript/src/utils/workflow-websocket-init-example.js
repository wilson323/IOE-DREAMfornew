/**
 * 工作流WebSocket初始化示例代码
 * 
 * 此文件仅作为参考示例，展示如何在应用启动时初始化WebSocket连接
 * 实际使用时，请将相关代码添加到 main.js 或合适的位置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

// ==================== 示例1: 在main.js的getLoginInfo方法中初始化 ====================

/*
import { initWorkflowWebSocketConnection } from '/@/utils/workflow-websocket-init';
import { useUserStore } from '/@/store/modules/system/user';

async function getLoginInfo() {
  try {
    const res = await loginApi.getLoginInfo();
    const dictRes = await dictApi.getAllDictData();
    
    // 构建系统的路由
    let menuRouterList = res.data.menuList.filter((e) => e.path || e.frameUrl);
    buildRoutes(menuRouterList);
    initVue();
    
    // 初始化数据字典
    useDictStore().initData(dictRes.data);
    
    // 更新用户信息到pinia
    const userStore = useUserStore();
    userStore.setUserLoginInfo(res.data);
    
    // 初始化工作流WebSocket连接
    // 注意：确保在用户信息设置后再初始化WebSocket，因为需要token
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws/workflow';
    const token = userStore.getToken();
    
    if (token) {
      // 使用STOMP协议（推荐）
      initWorkflowWebSocketConnection(wsUrl, token, true);
      
      // 或者使用原生WebSocket
      // initWorkflowWebSocketConnection(wsUrl, token, false);
    }
  } catch (e) {
    message.error(e.data ? e.data.msg : e.message);
    smartSentry.captureError(e);
    initVue();
  }
}
*/

// ==================== 示例2: 在路由守卫中初始化 ====================

/*
// router/index.js 或 router/guard.js

import { initWorkflowWebSocketConnection, closeWorkflowWebSocketConnection } from '/@/utils/workflow-websocket-init';
import { useUserStore } from '/@/store/modules/system/user';

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.getToken();
  
  // 如果用户已登录且有token，初始化WebSocket
  if (token && !workflowWebSocketInitialized) {
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws/workflow';
    initWorkflowWebSocketConnection(wsUrl, token, true);
    workflowWebSocketInitialized = true;
  }
  
  // 如果用户退出登录，关闭WebSocket
  if (!token && workflowWebSocketInitialized) {
    closeWorkflowWebSocketConnection();
    workflowWebSocketInitialized = false;
  }
  
  next();
});
*/

// ==================== 示例3: 在用户登录成功后初始化 ====================

/*
// views/system/login/login.vue 或 login组件

import { initWorkflowWebSocketConnection } from '/@/utils/workflow-websocket-init';

async function handleLogin() {
  try {
    const response = await loginApi.login(loginForm);
    
    if (response.code === 1 || response.code === 200) {
      // 登录成功
      const userStore = useUserStore();
      userStore.setUserLoginInfo(response.data);
      
      // 初始化工作流WebSocket连接
      const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws/workflow';
      const token = userStore.getToken();
      
      if (token) {
        initWorkflowWebSocketConnection(wsUrl, token, true);
      }
      
      // 跳转到首页
      router.push('/');
    }
  } catch (err) {
    console.error('登录失败:', err);
  }
}
*/

// ==================== 示例4: 在用户退出时关闭WebSocket ====================

/*
// views/system/login/login.vue 或其他退出登录的地方

import { closeWorkflowWebSocketConnection } from '/@/utils/workflow-websocket-init';

async function handleLogout() {
  try {
    await loginApi.logout();
    
    // 关闭WebSocket连接
    closeWorkflowWebSocketConnection();
    
    // 清除用户信息
    const userStore = useUserStore();
    userStore.logout();
    
    // 跳转到登录页
    router.push('/login');
  } catch (err) {
    console.error('退出登录失败:', err);
  }
}
*/

// ==================== 环境变量配置示例 ====================

/*
// .env.development
VITE_WS_URL=http://localhost:8080/ws/workflow
VITE_API_BASE_URL=http://localhost:8080

// .env.production
VITE_WS_URL=https://your-domain.com/ws/workflow
VITE_API_BASE_URL=https://your-domain.com
*/

