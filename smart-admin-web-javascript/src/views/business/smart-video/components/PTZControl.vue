<!--
  云台控制组件

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-modal
    :open="visible"
    title="云台控制"
    width="600px"
    :footer="null"
    @cancel="handleClose"
  >
    <div class="ptz-control">
      <!-- 设备信息 -->
      <div class="device-info" v-if="device">
        <a-alert
          :message="`当前设备：${device.name}`"
          type="info"
          show-icon
        />
      </div>

      <!-- 云台控制区 -->
      <div class="ptz-control-area">
        <!-- 方向控制 -->
        <div class="direction-control">
          <a-card title="方向控制" size="small">
            <div class="direction-pad">
              <!-- 上 -->
              <div class="pad-space"></div>
              <a-button
                class="pad-btn"
                size="large"
                @mousedown="startControl('up')"
                @mouseup="stopControl"
                @mouseleave="stopControl"
              >
                <CaretUpOutlined />
              </a-button>
              <div class="pad-space"></div>

              <!-- 左 | 中 | 右 -->
              <a-button
                class="pad-btn"
                size="large"
                @mousedown="startControl('left')"
                @mouseup="stopControl"
                @mouseleave="stopControl"
              >
                <CaretLeftOutlined />
              </a-button>

              <div class="pad-center">
                <a-tag color="blue">停止</a-tag>
              </div>

              <a-button
                class="pad-btn"
                size="large"
                @mousedown="startControl('right')"
                @mouseup="stopControl"
                @mouseleave="stopControl"
              >
                <CaretRightOutlined />
              </a-button>

              <!-- 下 -->
              <div class="pad-space"></div>
              <a-button
                class="pad-btn"
                size="large"
                @mousedown="startControl('down')"
                @mouseup="stopControl"
                @mouseleave="stopControl"
              >
                <CaretDownOutlined />
              </a-button>
              <div class="pad-space"></div>
            </div>

            <!-- 缩放控制 -->
            <div class="zoom-control">
              <a-space>
                <a-button @mousedown="startControl('zoom_in')" @mouseup="stopControl">
                  <template #icon><PlusOutlined /></template>
                  放大
                </a-button>
                <a-button @mousedown="startControl('zoom_out')" @mouseup="stopControl">
                  <template #icon><MinusOutlined /></template>
                  缩小
                </a-button>
              </a-space>
            </div>

            <!-- 焦点控制 -->
            <div class="focus-control">
              <a-space>
                <a-button @mousedown="startControl('focus_in')" @mouseup="stopControl">
                  <template #icon><PlusOutlined /></template>
                  焦距+
                </a-button>
                <a-button @mousedown="startControl('focus_out')" @mouseup="stopControl">
                  <template #icon><MinusOutlined /></template>
                  焦距-
                </a-button>
              </a-space>
            </div>
          </a-card>
        </div>

        <!-- 预置位控制 -->
        <div class="preset-control">
          <a-card title="预置位管理" size="small">
            <template #extra>
              <a-button
                size="small"
                type="primary"
                @click="openAddPresetModal"
              >
                <template #icon><PlusCircleOutlined /></template>
                添加预置位
              </a-button>
            </template>

            <div class="preset-list" v-loading="loading">
              <a-list
                :data-source="presetList"
                size="small"
                bordered
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta
                      :title="item.name"
                      :description="item.description || item.position"
                    />
                    <template #actions>
                      <a-button
                        size="small"
                        type="link"
                        @click="gotoPreset(item)"
                      >
                        调用
                      </a-button>
                      <a-button
                        size="small"
                        type="link"
                        @click="openEditPresetModal(item)"
                      >
                        <template #icon><EditOutlined /></template>
                        编辑
                      </a-button>
                      <a-button
                        size="small"
                        type="link"
                        danger
                        @click="deletePresetItem(item)"
                      >
                        <template #icon><DeleteOutlined /></template>
                        删除
                      </a-button>
                    </template>
                  </a-list-item>
                </template>
                <template #footer>
                  <div class="preset-footer">
                    <a-space>
                      <a-button type="primary" @click="savePreset" size="small">
                        <template #icon><SaveOutlined /></template>
                        保存当前为预置位
                      </a-button>
                    </a-space>
                  </div>
                </template>
              </a-list>
            </div>
          </a-card>
        </div>
      </div>

      <!-- 速度控制 -->
      <div class="speed-control">
        <a-card title="控制速度" size="small">
          <a-slider
            v-model:value="controlSpeed"
            :min="1"
            :max="10"
            :marks="{ 1: '慢', 5: '中', 10: '快' }"
            @change="handleSpeedChange"
          />
        </a-card>
      </div>
    </div>

    <!-- 添加预置位弹窗 -->
    <a-modal
      v-model:open="showAddPresetModal"
      title="添加预置位"
      @ok="confirmAddPreset"
      @cancel="showAddPresetModal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="预置位名称" required>
          <a-input
            v-model:value="newPresetForm.name"
            placeholder="请输入预置位名称"
            :maxlength="50"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            v-model:value="newPresetForm.description"
            placeholder="请输入预置位描述"
            :rows="3"
            :maxlength="200"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑预置位弹窗 -->
    <a-modal
      v-model:open="showEditPresetModal"
      title="编辑预置位"
      @ok="confirmEditPreset"
      @cancel="showEditPresetModal = false"
    >
      <a-form layout="vertical" v-if="editingPreset">
        <a-form-item label="预置位名称" required>
          <a-input
            v-model:value="editingPreset.name"
            placeholder="请输入预置位名称"
            :maxlength="50"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            v-model:value="editingPreset.description"
            placeholder="请输入预置位描述"
            :rows="3"
            :maxlength="200"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  CaretUpOutlined,
  CaretDownOutlined,
  CaretLeftOutlined,
  CaretRightOutlined,
  PlusOutlined,
  MinusOutlined,
  SaveOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusCircleOutlined,
} from '@ant-design/icons-vue';
import { videoPcApi } from '/@/api/business/video/video-pc-api';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  device: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['close', 'control']);

