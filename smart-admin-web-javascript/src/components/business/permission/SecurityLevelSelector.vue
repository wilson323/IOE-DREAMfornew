<template>
  <div class="security-level-selector">
    <!-- 单选模式 -->
    <template v-if="!multiple">
      <a-radio-group
        v-model:value="selectedLevel"
        @change="handleChange"
        :disabled="disabled"
        :size="size"
        :button-style="buttonStyle"
      >
        <!-- 按钮样式 -->
        <template v-if="buttonStyle === 'solid'">
          <a-radio-button
            v-for="level in filteredLevels"
            :key="level.value"
            :value="level.value"
            :disabled="level.disabled"
          >
            <div class="level-button">
              <component
                v-if="showIcon && level.icon"
                :is="level.icon"
                :style="{ color: level.color }"
                class="level-icon"
              />
              <span class="level-text">{{ level.label }}</span>
              <a-tag
                v-if="showCount && level.count !== undefined"
                :color="level.color"
                size="small"
                class="level-count"
              >
                {{ level.count }}
              </a-tag>
            </div>
          </a-radio-button>
        </template>

        <!-- 默认样式 -->
        <template v-else>
          <a-radio
            v-for="level in filteredLevels"
            :key="level.value"
            :value="level.value"
            :disabled="level.disabled"
            class="level-radio"
          >
            <div class="level-content">
              <div class="level-info">
                <component
                  v-if="showIcon && level.icon"
                  :is="level.icon"
                  :style="{ color: level.color }"
                  class="level-icon"
                />
                <div class="level-details">
                  <div class="level-name">{{ level.label }}</div>
                  <div class="level-description" v-if="showDescription && level.description">
                    {{ level.description }}
                  </div>
                </div>
              </div>
              <div class="level-meta">
                <a-tag
                  v-if="showCount && level.count !== undefined"
                  :color="level.color"
                  size="small"
                >
                  {{ level.count }}
                </a-tag>
                <a-tooltip
                  v-if="showTooltip && level.tooltip"
                  :title="level.tooltip"
                >
                  <InfoCircleOutlined />
                </a-tooltip>
              </div>
            </div>
          </a-radio>
        </template>
      </a-radio-group>
    </template>

    <!-- 多选模式 -->
    <template v-else>
      <a-checkbox-group
        v-model:value="selectedLevels"
        @change="handleChange"
        :disabled="disabled"
        :size="size"
      >
        <!-- 卡片样式 -->
        <template v-if="cardStyle">
          <div class="level-cards">
            <div
              v-for="level in filteredLevels"
              :key="level.value"
              class="level-card"
              :class="{
                selected: selectedLevels.includes(level.value),
                disabled: level.disabled
              }"
              @click="handleCardClick(level)"
            >
              <a-checkbox
                :value="level.value"
                :disabled="level.disabled"
                class="level-checkbox"
              />
              <div class="card-content">
                <div class="card-header">
                  <component
                    v-if="showIcon && level.icon"
                    :is="level.icon"
                    :style="{ color: level.color }"
                    class="card-icon"
                  />
                  <h4 class="card-title">{{ level.label }}</h4>
                  <a-tag
                    v-if="showCount && level.count !== undefined"
                    :color="level.color"
                    size="small"
                  >
                    {{ level.count }}
                  </a-tag>
                </div>
                <p class="card-description" v-if="showDescription && level.description">
                  {{ level.description }}
                </p>
                <div class="card-features" v-if="showFeatures && level.features">
                  <a-tag
                    v-for="feature in level.features"
                    :key="feature"
                    size="small"
                    :color="level.color"
                  >
                    {{ feature }}
                  </a-tag>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 默认样式 -->
        <template v-else>
          <div class="level-list">
            <div
              v-for="level in filteredLevels"
              :key="level.value"
              class="level-item"
              :class="{ disabled: level.disabled }"
            >
              <a-checkbox
                :value="level.value"
                :disabled="level.disabled"
              >
                <div class="level-content">
                  <div class="level-info">
                    <component
                      v-if="showIcon && level.icon"
                      :is="level.icon"
                      :style="{ color: level.color }"
                      class="level-icon"
                    />
                    <div class="level-details">
                      <div class="level-name">
                        {{ level.label }}
                        <a-tag
                          v-if="showCount && level.count !== undefined"
                          :color="level.color"
                          size="small"
                        >
                          {{ level.count }}
                        </a-tag>
                      </div>
                      <div class="level-description" v-if="showDescription && level.description">
                        {{ level.description }}
                      </div>
                    </div>
                  </div>
                  <a-tooltip
                    v-if="showTooltip && level.tooltip"
                    :title="level.tooltip"
                  >
                    <InfoCircleOutlined />
                  </a-tooltip>
                </div>
              </a-checkbox>
            </div>
          </div>
        </template>
      </a-checkbox-group>
    </template>

    <!-- 下拉选择模式 -->
    <template v-if="dropdownMode">
      <a-select
        v-model:value="dropdownValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :size="size"
        :multiple="multiple"
        :allow-clear="allowClear"
        :show-search="showSearch"
        :filter-option="filterOption"
        @change="handleDropdownChange"
      >
        <a-select-option
          v-for="level in filteredLevels"
          :key="level.value"
          :value="level.value"
          :disabled="level.disabled"
        >
          <div class="dropdown-option">
            <component
              v-if="showIcon && level.icon"
              :is="level.icon"
              :style="{ color: level.color }"
              class="option-icon"
            />
            <span class="option-text">{{ level.label }}</span>
            <a-tag
              v-if="showCount && level.count !== undefined"
              :color="level.color"
              size="small"
            >
              {{ level.count }}
            </a-tag>
          </div>
        </a-select-option>
      </a-select>
    </template>

    <!-- 级别对比 -->
    <div class="level-comparison" v-if="showComparison">
      <a-collapse ghost>
        <a-collapse-panel key="comparison" header="安全级别对比">
          <a-table
            :columns="comparisonColumns"
            :data-source="comparisonData"
            :pagination="false"
            size="small"
            bordered
          />
        </a-collapse-panel>
      </a-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import {
  SecurityLevel,
  SecurityLevel as SecurityLevelEnum
} from '/@/types/permission';

