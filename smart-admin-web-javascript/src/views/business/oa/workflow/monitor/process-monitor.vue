<!--
  * 流程监控页
  * 提供工作流流程统计信息和监控功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="process-monitor-page">
    <!-- 统计信息卡片 -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :span="6">
        <a-card size="small">
          <a-statistic
            title="总流程数"
            :value="statistics.totalProcesses"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small">
          <a-statistic
            title="进行中"
            :value="statistics.runningProcesses"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small">
          <a-statistic
            title="已完成"
            :value="statistics.completedProcesses"
            :value-style="{ color: '#722ed1' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small">
          <a-statistic
            title="待办任务"
            :value="statistics.pendingTasks"
            :value-style="{ color: '#fa8c16' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户工作量统计 -->
    <a-card size="small" style="margin-top: 16px" title="用户工作量统计">
      <a-table
        row-key="userId"
        :columns="workloadColumns"
        :data-source="workloadList"
        :pagination="false"
        :loading="workloadLoading"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, text }">
          <template v-if="column.dataIndex === 'userName'">
            {{ text }}
          </template>
          <template v-else>
            {{ text || 0 }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { smartSentry } from '/@/lib/smart-sentry';

  const workflowStore = useWorkflowStore();

  const statistics = reactive({
    totalProcesses: 0,
    runningProcesses: 0,
    completedProcesses: 0,
    pendingTasks: 0,
  });

  const workloadList = ref([]);
  const workloadLoading = ref(false);

  // 工作量统计表格列
  const workloadColumns = ref([
    {
      title: '用户姓名',
      dataIndex: 'userName',
      width: 150,
    },
    {
      title: '待办任务数',
      dataIndex: 'pendingCount',
      width: 120,
    },
    {
      title: '已办任务数',
      dataIndex: 'completedCount',
      width: 120,
    },
    {
      title: '发起的流程数',
      dataIndex: 'startedCount',
      width: 120,
    },
    {
      title: '总任务数',
      dataIndex: 'totalCount',
      width: 120,
    },
  ]);

  /**
   * 查询流程统计信息
   */
  async function queryStatistics() {
    try {
      await workflowStore.fetchProcessStatistics({});
      const stats = workflowStore.processStatistics;
      if (stats) {
        statistics.totalProcesses = stats.totalProcesses || 0;
        statistics.runningProcesses = stats.runningProcesses || 0;
        statistics.completedProcesses = stats.completedProcesses || 0;
        statistics.pendingTasks = stats.pendingTasks || 0;
      }
    } catch (err) {
      console.error('查询流程统计信息失败:', err);
      smartSentry.captureError(err);
    }
  }

  /**
   * 查询用户工作量统计
   */
  async function queryWorkloadStatistics() {
    try {
      workloadLoading.value = true;
      // 注意：此功能需要后端提供相应的API接口
      // 如果需要查询特定用户的工作量，可以通过以下方式：
      // const response = await workflowApi.getUserWorkloadStatistics(userId, {});
      // workloadList.value = response.data || [];
      
      // 暂时使用空列表，待后端API实现后可取消注释上面的代码
      workloadList.value = [];
    } catch (err) {
      console.error('查询用户工作量统计失败:', err);
      smartSentry.captureError(err);
    } finally {
      workloadLoading.value = false;
    }
  }

  // 初始化
  onMounted(() => {
    queryStatistics();
    queryWorkloadStatistics();
  });
</script>

<style lang="less" scoped>
  .process-monitor-page {
    padding: 16px;

    .statistics-row {
      margin-bottom: 16px;
    }
  }
</style>

