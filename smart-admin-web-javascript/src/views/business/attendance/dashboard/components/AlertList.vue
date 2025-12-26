<!--
  @fileoverview 异常告警列表组件
  @author IOE-DREAM Team
  @description 展示考勤异常告警信息
  @example
  <AlertList :data="alertList" :loading="alertLoading" @view-detail="handleAlertDetail" />
-->
<template>
  <div class="alert-list">
    <a-spin :spinning="loading">
      <div v-if="data.length > 0" class="alert-list__content">
        <div
          v-for="alert in data"
          :key="alert.alertId"
          class="alert-item"
          :class="getAlertClass(alert.level)"
          @click="handleClick(alert)"
        >
          <div class="alert-item__icon">
            <WarningOutlined v-if="alert.level === 3" class="icon-high" />
            <ExclamationCircleOutlined v-else-if="alert.level === 2" class="icon-medium" />
            <InfoCircleOutlined v-else class="icon-low" />
          </div>
          <div class="alert-item__content">
            <div class="alert-item__title">{{ alert.title }}</div>
            <div class="alert-item__description">{{ alert.description }}</div>
            <div class="alert-item__meta">
              <span class="alert-item__employee">{{ alert.employeeName }}</span>
              <span class="alert-item__time">{{ formatTime(alert.alertTime) }}</span>
            </div>
          </div>
          <div class="alert-item__action">
            <a-button type="link" size="small" @click.stop="handleViewDetail(alert)">
              查看详情
            </a-button>
          </div>
        </div>
      </div>
      <a-empty v-else description="暂无告警信息" class="alert-list__empty" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';
import {
  WarningOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

/**
 * 告警级别
 */
export type AlertLevel = 1 | 2 | 3;

/**
 * 告警数据
 */
export interface AlertData {
  /** 告警ID */
  alertId: number;
  /** 员工ID */
  employeeId: number;
  /** 员工姓名 */
  employeeName: string;
  /** 告警级别 */
  level: AlertLevel;
  /** 告警标题 */
  title: string;
  /** 告警描述 */
  description: string;
  /** 告警时间 */
  alertTime: string;
}

/**
 * 组件属性
 */
interface Props {
  /** 告警列表数据 */
  data?: AlertData[];
  /** 加载状态 */
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false
});

/**
 * 组件事件
 */
const emit = defineEmits<{
  /** 点击查看详情事件 */
  (e: 'view-detail', alert: AlertData): void;
  /** 点击告警项事件 */
  (e: 'click', alert: AlertData): void;
}];

/** 获取告警样式类 */
const getAlertClass = (level: AlertLevel) => {
  return {
    1: 'alert-item--low',
    2: 'alert-item--medium',
    3: 'alert-item--high'
  }[level] || 'alert-item--low';
};

/** 格式化时间（相对时间） */
const formatTime = (time: string) => {
  return dayjs(time).fromNow();
};

/** 处理点击事件 */
const handleClick = (alert: AlertData) => {
  emit('click', alert);
};

/** 处理查看详情事件 */
const handleViewDetail = (alert: AlertData) => {
  emit('view-detail', alert);
};
</script>

<style scoped lang="less">
.alert-list {
  width: 100%;

  &__content {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 8px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.15);
      border-radius: 3px;

      &:hover {
        background: rgba(0, 0, 0, 0.25);
      }
    }
  }

  &__empty {
    padding: 40px 0;
  }
}

.alert-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #fafafa;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: #f5f5f5;
    transform: translateX(2px);
  }

  &--high {
    border-left-color: #f5222d;
    background: #fff1f0;

    &:hover {
      background: #ffccc7;
    }
  }

  &--medium {
    border-left-color: #faad14;
    background: #fffbe6;

    &:hover {
      background: #ffe58f;
    }
  }

  &--low {
    border-left-color: #1890ff;
    background: #e6f7ff;

    &:hover {
      background: #bae7ff;
    }
  }

  &__icon {
    margin-right: 12px;
    font-size: 20px;
    flex-shrink: 0;

    .icon-high {
      color: #f5222d;
    }

    .icon-medium {
      color: #faad14;
    }

    .icon-low {
      color: #1890ff;
    }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 4px;
    color: rgba(0, 0, 0, 0.85);
  }

  &__description {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
    margin-bottom: 8px;
    line-height: 1.5;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
  }

  &__employee {
    font-weight: 500;
  }

  &__action {
    flex-shrink: 0;
    margin-left: 8px;
  }
}
</style>