// Props定义
const props = defineProps({
  // 值相关
  value: {
    type: [String, Array],
    default: undefined
  },
  modelValue: {
    type: [String, Array],
    default: undefined
  },

  // 功能控制
  multiple: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  showSearch: {
    type: Boolean,
    default: true
  },

  // 显示控制
  showIcon: {
    type: Boolean,
    default: true
  },
  showDescription: {
    type: Boolean,
    default: true
  },
  showCount: {
    type: Boolean,
    default: false
  },
  showTooltip: {
    type: Boolean,
    default: true
  },
  showFeatures: {
    type: Boolean,
    default: false
  },
  showComparison: {
    type: Boolean,
    default: false
  },

  // 样式控制
  size: {
    type: String,
    default: 'middle' // small, middle, large
  },
  buttonStyle: {
    type: String,
    default: 'outline' // outline, solid
  },
  cardStyle: {
    type: Boolean,
    default: false
  },
  dropdownMode: {
    type: Boolean,
    default: false
  },

  // 过滤控制
  maxLevel: {
    type: String,
    default: SecurityLevelEnum.TOP_SECRET
  },
  minLevel: {
    type: String,
    default: SecurityLevelEnum.PUBLIC
  },
  excludeLevels: {
    type: Array,
    default: () => []
  },

  // 占位符
  placeholder: {
    type: String,
    default: '请选择安全级别'
  },

  // 额外数据
  levelCounts: {
    type: Object,
    default: () => ({})
  }
});

// Emits定义
const emit = defineEmits([
  'update:value',
  'update:modelValue',
  'change',
  'select',
  'deselect'
]);

// 响应式数据
const selectedLevel = ref(null);
const selectedLevels = ref([]);
const dropdownValue = ref(null);

