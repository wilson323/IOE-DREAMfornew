<!--
 * 访客详情弹窗组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    title="预约详情"
    width="900px"
    @cancel="handleCancel"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <div v-if="appointmentDetail" class="visitor-detail-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">访客信息</h3>
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="访客姓名">
              {{ appointmentDetail.visitorName }}
            </a-descriptions-item>
            <a-descriptions-item label="手机号">
              {{ appointmentDetail.phone }}
            </a-descriptions-item>
            <a-descriptions-item label="身份证号">
              {{ appointmentDetail.idCard }}
            </a-descriptions-item>
            <a-descriptions-item label="来访公司">
              {{ appointmentDetail.company || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 预约信息 -->
        <div class="detail-section">
          <h3 class="section-title">预约信息</h3>
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="预约编号">
              {{ appointmentDetail.appointmentCode }}
            </a-descriptions-item>
            <a-descriptions-item label="预约类型">
              {{ appointmentDetail.appointmentType }}
            </a-descriptions-item>
            <a-descriptions-item label="预约时间">
              {{ formatDateTime(appointmentDetail.appointmentTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="预约状态">
              <a-tag :color="getStatusColor(appointmentDetail.status)">
                {{ getStatusText(appointmentDetail.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="来访事由" :span="2">
              {{ appointmentDetail.purpose }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 签到签退信息 -->
        <div class="detail-section" v-if="appointmentDetail.checkInTime">
          <h3 class="section-title">签到签退信息</h3>
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="签到时间">
              {{ formatDateTime(appointmentDetail.checkInTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="签退时间">
              {{ formatDateTime(appointmentDetail.checkOutTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="停留时长">
              {{ formatDuration(appointmentDetail.actualDuration) }}
            </a-descriptions-item>
            <a-descriptions-item label="签到设备">
              {{ appointmentDetail.registrationDevice || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 通行记录 -->
        <div class="detail-section">
          <h3 class="section-title">通行记录</h3>
          <a-table
            :columns="accessRecordColumns"
            :data-source="accessRecords"
            :loading="loadingAccessRecords"
            :pagination="false"
            size="small"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <a-space>
            <a-button
              type="primary"
              v-if="appointmentDetail.status === 'APPROVED' && !appointmentDetail.checkInTime"
              @click="handleCheckIn"
            >
              签到
            </a-button>
            <a-button
              v-if="appointmentDetail.checkInTime && !appointmentDetail.checkOutTime"
              @click="handleCheckOut"
            >
              签退
            </a-button>
            <a-button @click="handleSendNotification">
              发送通知
            </a-button>
            <a-button @click="loadAccessRecords">
              刷新记录
            </a-button>
          </a-space>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import dayjs from 'dayjs';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  appointmentId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['update:visible']);

const loading = ref(false);
const appointmentDetail = ref(null);
const accessRecords = ref([]);
const loadingAccessRecords = ref(false);

// 通行记录列配置
const accessRecordColumns = [
  { title: '通行时间', dataIndex: 'accessTime', key: 'accessTime' },
  { title: '区域', dataIndex: 'areaName', key: 'areaName' },
  { title: '设备', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '通行方向', dataIndex: 'direction', key: 'direction' }
];

// 监听弹窗显示
watch(() => props.visible, (val) => {
  if (val && props.appointmentId) {
    loadAppointmentDetail();
    loadAccessRecords();
  }
});

// 加载预约详情
const loadAppointmentDetail = async () => {
  loading.value = true;
  try {
    const result = await visitorApi.getAppointmentDetail(props.appointmentId);
    if (result.code === 200 && result.data) {
      appointmentDetail.value = result.data;
    }
  } catch (error) {
    message.error('加载预约详情失败');
    console.error('加载预约详情失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载通行记录
const loadAccessRecords = async () => {
  loadingAccessRecords.value = true;
  try {
    const result = await visitorApi.getAccessRecords(props.appointmentId);
    if (result.code === 200 && result.data) {
      accessRecords.value = result.data;
    }
  } catch (error) {
    console.error('加载通行记录失败:', error);
  } finally {
    loadingAccessRecords.value = false;
  }
};

// 签到
const handleCheckIn = () => {
  message.info('请使用二维码或身份证进行签到');
};

// 签退
const handleCheckOut = async () => {
  try {
    const result = await visitorApi.checkout(props.appointmentId);
    if (result.code === 200) {
      message.success('签退成功');
      loadAppointmentDetail();
    } else {
      message.error(result.message || '签退失败');
    }
  } catch (error) {
    message.error('签退失败');
    console.error('签退失败:', error);
  }
};

// 发送通知
const handleSendNotification = async () => {
  try {
    const result = await visitorApi.sendNotification(props.appointmentId, 'REMINDER');
    if (result.code === 200) {
      message.success('通知已发送');
    } else {
      message.error(result.message || '发送通知失败');
    }
  } catch (error) {
    message.error('发送通知失败');
    console.error('发送通知失败:', error);
  }
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-';
  return dayjs(datetime).format('YYYY-MM-DD HH:mm:ss');
};

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '-';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'CANCELLED': 'default',
    'CHECKED_IN': 'blue',
    'CHECKED_OUT': 'cyan'
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '已签退'
  };
  return textMap[status] || status;
};
</script>

<style lang="scss" scoped>
.visitor-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }
}

.action-buttons {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}
</style>

