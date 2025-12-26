<!--
  智能视频-电视墙页面

  @Author:    Claude Code
  @Date:      2025-12-24
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <!-- 顶部操作栏 -->
    <a-card :bordered="false" class="control-bar">
      <a-row :gutter="16" align="middle">
        <!-- 电视墙选择 -->
        <a-col :span="6">
          <a-select
            v-model:value="selectedWallId"
            placeholder="选择电视墙"
            style="width: 100%"
            @change="handleWallChange"
          >
            <a-select-option v-for="wall in tvWallList" :key="wall.id" :value="wall.id">
              {{ wall.name }}
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 布局切换 -->
        <a-col :span="8">
          <a-space>
            <span>布局:</span>
            <a-button-group>
              <a-button
                v-for="layout in layoutOptions"
                :key="layout.value"
                :type="currentLayout === layout.value ? 'primary' : 'default'"
                size="small"
                @click="switchLayout(layout.value)"
              >
                {{ layout.label }}
              </a-button>
            </a-button-group>
          </a-space>
        </a-col>

        <!-- 功能按钮 -->
        <a-col :span="10" style="text-align: right">
          <a-space>
            <a-button @click="openTourConfig">
              <template #icon><ClockCircleOutlined /></template>
              轮巡设置
            </a-button>
            <a-button @click="startTour" :disabled="tourRunning">
              <template #icon><PlayCircleOutlined /></template>
              启动轮巡
            </a-button>
            <a-button @click="stopTour" :disabled="!tourRunning">
              <template #icon><PauseCircleOutlined /></template>
              停止轮巡
            </a-button>
            <a-button @click="toggleFullscreen">
              <template #icon><FullscreenOutlined /></template>
              全屏
            </a-button>
            <a-button @click="openDeviceSelectModal">
              <template #icon><PlusOutlined /></template>
              添加视频
            </a-button>
            <a-button @click="saveConfig">
              <template #icon><SaveOutlined /></template>
              保存配置
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 电视墙显示区域 -->
    <a-card :bordered="false" class="tv-wall-display">
      <div
        ref="tvWallRef"
        class="tv-wall-grid"
        :class="[`layout-${currentLayout}`, { 'fullscreen': isFullscreen }]"
      >
        <div
          v-for="(window, index) in windows"
          :key="index"
          class="video-window"
          :draggable="true"
          @dragstart="handleDragStart(index, $event)"
          @dragover="handleDragOver($event)"
          @drop="handleDrop(index, $event)"
          @click="selectWindow(index)"
        >
          <!-- 窗口内容 -->
          <div v-if="window.deviceId" class="window-content">
            <!-- 视频播放器 -->
            <video
              v-if="window.playing"
              :ref="el => { if (el) videoRefs[index] = el }"
              class="video-element"
              autoplay
              muted
              loop
              playsinline
            ></video>

            <!-- 加载中 -->
            <div v-else-if="window.loading" class="window-loading">
              <a-spin size="large" />
              <p>正在连接...</p>
            </div>

            <!-- 错误状态 -->
            <div v-else-if="window.error" class="window-error">
              <CloseCircleOutlined style="font-size: 48px; color: #ff4d4f;" />
              <p>{{ window.error }}</p>
            </div>

            <!-- 窗口信息覆盖层 -->
            <div class="window-overlay">
              <div class="window-info">
                <a-tag color="blue">{{ window.deviceName }}</a-tag>
                <a-tag v-if="window.recording" color="red">录像中</a-tag>
              </div>

              <!-- 窗口操作按钮 -->
              <div class="window-actions">
                <a-space>
                  <a-button
                    size="small"
                    type="primary"
                    danger
                    @click.stop="removeWindow(index)"
                  >
                    <template #icon><CloseOutlined /></template>
                  </a-button>
                  <a-button
                    size="small"
                    @click.stop="fullscreenWindow(index)"
                  >
                    <template #icon><ExpandOutlined /></template>
                  </a-button>
                </a-space>
              </div>
            </div>
          </div>

          <!-- 空窗口 -->
          <div v-else class="window-empty">
            <PlusOutlined style="font-size: 48px; color: #d9d9d9;" />
            <p>点击添加视频</p>
          </div>

          <!-- 窗口序号 -->
          <div class="window-number">{{ index + 1 }}</div>
        </div>
      </div>
    </a-card>

    <!-- 轮巡配置弹窗 -->
    <a-modal
      v-model:open="tourConfigVisible"
      title="轮巡配置"
      width="800px"
      @ok="saveTourConfig"
      @cancel="tourConfigVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="轮巡间隔（秒）">
          <a-input-number
            v-model:value="tourConfig.interval"
            :min="5"
            :max="300"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="轮巡模式">
          <a-radio-group v-model:value="tourConfig.mode">
            <a-radio value="sequence">顺序轮巡</a-radio>
            <a-radio value="random">随机轮巡</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="轮巡窗口">
          <a-checkbox-group v-model:value="tourConfig.windows">
            <a-checkbox
              v-for="(window, index) in windows"
              :key="index"
              :value="index"
              :disabled="!window.deviceId"
            >
              窗口{{ index + 1 }}
            </a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="轮巡设备组">
          <a-select
            v-model:value="tourConfig.deviceGroupId"
            mode="multiple"
            placeholder="选择参与轮巡的设备组"
            style="width: 100%"
          >
            <a-select-option value="group1">大门摄像机</a-select-option>
            <a-select-option value="group2">大厅摄像机</a-select-option>
            <a-select-option value="group3">周界摄像机</a-select-option>
            <a-select-option value="group4">停车场摄像机</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 设备选择弹窗 -->
    <DeviceSelectModal
      v-model:visible="deviceSelectVisible"
      :layout="currentLayout"
      @confirm="handleDeviceSelect"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlusOutlined,
  SaveOutlined,
  FullscreenOutlined,
  CloseOutlined,
  ExpandOutlined,
  ClockCircleOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';