// 安全级别定义
const securityLevels = [
  {
    value: SecurityLevelEnum.PUBLIC,
    label: '公开级',
    description: '完全公开，任何用户都可以访问',
    color: 'green',
    icon: 'GlobalOutlined',
    tooltip: '最低安全级别，适用于公开信息',
    features: ['无限制访问', '最低权限要求'],
    order: 1
  },
  {
    value: SecurityLevelEnum.INTERNAL,
    label: '内部级',
    description: '内部员工可以访问',
    color: 'blue',
    icon: 'TeamOutlined',
    tooltip: '适用于内部非敏感信息',
    features: ['需要登录', '内部员工访问'],
    order: 2
  },
  {
    value: SecurityLevelEnum.CONFIDENTIAL,
    label: '秘密级',
    description: '需要特定权限才能访问',
    color: 'orange',
    icon: 'LockOutlined',
    tooltip: '适用于敏感信息，需要授权',
    features: ['需要授权', '审计记录'],
    order: 3
  },
  {
    value: SecurityLevelEnum.SECRET,
    label: '机密级',
    description: '高级别权限才能访问',
    color: 'red',
    icon: 'SafetyCertificateOutlined',
    tooltip: '适用于重要机密信息',
    features: ['高级权限', '严格审计', '访问控制'],
    order: 4
  },
  {
    value: SecurityLevelEnum.TOP_SECRET,
    label: '绝密级',
    description: '最高权限才能访问',
    color: 'purple',
    icon: 'ShieldCheckOutlined',
    tooltip: '最高安全级别，绝密信息',
    features: ['最高权限', '多层审批', '实时监控'],
    order: 5
  }
];

// 计算属性
const currentValue = computed(() => {
  return props.modelValue !== undefined ? props.modelValue : props.value;
});

const filteredLevels = computed(() => {
  const levelOrder = [
    SecurityLevelEnum.PUBLIC,
    SecurityLevelEnum.INTERNAL,
    SecurityLevelEnum.CONFIDENTIAL,
    SecurityLevelEnum.SECRET,
    SecurityLevelEnum.TOP_SECRET
  ];

  const minIndex = levelOrder.indexOf(props.minLevel);
  const maxIndex = levelOrder.indexOf(props.maxLevel);

  let levels = securityLevels.filter(level => {
    const levelIndex = levelOrder.indexOf(level.value);
    const inRange = levelIndex >= minIndex && levelIndex <= maxIndex;
    const notExcluded = !props.excludeLevels.includes(level.value);
    return inRange && notExcluded;
  });

  // 添加计数信息
  if (props.showCount && props.levelCounts) {
    levels = levels.map(level => ({
      ...level,
      count: props.levelCounts[level.value] || 0
    }));
  }

  // 按order排序
  return levels.sort((a, b) => a.order - b.order);
});

const comparisonColumns = computed(() => [
  {
    title: '安全级别',
    dataIndex: 'label',
    key: 'level',
    width: 120
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '访问要求',
    dataIndex: 'requirements',
    key: 'requirements',
    width: 150
  },
  {
    title: '适用场景',
    dataIndex: 'scenarios',
    key: 'scenarios',
    ellipsis: true
  }
]);

const comparisonData = computed(() => {
  return filteredLevels.value.map(level => ({
    key: level.value,
    label: level.label,
    description: level.description,
    requirements: getLevelRequirements(level.value),
    scenarios: getLevelScenarios(level.value)
  }));
});

// 监听器
watch(currentValue, (newVal) => {
  if (props.multiple) {
    selectedLevels.value = Array.isArray(newVal) ? newVal : (newVal ? [newVal] : []);
    dropdownValue.value = selectedLevels.value;
  } else {
    selectedLevel.value = newVal;
    dropdownValue.value = newVal;
  }
}, { immediate: true });

watch(selectedLevel, (newVal) => {
  if (!props.multiple && !props.dropdownMode) {
    emit('update:value', newVal);
    emit('update:modelValue', newVal);
    emit('change', newVal);
    emit('select', newVal);
  }
});

watch(selectedLevels, (newVal) => {
  if (props.multiple && !props.dropdownMode) {
    emit('update:value', newVal);
    emit('update:modelValue', newVal);
    emit('change', newVal);
  }
});

// 生命周期
onMounted(() => {
  // 初始化图标组件
  loadIcons();
});

