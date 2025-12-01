<template>
  <div class="notification-panel">
    <a-list
      :data-source="items"
      :renderItem="renderItem"
      :locale="{ emptyText: '暂无通知' }"
    />
    <div class="actions">
      <a-space>
        <a-button size="small" @click="$emit('clear')">清空</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { h, computed } from 'vue'

/**
 * 通知面板
 * Props:
 * - notifications: Array<{ id, title, content, level, timestamp }>
 */
const props = defineProps({
  notifications: { type: Array, default: () => [] }
})

const items = computed(() =>
  props.notifications.map(n => ({
    key: n.id,
    title: n.title,
    description: n.content,
    level: n.level || 'info',
    time: n.timestamp ? new Date(n.timestamp).toLocaleString() : ''
  }))
)

function renderItem({ item }) {
  const color =
    item.level === 'error' ? 'red' :
    item.level === 'warning' ? 'orange' :
    item.level === 'success' ? 'green' : 'blue'
  return h('a-list-item', {}, {
    default: () => h('a-list-item-meta', {
      title: h('span', { style: { color } }, item.title),
      description: h('div', {}, [
        h('div', {}, item.description || ''),
        item.time ? h('div', { style: 'color:#8c8c8c;font-size:12px;margin-top:4px' }, item.time) : null
      ])
    })
  })
}
</script>

<style scoped>
.notification-panel {
  min-height: 200px;
}
.actions {
  margin-top: 8px;
  text-align: right;
}
</style>
<template>
  <div class="notification-panel">
    <a-list
      :data-source="notifications"
      :locale="{ emptyText: '暂无通知' }"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta
            :title="item.title"
            :description="item.description"
          >
            <template #avatar>
              <a-badge :status="getStatusType(item.type)" />
            </template>
          </a-list-item-meta>
          <template #extra>
            <span class="time">{{ item.time }}</span>
          </template>
        </a-list-item>
      </template>
    </a-list>
    <div class="panel-footer">
      <a-button type="link" @click="handleClear">清空通知</a-button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  notifications: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['clear']);

const getStatusType = (type) => {
  const statusMap = {
    info: 'processing',
    warning: 'warning',
    error: 'error',
    success: 'success'
  };
  return statusMap[type] || 'default';
};

const handleClear = () => {
  emit('clear');
};
</script>

<style scoped lang="scss">
.notification-panel {
  .time {
    font-size: 12px;
    color: #999;
  }

  .panel-footer {
    text-align: center;
    padding: 12px 0;
    border-top: 1px solid #f0f0f0;
  }
}
</style>

