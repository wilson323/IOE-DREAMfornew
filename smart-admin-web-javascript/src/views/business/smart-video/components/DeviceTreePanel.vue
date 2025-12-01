<!--
  设备树面板组件

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="device-tree-panel">
    <a-card :bordered="false" :body-style="{ padding: '8px' }">
      <!-- 搜索框 -->
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索设备"
        style="margin-bottom: 8px"
        @search="handleSearch"
      />

      <!-- 设备树 -->
      <div class="device-tree">
        <a-tree
          v-if="treeData.length > 0"
          :tree-data="treeData"
          :expanded-keys="expandedKeys"
          :selected-keys="selectedKeys"
          :block-node="true"
          show-icon
          @expand="handleExpand"
          @select="handleSelect"
        >
          <template #icon="{ dataRef }">
            <template v-if="dataRef.isGroup">
              <FolderOutlined style="color: #1890ff" />
            </template>
            <template v-else>
              <VideoCameraOutlined
                :style="{ color: dataRef.status === 'online' ? '#52c41a' : '#ff4d4f' }"
              />
            </template>
          </template>

          <template #title="{ dataRef }">
            <div class="tree-node-title">
              <span class="node-name">{{ dataRef.title }}</span>
              <template v-if="!dataRef.isGroup">
                <a-space class="node-actions" size="small">
                  <a-tooltip title="播放">
                    <PlayCircleOutlined
                      class="action-icon"
                      @click.stop="handlePlay(dataRef)"
                    />
                  </a-tooltip>
                  <a-tooltip title="双击播放">
                    <span class="node-tip">双击播放</span>
                  </a-tooltip>
                </a-space>
              </template>
            </div>
          </template>
        </a-tree>

        <a-empty v-else description="暂无设备数据" />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  FolderOutlined,
  VideoCameraOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons-vue';

// 状态
const searchKeyword = ref('');
const expandedKeys = ref(['0']);
const selectedKeys = ref([]);
const treeData = ref([]);

// 模拟设备数据
const mockDeviceTree = [
  {
    key: '0',
    title: '所有设备',
    isGroup: true,
    children: [
      {
        key: '0-1',
        title: '办公楼',
        isGroup: true,
        children: [
          {
            key: '0-1-1',
            title: '前门摄像头',
            deviceId: 'CAM-001',
            status: 'online',
            isGroup: false,
          },
          {
            key: '0-1-2',
            title: '大厅摄像头',
            deviceId: 'CAM-002',
            status: 'online',
            isGroup: false,
          },
          {
            key: '0-1-3',
            title: '电梯口摄像头',
            deviceId: 'CAM-003',
            status: 'offline',
            isGroup: false,
          },
        ],
      },
      {
        key: '0-2',
        title: '停车场',
        isGroup: true,
        children: [
          {
            key: '0-2-1',
            title: '入口摄像头',
            deviceId: 'CAM-004',
            status: 'online',
            isGroup: false,
          },
          {
            key: '0-2-2',
            title: 'A区摄像头',
            deviceId: 'CAM-005',
            status: 'online',
            isGroup: false,
          },
          {
            key: '0-2-3',
            title: 'B区摄像头',
            deviceId: 'CAM-006',
            status: 'online',
            isGroup: false,
          },
        ],
      },
      {
        key: '0-3',
        title: '生产车间',
        isGroup: true,
        children: [
          {
            key: '0-3-1',
            title: '生产线摄像头',
            deviceId: 'CAM-007',
            status: 'online',
            isGroup: false,
          },
          {
            key: '0-3-2',
            title: '仓库摄像头',
            deviceId: 'CAM-008',
            status: 'online',
            isGroup: false,
          },
        ],
      },
    ],
  },
];

// 搜索处理
const handleSearch = (value) => {
  if (!value) {
    treeData.value = mockDeviceTree;
    return;
  }

  const filtered = JSON.parse(JSON.stringify(mockDeviceTree));
  filtered[0].children = filtered[0].children.map(group => ({
    ...group,
    children: group.children.filter(
      device =>
        device.title.includes(value) || device.deviceId.includes(value)
    ),
  })).filter(group => group.children.length > 0);

  treeData.value = filtered;
};

// 展开处理
const handleExpand = (keys) => {
  expandedKeys.value = keys;
};

// 选择处理
const handleSelect = (keys, info) => {
  selectedKeys.value = keys;
};

// 播放处理
const handlePlay = (node) => {
  if (node.isGroup) {
    message.info(`开始播放分组：${node.title}`);
    return;
  }

  emit('play', {
    id: node.deviceId,
    name: node.title,
    status: node.status,
  });
};

// 初始化数据
const initData = () => {
  treeData.value = mockDeviceTree;
};

onMounted(() => {
  initData();
});
</script>

<style scoped lang="less">
.device-tree-panel {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.05);

  .device-tree {
    max-height: calc(100vh - 200px);
    overflow-y: auto;

    :deep(.ant-tree) {
      background: transparent;
      color: #fff;

      .ant-tree-node-content-wrapper {
        width: 100%;
      }

      .ant-tree-node-selected {
        background-color: rgba(24, 144, 255, 0.3) !important;
      }
    }
  }
}

.tree-node-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  color: #fff;

  .node-name {
    flex: 1;
  }

  .node-actions {
    display: none;
  }
}

:deep(.ant-tree-node-content-wrapper:hover) {
  .node-actions {
    display: flex !important;
  }

  .node-tip {
    display: none;
  }
}

.action-icon {
  color: #fff;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #1890ff;
  }
}
</style>