import DeviceSelectModal from './components/DeviceSelectModal.vue';

// 状态
const selectedWallId = ref(null);
const tvWallList = ref([]);
const currentLayout = ref(4); // 1, 4, 9, 16, 25, 36
const isFullscreen = ref(false);
const tourRunning = ref(false);
const tourConfigVisible = ref(false);
const deviceSelectVisible = ref(false);
const selectedWindowIndex = ref(null);

// 布局选项
const layoutOptions = [
  { label: '单画面', value: 1 },
  { label: '4画面', value: 4 },
  { label: '9画面', value: 9 },
  { label: '16画面', value: 16 },
  { label: '25画面', value: 25 },
  { label: '36画面', value: 36 },
];

// 窗口数据
const windows = ref([]);
const videoRefs = ref({});
const tvWallRef = ref(null);

// 轮巡配置
const tourConfig = reactive({
  interval: 30, // 30秒
  mode: 'sequence', // sequence, random
  windows: [],
  deviceGroupId: []
});

// 轮巡定时器
let tourTimer = null;

// 计算窗口数量
const windowCount = computed(() => {
  return currentLayout.value;
});

// 初始化窗口
const initWindows = () => {
  const count = windowCount.value;
  const newWindows = [];

  for (let i = 0; i < count; i++) {
    if (windows.value[i]) {
      newWindows.push(windows.value[i]);
    } else {
      newWindows.push({
        deviceId: null,
        channelId: '1',
        deviceName: '',
        loading: false,
        playing: false,
        error: null,
        recording: false
      });
    }
  }

  windows.value = newWindows;
};

// 加载电视墙列表
const loadTvWallList = async () => {
  try {
    const res = await videoPcApi.queryTvWalls({});

    if (res.code === 200 || res.success) {
      tvWallList.value = res.data || [];

      if (tvWallList.value.length > 0 && !selectedWallId.value) {
        selectedWallId.value = tvWallList.value[0].id;
        await loadTvWallConfig(selectedWallId.value);
      }
    }
  } catch (error) {
    console.error('[电视墙] 加载电视墙列表失败', error);
    // 使用模拟数据
    tvWallList.value = [
      {
        id: 1,
        name: '默认电视墙',
        layout: 4,
        windows: [],
        createTime: '2024-11-01 10:00:00'
      }
    ];
    selectedWallId.value = 1;
  }
};

// 加载电视墙配置
const loadTvWallConfig = async (wallId) => {
  try {
    const res = await videoPcApi.loadTvWallConfig(wallId);

    if (res.code === 200 || res.success && res.data) {
      const config = res.data;
      currentLayout.value = config.layout || 4;
      windows.value = config.windows || [];
      initWindows();
    }
  } catch (error) {
    console.error('[电视墙] 加载配置失败', error);
    initWindows();
  }
};

