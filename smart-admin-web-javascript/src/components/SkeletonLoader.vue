<!--
 * 通用骨架屏组件
 *
 * 提供多种加载状态的骨架屏效果
 * - 表格骨架屏
 * - 卡片骨架屏
 * - 表单骨架屏
 * - 列表骨架屏
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="skeleton-wrapper">
    <!-- 表格骨架屏 -->
    <div v-if="type === 'table'" class="skeleton-table">
      <a-row :gutter="16">
        <a-col :span="24">
          <div class="skeleton-table-header">
            <a-skeleton active :paragraph="{ rows: 1 }" />
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="24">
          <div v-for="i in rowCount" :key="i" class="skeleton-table-row">
            <a-skeleton active :paragraph="{ rows: 1 }" />
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 卡片骨架屏 -->
    <div v-else-if="type === 'card'" class="skeleton-card">
      <a-row :gutter="16">
        <a-col v-for="i in cardCount" :key="i" :span="colSpan">
          <a-card>
            <a-skeleton active :loading="true" avatar>
              <template #title>
                <a-skeleton-button active size="small" style="width: 120px;" />
              </template>
              <template #description>
                <a-skeleton-button active size="small" style="width: 80px;" />
              </template>
            </a-skeleton>
            <a-skeleton
              active
              :paragraph="{ rows: 3 }"
              style="margin-top: 16px;"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 表单骨架屏 -->
    <div v-else-if="type === 'form'" class="skeleton-form">
      <a-row :gutter="24">
        <a-col v-for="i in fieldCount" :key="i" :span="12">
          <a-skeleton active :paragraph="{ rows: 1 }" />
        </a-col>
      </a-row>
      <div style="margin-top: 24px;">
        <a-skeleton-button active size="large" style="width: 120px; margin-right: 12px;" />
        <a-skeleton-button active size="large" style="width: 80px;" />
      </div>
    </div>

    <!-- 列表骨架屏 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in listCount" :key="i" class="skeleton-list-item">
        <a-skeleton active avatar :paragraph="{ rows: 2 }" />
      </div>
    </div>

    <!-- 统计卡片骨架屏 -->
    <div v-else-if="type === 'statistic'" class="skeleton-statistic">
      <a-row :gutter="16">
        <a-col v-for="i in statisticCount" :key="i" :span="6">
          <a-card>
            <a-skeleton active :paragraph="{ rows: 1 }" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 图表骨架屏 -->
    <div v-else-if="type === 'chart'" class="skeleton-chart">
      <a-row :gutter="16">
        <a-col v-for="i in chartCount" :key="i" :span="12">
          <a-card>
            <a-skeleton active :paragraph="{ rows: 1 }" />
            <div class="skeleton-chart-content">
              <a-skeleton active :paragraph="{ rows: 8 }" />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 通用骨架屏（默认） -->
    <div v-else class="skeleton-default">
      <a-skeleton active :paragraph="{ rows: rowCount }" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 骨架屏类型
  type: {
    type: String,
    default: 'default',
    validator: (value) => {
      return ['default', 'table', 'card', 'form', 'list', 'statistic', 'chart'].includes(value);
    },
  },
  // 行数（用于default和table类型）
  rowCount: {
    type: Number,
    default: 5,
  },
  // 卡片数量（用于card类型）
  cardCount: {
    type: Number,
    default: 4,
  },
  // 列表项数量（用于list类型）
  listCount: {
    type: Number,
    default: 5,
  },
  // 字段数量（用于form类型）
  fieldCount: {
    type: Number,
    default: 6,
  },
  // 统计卡片数量
  statisticCount: {
    type: Number,
    default: 4,
  },
  // 图表数量
  chartCount: {
    type: Number,
    default: 2,
  },
  // 列宽（用于card类型）
  colSpan: {
    type: Number,
    default: 8,
  },
});

// 响应式列宽计算
const colSpan = computed(() => {
  if (window.innerWidth < 768) {
    return 24; // 移动端单列
  } else if (window.innerWidth < 1024) {
    return 12; // 平板双列
  } else {
    return props.colSpan; // 桌面端使用prop值
  }
});
</script>

<style lang="scss" scoped>
.skeleton-wrapper {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
}

.skeleton-table {
  .skeleton-table-row {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.skeleton-list {
  .skeleton-list-item {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.skeleton-chart {
  .skeleton-chart-content {
    margin-top: 16px;
    height: 300px;
  }
}

// 响应式优化
@media (max-width: 767px) {
  .skeleton-wrapper {
    padding: 12px;
  }

  .skeleton-statistic {
    :deep(.ant-row) {
      flex-direction: column;
    }

    :deep(.ant-col) {
      width: 100% !important;
      margin-bottom: 12px;
    }
  }

  .skeleton-chart {
    :deep(.ant-row) {
      flex-direction: column;
    }

    :deep(.ant-col) {
      width: 100% !important;
      margin-bottom: 12px;
    }

    .skeleton-chart-content {
      height: 220px;
    }
  }
}
</style>
