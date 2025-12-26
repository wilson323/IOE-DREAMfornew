# 视频模块前端功能补充实施规范

**文档版本**: v1.0
**创建日期**: 2025-12-24
**目标**: 2周内完成所有P0级功能，确保全局一致性、避免冗余

---

## 📋 已完成功能（截至2025-12-24）

### ✅ PC端 - 实时监控

**25画面布局** - 已完成
- 文件: `LayoutSelector.vue` + `monitor-preview.vue`
- 新增: 5x5布局选项
- 布局支持: 1/4/9/16/25画面
- 状态: ✅ 测试通过

**技术实现**:
```vue
<!-- LayoutSelector.vue -->
<a-menu-item key="25">
  <div class="layout-option">
    <div class="layout-grid layout-25"></div>
    <span>二十五画面</span>
  </div>
</a-menu-item>

<style>
.layout-25 {
  grid-template-columns: repeat(5, 1fr);
  grid-template-rows: repeat(5, 1fr);
  background: repeating-linear-gradient(...);
}
</style>
```

---

## 🎯 剩余P0级功能实施清单

### 优先级排序（按业务价值）

| 优先级 | 功能模块 | 预估工作量 | 依赖项 | 责任人 |
|--------|---------|-----------|--------|--------|
| P0-1 | 手动录像（开始/停止） | 4小时 | - | 前端组 |
| P0-2 | 批量截图 | 3小时 | P0-1 | 前端组 |
| P0-3 | 云台预置位管理 | 6小时 | - | 前端组 |
| P0-4 | 画面控制（旋转/亮度等） | 8小时 | - | 前端组 |
| P0-5 | 视频回放-变速播放 | 6小时 | - | 前端组 |
| P0-6 | 视频回放-24小时时间轴 | 8小时 | - | 前端组 |
| P0-7 | 实时告警推送（WebSocket） | 10小时 | 后端 | 前端+后端 |
| P0-8 | 告警详情查看 | 6小时 | P0-7 | 前端组 |
| P0-9 | 告警确认处理 | 8小时 | P0-7 | 前端组 |

**总计**: 约59小时（约7.5个工作日）

---

## 📝 详细实施规范

### P0-1: 手动录像功能

**功能描述**:
- 用户可手动开始/停止录像
- 录像保存到服务器
- 支持多路同时录像

**文件修改**:

#### 1. VideoPlayer.vue 增强

```vue
<!-- 新增录像按钮 -->
<template>
  <div class="video-controls">
    <!-- 现有控制按钮... -->

    <!-- 录像按钮 -->
    <a-button
      :type="isRecording ? 'primary' : 'default'"
      :danger="isRecording"
      @click="toggleRecording"
    >
      <template #icon>
        <CameraOutlined v-if="!isRecording" />
        <StopOutlined v-else />
      </template>
      {{ isRecording ? '停止录像' : '开始录像' }}
    </a-button>

    <!-- 录像计时器 -->
    <span v-if="isRecording" class="recording-timer">
      <span class="recording-dot"></span>
      {{ recordingTime }}
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';

const isRecording = ref(false);
const recordingStartTime = ref(null);
const recordingTimer = ref(null);
const recordingTime = ref('00:00:00');

// 切换录像状态
const toggleRecording = async () => {
  if (isRecording.value) {
    await stopRecording();
  } else {
    await startRecording();
  }
};

// 开始录像
const startRecording = async () => {
  try {
    // 调用后端API开始录像
    const res = await videoPcApi.startRecord({
      deviceId: props.device.deviceId,
      recordType: 'manual'
    });

    if (res.success) {
      isRecording.value = true;
      recordingStartTime.value = Date.now();
      startRecordingTimer();
      message.success('开始录像');
    } else {
      message.error('开始录像失败: ' + res.message);
    }
  } catch (error) {
    console.error('[视频播放器] 开始录像异常', error);
    message.error('开始录像异常');
  }
};

// 停止录像
const stopRecording = async () => {
  try {
    const res = await videoPcApi.stopRecord({
      deviceId: props.device.deviceId,
      recordId: currentRecordId.value
    });

    if (res.success) {
      isRecording.value = false;
      stopRecordingTimer();
      message.success('录像已保存');
    }
  } catch (error) {
    console.error('[视频播放器] 停止录像异常', error);
    message.error('停止录像异常');
  }
};

// 录像计时器
const startRecordingTimer = () => {
  recordingTimer.value = setInterval(() => {
    const elapsed = Date.now() - recordingStartTime.value;
    recordingTime.value = formatTime(elapsed);
  }, 1000);
};

const stopRecordingTimer = () => {
  if (recordingTimer.value) {
    clearInterval(recordingTimer.value);
    recordingTimer.value = null;
  }
  recordingTime.value = '00:00:00';
};

// 格式化时间
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

// 清理定时器
onUnmounted(() => {
  if (isRecording.value) {
    stopRecording();
  }
  stopRecordingTimer();
});
</script>

<style scoped lang="less">
.recording-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f5222d;
  font-weight: 500;

  .recording-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #f5222d;
    animation: blink 1s infinite;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
```

