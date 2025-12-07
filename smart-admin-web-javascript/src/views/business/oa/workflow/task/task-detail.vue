<!--
  * 任务详情页
  * 提供工作流任务详情查看和审批功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="task-detail-page">
    <!-- 任务基本信息 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-descriptions :title="taskDetail.taskName || '任务详情'" :column="4" size="small" bordered>
        <a-descriptions-item label="流程名称">{{ taskDetail.processName }}</a-descriptions-item>
        <a-descriptions-item label="任务名称">{{ taskDetail.taskName }}</a-descriptions-item>
        <a-descriptions-item label="发起人">{{ taskDetail.startUserName }}</a-descriptions-item>
        <a-descriptions-item label="当前处理人">{{ taskDetail.assigneeName || '未分配' }}</a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag v-if="taskDetail.priority === 4" color="red">紧急</a-tag>
          <a-tag v-else-if="taskDetail.priority === 3" color="orange">高</a-tag>
          <a-tag v-else-if="taskDetail.priority === 2" color="blue">普通</a-tag>
          <a-tag v-else color="default">低</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="到期时间">
          <span :class="{ 'overdue-text': taskDetail.isOverdue }">
            {{ taskDetail.dueDate || '无期限' }}
          </span>
          <a-tag v-if="taskDetail.isOverdue" color="red" style="margin-left: 8px">已过期</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ taskDetail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="任务状态">
          <a-tag v-if="taskDetail.claimed" color="green">已受理</a-tag>
          <a-tag v-else color="orange">待受理</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-row :gutter="16">
      <!-- 左侧：申请内容和审批历史 -->
      <a-col :span="16">
        <!-- 流程进度 -->
        <a-card size="small" style="margin-bottom: 16px" title="流程进度">
          <ProcessSteps v-if="historyList.length > 0" :history-list="historyList" />
          <a-empty v-else description="暂无流程进度信息" />
        </a-card>

        <!-- 申请内容 -->
        <a-card size="small" style="margin-bottom: 16px" title="申请内容">
          <div class="form-content" v-html="taskDetail.formData || '暂无申请内容'"></div>
        </a-card>

        <!-- 审批历史 -->
        <a-card size="small" title="审批历史">
          <a-timeline v-if="historyList.length > 0">
            <a-timeline-item
              v-for="(item, index) in historyList"
              :key="index"
              :color="getTimelineColor(item.outcome)"
            >
              <template #dot>
                <component :is="getTimelineIcon(item.outcome)" />
              </template>
              <p class="timeline-title">{{ item.nodeName }}</p>
              <p class="timeline-content">处理人：{{ item.assigneeName }}</p>
              <p class="timeline-content" v-if="item.comment">处理意见：{{ item.comment }}</p>
              <p class="timeline-time">{{ item.endTime || item.startTime }}</p>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无审批历史" />
        </a-card>
      </a-col>

      <!-- 右侧：审批表单和流程图 -->
      <a-col :span="8">
        <!-- 审批表单 -->
        <a-card size="small" style="margin-bottom: 16px" title="审批操作">
          <ApprovalForm
            v-if="taskDetail.taskId && !taskDetail.completed"
            :task-id="taskDetail.taskId"
            :task-detail="taskDetail"
            @submit-success="handleApprovalSuccess"
          />
          <a-result v-else status="success" title="任务已完成" />
        </a-card>

        <!-- 流程图 -->
        <a-card size="small" title="流程图">
          <ProcessDiagram
            v-if="taskDetail.instanceId"
            :instance-id="taskDetail.instanceId"
            :highlight-node="taskDetail.nodeId"
          />
          <a-empty v-else description="暂无流程图" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined } from '@ant-design/icons-vue';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import ProcessSteps from '/@/components/workflow/ProcessSteps.vue';
  import ProcessDiagram from '/@/components/workflow/ProcessDiagram.vue';
  import ApprovalForm from '/@/components/workflow/ApprovalForm.vue';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  const route = useRoute();
  const router = useRouter();
  const workflowStore = useWorkflowStore();

  const taskDetail = ref({});
  const historyList = ref([]);
  const loading = ref(false);

  /**
   * 获取时间线颜色
   * @param {String} outcome - 处理结果
   * @returns {String} 颜色值
   */
  function getTimelineColor(outcome) {
    if (outcome === '1') return 'green';
    if (outcome === '2') return 'red';
    return 'blue';
  }

  /**
   * 获取时间线图标
   * @param {String} outcome - 处理结果
   * @returns {Component} 图标组件
   */
  function getTimelineIcon(outcome) {
    if (outcome === '1') return CheckCircleOutlined;
    if (outcome === '2') return CloseCircleOutlined;
    return ClockCircleOutlined;
  }

  /**
   * 查询任务详情
   */
  async function queryTaskDetail() {
    const taskId = route.query.taskId;
    if (!taskId) {
      return;
    }

    try {
      loading.value = true;
      SmartLoading.show();
      await workflowStore.fetchTaskDetail(taskId);
      taskDetail.value = workflowStore.currentTask || {};

      // 查询流程历史
      if (taskDetail.value.instanceId) {
        await queryProcessHistory(taskDetail.value.instanceId);
      }
    } catch (err) {
      console.error('查询任务详情失败:', err);
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
      SmartLoading.hide();
    }
  }

  /**
   * 查询流程历史
   * @param {Number} instanceId - 流程实例ID
   */
  async function queryProcessHistory(instanceId) {
    try {
      await workflowStore.fetchProcessHistory(instanceId);
      historyList.value = workflowStore.processHistory || [];
    } catch (err) {
      console.error('查询流程历史失败:', err);
      smartSentry.captureError(err);
    }
  }

  /**
   * 审批成功回调
   */
  function handleApprovalSuccess() {
    queryTaskDetail();
    // 可以返回到列表页或刷新当前页
    // router.back();
  }

  // 初始化
  onMounted(() => {
    queryTaskDetail();
  });
</script>

<style lang="less" scoped>
  .task-detail-page {
    padding: 16px;

    .overdue-text {
      color: #ff4d4f;
    }

    .form-content {
      min-height: 200px;
      padding: 16px;
      background: #fafafa;
      border-radius: 4px;
    }

    .timeline-title {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .timeline-content {
      color: #666;
      margin-bottom: 4px;
      font-size: 12px;
    }

    .timeline-time {
      color: #999;
      font-size: 12px;
    }
  }
</style>