// 方法
/**
 * 加载图标组件
 */
const loadIcons = async () => {
  try {
    // 动态导入图标组件
    const icons = await import('@ant-design/icons-vue');
    securityLevels.forEach(level => {
      if (icons[level.icon]) {
        level.iconComponent = icons[level.icon];
      }
    });
  } catch (error) {
    console.warn('无法加载图标组件:', error);
  }
};

/**
 * 处理变化事件
 */
const handleChange = (value) => {
  if (props.multiple) {
    emit('change', value);
  } else {
    emit('change', value);
    emit('select', value);
  }
};

/**
 * 处理下拉选择变化
 */
const handleDropdownChange = (value) => {
  if (props.multiple) {
    emit('update:value', value);
    emit('update:modelValue', value);
    emit('change', value);
  } else {
    emit('update:value', value);
    emit('update:modelValue', value);
    emit('change', value);
    emit('select', value);
  }
};

/**
 * 处理卡片点击
 */
const handleCardClick = (level) => {
  if (level.disabled || props.disabled) return;

  const index = selectedLevels.value.indexOf(level.value);
  if (index > -1) {
    selectedLevels.value.splice(index, 1);
    emit('deselect', level.value);
  } else {
    selectedLevels.value.push(level.value);
    emit('select', level.value);
  }
};

/**
 * 过理选项
 */
const filterOption = (input, option) => {
  const level = filteredLevels.value.find(l => l.value === option.value);
  if (!level) return false;

  const searchText = input.toLowerCase();
  return (
    level.label.toLowerCase().includes(searchText) ||
    level.description?.toLowerCase().includes(searchText)
  );
};

/**
 * 获取级别要求
 */
const getLevelRequirements = (level) => {
  const requirements = {
    [SecurityLevelEnum.PUBLIC]: '无要求',
    [SecurityLevelEnum.INTERNAL]: '需要登录认证',
    [SecurityLevelEnum.CONFIDENTIAL]: '需要权限授权',
    [SecurityLevelEnum.SECRET]: '需要高级权限',
    [SecurityLevelEnum.TOP_SECRET]: '需要最高权限'
  };
  return requirements[level] || '未知';
};

/**
 * 获取级别适用场景
 */
const getLevelScenarios = (level) => {
  const scenarios = {
    [SecurityLevelEnum.PUBLIC]: '公开信息、帮助文档',
    [SecurityLevelEnum.INTERNAL]: '内部通知、工作资料',
    [SecurityLevelEnum.CONFIDENTIAL]: '项目文档、技术资料',
    [SecurityLevelEnum.SECRET]: '核心数据、商业机密',
    [SecurityLevelEnum.TOP_SECRET]: '战略规划、核心算法'
  };
  return scenarios[level] || '未知';
};

/**
 * 获取选中的级别
 */
const getSelectedLevels = () => {
  return props.multiple ? selectedLevels.value : [selectedLevel.value];
};

/**
 * 设置选中的级别
 */
const setSelectedLevels = (levels) => {
  if (props.multiple) {
    selectedLevels.value = Array.isArray(levels) ? levels : [];
  } else {
    selectedLevel.value = Array.isArray(levels) ? levels[0] : levels;
  }
};

/**
 * 清空选择
 */
const clearSelection = () => {
  selectedLevel.value = null;
  selectedLevels.value = [];
  dropdownValue.value = null;
};

/**
 * 获取级别信息
 */
const getLevelInfo = (levelValue) => {
  return filteredLevels.value.find(level => level.value === levelValue);
};

/**
 * 验证级别是否可选
 */
const isLevelSelectable = (levelValue) => {
  const level = getLevelInfo(levelValue);
  return level && !level.disabled && !props.disabled;
};

/**
 * 获取下一个级别
 */
const getNextLevel = (currentLevel) => {
  const levels = filteredLevels.value.map(l => l.value);
  const currentIndex = levels.indexOf(currentLevel);
  if (currentIndex < levels.length - 1) {
    return levels[currentIndex + 1];
  }
  return null;
};

/**
 * 获取上一个级别
 */
