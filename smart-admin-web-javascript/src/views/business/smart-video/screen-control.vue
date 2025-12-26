<!--
  智能视频-大屏管控页面

  @Author:    Claude Code
  @Date:      2025-12-24
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-row :gutter="16">
      <!-- 左侧：大屏列表 -->
      <a-col :span="6">
        <a-card title="大屏列表" :bordered="false">
          <template #extra>
            <a-button type="primary" size="small" @click="openAddScreenModal">
              <PlusOutlined />
            </a-button>
          </template>

          <a-list
            :data-source="screenList"
            size="small"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'active-screen': selectedScreenId === item.id }"
                @click="selectScreen(item)"
              >
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge :status="item.status === '在线' ? 'processing' : 'default'">
                      <MonitorOutlined style="font-size: 24px;" />
                    </a-badge>
                  </template>
                  <template #title>
                    <a-space>
                      <span>{{ item.name }}</span>
                      <a-tag v-if="item.running" color="green">运行中</a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    {{ item.resolution }} | {{ item.ip }}
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-dropdown>
                    <MoreOutlined />
                    <template #overlay>
                      <a-menu @click="(e) => handleMenuAction(e, item)">
                        <a-menu-item key="edit">
                          <EditOutlined />
                          编辑
                        </a-menu-item>
                        <a-menu-item key="start" :disabled="item.running">
                          <PlayCircleOutlined />
                          启动
                        </a-menu-item>
                        <a-menu-item key="stop" :disabled="!item.running">
                          <PauseCircleOutlined />
                          停止
                        </a-menu-item>
                        <a-menu-item key="restart" :disabled="!item.running">
                          <ReloadOutlined />
                          重启
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item key="delete" danger>
                          <DeleteOutlined />
                          删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- 屏幕信息卡片 -->
        <a-card v-if="selectedScreen" title="屏幕信息" :bordered="false" style="margin-top: 16px;">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="屏幕名称">{{ selectedScreen.name }}</a-descriptions-item>
            <a-descriptions-item label="IP地址">{{ selectedScreen.ip }}</a-descriptions-item>
            <a-descriptions-item label="分辨率">{{ selectedScreen.resolution }}</a-descriptions-item>
            <a-descriptions-item label="尺寸">{{ selectedScreen.size }}</a-descriptions-item>
            <a-descriptions-item label="品牌">{{ selectedScreen.brand || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-badge :status="selectedScreen.status === '在线' ? 'processing' : 'default'" :text="selectedScreen.status" />
            </a-descriptions-item>
            <a-descriptions-item label="运行时间">
              {{ selectedScreen.running ? runningTime : '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>

      <!-- 右侧：大屏控制 -->
      <a-col :span="18">
        <a-card :bordered="false">
          <template #title>
            <a-space>
              <span>大屏控制</span>
              <a-tag v-if="selectedScreen" :color="selectedScreen.status === '在线' ? 'green' : 'red'">
                {{ selectedScreen?.status }}
              </a-tag>
            </a-space>
          </template>

          <template #extra>
            <a-space v-if="selectedScreen">
              <a-button type="primary" :disabled="selectedScreen.status !== '在线' || selectedScreen.running" @click="startScreen">
                <template #icon><PlayCircleOutlined /></template>
                启动大屏
              </a-button>
              <a-button :disabled="!selectedScreen.running" @click="stopScreen">
                <template #icon><PauseCircleOutlined /></template>
                停止大屏
              </a-button>
              <a-button :disabled="!selectedScreen.running" @click="restartScreen">
                <template #icon><ReloadOutlined /></template>
                重启
              </a-button>
              <a-button @click="refreshScreen">
                <template #icon><SyncOutlined /></template>
                刷新
              </a-button>
              <a-button @click="takeSnapshot">
                <template #icon><CameraOutlined /></template>
                截图
              </a-button>
            </a-space>
          </template>

          <a-empty v-if="!selectedScreen" description="请选择大屏进行控制" />

          <div v-else class="screen-control-content">
            <!-- 大屏预览 -->
            <div class="screen-preview-container">
              <div class="screen-preview" :style="{ aspectRatio: getAspectRatio() }">
                <!-- 布局网格 -->
                <div class="layout-grid" :class="`layout-${currentLayout}`">
                  <div
                    v-for="(cell, index) in layoutCells"
                    :key="index"
                    class="grid-cell"
                  >
                    <div v-if="cell.deviceId" class="cell-content">
                      <video
                        v-if="cell.playing"
                        :ref="el => { if (el) videoRefs[index] = el }"
                        class="cell-video"
                        autoplay
                        muted
                        loop
                        playsinline
                      ></video>

                      <div v-else-if="cell.loading" class="cell-loading">
                        <a-spin size="small" />
                      </div>

                      <!-- 设备信息 -->
                      <div class="cell-info">
                        <a-tag size="small" color="blue">{{ cell.deviceName }}</a-tag>
                      </div>
                    </div>

                    <div v-else class="cell-empty">
                      <PlusOutlined style="font-size: 24px; color: #d9d9d9;" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <a-divider />

            <!-- 布局配置 -->
            <a-card title="布局配置" :bordered="false" size="small">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form layout="vertical">
                    <a-form-item label="布局模式">
                      <a-select v-model:value="currentLayout" @change="handleLayoutChange">
                        <a-select-option :value="1">1x1 (单画面)</a-select-option>
                        <a-select-option :value="4">2x2 (4画面)</a-select-option>
                        <a-select-option :value="9">3x3 (9画面)</a-select-option>
                        <a-select-option :value="16">4x4 (16画面)</a-select-option>
                        <a-select-option :value="25">5x5 (25画面)</a-select-option>
                      </a-select>
                    </a-form-item>

                    <a-form-item label="快速布局">
                      <a-space>
                        <a-button size="small" @click="applyPresetLayout(1)">单屏</a-button>
                        <a-button size="small" @click="applyPresetLayout(4)">4分屏</a-button>
                        <a-button size="small" @click="applyPresetLayout(9)">9分屏</a-button>
                        <a-button size="small" @click="applyPresetLayout(16)">16分屏</a-button>
                      </a-space>
                    </a-form-item>

                    <a-form-item label="双屏模式">
                      <a-switch
                        v-model:checked="dualScreenMode"
                        checked-children="开启"
                        un-checked-children="关闭"
                        @change="handleDualScreenChange"
                      />
                      <span style="margin-left: 8px; color: #999;">需要硬件支持</span>
                    </a-form-item>
                  </a-form>
                </a-col>

                <a-col :span="12">
                  <a-form layout="vertical">
                    <a-form-item label="轮巡设置">
                      <a-space direction="vertical" style="width: 100%;">
                        <a-checkbox v-model:checked="enableRotation">启用轮巡</a-checkbox>
                        <a-input-number
                          v-model:value="rotationInterval"
                          :min="5"
                          :max="300"
                          :disabled="!enableRotation"
                          suffix="秒"
                          style="width: 100%"
                        />
                        <a-button
                          type="primary"
                          :disabled="!enableRotation || !selectedScreen.running"
                          @click="applyRotation"
                        >
                          应用轮巡
                        </a-button>
                      </a-space>
                    </a-form-item>

                    <a-form-item label="显示设置">
                      <a-space direction="vertical" style="width: 100%;">
                        <a-checkbox v-model:checked="showDeviceInfo">显示设备信息</a-checkbox>
                        <a-checkbox v-model:checked="showTimestamp">显示时间戳</a-checkbox>
                        <a-checkbox v-model:checked="enableOsd">启用OSD叠加</a-checkbox>
                      </a-space>
                    </a-form-item>
                  </a-form>
                </a-col>
              </a-row>

              <a-divider />

              <!-- 视频源配置 -->
              <a-form layout="vertical">
                <a-form-item label="视频源分配">
                  <div class="source-assignment">
                    <div
                      v-for="(cell, index) in layoutCells"
                      :key="index"
                      class="source-row"
                    >
                      <span class="cell-label">画面 {{ index + 1 }}:</span>
                      <a-select
                        v-model:value="cell.deviceId"
                        placeholder="选择视频源"
                        style="flex: 1;"
                        show-search
                        :filter-option="filterDeviceOption"
                        @change="(value) => handleDeviceChange(index, value)"
                      >
                        <a-select-option v-for="device in deviceList" :key="device.id" :value="device.id">
                          {{ device.name }}
                        </a-select-option>
                      </a-select>
                      <a-button
                        size="small"
                        @click="clearCellSource(index)"
                      >
                        <template #icon><CloseOutlined /></template>
                      </a-button>
                    </div>
                  </div>
                </a-form-item>

                <a-form-item :wrapper-col="{ offset: 0 }">
                  <a-space>
                    <a-button type="primary" @click="applyLayoutConfig">
                      <template #icon><CheckOutlined /></template>
                      应用配置
                    </a-button>
                    <a-button @click="resetLayoutConfig">
                      <template #icon><ReloadOutlined /></template>
                      重置
                    </a-button>
                    <a-button @click="saveAsPreset">
                      <template #icon><SaveOutlined /></template>
                      保存为预设
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-card>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 添加大屏弹窗 -->
    <a-modal
      v-model:open="addScreenVisible"
      title="添加大屏"
      @ok="handleAddScreen"
      @cancel="addScreenVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="大屏名称" required>
          <a-input v-model:value="newScreenForm.name" placeholder="请输入大屏名称" />
        </a-form-item>
        <a-form-item label="IP地址" required>
          <a-input v-model:value="newScreenForm.ip" placeholder="例如: 192.168.1.100" />
        </a-form-item>
        <a-form-item label="分辨率">
          <a-select v-model:value="newScreenForm.resolution">
            <a-select-option value="1920x1080">1920x1080 (Full HD)</a-select-option>
            <a-select-option value="2560x1440">2560x1440 (2K)</a-select-option>
            <a-select-option value="3840x2160">3840x2160 (4K)</a-select-option>
            <a-select-option value="7680x4320">7680x4320 (8K)</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="尺寸">
          <a-input v-model:value="newScreenForm.size" placeholder="例如: 55英寸" />
        </a-form-item>
        <a-form-item label="品牌">
          <a-input v-model:value="newScreenForm.brand" placeholder="例如: Samsung, LG" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预设布局管理 -->
    <a-modal
      v-model:open="presetModalVisible"
      title="预设布局"
      @ok="savePresetName"
      @cancel="presetModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="预设名称" required>
          <a-input v-model:value="presetName" placeholder="请输入预设名称" />
        </a-form-item>
        <a-form-item label="预设描述">
          <a-textarea v-model:value="presetDescription" :rows="3" placeholder="请输入预设描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlusOutlined,
  MonitorOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  SyncOutlined,
  CameraOutlined,
  ReloadOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined,
  CloseOutlined,
  CheckOutlined,
  SaveOutlined
} from '@ant-design/icons-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';

// 状态
const loading = ref(false);
const screenList = ref([]);
const selectedScreenId = ref(null);
const selectedScreen = ref(null);
const currentLayout = ref(4);
const dualScreenMode = ref(false);
const enableRotation = ref(false);
const rotationInterval = ref(30);
const showDeviceInfo = ref(true);
const showTimestamp = ref(true);
const enableOsd = ref(false);

// 布局单元格
const layoutCells = ref([]);
const videoRefs = ref({});

// 设备列表
const deviceList = ref([]);

// 运行时间
const runningTime = ref('00:00:00');
let runningTimer = null;

// 弹窗状态
const addScreenVisible = ref(false);
const presetModalVisible = ref(false);

// 表单数据
const newScreenForm = reactive({
  name: '',
  ip: '',
  resolution: '1920x1080',
  size: '',
  brand: ''
});

const presetName = ref('');
const presetDescription = ref('');

// 计算属性
const getAspectRatio = () => {
  if (!selectedScreen.value) return '16 / 9';
  const resolution = selectedScreen.value.resolution;
  if (resolution === '1920x1080') return '16 / 9';
  if (resolution === '2560x1440') return '16 / 9';
  if (resolution === '3840x2160') return '16 / 9';
  return '16 / 9';
};

// 初始化布局单元格
const initLayoutCells = () => {
  const count = currentLayout.value;
  const cells = [];

  for (let i = 0; i < count; i++) {
    if (layoutCells.value[i]) {
      cells.push(layoutCells.value[i]);
    } else {
      cells.push({
        deviceId: null,
        deviceName: '',
        loading: false,
        playing: false
      });
    }
  }

  layoutCells.value = cells;
};

// 加载大屏列表
const loadScreenList = async () => {
  loading.value = true;
  try {
    // 调用后端API
    // const res = await videoPcApi.queryScreens();

    // 模拟数据
    screenList.value = [
      {
        id: 1,
        name: '监控大厅大屏1',
        ip: '192.168.1.100',
        resolution: '3840x2160',
        size: '85英寸',
        brand: 'Samsung',
        status: '在线',
        running: false,
        layout: 9,
        createTime: '2024-11-01 10:00:00'
      },
      {
        id: 2,
        name: '安保中心大屏',
        ip: '192.168.1.101',
        resolution: '1920x1080',
        size: '55英寸',
        brand: 'LG',
        status: '在线',
        running: true,
        layout: 4,
        createTime: '2024-11-02 14:20:00'
      },
      {
        id: 3,
        name: '会议室大屏',
        ip: '192.168.1.102',
        resolution: '2560x1440',
        size: '65英寸',
        brand: 'Sony',
        status: '离线',
        running: false,
        layout: 1,
        createTime: '2024-11-03 09:15:00'
      }
    ];

    // 加载设备列表
    loadDeviceList();
  } catch (error) {
    console.error('[大屏管控] 加载大屏列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 加载设备列表
const loadDeviceList = async () => {
  try {
    const res = await videoPcApi.queryDevices({});

    if (res.code === 200 || res.success) {
      deviceList.value = res.data?.list || [];
    }
  } catch (error) {
    console.error('[大屏管控] 加载设备列表失败', error);
    // 模拟设备数据
    deviceList.value = [
      { id: 1, name: '前门摄像头-001', status: 'online' },
      { id: 2, name: '停车场摄像头-01', status: 'online' },
      { id: 3, name: '走廊摄像头-003', status: 'online' },
      { id: 4, name: '大厅摄像头-002', status: 'online' },
      { id: 5, name: '周界摄像头-005', status: 'online' }
    ];
  }
};

// 选择大屏
const selectScreen = async (screen) => {
  selectedScreenId.value = screen.id;
  selectedScreen.value = screen;
  currentLayout.value = screen.layout || 4;

  initLayoutCells();

  if (screen.running) {
    startRunningTimer();
  }
};

// 启动大屏
const startScreen = async () => {
  if (!selectedScreen.value) return;

  try {
    // const res = await videoPcApi.startScreen(selectedScreen.value.id);
    selectedScreen.value.running = true;
    message.success('大屏启动成功');
    startRunningTimer();
  } catch (error) {
    console.error('[大屏管控] 启动大屏失败', error);
    message.error('启动大屏失败');
  }
};

// 停止大屏
const stopScreen = async () => {
  if (!selectedScreen.value) return;

  Modal.confirm({
    title: '确认停止',
    content: '确定要停止该大屏吗？',
    onOk: async () => {
      try {
        // const res = await videoPcApi.stopScreen(selectedScreen.value.id);
        selectedScreen.value.running = false;
        message.success('大屏已停止');

        if (runningTimer) {
          clearInterval(runningTimer);
          runningTimer = null;
        }
        runningTime.value = '00:00:00';
      } catch (error) {
        console.error('[大屏管控] 停止大屏失败', error);
        message.error('停止大屏失败');
      }
    }
  });
};

// 重启大屏
const restartScreen = async () => {
  if (!selectedScreen.value) return;

  Modal.confirm({
    title: '确认重启',
    content: '确定要重启该大屏吗？',
    onOk: async () => {
      try {
        await stopScreen();
        await new Promise(resolve => setTimeout(resolve, 2000));
        await startScreen();
        message.success('大屏重启成功');
      } catch (error) {
        console.error('[大屏管控] 重启大屏失败', error);
        message.error('重启大屏失败');
      }
    }
  });
};

// 刷新大屏
const refreshScreen = async () => {
  if (!selectedScreen.value) return;

  try {
    await loadScreenList();
    const screen = screenList.value.find(s => s.id === selectedScreenId.value);
    if (screen) {
      selectedScreen.value = screen;
    }
    message.success('刷新成功');
  } catch (error) {
    console.error('[大屏管控] 刷新失败', error);
    message.error('刷新失败');
  }
};

// 截图
const takeSnapshot = async () => {
  if (!selectedScreen.value) return;

  try {
    // const res = await videoPcApi.screenSnapshot(selectedScreen.value.id);
    message.success('截图已保存');
  } catch (error) {
    console.error('[大屏管控] 截图失败', error);
    message.error('截图失败');
  }
};

// 布局切换
const handleLayoutChange = (layout) => {
  currentLayout.value = layout;
  initLayoutCells();
};

// 应用预设布局
const applyPresetLayout = (layout) => {
  currentLayout.value = layout;
  initLayoutCells();
};

// 双屏模式切换
const handleDualScreenChange = (checked) => {
  dualScreenMode.value = checked;
  if (checked) {
    message.info('双屏模式已启用，需要硬件支持');
  }
};

// 应用轮巡
const applyRotation = async () => {
  if (!selectedScreen.value) return;

  try {
    // const res = await videoPcApi.applyRotation(selectedScreen.value.id, {
    //   enable: enableRotation.value,
    //   interval: rotationInterval.value
    // });

    message.success('轮巡配置已应用');
  } catch (error) {
    console.error('[大屏管控] 应用轮巡失败', error);
    message.error('应用轮巡失败');
  }
};

// 设备选择过滤
const filterDeviceOption = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

// 设备变更
const handleDeviceChange = async (index, deviceId) => {
  if (!deviceId) return;

  const device = deviceList.value.find(d => d.id === deviceId);
  if (device) {
    layoutCells.value[index] = {
      ...layoutCells.value[index],
      deviceId: deviceId,
      deviceName: device.name,
      loading: true,
      playing: false
    };

    // 模拟加载视频
    setTimeout(() => {
      layoutCells.value[index].loading = false;
      layoutCells.value[index].playing = true;

      const videoEl = videoRefs.value[index];
      if (videoEl) {
        console.log('[大屏管控] 加载视频:', device.name);
      }
    }, 1000);
  }
};

// 清除单元格视频源
const clearCellSource = (index) => {
  layoutCells.value[index] = {
    deviceId: null,
    deviceName: '',
    loading: false,
    playing: false
  };
};

// 应用布局配置
const applyLayoutConfig = async () => {
  if (!selectedScreen.value) return;

  try {
    const config = {
      screenId: selectedScreen.value.id,
      layout: currentLayout.value,
      cells: layoutCells.value,
      settings: {
        dualScreenMode: dualScreenMode.value,
        enableRotation: enableRotation.value,
        rotationInterval: rotationInterval.value,
        showDeviceInfo: showDeviceInfo.value,
        showTimestamp: showTimestamp.value,
        enableOsd: enableOsd.value
      }
    };

    // const res = await videoPcApi.applyScreenConfig(config);

    message.success('布局配置已应用');
  } catch (error) {
    console.error('[大屏管控] 应用配置失败', error);
    message.error('应用配置失败');
  }
};

// 重置布局配置
const resetLayoutConfig = () => {
  currentLayout.value = 4;
  dualScreenMode.value = false;
  enableRotation.value = false;
  rotationInterval.value = 30;
  showDeviceInfo.value = true;
  showTimestamp.value = true;
  enableOsd.value = false;
  initLayoutCells();
  message.info('配置已重置');
};

// 保存为预设
const saveAsPreset = () => {
  presetModalVisible.value = true;
};

// 保存预设名称
const savePresetName = () => {
  if (!presetName.value.trim()) {
    message.warning('请输入预设名称');
    return;
  }

  const preset = {
    name: presetName.value,
    description: presetDescription.value,
    layout: currentLayout.value,
    cells: layoutCells.value
  };

  // 保存到本地存储
  const presets = JSON.parse(localStorage.getItem('screenPresets') || '[]');
  presets.push(preset);
  localStorage.setItem('screenPresets', JSON.stringify(presets));

  message.success('预设保存成功');
  presetModalVisible.value = false;
  presetName.value = '';
  presetDescription.value = '';
};

// 菜单操作
const handleMenuAction = (e, screen) => {
  const key = e.key;

  switch (key) {
    case 'edit':
      message.info('编辑功能开发中');
      break;
    case 'start':
      selectScreen(screen);
      startScreen();
      break;
    case 'stop':
      selectScreen(screen);
      stopScreen();
      break;
    case 'restart':
      selectScreen(screen);
      restartScreen();
      break;
    case 'delete':
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除大屏"${screen.name}"吗？`,
        onOk: () => {
          const index = screenList.value.findIndex(s => s.id === screen.id);
          if (index !== -1) {
            screenList.value.splice(index, 1);
            if (selectedScreenId.value === screen.id) {
              selectedScreen.value = null;
              selectedScreenId.value = null;
            }
          }
          message.success('删除成功');
        }
      });
      break;
  }
};

// 打开添加大屏弹窗
const openAddScreenModal = () => {
  newScreenForm.name = '';
  newScreenForm.ip = '';
  newScreenForm.resolution = '1920x1080';
  newScreenForm.size = '';
  newScreenForm.brand = '';
  addScreenVisible.value = true;
};

// 添加大屏
const handleAddScreen = () => {
  if (!newScreenForm.name.trim()) {
    message.warning('请输入大屏名称');
    return;
  }
  if (!newScreenForm.ip.trim()) {
    message.warning('请输入IP地址');
    return;
  }

  const newScreen = {
    id: Date.now(),
    name: newScreenForm.name,
    ip: newScreenForm.ip,
    resolution: newScreenForm.resolution,
    size: newScreenForm.size || '-',
    brand: newScreenForm.brand || '-',
    status: '在线',
    running: false,
    layout: 4,
    createTime: new Date().toLocaleString()
  };

  screenList.value.push(newScreen);
  message.success('添加成功');
  addScreenVisible.value = false;
};

// 运行时间计时器
const startRunningTimer = () => {
  if (runningTimer) {
    clearInterval(runningTimer);
  }

  let seconds = 0;
  runningTimer = setInterval(() => {
    seconds++;
    const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    runningTime.value = `${h}:${m}:${s}`;
  }, 1000);
};

// 生命周期
onMounted(async () => {
  await loadScreenList();
});

onUnmounted(() => {
  if (runningTimer) {
    clearInterval(runningTimer);
  }
});
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .active-screen {
    background-color: #e6f7ff;
    border-left: 3px solid #1890ff;
  }

  .screen-control-content {
    .screen-preview-container {
      background-color: #000;
      padding: 16px;
      border-radius: 4px;
      margin-bottom: 16px;
    }

    .screen-preview {
      width: 100%;
      background-color: #1a1a1a;
      border-radius: 4px;
      overflow: hidden;
    }

    .layout-grid {
      display: grid;
      gap: 2px;
      height: 100%;
      width: 100%;

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
    }

    .grid-cell {
      position: relative;
      background-color: #2a2a2a;
      aspect-ratio: 16 / 9;
      overflow: hidden;
    }

    .cell-content {
      width: 100%;
      height: 100%;
      position: relative;
    }

    .cell-video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cell-loading {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #fff;
    }

    .cell-info {
      position: absolute;
      top: 4px;
      left: 4px;
    }

    .cell-empty {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #d9d9d9;
    }

    .source-assignment {
      display: flex;
      flex-direction: column;
      gap: 8px;
      max-height: 300px;
      overflow-y: auto;

      .source-row {
        display: flex;
        align-items: center;
        gap: 8px;

        .cell-label {
          min-width: 70px;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