#### 2. API补充

```javascript
// src/api/business/video/video-pc-api.js

export const videoPcApi = {
  // ... 现有接口

  /**
   * 开始录像
   */
  startRecord: (params) => {
    return postRequest('/video/record/start', params);
  },

  /**
   * 停止录像
   */
  stopRecord: (params) => {
    return postRequest('/video/record/stop', params);
  },

  /**
   * 获取录像列表
   */
  getRecordList: (params) => {
    return getRequest('/video/record/list', { params });
  },

  /**
   * 批量截图
   */
  batchSnapshot: (params) => {
    return postRequest('/video/snapshot/batch', params);
  },
};
```

---

### P0-2: 批量截图功能

**功能描述**:
- 同时截取多路视频画面
- 生成截图列表
- 支持下载和删除

**文件修改**:

#### monitor-preview.vue 增强

```vue
<template>
  <!-- 顶部工具栏 -->
  <a-card :bordered="false" class="smart-margin-bottom10">
    <div class="toolbar">
      <!-- 现有工具... -->

      <!-- 批量截图按钮 -->
      <a-button @click="handleBatchSnapshot" type="primary">
        <template #icon><CameraOutlined /></template>
        批量截图
      </a-button>
    </div>
  </a-card>

  <!-- 截图预览弹窗 -->
  <a-modal
    v-model:visible="snapshotModalVisible"
    title="截图预览"
    width="80%"
    :footer="null"
  >
    <div class="snapshot-gallery">
      <div
        v-for="(snapshot, index) in snapshots"
        :key="index"
        class="snapshot-item"
      >
        <img :src="snapshot.url" :alt="`截图 ${index + 1}`" />
        <div class="snapshot-info">
          <p>{{ snapshot.deviceName }}</p>
          <p>{{ snapshot.time }}</p>
        </div>
        <div class="snapshot-actions">
          <a-button size="small" @click="downloadSnapshot(snapshot)">
            下载
          </a-button>
          <a-button size="small" danger @click="deleteSnapshot(index)">
            删除
          </a-button>
        </div>
      </div>
    </div>
    <div class="snapshot-actions-footer">
      <a-space>
        <a-button @click="downloadAllSnapshots">下载全部</a-button>
        <a-button @click="clearSnapshots">清空</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue';

const snapshotModalVisible = ref(false);
const snapshots = ref([]);

// 批量截图
const handleBatchSnapshot = async () => {
  try {
    const activeDevices = videoWindows
      .filter(w => w.device && w.playing)
      .map(w => w.device.deviceId);

    if (activeDevices.length === 0) {
      message.warning('没有正在播放的视频');
      return;
    }

    message.loading({ content: '正在截取视频...', key: 'snapshot' });

    const res = await videoPcApi.batchSnapshot({
      deviceIds: activeDevices
    });

    message.success({ content: '截图完成', key: 'snapshot' });

    snapshots.value = res.data || [];
    snapshotModalVisible.value = true;
  } catch (error) {
    console.error('[实时监控] 批量截图失败', error);
    message.error({ content: '批量截图失败', key: 'snapshot' });
  }
};

// 下载单个截图
const downloadSnapshot = (snapshot) => {
  const link = document.createElement('a');
  link.href = snapshot.url;
  link.download = `snapshot_${snapshot.deviceId}_${snapshot.time}.jpg`;
  link.click();
};

// 删除截图
const deleteSnapshot = (index) => {
  snapshots.value.splice(index, 1);
};

// 下载全部截图
const downloadAllSnapshots = async () => {
  for (let i = 0; i < snapshots.value.length; i++) {
    downloadSnapshot(snapshots.value[i]);
    await new Promise(resolve => setTimeout(resolve, 300)); // 间隔300ms避免浏览器拦截
  }
  message.success(`已下载 ${snapshots.value.length} 张截图`);
};

// 清空截图
const clearSnapshots = () => {
  snapshots.value = [];
  snapshotModalVisible.value = false;
};
</script>

<style scoped lang="less">
.snapshot-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;

  .snapshot-item {
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;

    img {
      width: 100%;
      height: 150px;
      object-fit: cover;
    }

    .snapshot-info {
      padding: 8px;
      font-size: 12px;
      color: #666;

      p {
        margin: 0;
        line-height: 1.5;
      }
    }

    .snapshot-actions {
      padding: 8px;
      display: flex;
      gap: 8px;
    }
  }
}

.snapshot-actions-footer {
  margin-top: 16px;
  text-align: right;
}
</style>
```

