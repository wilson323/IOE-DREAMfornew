<template>
  <div class="ws-console">
    <div class="toolbar">
      <a-space>
        <a-button size="small" @click="clear">清空</a-button>
        <a-button size="small" type="default" @click="reconnect">重连</a-button>
      </a-space>
    </div>
    <div class="log" ref="logRef">
      <div v-for="(line, idx) in logs" :key="idx" class="line">
        <span class="time">[{{ line.time }}]</span>
        <span :class="['level', line.level]">{{ line.level.toUpperCase() }}</span>
        <span class="msg">{{ line.msg }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

/**
 * WebSocket 控制台
 * 依赖上层事件总线/Store推送日志，这里提供最小可用展示与交互
 */
const logs = ref([])
const logRef = ref(null)

function push(level, msg) {
  logs.value.push({
    time: new Date().toLocaleTimeString(),
    level,
    msg
  })
  nextTick(() => {
    if (logRef.value) {
      logRef.value.scrollTop = logRef.value.scrollHeight
    }
  })
}

function clear() {
  logs.value = []
}

function reconnect() {
  push('info', '请求重连 WebSocket...')
  // 实际重连由上层 Store 触发；此处仅记录操作
}

onMounted(() => {
  push('info', 'WebSocket 控制台就绪')
})
</script>

<style scoped>
.ws-console {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.toolbar {
  margin-bottom: 8px;
}
.log {
  flex: 1;
  background: #0f172a;
  color: #e2e8f0;
  padding: 8px;
  border-radius: 6px;
  overflow-y: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
}
.line {
  margin: 4px 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.time { color: #94a3b8; margin-right: 6px; }
.level { margin-right: 6px; }
.level.info { color: #60a5fa; }
.level.warn { color: #fbbf24; }
.level.error { color: #f87171; }
</style>

