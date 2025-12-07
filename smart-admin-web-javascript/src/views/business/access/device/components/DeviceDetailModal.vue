<!--
  * 设备详情弹窗组件
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="props.visible"
    title="设备详情"
    width="700px"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered v-if="deviceDetail">
        <a-descriptions-item label="设备ID">{{ deviceDetail.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ deviceDetail.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="设备编号">{{ deviceDetail.deviceCode }}</a-descriptions-item>
        <a-descriptions-item label="区域ID">{{ deviceDetail.areaId }}</a-descriptions-item>
        <a-descriptions-item label="设备类型">{{ deviceDetail.deviceType }}</a-descriptions-item>
        <a-descriptions-item label="设备状态">
          <a-badge
            :status="getStatusBadge(deviceDetail.status)"
            :text="getStatusText(deviceDetail.status)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(deviceDetail.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间" :span="2">
          {{ formatDateTime(deviceDetail.updateTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ deviceDetail.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-spin>
  </a-modal>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    deviceId: {
      type: Number,
      default: null,
    },
  });

  const emit = defineEmits(['update:visible']);

  const loading = ref(false);
  const deviceDetail = ref(null);

  // 取消
  const handleCancel = () => {
    emit('update:visible', false);
  };

  // 监听props变化
  watch(
    () => props.visible,
    (val) => {
      if (val && props.deviceId) {
        loadDeviceDetail();
      }
    }
  );

  // 加载设备详情
  const loadDeviceDetail = async () => {
    if (!props.deviceId) return;

    loading.value = true;
    try {
      const result = await accessApi.getDeviceDetail(props.deviceId);
      if (result.code === 200 && result.data) {
        deviceDetail.value = result.data;
      }
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      loading.value = false;
    }
  };

  // 格式化日期时间
  const formatDateTime = (datetime) => {
    if (!datetime) return '-';
    return datetime;
  };

  // 获取状态Badge
  const getStatusBadge = (status) => {
    const badgeMap = {
      1: 'success',
      2: 'default',
      3: 'error',
    };
    return badgeMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      1: '在线',
      2: '离线',
      3: '故障',
    };
    return textMap[status] || '未知';
  };
</script>

<style lang="less" scoped>
</style>