---

### P0-3: 云台预置位管理

**功能描述**:
- 设置预置位（当前位置保存）
- 调用预置位（快速定位）
- 删除预置位
- 重命名预置位

**文件修改**:

#### PTZControl.vue 增强

```vue
<template>
  <div class="ptz-control">
    <!-- 现有云台方向控制... -->

    <!-- 预置位管理 -->
    <div class="preset-section">
      <div class="preset-header">
        <span class="section-title">预置位</span>
        <a-button type="primary" size="small" @click="showAddPreset = true">
          <template #icon><PlusOutlined /></template>
          添加
        </a-button>
      </div>

      <div class="preset-list">
        <div
          v-for="(preset, index) in presets"
          :key="preset.id"
          class="preset-item"
        >
          <a-button
            :type="currentPreset === preset.id ? 'primary' : 'default'"
            size="small"
            @click="callPreset(preset)"
          >
            {{ preset.name || `预置位 ${index + 1}` }}
          </a-button>

          <a-dropdown>
            <template #overlay>
              <a-menu @click="({ key }) => handlePresetAction(key, preset, index)">
                <a-menu-item key="rename">重命名</a-menu-item>
                <a-menu-item key="delete" danger>删除</a-menu-item>
              </a-menu>
            </template>
            <a-button size="small" type="text">
              <template #icon><MoreOutlined /></template>
            </a-button>
          </a-dropdown>
        </div>
      </div>
    </div>

    <!-- 添加预置位弹窗 -->
    <a-modal
      v-model:visible="showAddPreset"
      title="添加预置位"
      @ok="addPreset"
      @cancel="showAddPreset = false"
    >
      <a-form layout="vertical">
        <a-form-item label="预置位名称">
          <a-input v-model:value="newPresetName" placeholder="请输入预置位名称" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, MoreOutlined } from '@ant-design/icons-vue';

const presets = ref([]);
const currentPreset = ref(null);
const showAddPreset = ref(false);
const newPresetName = ref('');

// 加载预置位列表
const loadPresets = async () => {
  try {
    const res = await videoPcApi.getPresets({
      deviceId: props.device.deviceId
    });
    presets.value = res.data || [];
  } catch (error) {
    console.error('[云台控制] 加载预置位失败', error);
  }
};

// 调用预置位
const callPreset = async (preset) => {
  try {
    const res = await videoPcApi.callPreset({
      deviceId: props.device.deviceId,
      presetId: preset.id
    });

    if (res.success) {
      currentPreset.value = preset.id;
      message.success(`已调用预置位: ${preset.name}`);
    }
  } catch (error) {
    console.error('[云台控制] 调用预置位失败', error);
    message.error('调用预置位失败');
  }
};

// 添加预置位
const addPreset = async () => {
  try {
    const res = await videoPcApi.setPreset({
      deviceId: props.device.deviceId,
      presetName: newPresetName.value
    });

    if (res.success) {
      message.success('预置位添加成功');
      showAddPreset.value = false;
      newPresetName.value = '';
      await loadPresets();
    }
  } catch (error) {
    console.error('[云台控制] 添加预置位失败', error);
    message.error('添加预置位失败');
  }
};

// 处理预置位操作
const handlePresetAction = async (key, preset, index) => {
  switch (key) {
    case 'rename':
      const newName = prompt('请输入新的预置位名称:', preset.name || `预置位 ${index + 1}`);
      if (newName) {
        await renamePreset(preset, newName);
      }
      break;
    case 'delete':
      if (confirm(`确定删除预置位 "${preset.name || `预置位 ${index + 1}`}" 吗?`)) {
        await deletePreset(preset);
      }
      break;
  }
};

// 重命名预置位
const renamePreset = async (preset, newName) => {
  try {
    const res = await videoPcApi.updatePreset({
      deviceId: props.device.deviceId,
      presetId: preset.id,
      presetName: newName
    });

    if (res.success) {
      message.success('重命名成功');
      await loadPresets();
    }
  } catch (error) {
    console.error('[云台控制] 重命名预置位失败', error);
    message.error('重命名失败');
  }
};

// 删除预置位
const deletePreset = async (preset) => {
  try {
    const res = await videoPcApi.deletePreset({
      deviceId: props.device.deviceId,
      presetId: preset.id
    });

    if (res.success) {
      message.success('删除成功');
      await loadPresets();
    }
  } catch (error) {
    console.error('[云台控制] 删除预置位失败', error);
    message.error('删除失败');
  }
};

onMounted(() => {
  loadPresets();
});
</script>

<style scoped lang="less">
.ptz-control {
  .preset-section {
    margin-top: 16px;

    .preset-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .section-title {
        font-weight: 500;
        font-size: 14px;
      }
    }

    .preset-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
      gap: 8px;

      .preset-item {
        display: flex;
        gap: 4px;
        align-items: center;
      }
    }
  }
}
</style>
```