const getPreviousLevel = (currentLevel) => {
  const levels = filteredLevels.value.map(l => l.value);
  const currentIndex = levels.indexOf(currentLevel);
  if (currentIndex > 0) {
    return levels[currentIndex - 1];
  }
  return null;
};

// 暴露方法给父组件
defineExpose({
  getSelectedLevels,
  setSelectedLevels,
  clearSelection,
  getLevelInfo,
  isLevelSelectable,
  getNextLevel,
  getPreviousLevel
});
</script>

<style scoped lang="less">
.security-level-selector {
  width: 100%;

  // 单选按钮样式
  .level-radio {
    width: 100%;
    margin-bottom: 8px;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      border-color: #d9d9d9;
      background-color: #fafafa;
    }

    :deep(.ant-radio-wrapper) {
      width: 100%;
      margin-right: 0;
      align-items: flex-start;
    }

    .level-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      width: 100%;
      margin-left: 8px;

      .level-info {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        flex: 1;

        .level-icon {
          font-size: 18px;
          margin-top: 2px;
        }

        .level-details {
          flex: 1;

          .level-name {
            font-weight: 500;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .level-description {
            font-size: 12px;
            color: #8c8c8c;
            line-height: 1.4;
          }
        }
      }

      .level-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;
        color: #8c8c8c;
      }
    }
  }

  // 多选列表样式
  .level-list {
    .level-item {
      margin-bottom: 8px;
      padding: 8px;
      border: 1px solid #f0f0f0;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        border-color: #d9d9d9;
        background-color: #fafafa;
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      :deep(.ant-checkbox-wrapper) {
        width: 100%;
        margin-right: 0;
        align-items: flex-start;
      }

      .level-content {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        width: 100%;
        margin-left: 8px;

        .level-info {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          flex: 1;

          .level-icon {
            font-size: 16px;
            margin-top: 2px;
          }

          .level-details {
            flex: 1;

            .level-name {
              font-weight: 500;
              margin-bottom: 2px;
              display: flex;
              align-items: center;
              gap: 8px;
            }

            .level-description {
              font-size: 12px;
              color: #8c8c8c;
              line-height: 1.4;
            }
          }
        }
      }
    }
  }

  // 卡片样式
  .level-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 16px;

    .level-card {
      border: 2px solid #f0f0f0;
      border-radius: 8px;
      padding: 16px;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
      background-color: #fff;

      &:hover {
        border-color: #d9d9d9;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      &.selected {
        border-color: #1890ff;
        background-color: #f6ffed;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
        background-color: #fafafa;
      }

      .level-checkbox {
        position: absolute;
        top: 12px;
        right: 12px;
      }

      .card-content {
        .card-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;

          .card-icon {
            font-size: 24px;
          }

          .card-title {
            flex: 1;
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: #262626;
          }
        }

        .card-description {
          margin: 0 0 12px 0;
          font-size: 13px;
          color: #595959;
          line-height: 1.4;
        }

        .card-features {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
        }
      }
    }
  }

  // 按钮组样式
  :deep(.ant-radio-group) {
    width: 100%;

    &.ant-radio-group-solid .ant-radio-button-wrapper {
      border-color: #d9d9d9;
      background-color: #fafafa;
      color: #262626;

      &:hover {
        border-color: #40a9ff;
        color: #40a9ff;
      }

      &.ant-radio-button-wrapper-checked {
        background-color: #1890ff;
        border-color: #1890ff;
        color: #fff;
      }
    }

    .ant-radio-button-wrapper {
      .level-button {
        display: flex;
        align-items: center;
        gap: 6px;

        .level-icon {
          font-size: 14px;
        }

        .level-count {
          margin-left: 4px;
        }
      }
    }
  }

  // 下拉选择样式
  .dropdown-option {
    display: flex;
    align-items: center;
    gap: 8px;

    .option-icon {
      font-size: 14px;
    }

    .option-text {
      flex: 1;
    }
  }

  // 级别对比表格
  .level-comparison {
    margin-top: 16px;

    :deep(.ant-table) {
      font-size: 12px;

      .ant-table-tbody > tr {
        &:hover {
          background-color: #f5f5f5;
        }
      }
    }
  }
}
</style>