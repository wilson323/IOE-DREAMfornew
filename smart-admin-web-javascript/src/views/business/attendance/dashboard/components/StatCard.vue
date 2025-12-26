<!--
  @fileoverview 统计卡片组件
  @author IOE-DREAM Team
  @description 用于展示仪表中心的统计指标卡片
  @example
  <StatCard
    title="应出勤"
    :value="stats.totalAttendance"
    suffix="人"
    color="#1890ff"
    icon="UserOutlined"
  />
-->
<template>
  <a-card :bordered="false" class="stat-card">
    <div class="stat-card__content">
      <div class="stat-card__icon" :style="{ backgroundColor: iconBgColor }">
        <component :is="iconComponent" class="stat-card__icon-svg" />
      </div>
      <div class="stat-card__info">
        <div class="stat-card__title">{{ title }}</div>
        <div class="stat-card__value" :style="{ color }">
          <span class="stat-card__number">{{ animatedValue }}</span>
          <span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
        </div>
        <div v-if="change !== undefined" class="stat-card__change">
          <span :class="changeClass">
            <ArrowUpOutlined v-if="change > 0" />
            <ArrowDownOutlined v-else-if="change < 0" />
            <MinusOutlined v-else />
            <span>{{ Math.abs(change) }}%</span>
          </span>
          <span class="stat-card__change-label">较昨日</span>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import {
  UserOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined,
  CalendarOutlined,
  FileTextOutlined,
  TeamOutlined
} from '@ant-design/icons-vue';

/**
 * 组件属性
 */
interface Props {
  /** 卡片标题 */
  title: string;
  /** 统计数值 */
  value: number;
  /** 单位后缀 */
  suffix?: string;
  /** 主题色 */
  color?: string;
  /** 图标名称 */
  icon?: string;
  /** 变化百分比（可选） */
  change?: number;
  /** 是否启用数字动画 */
  animated?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  color: '#1890ff',
  animated: true
});

/** 图标组件映射 */
const iconMap: Record<string, any> = {
  UserOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  CalendarOutlined,
  FileTextOutlined,
  TeamOutlined
};

/** 当前显示的数值（动画用） */
const animatedValue = ref(0);

/** 图标组件 */
const iconComponent = computed(() => iconMap[props.icon || 'UserOutlined'] || UserOutlined);

/** 图标背景色（带透明度） */
const iconBgColor = computed(() => {
  const hex = props.color.replace('#', '');
  const r = parseInt(hex.substring(0, 2), 16);
  const g = parseInt(hex.substring(2, 4), 16);
  const b = parseInt(hex.substring(4, 6), 16);
  return `rgba(${r}, ${g}, ${b}, 0.1)`;
});

/** 变化样式类 */
const changeClass = computed(() => {
  if (props.change === undefined) return '';
  return {
    'stat-card__change--up': props.change > 0,
    'stat-card__change--down': props.change < 0,
    'stat-card__change--flat': props.change === 0
  };
});

/** 数字动画函数 */
const animateValue = (start: number, end: number, duration: number = 1000) => {
  if (!props.animated) {
    animatedValue.value = end;
    return;
  }

  const startTime = performance.now();
  const range = end - start;

  const step = (currentTime: number) => {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeProgress = 1 - Math.pow(1 - progress, 3); // easeOutCubic

    animatedValue.value = Math.floor(start + range * easeProgress);

    if (progress < 1) {
      requestAnimationFrame(step);
    } else {
      animatedValue.value = end;
    }
  };

  requestAnimationFrame(step);
};

/** 监听value变化，触发动画 */
watch(() => props.value, (newVal, oldVal) => {
  if (newVal !== undefined) {
    animateValue(oldVal || 0, newVal);
  }
}, { immediate: true });

onMounted(() => {
  if (props.value !== undefined) {
    animatedValue.value = props.value;
  }
});
</script>

<style scoped lang="less">
.stat-card {
  height: 100%;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &__content {
    display: flex;
    align-items: center;
    padding: 8px 0;
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    flex-shrink: 0;

    &-svg {
      font-size: 24px;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    margin-bottom: 8px;
  }

  &__value {
    display: flex;
    align-items: baseline;
    margin-bottom: 4px;
  }

  &__number {
    font-size: 28px;
    font-weight: 600;
    line-height: 1;
  }

  &__suffix {
    font-size: 14px;
    margin-left: 4px;
    color: rgba(0, 0, 0, 0.45);
  }

  &__change {
    display: flex;
    align-items: center;
    font-size: 12px;
    gap: 4px;

    &--up {
      color: #52c41a;
    }

    &--down {
      color: #f5222d;
    }

    &--flat {
      color: rgba(0, 0, 0, 0.45);
    }

    &-label {
      color: rgba(0, 0, 0, 0.45);
      margin-left: 4px;
    }
  }
}
</style>
