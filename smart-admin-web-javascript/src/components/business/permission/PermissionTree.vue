<template>
  <div class="permission-tree">
    <div class="tree-header" v-if="showHeader">
      <div class="tree-title">
        <slot name="title">
          <span>{{ title }}</span>
        </slot>
      </div>
      <div class="tree-actions">
        <slot name="actions">
          <a-button
            v-if="expandable"
            type="link"
            size="small"
            @click="handleExpandAll"
          >
            {{ allExpanded ? '收起全部' : '展开全部' }}
          </a-button>
          <a-input-search
            v-if="searchable"
            v-model:value="searchKeyword"
            placeholder="搜索权限"
            style="width: 200px"
            allowClear
            @search="handleSearch"
          />
        </slot>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div class="tree-container" :style="{ height: containerHeight }">
        <a-tree
          v-model:expandedKeys="expandedKeys"
          v-model:selectedKeys="selectedKeys"
          v-model:checkedKeys="checkedKeys"
          :tree-data="treeData"
          :checkable="checkable"
          :selectable="selectable"
          :multiple="multiple"
          :default-expand-all="defaultExpandAll"
          :show-line="showLine"
          :show-icon="showIcon"
          :draggable="draggable"
          :block-node="blockNode"
          :check-strictly="checkStrictly"
          :field-names="fieldNames"
          :disabled="disabled"
          @select="handleSelect"
          @check="handleCheck"
          @expand="handleExpand"
          @drop="handleDrop"
          @rightClick="handleRightClick"
        >
          <template #title="{ title, dataRef }">
            <div class="tree-node-title">
              <span class="node-title-text" :title="title">
                {{ title }}
              </span>

              <!-- 安全级别标识 -->
              <a-tag
                v-if="showSecurityLevel && dataRef.securityLevel"
                :color="getSecurityLevelColor(dataRef.securityLevel)"
                size="small"
                class="security-level-tag"
              >
                {{ getSecurityLevelText(dataRef.securityLevel) }}
              </a-tag>

              <!-- 权限类型标识 -->
              <a-tag
                v-if="showPermissionType && dataRef.type"
                :color="getPermissionTypeColor(dataRef.type)"
                size="small"
                class="permission-type-tag"
              >
                {{ getPermissionTypeText(dataRef.type) }}
              </a-tag>

              <!-- 状态标识 -->
              <a-badge
                v-if="showStatus && dataRef.status"
                :status="getStatusBadgeType(dataRef.status)"
                :text="getStatusText(dataRef.status)"
                size="small"
              />

              <!-- 自定义内容插槽 -->
              <slot name="node-extra" :node="dataRef"></slot>
            </div>
          </template>

          <template #icon="{ dataRef }">
            <slot name="icon" :node="dataRef">
              <component
                v-if="dataRef.icon"
                :is="dataRef.icon"
                :style="{ color: dataRef.iconColor }"
              />
              <FolderOutlined
                v-else-if="dataRef.children && dataRef.children.length > 0"
              />
              <FileOutlined v-else />
            </slot>
          </template>
        </a-tree>
      </div>
    </a-spin>

    <!-- 空状态 -->
    <a-empty
      v-if="!loading && (!treeData || treeData.length === 0)"
      :description="emptyDescription"
      :image="emptyImage"
    >
      <template #image>
        <slot name="empty-image">
          <FileSearchOutlined />
        </slot>
      </template>
    </a-empty>

    <!-- 右键菜单 -->
    <a-dropdown
      v-model:open="contextMenuVisible"
      :trigger="['contextmenu']"
      placement="bottomLeft"
    >
      <div
        ref="contextMenuRef"
        :style="{
          position: 'fixed',
          left: contextMenuPosition.x + 'px',
          top: contextMenuPosition.y + 'px',
          width: '0',
          height: '0'
        }"
      />
      <template #overlay>
        <a-menu @click="handleContextMenuClick">
          <a-menu-item
            v-for="item in contextMenuItems"
            :key="item.key"
            :disabled="item.disabled"
          >
            <component v-if="item.icon" :is="item.icon" />
            {{ item.label }}
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  FolderOutlined,
  FileOutlined,
  FileSearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  EyeOutlined,
  SettingOutlined,
  ExportOutlined
} from '@ant-design/icons-vue';
import { SecurityLevel, PermissionType, PermissionStatus } from '/@/types/permission';