// 状态
const controlSpeed = ref(5);
const controlTimer = ref(null);
const loading = ref(false);

// 预置位列表（从后端加载）
const presetList = ref([]);
const selectedPresetId = ref(null);

// 新建预置位
const showAddPresetModal = ref(false);
const newPresetForm = reactive({
  name: '',
  description: ''
});

// 编辑预置位
const showEditPresetModal = ref(false);
const editingPreset = ref(null);

// 监听设备变化，加载预置位列表
watch(() => props.visible, async (newVal) => {
  if (newVal && props.device) {
    await loadPresetList();
  }
});

// 监听设备变化
watch(() => props.device, async (newDevice) => {
  if (newDevice && props.visible) {
    await loadPresetList();
  }
});

// 加载预置位列表
const loadPresetList = async () => {
  if (!props.device) return;

  try {
    loading.value = true;
    const res = await videoPcApi.getPresetList(
      props.device.deviceId,
      props.device.channelId || '1'
    );

    if (res.code === 200 || res.success) {
      presetList.value = res.data || [];
      if (presetList.value.length === 0) {
        // 如果后端没有数据，使用默认示例数据
        presetList.value = [
          { id: 1, name: '预置位1', description: '前端视角' },
          { id: 2, name: '预置位2', description: '后端视角' },
          { id: 3, name: '预置位3', description: '左侧视角' },
          { id: 4, name: '预置位4', description: '右侧视角' },
        ];
      }
    } else {
      message.warning('获取预置位列表失败，使用默认数据');
      presetList.value = [
        { id: 1, name: '预置位1', description: '前端视角' },
        { id: 2, name: '预置位2', description: '后端视角' },
        { id: 3, name: '预置位3', description: '左侧视角' },
        { id: 4, name: '预置位4', description: '右侧视角' },
      ];
    }
  } catch (error) {
    console.error('[云台控制] 加载预置位列表失败', error);
    // 使用默认数据
    presetList.value = [
      { id: 1, name: '预置位1', description: '前端视角' },
      { id: 2, name: '预置位2', description: '后端视角' },
      { id: 3, name: '预置位3', description: '左侧视角' },
      { id: 4, name: '预置位4', description: '右侧视角' },
    ];
  } finally {
    loading.value = false;
  }
};

// 开始控制
const startControl = (command) => {
  if (!props.device) {
    message.warning('请先选择设备');
    return;
  }

  const params = {
    deviceId: props.device.deviceId || props.device.id,
    channelId: props.device.channelId || '1',
    command,
    speed: controlSpeed.value,
  };

  emit('control', command);

  // 如果是长按操作，持续发送指令
  if (['up', 'down', 'left', 'right', 'zoom_in', 'zoom_out', 'focus_in', 'focus_out'].includes(command)) {
    if (controlTimer.value) {
      clearInterval(controlTimer.value);
    }

    controlTimer.value = setInterval(() => {
      emit('control', command);
    }, 500);
  }
};

// 停止控制
const stopControl = () => {
  if (controlTimer.value) {
    clearInterval(controlTimer.value);
    controlTimer.value = null;
  }

  // 发送停止指令
  if (props.device) {
    emit('control', 'stop');
  }
};

// 速度变化
const handleSpeedChange = (value) => {
  message.info(`控制速度已调整为：${value}`);
};

// 保存预置位
const savePreset = async () => {
  if (!props.device) {
    message.warning('请先选择设备');
    return;
  }

  const newPresetId = presetList.value.length + 1;

  try {
    const res = await videoPcApi.setPreset(
      props.device.deviceId,
      props.device.channelId || '1',
      newPresetId,
      `预置位${newPresetId}`
    );

    if (res.code === 200 || res.success) {
      presetList.value.push({
        id: newPresetId,
        name: `预置位${newPresetId}`,
        description: '自定义预置位'
      });
      message.success('预置位保存成功');
    } else {
      message.error(res.message || '预置位保存失败');
    }
  } catch (error) {
    console.error('[云台控制] 保存预置位失败', error);
    message.error('保存预置位异常');
  }
};

