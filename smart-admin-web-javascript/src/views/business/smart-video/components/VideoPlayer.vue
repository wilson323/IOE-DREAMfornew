<!--
  视频播放器组件

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div
    class="video-player"
    :class="{ selected: isSelected }"
    @click="$emit('click')"
  >
    <!-- 视频容器 -->
    <div ref="videoContainer" class="video-container">
      <!-- 模拟视频流（开发阶段使用Mock数据） -->
      <video
        v-if="isPlaying"
        ref="videoRef"
        class="video-element"
        autoplay
        muted
        loop
        playsinline
      >
        <source :src="mockVideoUrl" type="video/mp4" />
        您的浏览器不支持视频播放
      </video>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-overlay">
        <a-spin size="large" />
        <p>加载中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-overlay">
        <a-result
          status="500"
          title="播放失败"
          :sub-title="error.message || '无法播放该视频流'"
        >
          <template #extra>
            <a-button type="primary" @click="retry">
              重试
            </a-button>
          </template>
        </a-result>
      </div>

      <!-- 控制栏 -->
      <div v-if="showControls && isPlaying && !error" class="video-controls">
        <div class="controls-left">
          <a-space>
            <a-button size="small" type="link" @click.stop="togglePlay">
              <template #icon>
                <PauseCircleOutlined v-if="!paused" />
                <PlayCircleOutlined v-else />
              </template>
            </a-button>
            <a-button size="small" type="link" @click.stop="stop">
              <template #icon><StopOutlined /></template>
            </a-button>
          </a-space>
        </div>

        <div class="controls-right">
          <a-space>
            <a-button size="small" type="link" @click.stop="capture">
              <template #icon><CameraOutlined /></template>
            </a-button>
            <a-button size="small" type="link" @click.stop="record">
              <template #icon><VideoCameraOutlined /></template>
            </a-button>
            <a-button size="small" type="link" @click.stop="fullscreen">
              <template #icon><FullscreenOutlined /></template>
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 设备信息 -->
      <div v-if="device && !error" class="device-info">
        <a-tag :color="device.status === 'online' ? 'green' : 'red'">
          {{ device.status === 'online' ? '在线' : '离线' }}
        </a-tag>
        <span class="device-name">{{ device.name }}</span>
        <span class="device-time">{{ currentTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  CameraOutlined,
  VideoCameraOutlined,
  FullscreenOutlined,
} from '@ant-design/icons-vue';

const props = defineProps({
  device: {
    type: Object,
    default: null,
  },
  isSelected: {
    type: Boolean,
    default: false,
  },
  showControls: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['play', 'stop', 'error', 'click']);

// 状态
const videoContainer = ref(null);
const videoRef = ref(null);
const loading = ref(false);
const error = ref(null);
const isPlaying = ref(false);
const paused = ref(false);
const currentTime = ref('');

// 模拟视频URL（开发阶段）
const mockVideoUrl = ref('https://vjs.zencdn.net/v/oceans.mp4');

// 定时器
let timeTimer = null;

// 播放
const play = () => {
  if (!props.device) return;

  loading.value = true;
  error.value = null;

  // 模拟加载
  setTimeout(() => {
    if (videoRef.value) {
      videoRef.value.play().then(() => {
        isPlaying.value = true;
        paused.value = false;
        loading.value = false;
        emit('play', props.device.id);
        startTimeUpdate();
      }).catch((err) => {
        loading.value = false;
        error.value = { message: '视频播放失败' };
        emit('error', props.device.id, err);
      });
    }
  }, 500);
};

// 停止
const stop = () => {
  if (videoRef.value) {
    videoRef.value.pause();
    videoRef.value.currentTime = 0;
  }
  isPlaying.value = false;
  paused.value = false;
  stopTimeUpdate();
  emit('stop', props.device?.id);
};

// 切换播放/暂停
const togglePlay = () => {
  if (!isPlaying.value) {
    play();
  } else {
    if (paused.value) {
      videoRef.value?.play();
      paused.value = false;
    } else {
      videoRef.value?.pause();
      paused.value = true;
    }
  }
};

// 截图
const capture = () => {
  if (!props.device) return;
  message.success(`开始截图：${props.device.name}`);
};

// 录像
const record = () => {
  if (!props.device) return;
  message.info(`开始录像：${props.device.name}`);
};

// 全屏
const fullscreen = () => {
  if (videoContainer.value) {
    if (videoContainer.value.requestFullscreen) {
      videoContainer.value.requestFullscreen();
    }
  }
};

// 重试
const retry = () => {
  error.value = null;
  play();
};

// 更新时间
const startTimeUpdate = () => {
  timeTimer = setInterval(() => {
    const now = new Date();
    currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false });
  }, 1000);
};

// 停止时间更新
const stopTimeUpdate = () => {
  if (timeTimer) {
    clearInterval(timeTimer);
    timeTimer = null;
  }
};

// 监听设备变化
watch(() => props.device, (newDevice) => {
  if (newDevice) {
    play();
  } else {
    stop();
  }
}, { immediate: true });

onMounted(() => {
  if (props.device) {
    play();
  }
});

onUnmounted(() => {
  stop();
});
</script>

<style scoped lang="less">
.video-player {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    .video-controls {
      opacity: 1;
    }
  }

  &.selected {
    box-shadow: 0 0 0 2px #1890ff;
  }
}

.video-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: #000;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  z-index: 10;
}

.loading-overlay {
  p {
    margin-top: 16px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 48px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 5;

  .controls-left,
  .controls-right {
    button {
      color: #fff;

      &:hover {
        color: #1890ff;
      }
    }
  }
}

.device-info {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  z-index: 5;

  .device-name {
    font-weight: 500;
  }

  .device-time {
    color: rgba(255, 255, 255, 0.8);
  }
}
</style>
