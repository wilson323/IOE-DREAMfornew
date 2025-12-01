<template>
  <div class="message-list">
    <a-list
      :data-source="messages"
      :loading="loading"
      item-layout="horizontal"
      size="small"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #title>
              <span :class="['message-title', `level-${item.level}`]">
                {{ item.title }}
              </span>
            </template>
            <template #description>
              <div class="message-content">
                {{ item.content }}
              </div>
              <div class="message-time">
                {{ formatTime(item.timestamp) }}
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'

// Props
const props = defineProps({
  maxItems: {
    type: Number,
    default: 50
  },
  autoRefresh: {
    type: Boolean,
    default: true
  }
})

// Data
const loading = ref(false)
const messages = ref([])

// Methods
const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss')
}

const fetchMessages = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取实时消息
    // const response = await messageApi.getRecentMessages(props.maxItems)
    // messages.value = response.data
  } catch (error) {
    console.error('获取消息失败:', error)
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  fetchMessages()

  if (props.autoRefresh) {
    setInterval(fetchMessages, 30000) // 30秒刷新一次
  }
})
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
}

.message-title {
  font-weight: 500;
}

.message-title.level-error {
  color: #ff4d4f;
}

.message-title.level-warning {
  color: #faad14;
}

.message-title.level-info {
  color: #1890ff;
}

.message-content {
  margin: 4px 0;
  color: #666;
}

.message-time {
  font-size: 12px;
  color: #999;
}
</style>