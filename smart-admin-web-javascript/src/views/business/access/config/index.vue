<!--
  * 门禁配置管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-config-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h2>门禁配置管理</h2>
        <p>管理门禁系统配置、时间策略、安防级别和设备参数</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleSaveAll">
            <template #icon><SaveOutlined /></template>
            保存所有配置
          </a-button>
          <a-button @click="handleReload">
            <template #icon><ReloadOutlined /></template>
            重新加载
          </a-button>
          <a-button @click="handleResetToDefault">
            <template #icon><UndoOutlined /></template>
            恢复默认
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 配置标签页 -->
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" type="card" @change="handleTabChange">
        <!-- 系统配置 -->
        <a-tab-pane key="system" tab="系统配置">
          <SystemConfig
            ref="systemConfigRef"
            v-model:config="systemConfig"
            @change="handleConfigChange"
          />
        </a-tab-pane>

        <!-- 时间策略 -->
        <a-tab-pane key="timeStrategy" tab="时间策略">
          <TimeStrategyConfig
            ref="timeStrategyRef"
            @change="handleConfigChange"
          />
        </a-tab-pane>

        <!-- 安防级别 -->
        <a-tab-pane key="security" tab="安防级别">
          <SecurityLevelConfig
            ref="securityLevelRef"
            @change="handleConfigChange"
          />
        </a-tab-pane>

        <!-- 设备类型 -->
        <a-tab-pane key="deviceType" tab="设备类型">
          <DeviceTypeConfig
            ref="deviceTypeRef"
            @change="handleConfigChange"
          />
        </a-tab-pane>

        <!-- 告警配置 -->
        <a-tab-pane key="alert" tab="告警配置">
          <AlertConfig
            ref="alertConfigRef"
            v-model:config="alertConfig"
            @change="handleConfigChange"
          />
        </a-tab-pane>

        <!-- 日志配置 -->
        <a-tab-pane key="log" tab="日志配置">
          <LogConfig
            ref="logConfigRef"
            v-model:config="logConfig"
            @change="handleConfigChange"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 配置保存提示 -->
    <div v-if="hasUnsavedChanges" class="save-indicator">
      <a-alert
        message="配置已修改"
        description="您有未保存的配置更改，请记得保存"
        type="warning"
        show-icon
        closable
        @close="handleCloseSaveIndicator"
      />
    </div>
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    SaveOutlined,
    ReloadOutlined,
    UndoOutlined,
  } from '@ant-design/icons-vue';
  import { accessConfigApi } from '/@/api/business/access/config-api';
  import SystemConfig from './components/SystemConfig.vue';
  import TimeStrategyConfig from './components/TimeStrategyConfig.vue';
  import SecurityLevelConfig from './components/SecurityLevelConfig.vue';
  import DeviceTypeConfig from './components/DeviceTypeConfig.vue';
  import AlertConfig from './components/AlertConfig.vue';
  import LogConfig from './components/LogConfig.vue';

  // 响应式数据
  const activeTab = ref('system');
  const systemConfigRef = ref();
  const timeStrategyRef = ref();
  const securityLevelRef = ref();
  const deviceTypeRef = ref();
  const alertConfigRef = ref();
  const logConfigRef = ref();

  // 配置数据
  const systemConfig = reactive({});
  const alertConfig = reactive({});
  const logConfig = reactive({});

  // 状态
  const loading = ref(false);
  const hasUnsavedChanges = ref(false);
  const originalConfigs = ref({});

  // 计算属性
  const isSystemConfigChanged = computed(() => {
    return JSON.stringify(systemConfig) !== JSON.stringify(originalConfigs.value.system || {});
  });

  const isAlertConfigChanged = computed(() => {
    return JSON.stringify(alertConfig) !== JSON.stringify(originalConfigs.value.alert || {});
  });

  const isLogConfigChanged = computed(() => {
    return JSON.stringify(logConfig) !== JSON.stringify(originalConfigs.value.log || {});
  });

  // 方法
  const loadConfigs = async () => {
    loading.value = true;
    try {
      await Promise.all([
        loadSystemConfig(),
        loadAlertConfig(),
        loadLogConfig(),
      ]);

      // 保存原始配置用于比较
      originalConfigs.value = {
        system: JSON.parse(JSON.stringify(systemConfig)),
        alert: JSON.parse(JSON.stringify(alertConfig)),
        log: JSON.parse(JSON.stringify(logConfig)),
      };

      hasUnsavedChanges.value = false;
    } catch (error) {
      console.error('加载配置失败:', error);
      message.error('加载配置失败');
    } finally {
      loading.value = false;
    }
  };

  const loadSystemConfig = async () => {
    try {
      const response = await accessConfigApi.getSystemConfig();
      if (response.code === 1) {
        Object.assign(systemConfig, response.data);
      }
    } catch (error) {
      console.error('加载系统配置失败:', error);
    }
  };

  const loadAlertConfig = async () => {
    try {
      const response = await accessConfigApi.getAlertConfig();
      if (response.code === 1) {
        Object.assign(alertConfig, response.data);
      }
    } catch (error) {
      console.error('加载告警配置失败:', error);
    }
  };

  const loadLogConfig = async () => {
    try {
      const response = await accessConfigApi.getLogConfig();
      if (response.code === 1) {
        Object.assign(logConfig, response.data);
      }
    } catch (error) {
      console.error('加载日志配置失败:', error);
    }
  };

  const handleSaveAll = async () => {
    if (!hasUnsavedChanges.value) {
      message.info('没有需要保存的配置更改');
      return;
    }

    Modal.confirm({
      title: '保存配置确认',
      content: '确定要保存所有配置更改吗？某些配置更改可能需要重启服务才能生效。',
      okText: '确认保存',
      cancelText: '取消',
      onOk: async () => {
        await saveAllConfigs();
      },
    });
  };

  const saveAllConfigs = async () => {
    loading.value = true;
    try {
      const promises = [];

      // 保存系统配置
      if (isSystemConfigChanged.value) {
        promises.push(accessConfigApi.updateSystemConfig(systemConfig));
      }

      // 保存告警配置
      if (isAlertConfigChanged.value) {
        promises.push(accessConfigApi.updateAlertConfig(alertConfig));
      }

      // 保存日志配置
      if (isLogConfigChanged.value) {
        promises.push(accessConfigApi.updateLogConfig(logConfig));
      }

      const results = await Promise.all(promises);
      const failedConfigs = results.filter(result => result.code !== 1);

      if (failedConfigs.length === 0) {
        message.success('所有配置保存成功');
        hasUnsavedChanges.value = false;

        // 更新原始配置
        originalConfigs.value = {
          system: JSON.parse(JSON.stringify(systemConfig)),
          alert: JSON.parse(JSON.stringify(alertConfig)),
          log: JSON.parse(JSON.stringify(logConfig)),
        };
      } else {
        message.error(`${failedConfigs.length} 个配置保存失败`);
      }
    } catch (error) {
      console.error('保存配置失败:', error);
      message.error('保存配置失败');
    } finally {
      loading.value = false;
    }
  };

  const handleReload = () => {
    Modal.confirm({
      title: '重新加载确认',
      content: '重新加载将丢失当前未保存的更改，确定要继续吗？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await loadConfigs();
        message.success('配置已重新加载');
      },
    });
  };

  const handleResetToDefault = () => {
    Modal.confirm({
      title: '恢复默认配置',
      content: '确定要将所有配置恢复为默认值吗？此操作不可恢复。',
      okText: '确认恢复',
      cancelText: '取消',
      okType: 'danger',
      onOk: () => {
        message.info('恢复默认配置功能开发中...');
      },
    });
  };

  const handleTabChange = (key) => {
    activeTab.value = key;
  };

  const handleConfigChange = () => {
    hasUnsavedChanges.value = true;
  };

  const handleCloseSaveIndicator = () => {
    hasUnsavedChanges.value = false;
  };

  const handleBeforeUnload = (event) => {
    if (hasUnsavedChanges.value) {
      event.preventDefault();
      event.returnValue = '您有未保存的配置更改，确定要离开吗？';
      return event.returnValue;
    }
  };

  // 监听配置变化
  const checkConfigChanges = () => {
    const changed = isSystemConfigChanged.value ||
                   isAlertConfigChanged.value ||
                   isLogConfigChanged.value;
    hasUnsavedChanges.value = changed;
  };

  // 生命周期
  onMounted(async () => {
    await loadConfigs();
    window.addEventListener('beforeunload', handleBeforeUnload);

    // 定时检查配置变化
    const checkInterval = setInterval(checkConfigChanges, 5000);

    // 清理定时器
    onBeforeUnmount(() => {
      clearInterval(checkInterval);
      window.removeEventListener('beforeunload', handleBeforeUnload);
    });
  });

  // 暴露方法给父组件
  defineExpose({
    loadConfigs,
    saveAllConfigs,
    hasUnsavedChanges,
  });
</script>

<style lang="less" scoped>
  .access-config-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }

    .save-indicator {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
      max-width: 400px;

      :deep(.ant-alert) {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }

    // 标签页样式优化
    :deep(.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab) {
      border-radius: 8px 8px 0 0;
      border: 1px solid #d9d9d9;
      background: #fafafa;
      margin-right: 2px;

      &.ant-tabs-tab-active {
        background: #fff;
        border-bottom-color: #fff;
      }
    }

    :deep(.ant-tabs-content-holder) {
      border: 1px solid #d9d9d9;
      border-top: none;
      border-radius: 0 0 8px 8px;
      background: #fff;
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .save-indicator {
        bottom: 16px;
        right: 16px;
        left: 16px;
        max-width: none;
      }
    }
  }
</style>