// 清除预置位（已弃用，使用deletePreset代替）
const clearPreset = () => {
  message.info('请使用删除按钮删除指定预置位');
};

// 调用预置位
const gotoPreset = async (preset) => {
  if (!props.device) {
    message.warning('请先选择设备');
    return;
  }

  try {
    const res = await videoPcApi.gotoPreset(
      props.device.deviceId,
      props.device.channelId || '1',
      preset.id,
      controlSpeed.value
    );

    if (res.code === 200 || res.success) {
      message.success(`调用预置位：${preset.name}`);
      emit('control', `goto_preset_${preset.id}`);
    } else {
      message.error(res.message || '调用预置位失败');
    }
  } catch (error) {
    console.error('[云台控制] 调用预置位失败', error);
    message.error('调用预置位异常');
  }
};

// 打开添加预置位弹窗
const openAddPresetModal = () => {
  newPresetForm.name = '';
  newPresetForm.description = '';
  showAddPresetModal.value = true;
};

// 确认添加预置位
const confirmAddPreset = async () => {
  if (!newPresetForm.name.trim()) {
    message.warning('请输入预置位名称');
    return;
  }

  if (!props.device) {
    message.warning('请先选择设备');
    return;
  }

  const newPresetId = presetList.value.length > 0
    ? Math.max(...presetList.value.map(p => p.id)) + 1
    : 1;

  try {
    const res = await videoPcApi.setPreset(
      props.device.deviceId,
      props.device.channelId || '1',
      newPresetId,
      newPresetForm.name
    );

    if (res.code === 200 || res.success) {
      presetList.value.push({
        id: newPresetId,
        name: newPresetForm.name,
        description: newPresetForm.description || '自定义预置位'
      });
      message.success('预置位添加成功');
      showAddPresetModal.value = false;
    } else {
      message.error(res.message || '添加预置位失败');
    }
  } catch (error) {
    console.error('[云台控制] 添加预置位失败', error);
    message.error('添加预置位异常');
  }
};

// 打开编辑预置位弹窗
const openEditPresetModal = (preset) => {
  editingPreset.value = {
    ...preset
  };
  showEditPresetModal.value = true;
};

// 确认编辑预置位
const confirmEditPreset = () => {
  if (!editingPreset.value.name.trim()) {
    message.warning('请输入预置位名称');
    return;
  }

  // 更新本地数据
  const index = presetList.value.findIndex(p => p.id === editingPreset.value.id);
  if (index !== -1) {
    presetList.value[index] = {
      ...presetList.value[index],
      name: editingPreset.value.name,
      description: editingPreset.value.description
    };
  }

  message.success('预置位更新成功');
  showEditPresetModal.value = false;
};

// 删除预置位
const deletePresetItem = (preset) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除预置位"${preset.name}"吗？`,
    onOk: async () => {
      if (!props.device) {
        message.warning('请先选择设备');
        return;
      }

      try {
        const res = await videoPcApi.deletePreset(
          props.device.deviceId,
          props.device.channelId || '1',
          preset.id
        );

        if (res.code === 200 || res.success) {
          const index = presetList.value.findIndex(p => p.id === preset.id);
          if (index !== -1) {
            presetList.value.splice(index, 1);
          }
          message.success('预置位删除成功');
        } else {
          message.error(res.message || '删除预置位失败');
        }
      } catch (error) {
        console.error('[云台控制] 删除预置位失败', error);
        message.error('删除预置位异常');
      }
    }
  });
};

// 关闭
const handleClose = () => {
  stopControl();
  emit('close');
};

// 初始化加载
onMounted(async () => {
  if (props.visible && props.device) {
    await loadPresetList();
  }
});
</script>

<style scoped lang="less">
.ptz-control {
  .device-info {
    margin-bottom: 16px;
  }

  .ptz-control-area {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }

  .direction-control {
    .direction-pad {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 60px);
      gap: 8px;
      margin-bottom: 16px;
    }

    .pad-space {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .pad-btn {
      width: 100%;
      height: 100%;
      font-size: 24px;
    }

    .pad-center {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .zoom-control,
    .focus-control {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
    }
  }

  .preset-control {
    .preset-list {
      max-height: 250px;
      overflow-y: auto;
      margin-bottom: 12px;
    }

    .preset-footer {
      display: flex;
      justify-content: center;
      padding: 8px 0;
      border-top: 1px solid #f0f0f0;
    }
  }

  .speed-control {
    :deep(.ant-slider) {
      margin: 16px 8px;
    }
  }
}
</style>