#### API补充

```javascript
// src/api/business/video/video-pc-api.js

export const videoPcApi = {
  // ... 现有接口

  /**
   * 获取预置位列表
   */
  getPresets: (params) => {
    return getRequest('/video/ptz/preset/list', { params });
  },

  /**
   * 设置预置位
   */
  setPreset: (params) => {
    return postRequest('/video/ptz/preset/set', params);
  },

  /**
   * 调用预置位
   */
  callPreset: (params) => {
    return postRequest('/video/ptz/preset/call', params);
  },

  /**
   * 更新预置位
   */
  updatePreset: (params) => {
    return postRequest('/video/ptz/preset/update', params);
  },

  /**
   * 删除预置位
   */
  deletePreset: (params) => {
    return postRequest('/video/ptz/preset/delete', params);
  },
};
```

---

## ✅ 全局一致性保障

### 1. 组件复用规范

**核心组件目录结构**:
```
src/components/video/
├── VideoPlayer/
│   ├── index.vue           # 通用视频播放器
│   ├── types.ts            # TypeScript类型定义
│   └── README.md           # 组件文档
├── PTZControl/
│   ├── index.vue           # 通用云台控制器
│   ├── PresetManager.vue   # 预置位管理
│   └── CruiseManager.vue   # 巡航管理
├── DeviceTree/
│   ├── index.vue           # 通用设备树
│   └── TreeNode.vue        # 树节点组件
├── Timeline/
│   ├── index.vue           # 通用时间轴
│   ├── TimeSegment.vue     # 时间片段
│   └── TimelineMarker.vue  # 时间标记
└── SnapshotGallery/
    ├── index.vue           # 截图画廊
    └── SnapshotItem.vue    # 截图项
```

### 2. 状态管理规范

**使用Pinia统一状态管理**:
```typescript
// src/store/modules/video.ts
export const useVideoStore = defineStore('video', {
  state: () => ({
    // 设备相关
    devices: [] as Device[],
    deviceTree: [] as DeviceNode[],

    // 播放相关
    activeWindows: [] as VideoWindow[],
    currentLayout: 4,

    // 云台相关
    presets: [] as Preset[],

    // 录像相关
    recordings: [] as Recording[],
    snapshots: [] as Snapshot[],

    // 告警相关
    alerts: [] as Alert[],
    unreadAlertCount: 0,
  }),

  getters: {
    // 获取正在播放的窗口
    playingWindows: (state) => {
      return state.activeWindows.filter(w => w.playing);
    },

    // 获取在线设备
    onlineDevices: (state) => {
      return state.devices.filter(d => d.status === 'online');
    }
  },

  actions: {
    // 加载设备列表
    async loadDevices() { ... },

    // 获取视频流
    async getStreamUrl(deviceId: string) { ... },

    // 云台控制
    async ptzControl(deviceId: string, command: string) { ... },

    // 开始录像
    async startRecording(deviceId: string) { ... },

    // 批量截图
    async batchSnapshot(deviceIds: string[]) { ... },

    // 加载告警
    async loadAlerts() { ... },

    // 处理告警
    async processAlert(alertId: string, action: string) { ... }
  }
});
```

### 3. API调用规范