// Props定义
const props = defineProps({
  // 数据相关
  value: {
    type: Array,
    default: () => []
  },
  treeData: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },

  // 显示控制
  showHeader: {
    type: Boolean,
    default: true
  },
  showIcon: {
    type: Boolean,
    default: true
  },
  showLine: {
    type: Boolean,
    default: true
  },
  showSecurityLevel: {
    type: Boolean,
    default: true
  },
  showPermissionType: {
    type: Boolean,
    default: false
  },
  showStatus: {
    type: Boolean,
    default: false
  },

  // 功能控制
  checkable: {
    type: Boolean,
    default: true
  },
  selectable: {
    type: Boolean,
    default: true
  },
  multiple: {
    type: Boolean,
    default: false
  },
  draggable: {
    type: Boolean,
    default: false
  },
  searchable: {
    type: Boolean,
    default: true
  },
  expandable: {
    type: Boolean,
    default: true
  },
  blockNode: {
    type: Boolean,
    default: true
  },

  // 行为控制
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  checkStrictly: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },

  // 过滤控制
  securityLevel: {
    type: String,
    default: null
  },
  permissionTypes: {
    type: Array,
    default: () => []
  },

  // 样式控制
  height: {
    type: [String, Number],
    default: '400px'
  },
  title: {
    type: String,
    default: '权限树'
  },
  emptyDescription: {
    type: String,
    default: '暂无权限数据'
  },
  emptyImage: {
    type: [String, Object],
    default: null
  },

  // 字段映射
  fieldNames: {
    type: Object,
    default: () => ({
      children: 'children',
      title: 'name',
      key: 'id'
    })
  }
});

// Emits定义
const emit = defineEmits([
  'select',
  'check',
  'expand',
  'drop',
  'rightClick',
  'search',
  'nodeAdd',
  'nodeEdit',
  'nodeDelete',
  'nodeCopy',
  'nodeView',
  'nodeSetting',
  'export',
  'update:value'
]);

// 响应式数据
const searchKeyword = ref('');
const expandedKeys = ref([]);
const selectedKeys = ref([]);
const checkedKeys = ref([]);
const allExpanded = ref(false);

// 右键菜单
const contextMenuVisible = ref(false);
const contextMenuPosition = reactive({ x: 0, y: 0 });
const contextMenuNode = ref(null);
const contextMenuItems = ref([]);

