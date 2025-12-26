<!--
  @fileoverview 请假详情展示组件
  @author IOE-DREAM Team
  @description 请假详细信息展示
-->
<template>
  <div class="leave-detail">
    <a-spin :spinning="loading">
      <!-- 基本信息 -->
      <a-descriptions title="基本信息" bordered :column="1" size="small">
        <a-descriptions-item label="申请编号">
          {{ leaveData?.leaveNo }}
        </a-descriptions-item>
        <a-descriptions-item label="员工姓名">
          {{ leaveData?.employeeName }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ leaveData?.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="请假类型">
          <a-tag :color="getLeaveTypeColor(leaveData?.leaveType)">
            {{ getLeaveTypeName(leaveData?.leaveType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请假日期">
          {{ leaveData?.startDate }} 至 {{ leaveData?.endDate }}
        </a-descriptions-item>
        <a-descriptions-item label="请假天数">
          <a-statistic
            :value="leaveData?.leaveDays || 0"
            :precision="1"
            suffix="天"
            :value-style="{ fontSize: '16px', color: '#1890ff' }"
          />
        </a-descriptions-item>
        <a-descriptions-item label="申请状态">
          <a-tag :color="getStatusColor(leaveData?.status)">
            {{ getStatusName(leaveData?.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申请时间">
          {{ leaveData?.createTime }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 请假原因 -->
      <a-card title="请假原因" size="small" style="margin-top: 16px">
        <p style="white-space: pre-wrap; word-break: break-word; margin: 0">
          {{ leaveData?.reason || '无' }}
        </p>
      </a-card>

      <!-- 备注信息 -->
      <a-card v-if="leaveData?.remark" title="备注信息" size="small" style="margin-top: 16px">
        <p style="white-space: pre-wrap; word-break: break-word; margin: 0">
          {{ leaveData?.remark }}
        </p>
      </a-card>

      <!-- 审批信息 -->
      <a-card v-if="showApprovalInfo" title="审批信息" size="small" style="margin-top: 16px">
        <a-descriptions :column="1" size="small">
          <a-descriptions-item v-if="leaveData?.approvalComment" label="审批意见">
            {{ leaveData?.approvalComment }}
          </a-descriptions-item>
          <a-descriptions-item v-if="leaveData?.approvalTime" label="审批时间">
            {{ leaveData?.approvalTime }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 审批历史 -->
      <a-card title="审批历史" size="small" style="margin-top: 16px">
        <a-timeline v-if="approvalHistory.length > 0">
          <a-timeline-item v-for="record in approvalHistory" :key="record.approvalId">
            <template #dot>
              <CheckCircleOutlined v-if="record.approvalResult === 'APPROVED'" style="color: #52c41a; font-size: 16px" />
              <CloseCircleOutlined v-else-if="record.approvalResult === 'REJECTED'" style="color: #f5222d; font-size: 16px" />
              <ClockCircleOutlined v-else style="color: #faad14; font-size: 16px" />
            </template>
            <div>
              <div style="font-weight: 500">{{ record.approverName }}</div>
              <div style="color: #666; font-size: 12px; margin-top: 4px">
                {{ record.approvalStep }} · {{ record.approvalTime }}
              </div>
              <div v-if="record.approvalComment" style="color: #999; font-size: 12px; margin-top: 4px">
                {{ record.approvalComment }}
              </div>
              <div v-if="record.isProxyApproval" style="color: #1890ff; font-size: 12px; margin-top: 4px">
                代理审批：{{ record.proxyApproverName }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无审批历史" :image-style="{ height: '60px' }" />
      </a-card>

      <!-- 附件信息 -->
      <a-card v-if="attachments.length > 0" title="附件信息" size="small" style="margin-top: 16px">
        <a-list :data-source="attachments" size="small">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <a :href="item.fileUrl" target="_blank">{{ item.fileName }}</a>
                </template>
                <template #description>
                  <span>大小：{{ formatFileSize(item.fileSize) }}</span>
                  <span style="margin-left: 16px">类型：{{ item.fileType }}</span>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a :href="item.fileUrl" target="_blank">
                  <DownloadOutlined />
                  下载
                </a>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-card>

      <!-- 操作按钮 -->
      <div style="margin-top: 24px; text-align: center">
        <a-space>
          <a-button @click="handlePrint">
            <PrinterOutlined />
            打印
          </a-button>
          <a-button @click="handleClose">
            关闭
          </a-button>
        </a-space>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  DownloadOutlined,
  PrinterOutlined,
} from '@ant-design/icons-vue';
import { LeaveType, LeaveStatus, LeaveRecordVO, ApprovalRecord, AttachmentInfo } from '@/api/business/attendance/leave';

/**
 * 组件属性
 */
interface Props {
  leaveData?: LeaveRecordVO | null;
}

const props = defineProps<Props>();

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 审批历史
 */
const approvalHistory = ref<ApprovalRecord[]>([]);

/**
 * 附件列表
 */
const attachments = ref<AttachmentInfo[]>([]);

/**
 * 是否显示审批信息
 */
const showApprovalInfo = computed(() => {
  return props.leaveData?.status === 'APPROVED' || props.leaveData?.status === 'REJECTED';
});

/**
 * 获取请假类型名称
 */
const getLeaveTypeName = (type?: LeaveType) => {
  if (!type) return '';
  const map: Record<LeaveType, string> = {
    ANNUAL: '年假',
    SICK: '病假',
    PERSONAL: '事假',
    MARRIAGE: '婚假',
    MATERNITY: '产假',
    PATERNITY: '陪产假',
    OTHER: '其他',
  };
  return map[type] || type;
};

/**
 * 获取请假类型颜色
 */
const getLeaveTypeColor = (type?: LeaveType) => {
  if (!type) return 'default';
  const map: Record<LeaveType, string> = {
    ANNUAL: 'blue',
    SICK: 'orange',
    PERSONAL: 'red',
    MARRIAGE: 'pink',
    MATERNITY: 'purple',
    PATERNITY: 'cyan',
    OTHER: 'default',
  };
  return map[type] || 'default';
};

/**
 * 获取状态名称
 */
const getStatusName = (status?: LeaveStatus) => {
  if (!status) return '';
  const map: Record<LeaveStatus, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
  };
  return map[status] || status;
};

/**
 * 获取状态颜色
 */
const getStatusColor = (status?: LeaveStatus) => {
  if (!status) return 'default';
  const map: Record<LeaveStatus, string> = {
    PENDING: 'orange',
    APPROVED: 'green',
    REJECTED: 'red',
    CANCELLED: 'default',
  };
  return map[status] || 'default';
};

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
};

/**
 * 打印
 */
const handlePrint = () => {
  window.print();
};

/**
 * 关闭
 */
const handleClose = () => {
  // 父组件控制抽屉关闭
};
</script>

<style scoped lang="less">
.leave-detail {
  padding: 0;

  :deep(.ant-descriptions) {
    .ant-descriptions-item-label {
      font-weight: 500;
      width: 120px;
    }
  }

  :deep(.ant-card) {
    .ant-card-head {
      background-color: #fafafa;
      font-weight: 500;
    }

    .ant-card-body {
      padding: 16px;
    }
  }

  :deep(.ant-timeline) {
    margin-top: 16px;
  }

  @media print {
    .ant-card {
      border: 1px solid #d9d9d9;
      box-shadow: none;
    }

    .ant-btn {
      display: none;
    }
  }
}
</style>
