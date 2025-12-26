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
        :style="videoTransformStyle"
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
            <a-button size="small" type="link" @click.stop="showImageControl = !showImageControl">
              <template #icon><SettingOutlined /></template>
              画面
            </a-button>
          </a-space>
        </div>

        <div class="controls-right">
          <a-space>
            <!-- 录像计时器 -->
            <div v-if="isRecording" class="recording-timer">
              <DotChartOutlined class="recording-dot" />
              <span>{{ recordingTime }}</span>
            </div>

            <a-button size="small" type="link" @click.stop="capture">
              <template #icon><CameraOutlined /></template>
            </a-button>
            <a-button
              size="small"
              type="link"
              :class="{ 'recording-button': isRecording }"
              @click.stop="record"
            >
              <template #icon><VideoCameraOutlined /></template>
            </a-button>
            <a-button size="small" type="link" @click.stop="fullscreen">
              <template #icon><FullscreenOutlined /></template>
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 画面控制面板 -->
      <div v-if="showImageControl" class="image-control-panel">
        <a-card size="small" :body-style="{ padding: '12px' }">
          <!-- 旋转/镜像控制 -->
          <div class="control-group">
            <div class="control-label">旋转与镜像</div>
            <a-space>
              <a-button size="small" @click="rotateLeft">
                <template #icon><RotateLeftOutlined /></template>
                左旋90°
              </a-button>
              <a-button size="small" @click="rotateRight">
                <template #icon><RotateRightOutlined /></template>
                右旋90°
              </a-button>
              <a-button size="small" @click="flipHorizontal">
                <template #icon><SwapLeftOutlined /></template>
                水平镜像
              </a-button>
              <a-button size="small" @click="flipVertical">
                <template #icon><SwapRightOutlined /></template>
                垂直镜像
              </a-button>
              <a-button size="small" @click="resetTransform">
                <template #icon><UndoOutlined /></template>
                重置
              </a-button>
            </a-space>
          </div>

          <!-- 亮度/对比度/饱和度控制 -->
          <div class="control-group">
            <div class="control-label">图像调节</div>
            <div class="slider-control">
              <div class="slider-item">
                <span class="slider-label">亮度</span>
                <a-slider
                  v-model:value="brightness"
                  :min="0"
                  :max="200"
                  :step="5"
                  :tooltip-formatter="(value) => `${value}%`"
                  @change="applyImageAdjust"
                />
              </div>
              <div class="slider-item">
                <span class="slider-label">对比度</span>
                <a-slider
                  v-model:value="contrast"
                  :min="0"
                  :max="200"
                  :step="5"
                  :tooltip-formatter="(value) => `${value}%`"
                  @change="applyImageAdjust"
                />
              </div>
              <div class="slider-item">
                <span class="slider-label">饱和度</span>
                <a-slider
                  v-model:value="saturation"
                  :min="0"
                  :max="200"
                  :step="5"
                  :tooltip-formatter="(value) => `${value}%`"
                  @change="applyImageAdjust"
                />
              </div>
            </div>
          </div>

          <!-- 电子放大控制 -->
          <div class="control-group">
            <div class="control-label">电子放大 ({{ zoomLevel }}x)</div>
            <a-space>
              <a-button size="small" @click="zoomIn" :disabled="zoomLevel >= 4">
                <template #icon><ZoomInOutlined /></template>
                放大
              </a-button>
              <a-button size="small" @click="zoomOut" :disabled="zoomLevel <= 1">
                <template #icon><ZoomOutOutlined /></template>
                缩小
              </a-button>
              <a-button size="small" @click="resetZoom">
                <template #icon><UndoOutlined /></template>
                重置
              </a-button>
            </a-space>
          </div>
        </a-card>
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
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  CameraOutlined,
  VideoCameraOutlined,
  FullscreenOutlined,
  DotChartOutlined,
  SettingOutlined,
  RotateLeftOutlined,
  RotateRightOutlined,
  SwapLeftOutlined,
  SwapRightOutlined,
  UndoOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@ant-design/icons-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';

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

// 录像状态
const isRecording = ref(false);
const recordingStartTime = ref(null);
const recordingTime = ref('00:00:00');

// 画面控制状态
const showImageControl = ref(false);
const brightness = ref(100);      // 亮度 (0-200)
const contrast = ref(100);        // 对比度 (0-200)
const saturation = ref(100);      // 饱和度 (0-200)
const zoomLevel = ref(1);         // 电子放大倍数 (1-4)
const rotation = ref(0);          // 旋转角度 (0, 90, 180, 270)
const flipH = ref(false);         // 水平镜像
const flipV = ref(false);         // 垂直镜像

// 模拟视频URL（开发阶段）
const mockVideoUrl = ref('https://vjs.zencdn.net/v/oceans.mp4');

// 定时器
let timeTimer = null;
let recordingTimer = null;

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

// 格式化录像时间
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

// 开始录像计时器
const startRecordingTimer = () => {
  recordingTimer = setInterval(() => {
    if (recordingStartTime.value) {
      const elapsed = Date.now() - recordingStartTime.value;
      recordingTime.value = formatTime(elapsed);
    }
  }, 1000);
};