// 容器高度计算
const containerHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`;
  }
  return props.height;
});

// 计算属性
const filteredTreeData = computed(() => {
  let data = [...props.treeData];

  // 安全级别过滤
  if (props.securityLevel) {
    data = filterBySecurityLevel(data, props.securityLevel);
  }

  // 权限类型过滤
  if (props.permissionTypes && props.permissionTypes.length > 0) {
    data = filterByPermissionTypes(data, props.permissionTypes);
  }

  // 关键词搜索
  if (searchKeyword.value) {
    data = filterByKeyword(data, searchKeyword.value);
  }

  return data;
});

// 监听器
watch(() => props.value, (newVal) => {
  if (props.checkable) {
    checkedKeys.value = newVal || [];
  } else {
    selectedKeys.value = newVal || [];
  }
}, { immediate: true });

watch(checkedKeys, (newVal) => {
  if (props.checkable) {
    emit('update:value', newVal);
  }
});

watch(selectedKeys, (newVal) => {
  if (!props.checkable) {
    emit('update:value', newVal);
  }
});

// 生命周期
onMounted(() => {
  if (props.defaultExpandAll) {
    handleExpandAll();
  }
  initContextMenuItems();
});

// 方法
/**
 * 处理选择事件
 */
const handleSelect = (selectedKeys, info) => {
  emit('select', selectedKeys, info);
};

/**
 * 处理勾选事件
 */
const handleCheck = (checkedKeys, info) => {
  emit('check', checkedKeys, info);
};

/**
 * 处理展开事件
 */
const handleExpand = (expandedKeys, info) => {
  emit('expand', expandedKeys, info);
};

/**
 * 处理拖拽事件
 */
const handleDrop = (info) => {
  emit('drop', info);
};

/**
 * 处理右键点击事件
 */
const handleRightClick = ({ event, node }) => {
  event.preventDefault();
  contextMenuNode.value = node;
  contextMenuPosition.x = event.clientX;
  contextMenuPosition.y = event.clientY;
  contextMenuVisible.value = true;
  emit('rightClick', { event, node });
};

/**
 * 处理搜索
 */
const handleSearch = (value) => {
  emit('search', value);
  if (value) {
    expandSearchNodes(value);
  }
};

/**
 * 展开/收起全部
 */
const handleExpandAll = () => {
  if (allExpanded.value) {
    expandedKeys.value = [];
    allExpanded.value = false;
  } else {
    const allKeys = getAllNodeKeys(filteredTreeData.value);
    expandedKeys.value = allKeys;
    allExpanded.value = true;
  }
};

/**
 * 获取所有节点键值
 */
const getAllNodeKeys = (nodes) => {
  let keys = [];
  nodes.forEach(node => {
    keys.push(node[props.fieldNames.key]);
    if (node.children && node.children.length > 0) {
      keys = keys.concat(getAllNodeKeys(node.children));
    }
  });
  return keys;
};

/**
 * 展开搜索节点
 */
const expandSearchNodes = (keyword) => {
  const keys = [];
  const searchInNodes = (nodes) => {
    nodes.forEach(node => {
      const title = node[props.fieldNames.title] || '';
      if (title.toLowerCase().includes(keyword.toLowerCase())) {
        // 展开所有父节点
        keys.push(node[props.fieldNames.key]);
        // 展开到根节点
        expandParentNodes(filteredTreeData.value, node[props.fieldNames.key], keys);
      }
      if (node.children && node.children.length > 0) {
        searchInNodes(node.children);
      }
    });
  };
  searchInNodes(filteredTreeData.value);
  expandedKeys.value = [...new Set(keys)];
};

/**
 * 展开父节点
 */
const expandParentNodes = (nodes, targetKey, keys) => {
  const findParent = (nodes, targetKey, parentKey = null) => {
    for (let node of nodes) {
      if (node[props.fieldNames.key] === targetKey) {
        if (parentKey) {
          keys.push(parentKey);
          findParent(nodes, parentKey);
        }
        return;
      }
      if (node.children && node.children.length > 0) {
        findParent(node.children, targetKey, node[props.fieldNames.key]);
      }
    }
  };
  findParent(nodes, targetKey);
};

/**
 * 按安全级别过滤
 */
const filterBySecurityLevel = (nodes, level) => {
  const levelOrder = [
    SecurityLevel.PUBLIC,
    SecurityLevel.INTERNAL,
    SecurityLevel.CONFIDENTIAL,
    SecurityLevel.SECRET,
    SecurityLevel.TOP_SECRET
  ];
  const maxIndex = levelOrder.indexOf(level);

  return nodes.filter(node => {
    const nodeIndex = levelOrder.indexOf(node.securityLevel);
    if (nodeIndex <= maxIndex) {
      if (node.children && node.children.length > 0) {
        node.children = filterBySecurityLevel(node.children, level);
      }
      return true;
    }
    return false;
  });
};

/**
 * 按权限类型过滤
 */
const filterByPermissionTypes = (nodes, types) => {
  return nodes.filter(node => {
    if (types.includes(node.type)) {
      if (node.children && node.children.length > 0) {
        node.children = filterByPermissionTypes(node.children, types);
      }
      return true;
    }
    return false;
  });
};

/**
 * 按关键词过滤
 */
const filterByKeyword = (nodes, keyword) => {
  return nodes.filter(node => {
    const title = node[props.fieldNames.title] || '';
    const description = node.description || '';
    const code = node.code || '';

    const matchKeyword =
      title.toLowerCase().includes(keyword.toLowerCase()) ||
      description.toLowerCase().includes(keyword.toLowerCase()) ||
      code.toLowerCase().includes(keyword.toLowerCase());

    if (matchKeyword) {
      return true;
    }

    if (node.children && node.children.length > 0) {
      node.children = filterByKeyword(node.children, keyword);
      return node.children.length > 0;
    }

    return false;
  });
};

/**
 * 获取安全级别颜色
 */
const getSecurityLevelColor = (level) => {
  const colorMap = {
    [SecurityLevel.PUBLIC]: 'green',
    [SecurityLevel.INTERNAL]: 'blue',
    [SecurityLevel.CONFIDENTIAL]: 'orange',
    [SecurityLevel.SECRET]: 'red',
    [SecurityLevel.TOP_SECRET]: 'purple'
  };
  return colorMap[level] || 'default';
};

/**
 * 获取安全级别文本
 */
const getSecurityLevelText = (level) => {
  const textMap = {
    [SecurityLevel.PUBLIC]: '公开',
    [SecurityLevel.INTERNAL]: '内部',
    [SecurityLevel.CONFIDENTIAL]: '秘密',
    [SecurityLevel.SECRET]: '机密',
    [SecurityLevel.TOP_SECRET]: '绝密'
  };
  return textMap[level] || '未知';
};

/**
 * 获取权限类型颜色
 */
const getPermissionTypeColor = (type) => {
  const colorMap = {
    [PermissionType.MENU]: 'blue',
    [PermissionType.BUTTON]: 'green',
    [PermissionType.API]: 'orange',
    [PermissionType.DATA]: 'purple',
    [PermissionType.DEVICE]: 'cyan',
    [PermissionType.AREA]: 'pink',
    [PermissionType.TIME]: 'geekblue'
  };
  return colorMap[type] || 'default';
};

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = (type) => {
  const textMap = {
    [PermissionType.MENU]: '菜单',
    [PermissionType.BUTTON]: '按钮',
    [PermissionType.API]: '接口',
    [PermissionType.DATA]: '数据',
    [PermissionType.DEVICE]: '设备',
    [PermissionType.AREA]: '区域',
    [PermissionType.TIME]: '时间'
  };
  return textMap[type] || '未知';
};

/**
 * 获取状态徽章类型
 */
const getStatusBadgeType = (status) => {
  const statusMap = {
    [PermissionStatus.ACTIVE]: 'success',
    [PermissionStatus.INACTIVE]: 'default',
    [PermissionStatus.EXPIRED]: 'warning',
    [PermissionStatus.REVOKED]: 'error'
  };
  return statusMap[status] || 'default';
};

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    [PermissionStatus.ACTIVE]: '生效中',
    [PermissionStatus.INACTIVE]: '未生效',
    [PermissionStatus.EXPIRED]: '已过期',
    [PermissionStatus.REVOKED]: '已撤销'
  };
  return textMap[status] || '未知';
};

/**
 * 初始化右键菜单项
 */
const initContextMenuItems = () => {
  contextMenuItems.value = [
    {
      key: 'view',
      label: '查看详情',
      icon: EyeOutlined,
      disabled: false
    },
    {
      key: 'add',
      label: '添加子权限',
      icon: PlusOutlined,
      disabled: false
    },
    {
      key: 'edit',
      label: '编辑权限',
      icon: EditOutlined,
      disabled: false
    },
    {
      key: 'copy',
      label: '复制权限',
      icon: CopyOutlined,
      disabled: false
    },
    {
      key: 'delete',
      label: '删除权限',
      icon: DeleteOutlined,
      disabled: false
    },
    {
      key: 'setting',
      label: '权限设置',
      icon: SettingOutlined,
      disabled: false
    },
    {
      key: 'export',
      label: '导出权限',
      icon: ExportOutlined,
      disabled: false
    }
  ];
};

/**
 * 处理右键菜单点击
 */
const handleContextMenuClick = ({ key }) => {
  contextMenuVisible.value = false;

  const node = contextMenuNode.value;
  if (!node) return;

  switch (key) {
    case 'view':
      emit('nodeView', node);
      break;
    case 'add':
      emit('nodeAdd', node);
      break;
    case 'edit':
      emit('nodeEdit', node);
      break;
    case 'copy':
      emit('nodeCopy', node);
      break;
    case 'delete':
      emit('nodeDelete', node);
      break;
    case 'setting':
      emit('nodeSetting', node);
      break;
    case 'export':
      emit('export', node);
      break;
  }
};

/**
 * 展开指定节点
 */
const expandNode = (nodeKey) => {
  if (!expandedKeys.value.includes(nodeKey)) {
    expandedKeys.value.push(nodeKey);
  }
};

/**
 * 收起指定节点
 */
const collapseNode = (nodeKey) => {
  const index = expandedKeys.value.indexOf(nodeKey);
  if (index > -1) {
    expandedKeys.value.splice(index, 1);
  }
};

/**
 * 选中指定节点
 */
const selectNode = (nodeKey) => {
  if (!props.multiple) {
    selectedKeys.value = [nodeKey];
  } else {
    if (!selectedKeys.value.includes(nodeKey)) {
      selectedKeys.value.push(nodeKey);
    }
  }
};

/**
 * 取消选中指定节点
 */
const unselectNode = (nodeKey) => {
  const index = selectedKeys.value.indexOf(nodeKey);
  if (index > -1) {
    selectedKeys.value.splice(index, 1);
  }
};

/**
 * 勾选指定节点
 */
const checkNode = (nodeKey) => {
  if (!checkedKeys.value.includes(nodeKey)) {
    checkedKeys.value.push(nodeKey);
  }
};

/**
 * 取消勾选指定节点
 */
const uncheckNode = (nodeKey) => {
  const index = checkedKeys.value.indexOf(nodeKey);
  if (index > -1) {
    checkedKeys.value.splice(index, 1);
  }
};

/**
 * 清空选中状态
 */
const clearSelection = () => {
  selectedKeys.value = [];
  checkedKeys.value = [];
};

/**
 * 清空展开状态
 */
const clearExpansion = () => {
  expandedKeys.value = [];
  allExpanded.value = false;
};

/**
 * 获取选中的节点
 */
const getSelectedNodes = () => {
  const selectedNodeKeys = props.checkable ? checkedKeys.value : selectedKeys.value;
  return selectedNodeKeys.map(key => findNodeByKey(filteredTreeData.value, key)).filter(Boolean);
};

/**
 * 根据键值查找节点
 */
const findNodeByKey = (nodes, key) => {
  for (let node of nodes) {
    if (node[props.fieldNames.key] === key) {
      return node;
    }
    if (node.children && node.children.length > 0) {
      const found = findNodeByKey(node.children, key);
      if (found) return found;
    }
  }
  return null;
};

// 暴露方法给父组件
defineExpose({
  expandNode,
  collapseNode,
  selectNode,
  unselectNode,
  checkNode,
  uncheckNode,
  clearSelection,
  clearExpansion,
  getSelectedNodes,
  findNodeByKey,
  handleExpandAll,
  getAllNodeKeys
});
</script>

<style scoped lang="less">
.permission-tree {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    margin-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .tree-title {
      font-size: 14px;
      font-weight: 500;
      color: #262626;
    }

    .tree-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }
  }

  .tree-container {
    flex: 1;
    overflow: auto;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 8px;
    background-color: #fafafa;

    .tree-node-title {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .node-title-text {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .security-level-tag,
      .permission-type-tag {
        flex-shrink: 0;
        font-size: 10px;
        line-height: 14px;
        padding: 1px 4px;
        margin: 0;
      }
    }
  }

  // 树组件样式重写
  :deep(.ant-tree) {
    .ant-tree-treenode {
      padding: 2px 0;

      &:hover {
        background-color: #f5f5f5;
      }

      &.ant-tree-treenode-selected {
        background-color: #e6f7ff;
      }
    }

    .ant-tree-node-content-wrapper {
      flex: 1;
      width: 100%;
      padding: 2px 4px;
      border-radius: 4px;

      &:hover {
        background-color: transparent;
      }
    }

    .ant-tree-switcher {
      width: 20px;
      height: 20px;
      line-height: 20px;
    }

    .ant-tree-iconEle {
      width: 20px;
      height: 20px;
      line-height: 20px;
      text-align: center;
    }
  }
}
</style>