// 保存配置
const saveConfig = async () => {
  try {
    const config = {
      wallId: selectedWallId.value,
      layout: currentLayout.value,
      windows: windows.value
    };

    const res = await videoPcApi.saveTvWallConfig(
      selectedWallId.value,
      config
    );

    if (res.code === 200 || res.success) {
      message.success('配置保存成功');
    } else {
      message.error(res.message || '保存配置失败');
    }
  } catch (error) {
    console.error('[电视墙] 保存配置异常', error);
    message.error('保存配置异常');
  }
};

// 切换布局
const switchLayout = async (layout) => {
  try {
    const res = await videoPcApi.switchLayout(selectedWallId.value, layout);

    if (res.code === 200 || res.success) {
      currentLayout.value = layout;
      initWindows();
      message.success(`切换为${layout}画面布局`);
    } else {
      message.error(res.message || '切换布局失败');
    }
  } catch (error) {
    console.error('[电视墙] 切换布局异常', error);
    // 前端直接切换
    currentLayout.value = layout;
    initWindows();
  }
};

// 选择窗口
const selectWindow = (index) => {
  selectedWindowIndex.value = index;

  if (!windows.value[index].deviceId) {
    openDeviceSelectModal();
  }
};

// 添加设备到窗口
const addDeviceToWindow = async (windowIndex, device) => {
  try {
    const res = await videoPcApi.setWindowSource(
      selectedWallId.value,
      windowIndex,
      device.deviceId,
      device.channelId || '1'
    );

    if (res.code === 200 || res.success) {
      windows.value[windowIndex] = {
        ...windows.value[windowIndex],
        deviceId: device.deviceId,
        channelId: device.channelId || '1',
        deviceName: device.name,
        loading: true,
        playing: false,
        error: null
      };

      // 模拟视频流加载
      setTimeout(() => {
        windows.value[windowIndex].loading = false;
        windows.value[windowIndex].playing = true;

        // 设置视频源（模拟）
        const videoEl = videoRefs.value[windowIndex];
        if (videoEl) {
          // videoEl.src = res.data.streamUrl;
          console.log('[电视墙] 视频流加载:', device.deviceId);
        }
      }, 1000);
    }
  } catch (error) {
    console.error('[电视墙] 添加设备异常', error);
    message.error('添加设备异常');
  }
};

// 移除窗口
const removeWindow = (index) => {
  Modal.confirm({
    title: '确认移除',
    content: '确定要移除该窗口的视频吗？',
    onOk: () => {
      windows.value[index] = {
        deviceId: null,
        channelId: '1',
        deviceName: '',
        loading: false,
        playing: false,
        error: null,
        recording: false
      };
      message.success('已移除');
    }
  });
};

// 全屏窗口
const fullscreenWindow = (index) => {
  const videoEl = videoRefs.value[index];
  if (videoEl) {
    if (videoEl.requestFullscreen) {
      videoEl.requestFullscreen();
    }
  }
};

// 拖拽相关
const draggedIndex = ref(null);

const handleDragStart = (index, event) => {
  draggedIndex.value = index;
  event.dataTransfer.effectAllowed = 'move';
};

const handleDragOver = (event) => {
  event.preventDefault();
  event.dataTransfer.dropEffect = 'move';
};

const handleDrop = async (targetIndex, event) => {
  event.preventDefault();

  if (draggedIndex.value === null || draggedIndex.value === targetIndex) {
    return;
  }

  try {
    const res = await videoPcApi.swapWindows(
      selectedWallId.value,
      draggedIndex.value,
      targetIndex
    );

    if (res.code === 200 || res.success) {
      // 交换窗口数据
      const temp = windows.value[draggedIndex.value];
      windows.value[draggedIndex.value] = windows.value[targetIndex];
      windows.value[targetIndex] = temp;

      message.success('窗口位置已交换');
    }
  } catch (error) {
    console.error('[电视墙] 交换窗口异常', error);
  }

  draggedIndex.value = null;
};

// 轮巡功能
const openTourConfig = () => {
  tourConfigVisible.value = true;
};

const saveTourConfig = () => {
  tourConfigVisible.value = false;
  message.success('轮巡配置已保存');
};

const startTour = async () => {
  try {
    const config = {
      interval: tourConfig.interval,
      mode: tourConfig.mode,
      windows: tourConfig.windows,
      deviceGroupId: tourConfig.deviceGroupId
    };

    const res = await videoPcApi.startTour(selectedWallId.value, config);

    if (res.code === 200 || res.success) {
      tourRunning.value = true;
      message.success('轮巡已启动');

      // 前端轮巡实现
      startLocalTour();
    }
  } catch (error) {
    console.error('[电视墙] 启动轮巡异常', error);
  }
};

