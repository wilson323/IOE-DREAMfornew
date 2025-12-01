<!--
  * 通行记录详情弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="通行记录详情"
    :width="800"
    :footer="null"
  >
    <div v-if="record" class="record-detail">
      <!-- 基本信息 -->
      <a-card title="基本信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="记录ID">
            {{ record.recordId }}
          </a-descriptions-item>
          <a-descriptions-item label="通行时间">
            {{ formatDateTime(record.accessTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="用户姓名">
            {{ record.userName }}
          </a-descriptions-item>
          <a-descriptions-item label="卡号">
            {{ record.userCardNumber }}
          </a-descriptions-item>
          <a-descriptions-item label="设备位置">
            {{ record.location }}
          </a-descriptions-item>
          <a-descriptions-item label="设备编码">
            {{ record.deviceCode }}
          </a-descriptions-item>
          <a-descriptions-item label="通行类型">
            <a-tag color="blue">{{ getAccessTypeText(record.accessType) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="通行结果">
            <a-tag
              :color="getResultColor(record.accessResult)"
              :icon="getResultIcon(record.accessResult)"
            >
              {{ getResultText(record.accessResult) }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 验证信息 -->
      <a-card title="验证信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="验证方式">
            <a-tag color="blue">{{ getVerificationMethodText(record.verificationMethod) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="验证状态">
            <a-badge
              :status="record.verificationSuccess ? 'success' : 'error'"
              :text="record.verificationSuccess ? '验证成功' : '验证失败'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="验证耗时" v-if="record.verificationDuration">
            {{ record.verificationDuration }}ms
          </a-descriptions-item>
          <a-descriptions-item label="验证得分" v-if="record.verificationScore">
            {{ record.verificationScore }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 用户信息 -->
      <a-card title="用户信息" class="detail-card">
        <a-row :gutter="16">
          <a-col :span="4">
            <div class="user-photo">
              <a-image
                v-if="record.photoUrl"
                :src="record.photoUrl"
                :width="80"
                :height="80"
                style="border-radius: 4px;"
                :preview="{ mask: '查看照片' }"
              />
              <a-avatar v-else :size="80" icon="user" />
            </div>
          </a-col>
          <a-col :span="20">
            <a-descriptions :column="3" bordered>
              <a-descriptions-item label="用户ID">
                {{ record.userId }}
              </a-descriptions-item>
              <a-descriptions-item label="部门">
                {{ record.departmentName || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="职位">
                {{ record.position || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="联系电话">
                {{ record.phoneNumber || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="邮箱">
                {{ record.email || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="有效期">
                {{ record.validityPeriod || '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
      </a-card>

      <!-- 设备信息 -->
      <a-card title="设备信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="设备名称">
            {{ record.deviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="设备类型">
            {{ record.deviceType || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="IP地址">
            {{ record.deviceIp || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="在线状态">
            <a-badge
              :status="record.deviceOnlineStatus ? 'success' : 'error'"
              :text="record.deviceOnlineStatus ? '在线' : '离线'"
            />
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 异常信息 -->
      <a-card v-if="record.isAbnormal" title="异常信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="异常类型">
            <a-tag color="red">{{ getAbnormalTypeText(record.abnormalType) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="异常原因">
            {{ record.abnormalReason || '未知原因' }}
          </a-descriptions-item>
          <a-descriptions-item label="处理状态">
            <a-tag :color="getProcessStatusColor(record.processedStatus)">
              {{ getProcessStatusText(record.processedStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="处理时间">
            {{ record.processedTime ? formatDateTime(record.processedTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="处理人">
            {{ record.processorName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="处理备注" :span="2">
            {{ record.processRemark || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 附加信息 -->
      <a-card title="附加信息" class="detail-card" v-if="hasAdditionalInfo">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="温度检测" v-if="record.temperature">
            {{ record.temperature }}°C
          </a-descriptions-item>
          <a-descriptions-item label="口罩检测" v-if="record.maskDetected !== undefined">
            <a-badge
              :status="record.maskDetected ? 'success' : 'error'"
              :text="record.maskDetected ? '佩戴' : '未佩戴'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="通行次数" v-if="record.todayAccessCount">
            今日第{{ record.todayAccessCount }}次
          </a-descriptions-item>
          <a-descriptions-item label="停留时长" v-if="record.duration">
            {{ record.duration }}秒
          </a-descriptions-item>
          <a-descriptions-item label="数据来源">
            {{ record.dataSource || '设备采集' }}
          </a-descriptions-item>
          <a-descriptions-item label="同步状态">
            <a-badge
              :status="record.syncStatus === 'synced' ? 'success' : 'processing'"
              :text="record.syncStatus === 'synced' ? '已同步' : '同步中'"
            />
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 原始数据 -->
      <a-card title="原始数据" class="detail-card">
        <a-timeline>
          <a-timeline-item v-for="(log, index) in operationLogs" :key="index">
            <template #dot>
              <a-icon :type="getLogIcon(log.type)" />
            </template>
            <div class="log-item">
              <div class="log-time">{{ formatDateTime(log.timestamp) }}</div>
              <div class="log-content">{{ log.description }}</div>
              <div v-if="log.operator" class="log-operator">操作人: {{ log.operator }}</div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { formatDateTime } from '/@/lib/format';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      type: Object,
      default: null,
    },
  });

  // Emits
  const emit = defineEmits(['update:visible']);

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  const hasAdditionalInfo = computed(() => {
    if (!props.record) return false;
    const record = props.record;
    return record.temperature !== undefined ||
           record.maskDetected !== undefined ||
           record.todayAccessCount ||
           record.duration ||
           record.dataSource ||
           record.syncStatus;
  });

  // 模拟操作日志数据
  const operationLogs = computed(() => {
    if (!props.record) return [];

    const logs = [
      {
        timestamp: props.record.accessTime,
        type: 'access',
        description: `用户 ${props.record.userName} 在 ${props.record.location} ${getAccessTypeText(props.record.accessType)}`,
        operator: '系统'
      }
    ];

    if (props.record.verificationTime) {
      logs.push({
        timestamp: props.record.verificationTime,
        type: 'verify',
        description: `通过${getVerificationMethodText(props.record.verificationMethod)}验证`,
        operator: '设备'
      });
    }

    if (props.record.isAbnormal && props.record.processedTime) {
      logs.push({
        timestamp: props.record.processedTime,
        type: 'process',
        description: `异常记录已处理`,
        operator: props.record.processorName || '管理员'
      });
    }

    return logs;
  });

  // 辅助方法
  const getAccessTypeText = (type) => {
    const typeMap = {
      in: '进入',
      out: '离开',
      pass: '通过',
    };
    return typeMap[type] || type;
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

  const getAbnormalTypeText = (type) => {
    const typeMap = {
      verification_failed: '验证失败',
      invalid_card: '无效卡片',
      expired_card: '卡片过期',
      blacklisted: '黑名单用户',
      access_denied: '拒绝访问',
      device_error: '设备异常',
      network_error: '网络异常',
    };
    return typeMap[type] || type;
  };

  const getProcessStatusColor = (status) => {
    const colorMap = {
      unprocessed: 'orange',
      processed: 'green',
      ignored: 'default',
    };
    return colorMap[status] || 'default';
  };

  const getProcessStatusText = (status) => {
    const textMap = {
      unprocessed: '未处理',
      processed: '已处理',
      ignored: '已忽略',
    };
    return textMap[status] || status;
  };

  const getLogIcon = (type) => {
    const iconMap = {
      access: 'login',
      verify: 'check-circle',
      process: 'tool',
      sync: 'sync',
      error: 'close-circle',
    };
    return iconMap[type] || 'info-circle';
  };
</script>

<style lang="less" scoped>
  .record-detail {
    .detail-card {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .user-photo {
      text-align: center;
    }

    .log-item {
      .log-time {
        font-size: 12px;
        color: #8c8c8c;
        margin-bottom: 4px;
      }

      .log-content {
        margin-bottom: 4px;
        font-weight: 500;
      }

      .log-operator {
        font-size: 12px;
        color: #666;
      }
    }
  }
</style>