// 停止录像计时器
const stopRecordingTimer = () => {
  if (recordingTimer) {
    clearInterval(recordingTimer);
    recordingTimer = null;
  }
};

// 开始录像
const startRecording = async () => {
  if (!props.device) return;

  try {
    const res = await videoPcApi.startRecording({
      deviceId: props.device.deviceId,
      channelId: props.device.channelId || '1',
      recordType: 'manual'
    });

    if (res.code === 200 || res.success) {
      isRecording.value = true;
      recordingStartTime.value = Date.now();
      startRecordingTimer();
      message.success(`开始录像：${props.device.name}`);
    } else {
      message.error(res.message || '开始录像失败');
    }
  } catch (error) {
    console.error('[视频播放器] 开始录像异常', error);
    message.error('开始录像异常');
  }
};

// 停止录像
const stopRecording = async () => {
  if (!props.device) return;

  try {
    const res = await videoPcApi.stopRecording(
      props.device.deviceId,
      props.device.channelId || '1'
    );

    if (res.code === 200 || res.success) {
      isRecording.value = false;
      stopRecordingTimer();
      recordingTime.value = '00:00:00';
      recordingStartTime.value = null;
      message.success(`录像已保存：${props.device.name}`);
    } else {
      message.error(res.message || '停止录像失败');
    }
  } catch (error) {
    console.error('[视频播放器] 停止录像异常', error);
    message.error('停止录像异常');
  }
};

// 切换录像
const record = () => {
  if (!props.device) return;

  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
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

// 计算视频变换样式
const videoTransformStyle = computed(() => {
  const transforms = [];
  const filters = [];

  // 旋转和镜像
  if (rotation.value !== 0) {
    transforms.push(`rotate(${rotation.value}deg)`);
  }
  if (flipH.value) {
    transforms.push('scaleX(-1)');
  }
  if (flipV.value) {
    transforms.push('scaleY(-1)');
  }
  if (zoomLevel.value !== 1) {
    transforms.push(`scale(${zoomLevel.value})`);
  }

  // 图像调节
  filters.push(`brightness(${brightness.value}%)`);
  filters.push(`contrast(${contrast.value}%)`);
  filters.push(`saturate(${saturation.value}%)`);

  return {
    transform: transforms.length > 0 ? transforms.join(' ') : 'none',
    filter: filters.join(' ')
  };
});

// ===== 画面控制函数 =====

// 左旋转90度
const rotateLeft = () => {
  rotation.value = (rotation.value - 90 + 360) % 360;
  message.info(`左旋90° (当前: ${rotation.value}°)`);
};

// 右旋转90度
const rotateRight = () => {
  rotation.value = (rotation.value + 90) % 360;
  message.info(`右旋90° (当前: ${rotation.value}°)`);
};

// 水平镜像
const flipHorizontal = () => {
  flipH.value = !flipH.value;
  message.info(flipH.value ? '已开启水平镜像' : '已关闭水平镜像');
};

// 垂直镜像
const flipVertical = () => {
  flipV.value = !flipV.value;
  message.info(flipV.value ? '已开启垂直镜像' : '已关闭垂直镜像');
};

// 重置变换
const resetTransform = () => {
  rotation.value = 0;
  flipH.value = false;
  flipV.value = false;
  message.success('画面变换已重置');
};

// 应用图像调节
const applyImageAdjust = () => {
  // CSS会自动通过computed style应用
  // 这里可以添加保存配置的逻辑
};

// 电子放大
const zoomIn = () => {
  if (zoomLevel.value < 4) {
    zoomLevel.value = Math.min(4, zoomLevel.value + 0.5);
    message.info(`放大至 ${zoomLevel.value}x`);
  }
};

// 电子缩小
const zoomOut = () => {
  if (zoomLevel.value > 1) {
    zoomLevel.value = Math.max(1, zoomLevel.value - 0.5);
    message.info(`缩小至 ${zoomLevel.value}x`);
  }
};

// 重置缩放
const resetZoom = () => {
  zoomLevel.value = 1;
  message.success('电子缩放已重置');
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
  // 清理录像定时器
  if (isRecording.value) {
    stopRecording();
  }
  stopRecordingTimer();
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

      &.recording-button {
        color: #f5222d;
        animation: pulse 1.5s infinite;
      }
    }
  }

  // 录像计时器
  .recording-timer {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    background: rgba(245, 34, 45, 0.15);
    border-radius: 4px;
    color: #f5222d;
    font-weight: 500;
    font-size: 13px;

    .recording-dot {
      font-size: 8px;
      animation: blink 1s infinite;
    }
  }
}

// 录像红点闪烁动画
@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

// 录像按钮脉冲动画
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
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

// 画面控制面板
.image-control-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 320px;
  max-height: 80%;
  overflow-y: auto;
  z-index: 20;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);

  .control-group {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .control-label {
      font-size: 13px;
      font-weight: 500;
      color: #333;
      margin-bottom: 8px;
    }

    .slider-control {
      .slider-item {
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        .slider-label {
          display: inline-block;
          width: 50px;
          font-size: 12px;
          color: #666;
          margin-right: 8px;
        }

        :deep(.ant-slider) {
          margin: 8px 0;
          display: inline-block;
          width: calc(100% - 60px);
          vertical-align: middle;
        }
      }
    }
  }
}
</style>
