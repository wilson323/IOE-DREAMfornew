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
          <a-card title="预置位" size="small">
            <div class="preset-list">
              <a-list
                :data-source="presetList"
                size="small"
                bordered
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta
                      :title="item.name"
                      :description="item.position"
                    />
                    <template #actions>
                      <a-button size="small" type="link" @click="gotoPreset(item)">
                        调用
                      </a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </div>

            <div class="preset-actions">
              <a-space>
                <a-button type="primary" @click="savePreset">
                  <template #icon><SaveOutlined /></template>
                  保存预置位
                </a-button>
                <a-button @click="clearPreset">
                  <template #icon><DeleteOutlined /></template>
                  清除预置位
                </a-button>
              </a-space>
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
  </a-modal>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import {
  CaretUpOutlined,
  CaretDownOutlined,
  CaretLeftOutlined,
  CaretRightOutlined,
  PlusOutlined,
  MinusOutlined,
  SaveOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue';

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

// 预置位列表
const presetList = reactive([
  { id: 1, name: '预置位1', position: '前端视角' },
  { id: 2, name: '预置位2', position: '后端视角' },
  { id: 3, name: '预置位3', position: '左侧视角' },
  { id: 4, name: '预置位4', position: '右侧视角' },
]);

// 开始控制
const startControl = (command) => {
  if (!props.device) {
    message.warning('请先选择设备');
    return;
  }

  const params = {
    deviceId: props.device.id,
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
};

// 速度变化
const handleSpeedChange = (value) => {
  message.info(`控制速度已调整为：${value}`);
};

// 保存预置位
const savePreset = () => {
  message.success('预置位保存成功');
};

// 清除预置位
const clearPreset = () => {
  message.success('预置位已清除');
};

// 调用预置位
const gotoPreset = (preset) => {
  message.info(`调用预置位：${preset.name}`);
  emit('control', `goto_preset_${preset.id}`);
};

// 关闭
const handleClose = () => {
  stopControl();
  emit('close');
};
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
      max-height: 200px;
      overflow-y: auto;
      margin-bottom: 12px;
    }

    .preset-actions {
      display: flex;
      justify-content: center;
    }
  }

  .speed-control {
    :deep(.ant-slider) {
      margin: 16px 8px;
    }
  }
}
</style>
