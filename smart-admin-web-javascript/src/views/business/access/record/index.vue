<!--
  * 门禁通行记录页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-record-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h2>通行记录</h2>
        <p>查看和管理门禁通行记录、异常告警和数据统计</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出记录
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <a-button type="primary" @click="toggleRealTime">
            <template #icon><WifiOutlined /></template>
            {{ realTimeEnabled ? '停止实时' : '开始实时' }}
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="今日通行"
              :value="stats?.totalAccessCount || 0"
              :loading="statsLoading"
            >
              <template #prefix><LoginOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card success">
            <a-statistic
              title="成功通行"
              :value="stats?.successCount || 0"
              :loading="statsLoading"
            >
              <template #prefix><CheckCircleOutlined /></template>
            </a-statistic>
            <div class="rate">{{ successRate }}%</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card failed">
            <a-statistic
              title="失败通行"
              :value="stats?.failedCount || 0"
              :loading="statsLoading"
            >
              <template #prefix><CloseCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card abnormal">
            <a-statistic
              title="异常记录"
              :value="stats?.abnormalCount || 0"
              :loading="statsLoading"
            >
              <template #icon><ExclamationCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="queryForm"
        layout="inline"
        @finish="handleSearch"
      >
        <a-form-item label="用户姓名" name="userName">
          <a-input
            v-model:value="queryForm.userName"
            placeholder="请输入用户姓名"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="卡号" name="userCardNumber">
          <a-input
            v-model:value="queryForm.userCardNumber"
            placeholder="请输入卡号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="设备名称" name="deviceName">
          <a-input
            v-model:value="queryForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="通行类型" name="accessType">
          <a-select
            v-model:value="queryForm.accessType"
            placeholder="请选择通行类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="in">进入</a-select-option>
            <a-select-option value="out">离开</a-select-option>
            <a-select-option value="pass">通过</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="通行结果" name="accessResult">
          <a-select
            v-model:value="queryForm.accessResult"
            placeholder="请选择通行结果"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="success">成功</a-select-option>
            <a-select-option value="failed">失败</a-select-option>
            <a-select-option value="denied">拒绝</a-select-option>
            <a-select-option value="timeout">超时</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="通行时间" name="accessTimeRange">
          <a-range-picker
            v-model:value="queryForm.accessTimeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="recordLoading">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ClearOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 实时记录指示器 -->
    <div v-if="realTimeEnabled" class="real-time-indicator">
      <a-badge status="processing" />
      <span>实时更新中 - 最新记录时间: {{ latestRecordTime }}</span>
    </div>

    <!-- 通行记录表格 -->
    <a-card :bordered="false">
      <!-- 表格工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <a-space>
            <a-button @click="handleShowAbnormal">
              <template #icon><ExclamationCircleOutlined /></template>
              异常记录
            </a-button>
            <a-button @click="handleShowStats">
              <template #icon><BarChartOutlined /></template>
              统计分析
            </a-button>
            <a-button @click="handleShowHeatmap">
              <template #icon><HeatMapOutlined /></template>
              热力图
            </a-button>
          </a-space>
        </div>
        <div class="toolbar-right">
          <a-radio-group v-model:value="tableSize" button-style="solid">
            <a-radio-button value="small">紧凑</a-radio-button>
            <a-radio-button value="middle">默认</a-radio-button>
            <a-radio-button value="large">宽松</a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <!-- 记录表格 -->
      <a-table
        :columns="tableColumns"
        :data-source="displayRecords"
        :loading="recordLoading"
        :pagination="pagination"
        :size="tableSize"
        :row-class-name="getRowClassName"
        row-key="recordId"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 通行结果 -->
        <template #accessResult="{ record }">
          <a-tag
            :color="getResultColor(record.accessResult)"
            :icon="getResultIcon(record.accessResult)"
          >
            {{ getResultText(record.accessResult) }}
          </a-tag>
          <a-tag v-if="record.isAbnormal" color="red" size="small">异常</a-tag>
        </template>

        <!-- 验证方式 -->
        <template #verificationMethod="{ record }">
          <a-tag color="blue">
            {{ getVerificationMethodText(record.verificationMethod) }}
          </a-tag>
        </template>

        <!-- 通行时间 -->
        <template #accessTime="{ record }">
          <div class="access-time">
            <div>{{ formatDateTime(record.accessTime) }}</div>
            <div class="time-ago">{{ getTimeAgo(record.accessTime) }}</div>
          </div>
        </template>

        <!-- 用户照片 -->
        <template #photo="{ record }">
          <a-image
            v-if="record.photoUrl"
            :src="record.photoUrl"
            :width="40"
            :height="40"
            style="border-radius: 4px;"
            :preview="{ mask: '查看照片' }"
          />
          <a-avatar v-else :size="40" icon="user" />
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              <template #icon><EyeOutlined /></template>
              详情
            </a-button>
            <a-button
              v-if="record.isAbnormal && record.processedStatus === 'unprocessed'"
              type="link"
              size="small"
              @click="handleProcessAbnormal(record)"
            >
              <template #icon><ToolOutlined /></template>
              处理
            </a-button>
            <a-dropdown>
              <a-button type="link" size="small">
                更多
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleViewUserInfo(record.userId)">
                    <UserOutlined />
                    查看用户信息
                  </a-menu-item>
                  <a-menu-item @click="handleViewDeviceInfo(record.deviceId)">
                    <DesktopOutlined />
                    查看设备信息
                  </a-menu-item>
                  <a-menu-item @click="handleExportRecord(record)">
                    <ExportOutlined />
                    导出记录
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item
                    @click="handleDeleteRecord(record)"
                    :disabled="!hasDeletePermission"
                  >
                    <DeleteOutlined />
                    删除记录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 记录详情弹窗 -->
    <RecordDetailModal
      v-model:visible="detailModalVisible"
      :record="currentRecord"
    />

    <!-- 异常记录处理弹窗 -->
    <AbnormalProcessModal
      v-model:visible="abnormalModalVisible"
      :record="currentRecord"
      @success="handleAbnormalProcessSuccess"
    />

    <!-- 统计分析弹窗 -->
    <StatisticsModal
      v-model:visible="statsModalVisible"
    />

    <!-- 热力图弹窗 -->
    <HeatmapModal
      v-model:visible="heatmapModalVisible"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
  import { message } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import relativeTime from 'dayjs/plugin/relativeTime';
  import 'dayjs/locale/zh-cn';
  import {
    ExportOutlined,
    ReloadOutlined,
    WifiOutlined,
    LoginOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    ExclamationCircleOutlined,
    SearchOutlined,
    ClearOutlined,
    BarChartOutlined,
    HeatMapOutlined,
    EyeOutlined,
    ToolOutlined,
    DownOutlined,
    UserOutlined,
    DesktopOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import { accessRecordApi } from '/@/api/business/access/record-api';
  import { useAccessMonitorStore } from '/@/store/modules/business/access-monitor';
  import { formatDateTime } from '/@/lib/format';
  import RecordDetailModal from './components/RecordDetailModal.vue';
  import AbnormalProcessModal from './components/AbnormalProcessModal.vue';
  import StatisticsModal from './components/StatisticsModal.vue';
  import HeatmapModal from './components/HeatmapModal.vue';

  // 配置dayjs
  dayjs.extend(relativeTime);
  dayjs.locale('zh-cn');

  // 状态管理
  const monitorStore = useAccessMonitorStore();

  // 响应式数据
  const tableSize = ref('middle');
  const realTimeEnabled = ref(false);
  const recordList = ref([]);
  const recordTotal = ref(0);
  const recordLoading = ref(false);
  const statsLoading = ref(false);
  const stats = ref(null);
  const latestRecordTime = ref('');
  const detailModalVisible = ref(false);
  const abnormalModalVisible = ref(false);
  const statsModalVisible = ref(false);
  const heatmapModalVisible = ref(false);
  const currentRecord = ref(null);
  const hasDeletePermission = ref(false); // 根据实际权限控制

  // 查询表单
  const queryForm = reactive({
    userName: '',
    userCardNumber: '',
    deviceName: '',
    accessType: undefined,
    accessResult: undefined,
    accessTimeRange: [],
  });

  // 表格列定义
  const tableColumns = [
    {
      title: '照片',
      dataIndex: 'photoUrl',
      key: 'photo',
      width: 80,
      slots: { customRender: 'photo' },
    },
    {
      title: '用户姓名',
      dataIndex: 'userName',
      key: 'userName',
      width: 120,
      fixed: 'left',
    },
    {
      title: '卡号',
      dataIndex: 'userCardNumber',
      key: 'userCardNumber',
      width: 150,
    },
    {
      title: '设备位置',
      dataIndex: 'location',
      key: 'location',
      width: 180,
    },
    {
      title: '通行类型',
      dataIndex: 'accessType',
      key: 'accessType',
      width: 80,
      customRender: ({ text }) => {
        const typeMap = {
          in: '进入',
          out: '离开',
          pass: '通过',
        };
        return typeMap[text] || text;
      },
    },
    {
      title: '通行结果',
      dataIndex: 'accessResult',
      key: 'accessResult',
      width: 120,
      slots: { customRender: 'accessResult' },
    },
    {
      title: '验证方式',
      dataIndex: 'verificationMethod',
      key: 'verificationMethod',
      width: 120,
      slots: { customRender: 'verificationMethod' },
    },
    {
      title: '通行时间',
      dataIndex: 'accessTime',
      key: 'accessTime',
      width: 160,
      slots: { customRender: 'accessTime' },
    },
    {
      title: '处理状态',
      dataIndex: 'processedStatus',
      key: 'processedStatus',
      width: 100,
      customRender: ({ record }) => {
        if (!record.isAbnormal) return '-';
        const statusMap = {
          unprocessed: { text: '未处理', color: 'orange' },
          processed: { text: '已处理', color: 'green' },
          ignored: { text: '已忽略', color: 'default' },
        };
        const status = statusMap[record.processedStatus] || statusMap.unprocessed;
        return h('a-tag', { color: status.color }, status.text);
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      slots: { customRender: 'action' },
    },
  ];

  // 分页配置
  const pagination = computed(() => ({
    current: queryForm.pageNum || 1,
    pageSize: queryForm.pageSize || 20,
    total: recordTotal.value,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条记录`,
  }));

  // 显示的记录数据
  const displayRecords = computed(() => {
    return recordList.value;
  });

  // 成功率
  const successRate = computed(() => {
    if (!stats.value || stats.value.totalAccessCount === 0) return 0;
    return Math.round((stats.value.successCount / stats.value.totalAccessCount) * 100);
  });

  // 方法
  const handleSearch = () => {
    const params = {
      ...queryForm,
      pageNum: 1,
      pageSize: queryForm.pageSize || 20,
    };

    // 处理时间范围
    if (queryForm.accessTimeRange && queryForm.accessTimeRange.length === 2) {
      params.accessTimeStart = queryForm.accessTimeRange[0].format('YYYY-MM-DD HH:mm:ss');
      params.accessTimeEnd = queryForm.accessTimeRange[1].format('YYYY-MM-DD HH:mm:ss');
    }

    fetchRecordList(params);
  };

  const handleReset = () => {
    Object.assign(queryForm, {
      userName: '',
      userCardNumber: '',
      deviceName: '',
      accessType: undefined,
      accessResult: undefined,
      accessTimeRange: [],
      pageNum: 1,
      pageSize: 20,
    });
    fetchRecordList();
  };

  const handleRefresh = () => {
    fetchRecordList();
    fetchStats();
  };

  const toggleRealTime = () => {
    realTimeEnabled.value = !realTimeEnabled.value;
    if (realTimeEnabled.value) {
      startRealTimeUpdates();
      message.success('已开启实时更新');
    } else {
      stopRealTimeUpdates();
      message.info('已停止实时更新');
    }
  };

  const startRealTimeUpdates = () => {
    // 启动WebSocket或定时器获取实时数据
    monitorStore.initWebSocket();
  };

  const stopRealTimeUpdates = () => {
    monitorStore.closeWebSocket();
  };

  const handleExport = () => {
    const params = {
      ...queryForm,
      format: 'excel',
    };

    if (queryForm.accessTimeRange && queryForm.accessTimeRange.length === 2) {
      params.accessTimeStart = queryForm.accessTimeRange[0].format('YYYY-MM-DD HH:mm:ss');
      params.accessTimeEnd = queryForm.accessTimeRange[1].format('YYYY-MM-DD HH:mm:ss');
    }

    accessRecordApi.exportRecords(params)
      .then(response => {
        if (response.code === 1) {
          message.success('导出成功');
          // 处理文件下载
          const link = document.createElement('a');
          link.href = response.data.downloadUrl;
          link.download = response.data.fileName;
          link.click();
        }
      })
      .catch(error => {
        console.error('导出失败:', error);
        message.error('导出失败');
      });
  };

  const handleTableChange = (paginationConfig) => {
    queryForm.pageNum = paginationConfig.current;
    queryForm.pageSize = paginationConfig.pageSize;
    fetchRecordList();
  };

  const handleViewDetail = (record) => {
    currentRecord.value = record;
    detailModalVisible.value = true;
  };

  const handleProcessAbnormal = (record) => {
    currentRecord.value = record;
    abnormalModalVisible.value = true;
  };

  const handleShowAbnormal = () => {
    // 显示异常记录
    queryForm.isAbnormal = true;
    fetchRecordList();
  };

  const handleShowStats = () => {
    statsModalVisible.value = true;
  };

  const handleShowHeatmap = () => {
    heatmapModalVisible.value = true;
  };

  const handleViewUserInfo = (userId) => {
    message.info('查看用户信息功能开发中...');
  };

  const handleViewDeviceInfo = (deviceId) => {
    message.info('查看设备信息功能开发中...');
  };

  const handleExportRecord = (record) => {
    message.info('单条记录导出功能开发中...');
  };

  const handleDeleteRecord = (record) => {
    message.warning('删除记录功能需要管理员权限');
  };

  const handleAbnormalProcessSuccess = () => {
    abnormalModalVisible.value = false;
    fetchRecordList();
  };

  // 获取通行记录列表
  const fetchRecordList = async (params = {}) => {
    recordLoading.value = true;
    try {
      const queryParams = {
        pageNum: queryForm.pageNum || 1,
        pageSize: queryForm.pageSize || 20,
        ...params,
      };

      const response = await accessRecordApi.queryRecordList(queryParams);
      if (response.code === 1) {
        recordList.value = response.data.list || [];
        recordTotal.value = response.data.total || 0;

        // 更新最新记录时间
        if (recordList.value.length > 0) {
          latestRecordTime.value = formatDateTime(recordList.value[0].accessTime);
        }
      }
    } catch (error) {
      console.error('获取通行记录失败:', error);
      message.error('获取通行记录失败');
    } finally {
      recordLoading.value = false;
    }
  };

  // 获取统计数据
  const fetchStats = async () => {
    statsLoading.value = true;
    try {
      const response = await accessRecordApi.getAccessStats({
        timeRange: 'today',
      });
      if (response.code === 1) {
        stats.value = response.data;
      }
    } catch (error) {
      console.error('获取统计数据失败:', error);
    } finally {
      statsLoading.value = false;
    }
  };

  // 辅助方法
  const getRowClassName = (record) => {
    if (record.isAbnormal) {
      return 'abnormal-row';
    }
    return '';
  };

  const getResultColor = (result) => {
    const colorMap = {
      success: 'green',
      failed: 'red',
      denied: 'orange',
      timeout: 'default',
    };
    return colorMap[result] || 'default';
  };

  const getResultIcon = (result) => {
    const iconMap = {
      success: 'check-circle',
      failed: 'close-circle',
      denied: 'stop',
      timeout: 'clock-circle',
    };
    return iconMap[result] || 'question-circle';
  };

  const getResultText = (result) => {
    const textMap = {
      success: '成功',
      failed: '失败',
      denied: '拒绝',
      timeout: '超时',
    };
    return textMap[result] || result;
  };

  const getVerificationMethodText = (method) => {
    const textMap = {
      card_only: '刷卡',
      card_password: '刷卡+密码',
      card_face: '刷卡+人脸',
      face_only: '人脸',
      fingerprint_only: '指纹',
      password_only: '密码',
      qr_code_only: '二维码',
    };
    return textMap[method] || method;
  };

  const getTimeAgo = (time) => {
    return dayjs(time).fromNow();
  };

  // 监听WebSocket消息
  monitorStore.$subscribe((mutation, state) => {
    if (realTimeEnabled.value && state.realTimeAccessData.length > 0) {
      // 添加新的实时记录到列表顶部
      const newRecords = state.realTimeAccessData.slice(0, 5);
      recordList.value = [...newRecords, ...recordList.value.slice(0, 95)];
      latestRecordTime.value = formatDateTime(newRecords[0].accessTime);
    }
  });

  // 生命周期
  onMounted(async () => {
    await Promise.all([
      fetchRecordList(),
      fetchStats(),
    ]);
  });

  onUnmounted(() => {
    stopRealTimeUpdates();
  });
</script>

<style lang="less" scoped>
  .access-record-page {
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

    .stats-cards {
      margin-bottom: 24px;

      .stat-card {
        text-align: center;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.success {
          border-left: 4px solid #52c41a;
        }

        &.failed {
          border-left: 4px solid #ff4d4f;
        }

        &.abnormal {
          border-left: 4px solid #faad14;
        }

        .rate {
          margin-top: 8px;
          font-size: 16px;
          font-weight: 600;
          color: #52c41a;
        }
      }
    }

    .search-card {
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .real-time-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      margin-bottom: 16px;
      background: #f0f9ff;
      border: 1px solid #91d5ff;
      border-radius: 4px;
      color: #1890ff;
      font-size: 14px;
    }

    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .toolbar-left {
        // 左侧工具栏样式
      }

      .toolbar-right {
        // 右侧工具栏样式
      }
    }

    .access-time {
      .time-ago {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }

    // 异常记录行样式
    :deep(.ant-table-tbody .abnormal-row) {
      background-color: #fff2f0;
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .stats-cards {
        .ant-col {
          margin-bottom: 16px;
        }
      }

      .table-toolbar {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
    }
  }
</style>