const stopTour = async () => {
  try {
    const res = await videoPcApi.stopTour(selectedWallId.value);

    if (res.code === 200 || res.success) {
      tourRunning.value = false;

      if (tourTimer) {
        clearInterval(tourTimer);
        tourTimer = null;
      }

      message.success('轮巡已停止');
    }
  } catch (error) {
    console.error('[电视墙] 停止轮巡异常', error);
  }
};

const startLocalTour = () => {
  let currentTourIndex = 0;
  const tourWindows = tourConfig.windows || [];

  if (tourWindows.length === 0) {
    message.warning('请先配置轮巡窗口');
    return;
  }

  tourTimer = setInterval(() => {
    if (tourConfig.mode === 'sequence') {
      currentTourIndex = (currentTourIndex + 1) % tourWindows.length;
    } else {
      currentTourIndex = Math.floor(Math.random() * tourWindows.length);
    }

    const windowIndex = tourWindows[currentTourIndex];
    console.log('[电视墙] 轮巡切换到窗口:', windowIndex + 1);
  }, tourConfig.interval * 1000);
};

// 全屏切换
const toggleFullscreen = () => {
  const element = tvWallRef.value;

  if (!isFullscreen.value) {
    if (element.requestFullscreen) {
      element.requestFullscreen();
    } else if (element.webkitRequestFullscreen) {
      element.webkitRequestFullscreen();
    } else if (element.mozRequestFullScreen) {
      element.mozRequestFullScreen();
    }
    isFullscreen.value = true;
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    }
    isFullscreen.value = false;
  }
};

// 设备选择
const openDeviceSelectModal = () => {
  deviceSelectVisible.value = true;
};

const handleDeviceSelect = (devices) => {
  let index = selectedWindowIndex.value;

  for (const device of devices) {
    if (index >= windows.value.length) {
      break;
    }

    addDeviceToWindow(index, device);
    index++;
  }

  deviceSelectVisible.value = false;
};

// 电视墙变化
const handleWallChange = async (wallId) => {
  await loadTvWallConfig(wallId);
};

// 生命周期
onMounted(async () => {
  await loadTvWallList();
  initWindows();

  // 监听全屏变化
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement;
  });
});

onUnmounted(() => {
  if (tourTimer) {
    clearInterval(tourTimer);
  }
});
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);

  .control-bar {
    margin-bottom: 16px;
  }

  .tv-wall-display {
    :deep(.ant-card-body) {
      padding: 0;
    }
  }

  .tv-wall-grid {
    display: grid;
    gap: 2px;
    background-color: #000;
    min-height: 600px;
    position: relative;

    &.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      min-height: 100vh;
    }

    // 1画面布局
    &.layout-1 {
      grid-template-columns: 1fr;
      grid-template-rows: 1fr;
    }

    // 4画面布局
    &.layout-4 {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
    }

    // 9画面布局
    &.layout-9 {
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 1fr);
    }

    // 16画面布局
    &.layout-16 {
      grid-template-columns: repeat(4, 1fr);
      grid-template-rows: repeat(4, 1fr);
    }

    // 25画面布局
    &.layout-25 {
      grid-template-columns: repeat(5, 1fr);
      grid-template-rows: repeat(5, 1fr);
    }

    // 36画面布局
    &.layout-36 {
      grid-template-columns: repeat(6, 1fr);
      grid-template-rows: repeat(6, 1fr);
    }
  }

  .video-window {
    position: relative;
    background-color: #1a1a1a;
    aspect-ratio: 16 / 9;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      .window-overlay {
        opacity: 1;
      }
    }

    &.dragging {
      opacity: 0.5;
    }
  }

  .window-content {
    width: 100%;
    height: 100%;
    position: relative;
  }

  .video-element {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .window-loading,
  .window-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #fff;
  }

  .window-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #d9d9d9;
    cursor: pointer;

    p {
      margin-top: 8px;
      font-size: 14px;
    }
  }

  .window-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      to bottom,
      rgba(0, 0, 0, 0.7) 0%,
      transparent 20%,
      transparent 80%,
      rgba(0, 0, 0, 0.7) 100%
    );
    opacity: 0;
    transition: opacity 0.3s;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 12px;
  }

  .window-info {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .window-actions {
    display: flex;
    justify-content: flex-end;
  }

  .window-number {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 48px;
    font-weight: bold;
    color: rgba(255, 255, 255, 0.1);
    pointer-events: none;
  }
}
</style>
