<!--
  * 活动动态组件
  * 
  * @Author:    SmartAdmin
  * @Date:      2025-11-04
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-card class="activity-feed-card" :bordered="false" :loading="loading">
    <template #title>
      <div class="card-title-wrapper">
        <UnorderedListOutlined class="card-icon" />
        <div>
          <div class="card-title">最近活动</div>
          <div class="card-description">系统最新动态和重要事件</div>
        </div>
      </div>
    </template>
    <template #extra>
      <a-button type="link" @click="handleRefresh">
        <template #icon>
          <ReloadOutlined :spin="loading" />
        </template>
        刷新
      </a-button>
    </template>

    <div class="activity-list">
      <a-empty v-if="!activities || activities.length === 0" description="暂无活动记录" />
      <div v-else class="activity-item" v-for="activity in activities" :key="activity.id">
        <div class="activity-icon" :class="`activity-icon-${activity.type}`">
          <component :is="$antIcons[activity.icon]" />
        </div>
        <div class="activity-content">
          <div class="activity-title">{{ activity.title }}</div>
          <div class="activity-description">{{ activity.description }}</div>
        </div>
        <div class="activity-time">{{ activity.time }}</div>
      </div>
    </div>
  </a-card>
</template>

<script setup>
  import { UnorderedListOutlined, ReloadOutlined } from '@ant-design/icons-vue';

  // ----------------------- props -----------------------
  const props = defineProps({
    activities: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
  });

  // ----------------------- emits -----------------------
  const emit = defineEmits(['refresh']);

  // ----------------------- methods -----------------------
  function handleRefresh() {
    emit('refresh');
  }
</script>

<style lang="less" scoped>
  .activity-feed-card {
    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-card-body) {
      padding: 0;
    }

    .card-title-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;

      .card-icon {
        font-size: 20px;
        color: #1890ff;
      }

      .card-title {
        font-size: 18px;
        font-weight: 600;
        color: #262626;
        line-height: 1.4;
      }

      .card-description {
        font-size: 14px;
        color: #8c8c8c;
        font-weight: normal;
        margin-top: 2px;
      }
    }

    .activity-list {
      .activity-item {
        padding: 16px 24px;
        border-bottom: 1px solid #f5f5f5;
        display: flex;
        align-items: center;
        gap: 16px;
        transition: background 0.2s ease;

        &:hover {
          background: #fafafa;
        }

        &:last-child {
          border-bottom: none;
        }

        .activity-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          flex-shrink: 0;

          &.activity-icon-success {
            background: #f6ffed;
            color: #52c41a;
          }

          &.activity-icon-info {
            background: #e6f4ff;
            color: #1890ff;
          }

          &.activity-icon-warning {
            background: #fffbe6;
            color: #faad14;
          }

          &.activity-icon-error {
            background: #fff2f0;
            color: #ff4d4f;
          }
        }

        .activity-content {
          flex: 1;
          min-width: 0;

          .activity-title {
            font-weight: 600;
            color: #262626;
            margin-bottom: 4px;
            font-size: 14px;
          }

          .activity-description {
            color: #8c8c8c;
            font-size: 13px;
            line-height: 1.5;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .activity-time {
          color: #bfbfbf;
          font-size: 12px;
          white-space: nowrap;
          flex-shrink: 0;
        }
      }
    }
  }

  @media (max-width: 768px) {
    .activity-feed-card {
      .activity-list {
        .activity-item {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;

          .activity-content {
            .activity-description {
              white-space: normal;
              overflow: visible;
            }
          }

          .activity-time {
            align-self: flex-end;
          }
        }
      }
    }
  }
</style>

