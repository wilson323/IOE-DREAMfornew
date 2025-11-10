<!--
 * 解码器统计信息组件
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 -->
<template>
  <a-row :gutter="16" class="decoder-statistics">
    <a-col :span="6">
      <a-card class="stat-card">
        <a-statistic
          title="总解码器数"
          :value="statistics.totalCount"
          :prefix="h(DesktopOutlined)"
          :value-style="{ color: '#1890ff' }"
        />
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="stat-card online">
        <a-statistic
          title="在线解码器"
          :value="statistics.onlineCount"
          :prefix="h(CheckCircleOutlined)"
          :value-style="{ color: '#52c41a' }"
        />
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="stat-card">
        <a-statistic
          title="总通道数"
          :value="statistics.totalChannels"
          :prefix="h(VideoCameraOutlined)"
          :value-style="{ color: '#1890ff' }"
        />
      </a-card>
    </a-col>
    <a-col :span="6">
      <a-card class="stat-card">
        <a-statistic
          title="使用率"
          :value="statistics.usageRate"
          suffix="%"
          :prefix="h(PieChartOutlined)"
          :value-style="{ color: '#722ed1' }"
        />
      </a-card>
    </a-col>
  </a-row>
</template>

<script setup>
import { h } from 'vue';
import {
  DesktopOutlined,
  CheckCircleOutlined,
  VideoCameraOutlined,
  PieChartOutlined,
} from '@ant-design/icons-vue';

defineProps({
  statistics: {
    type: Object,
    default: () => ({
      totalCount: 0,
      onlineCount: 0,
      totalChannels: 0,
      usageRate: 0,
    }),
  },
});
</script>

<style lang="less" scoped>
.decoder-statistics {
  margin-bottom: 16px;

  .stat-card {
    text-align: center;
    border-radius: 8px;
    transition: all 0.3s;
    border: 1px solid #f0f0f0;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    &.online {
      border-color: #b7eb8f;
      background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
    }

    :deep(.ant-statistic-title) {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    :deep(.ant-statistic-content) {
      font-size: 24px;
      font-weight: 600;
    }

    :deep(.anticon) {
      font-size: 20px;
      margin-right: 8px;
      color: inherit;
    }
  }
}
</style>