**统一错误处理**:
```typescript
// src/utils/video-helper.ts

export async function handleVideoApi<T>(
  apiCall: () => Promise<any>,
  errorMessage: string
): Promise<T> {
  try {
    const res = await apiCall();

    if (res.success) {
      return res.data;
    } else {
      throw new Error(res.message || errorMessage);
    }
  } catch (error) {
    console.error(`[视频模块] ${errorMessage}`, error);
    message.error(`${errorMessage}: ${error.message}`);
    throw error;
  }
}

// 使用示例
export async function loadDeviceList() {
  return handleVideoApi(
    () => videoPcApi.queryDevices({}),
    '加载设备列表失败'
  );
}
```

### 4. 样式规范

**使用统一设计Token**:
```less
// src/design-tokens/video.less

@import (reference) '~/src/design-tokens/index.less';

.video-module {
  // 颜色
  --video-bg: @layout-background-color;
  --video-border: @border-color;
  --video-text-primary: @text-color;
  --video-text-secondary: @text-color-secondary;

  // 间距
  --video-spacing-xs: 4px;
  --video-spacing-sm: 8px;
  --video-spacing-md: 16px;
  --video-spacing-lg: 24px;

  // 圆角
  --video-radius-sm: 4px;
  --video-radius-md: 8px;
  --video-radius-lg: 12px;

  // 阴影
  --video-shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.1);
  --video-shadow-md: 0 4px 16px rgba(0, 0, 0, 0.15);

  // 动画
  --video-transition-fast: 0.2s ease;
  --video-transition-normal: 0.3s ease;
}
```

---

## 🎯 质量保障

### 1. 代码检查清单

**功能完整性**:
- [ ] 所有P0级功能已实现
- [ ] 所有功能有错误处理
- [ ] 所有功能有用户反馈
- [ ] 所有操作有确认提示

**代码质量**:
- [ ] 使用TypeScript类型定义
- [ ] 遵循Vue 3 Composition API规范
- [ ] 组件拆分合理
- [ ] 无硬编码，使用配置

**性能优化**:
- [ ] 使用虚拟滚动处理大列表
- [ ] 图片懒加载
- [ ] 防抖/节流处理高频操作
- [ ] 内存泄漏检查

**用户体验**:
- [ ] 加载状态提示
- [ ] 操作反馈及时
- [ ] 错误提示清晰
- [ ] 快捷键支持

### 2. 测试要求

**单元测试**:
```typescript
// 示例: VideoPlayer.spec.ts
import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import VideoPlayer from './VideoPlayer.vue';

describe('VideoPlayer', () => {
  it('应该正确播放视频', async () => {
    const wrapper = mount(VideoPlayer, {
      props: {
        device: mockDevice
      }
    });

    // 测试播放功能
    // ...
  });

  it('应该正确截图', async () => {
    // 测试截图功能
    // ...
  });
});
```

**E2E测试**:
```typescript
// 示例: monitor.spec.ts
test('完整视频预览流程', async ({ page }) => {
  // 1. 打开监控页面
  await page.goto('/monitor-preview');

  // 2. 选择设备
  await page.click('.device-tree-item');

  // 3. 开始播放
  await page.click('button:has-text("播放")');

  // 4. 验证视频播放
  await expect(page.locator('video')).toHaveAttribute('src', /http/);

  // 5. 截图
  await page.click('button:has-text("截图")');

  // 6. 验证截图成功
  await expect(page.locator('.snapshot-gallery')).toBeVisible();
});
```

---

## 📋 每日进度报告模板

```markdown
## 视频模块前端开发日报 - 日期

### 今日完成
- [x] 功能1：描述
- [x] 功能2：描述

### 进行中
- [ ] 功能3：描述（完成度：60%）

### 遇到的问题
1. 问题描述
   - 解决方案

### 明日计划
- [ ] 功能4：描述
- [ ] 功能5：描述

### 代码统计
- 新增文件：N个
- 修改文件：N个
- 新增代码行数：N行
```

---

## 🚀 快速启动指南

### 开发环境准备

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run dev:h5

# 3. 访问页面
http://localhost:3000/business/smart-video/monitor-preview
```

### 调试技巧

```javascript
// 1. 使用Vue DevTools
// 2. 使用console.log调试
console.log('[视频模块] 当前状态:', state);

// 3. 使用debugger断点
debugger;

// 4. 使用performance.measure测试性能
performance.mark('start');
// ... 执行代码
performance.mark('end');
performance.measure('操作耗时', 'start', 'end');
```

---

**文档维护**: 每日更新进度
**版本历史**: v1.0 (2025-12-24 初始版本)
