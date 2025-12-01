<!--
 * 解码器通道列表组件
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 -->
<template>
  <a-drawer
    :open="visible"
    title="解码器通道列表"
    width="700px"
    placement="right"
    @close="handleClose"
  >
    <template #extra>
      <a-space>
        <a-button @click="refreshChannels">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </template>

    <!-- 解码器基本信息 -->
    <a-descriptions :column="1" bordered class="decoder-info">
      <a-descriptions-item label="解码器名称">
        {{ decoderInfo.decoderName }}
      </a-descriptions-item>
      <a-descriptions-item label="IP地址">
        {{ decoderInfo.ipAddress }}
      </a-descriptions-item>
      <a-descriptions-item label="厂商">
        {{ getManufacturerText(decoderInfo.manufacturer) }}
      </a-descriptions-item>
      <a-descriptions-item label="设备型号">
        {{ decoderInfo.model }}
      </a-descriptions-item>
      <a-descriptions-item label="总通道数">
        <a-tag color="blue">{{ decoderInfo.channelCount }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="已用通道">
        <a-tag color="green">{{ decoderInfo.usedChannels }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="使用率">
        <a-progress
          :percent="channelUsageRate"
          size="small"
          :status="channelUsageRate > 80 ? 'exception' : 'normal'"
        />
      </a-descriptions-item>
    </a-descriptions>

    <a-divider>通道详情</a-divider>

    <!-- 通道列表 -->
    <a-table
      :columns="channelColumns"
      :data-source="channelList"
      :pagination="false"
      size="small"
      :loading="loading"
      row-key="channelNumber"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'success' : 'default'">
            {{ record.status === 1 ? '使用中' : '空闲' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'mapping'">
          <span v-if="record.mapping" class="mapping-text">
            {{ record.mapping }}
          </span>
          <span v-else class="empty-text">未映射</span>
        </template>
        <template v-else-if="column.dataIndex === 'bitrate'">
          <span>{{ record.bitrate }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="configChannel(record)"
              :disabled="record.status !== 1"
            >
              配置
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              @click="releaseChannel(record)"
              :disabled="record.status !== 1"
            >
              释放
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { ReloadOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  decoderInfo: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['close']);

const loading = ref(false);
const channelList = ref([]);

// 通道使用率
const channelUsageRate = computed(() => {
  if (!props.decoderInfo.channelCount) return 0;
  return Math.round((props.decoderInfo.usedChannels / props.decoderInfo.channelCount) * 100);
});

// 通道表格列配置
const channelColumns = [
  {
    title: '通道编号',
    dataIndex: 'channelNumber',
    key: 'channelNumber',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '映射关系',
    dataIndex: 'mapping',
    key: 'mapping',
    ellipsis: true,
  },
  {
    title: '分辨率',
    dataIndex: 'resolution',
    key: 'resolution',
    width: 120,
  },
  {
    title: '码率',
    dataIndex: 'bitrate',
    key: 'bitrate',
    width: 100,
  },
  {
    title: '帧率',
    dataIndex: 'frameRate',
    key: 'frameRate',
    width: 80,
  },
  {
    title: '编码格式',
    dataIndex: 'codec',
    key: 'codec',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 120,
    fixed: 'right',
  },
];

// 获取厂商文本
const getManufacturerText = (manufacturer) => {
  const manufacturerMap = {
    hikvision: '海康威视',
    dahua: '大华',
    uniview: '宇视',
    tiandy: '天地伟业',
  };
  return manufacturerMap[manufacturer] || manufacturer;
};

// 生成模拟通道数据
const generateMockChannels = () => {
  if (!props.decoderInfo.channelCount) return [];

  const channels = [];
  const resolutions = ['1920*1080', '1280*720', '704*576'];
  const codecs = ['H.264', 'H.265', 'MPEG4'];
  const frameRates = [25, 30];
  const bitrates = ['1024kbps', '2048kbps', '4096kbps', '8192kbps'];

  // 模拟部分通道已使用
  const usedChannelCount = props.decoderInfo.usedChannels || 0;

  for (let i = 1; i <= props.decoderInfo.channelCount; i++) {
    channels.push({
      channelNumber: i,
      status: i <= usedChannelCount ? 1 : 0, // 部分通道已使用
      mapping: i <= usedChannelCount ? `摄像头-${String(i).padStart(3, '0')}` : null,
      resolution: resolutions[Math.floor(Math.random() * resolutions.length)],
      bitrate: bitrates[Math.floor(Math.random() * bitrates.length)],
      frameRate: frameRates[Math.floor(Math.random() * frameRates.length)],
      codec: codecs[Math.floor(Math.random() * codecs.length)],
    });
  }

  return channels;
};

// 获取通道列表
const fetchChannels = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));

    // 生成模拟数据
    channelList.value = generateMockChannels();
  } catch (error) {
    message.error('获取通道列表失败');
  } finally {
    loading.value = false;
  }
};

// 刷新通道列表
const refreshChannels = () => {
  fetchChannels();
};

// 配置通道
const configChannel = (record) => {
  message.info(`配置通道 ${record.channelNumber}`);
  // 实际项目中这里应该打开配置弹窗
};

// 释放通道
const releaseChannel = (record) => {
  Modal.confirm({
    title: '释放通道确认',
    content: `确定要释放通道 ${record.channelNumber} 吗？`,
    onOk: async () => {
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 500));

        // 更新通道状态
        record.status = 0;
        record.mapping = null;

        message.success('通道释放成功');

        // 更新使用中通道数
        props.decoderInfo.usedChannels--;
      } catch (error) {
        message.error('释放通道失败');
      }
    },
  });
};

// 关闭抽屉
const handleClose = () => {
  emit('close');
};

// 监听decoderInfo变化
watch(() => props.decoderInfo, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0 && props.visible) {
    fetchChannels();
  }
}, { immediate: true });

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal && props.decoderInfo && Object.keys(props.decoderInfo).length > 0) {
    fetchChannels();
  }
});
</script>

<style lang="less" scoped>
.decoder-info {
  margin-bottom: 16px;

  :deep(.ant-descriptions-item-label) {
    font-weight: 500;
  }
}

.mapping-text {
  color: #1890ff;
  font-weight: 500;
}

.empty-text {
  color: #999;
  font-style: italic;
}

:deep(.ant-progress-line) {
  margin-bottom: 0;
}
</style>