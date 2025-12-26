<template>
  <div class="area-config-editor">
    <JsonBaseEditor ref="baseEditor" :model-value="config" @update:model-value="handleUpdate">
      <template #editor>
        <a-form layout="vertical">
          <!-- 全部区域允许开关 -->
          <a-form-item>
            <template #label>
              <a-space>
                <span>允许全部区域</span>
                <a-tooltip title="开启后，该账户类别可在所有区域消费">
                  <QuestionCircleOutlined />
                </a-tooltip>
              </a-space>
            </template>
            <a-switch
              v-model:checked="config.allAreasAllowed"
              @change="handleAllAreasChange"
              :disabled="readonly"
            />
            <span class="hint-text">开启后，区域限制将失效</span>
          </a-form-item>

          <!-- 区域选择 -->
          <a-form-item v-if="!config.allAreasAllowed" label="允许消费的区域">
            <a-card size="small">
              <template #extra>
                <a-space>
                  <a-button
                    size="small"
                    @click="selectAllAreas"
                    :disabled="readonly"
                  >
                    全选
                  </a-button>
                  <a-button
                    size="small"
                    @click="clearAllAreas"
                    :disabled="readonly"
                  >
                    清空
                  </a-button>
                  <a-button
                    size="small"
                    type="primary"
                    @click="showAreaSelector"
                    :disabled="readonly"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加区域
                  </a-button>
                </a-space>
              </template>

              <!-- 已选区域列表 -->
              <a-empty
                v-if="config.areas.length === 0"
                description="暂无允许的区域"
                :image="false"
              />

              <a-list
                v-else
                :data-source="config.areas"
                bordered
                size="small"
              >
                <template #renderItem="{ item, index }">
                  <a-list-item>
                    <a-row :gutter="16" style="width: 100%">
                      <a-col :span="8">
                        <a-space>
                          <HomeOutlined />
                          <strong>{{ item.areaName }}</strong>
                        </a-space>
                      </a-col>
                      <a-col :span="6">
                        <a-space>
                          <span>ID:</span>
                          <a-tag color="blue">{{ item.areaId }}</a-tag>
                        </a-space>
                      </a-col>
                      <a-col :span="6">
                        <a-checkbox
                          v-model:checked="item.includeSubAreas"
                          :disabled="readonly"
                        >
                          包含子区域
                        </a-checkbox>
                      </a-col>
                      <a-col :span="4" style="text-align: right">
                        <a-button
                          danger
                          size="small"
                          @click="removeArea(index)"
                          :disabled="readonly"
                        >
                          <template #icon><DeleteOutlined /></template>
                          移除
                        </a-button>
                      </a-col>
                    </a-row>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-form-item>

          <!-- 区域权限说明 -->
          <a-alert
            type="info"
            message="区域权限说明"
            description=""
            show-icon
          >
            <template #description>
              <ul>
                <li>勾选"包含子区域"：允许在该区域及其所有下级区域消费</li>
                <li>不勾选"包含子区域"：仅允许在该具体区域消费</li>
                <li>开启"允许全部区域"：忽略区域限制，可在任何区域消费</li>
              </ul>
            </template>
          </a-alert>
        </a-form>
      </template>
    </JsonBaseEditor>

    <!-- 区域选择器弹窗 -->
    <a-modal
      v-model:visible="areaSelectorVisible"
      title="选择区域"
      width="600px"
      @ok="handleAreaSelectConfirm"
      @cancel="areaSelectorVisible = false"
    >
      <a-tree
        v-model:checkedKeys="tempSelectedAreas"
        checkable
        :tree-data="areaTreeData"
        :field-names="{ children: 'children', title: 'areaName', key: 'areaId' }"
        :default-expand-all="true"
      >
        <template #title="{ areaName, areaType }">
          <a-space>
            <span>{{ areaName }}</span>
            <a-tag :color="getAreaTypeColor(areaType)">
              {{ getAreaTypeName(areaType) }}
            </a-tag>
          </a-space>
        </template>
      </a-tree>
    </a-modal>
  </div>
</template>

<script setup>
  import { ref, reactive, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    PlusOutlined,
    DeleteOutlined,
    HomeOutlined,
    QuestionCircleOutlined
  } from '@ant-design/icons-vue';
  import JsonBaseEditor from './JsonBaseEditor.vue';

  const props = defineProps({
    modelValue: {
      type: Object,
      required: true,
      default: () => ({})
    },
    readonly: {
      type: Boolean,
      default: false
    }
  });

  const emit = defineEmits(['update:modelValue']);

  const baseEditor = ref(null);
  const areaSelectorVisible = ref(false);
  const tempSelectedAreas = ref([]);

  // 初始化配置
  const config = reactive({
    allAreasAllowed: false,
    areas: []
  });

  // 监听外部传入的值
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal && Object.keys(newVal).length > 0) {
        Object.assign(config, newVal);
      }
    },
    { immediate: true, deep: true }
  );

  // 监听内部变化，触发更新
  watch(
    config,
    (newVal) => {
      emit('update:modelValue', JSON.parse(JSON.stringify(newVal)));
    },
    { deep: true }
  );

  // 模拟区域树数据（实际应从API获取）
  const areaTreeData = ref([
    {
      areaId: 1,
      areaName: '第一食堂',
      areaType: 1,
      children: [
        { areaId: 11, areaName: '一楼餐厅', areaType: 2 },
        { areaId: 12, areaName: '二楼餐厅', areaType: 2 }
      ]
    },
    {
      areaId: 2,
      areaName: '第二食堂',
      areaType: 1,
      children: [
        { areaId: 21, areaName: '大众餐厅', areaType: 2 },
        { areaId: 22, areaName: '清真餐厅', areaType: 2 }
      ]
    },
    {
      areaId: 3,
      areaName: '第三食堂',
      areaType: 1,
      children: [
        { areaId: 31, areaName: '自助餐厅', areaType: 2 },
        { areaId: 32, areaName: '风味餐厅', areaType: 2 }
      ]
    }
  ]);

  // 获取区域类型颜色
  const getAreaTypeColor = (areaType) => {
    const colorMap = {
      1: 'blue',    // 食堂
      2: 'green',   // 餐厅
      3: 'orange',  // 超市
      4: 'purple'   // 其他
    };
    return colorMap[areaType] || 'default';
  };

  // 获取区域类型名称
  const getAreaTypeName = (areaType) => {
    const nameMap = {
      1: '食堂',
      2: '餐厅',
      3: '超市',
      4: '其他'
    };
    return nameMap[areaType] || '未知';
  };

  // 全部区域开关变更
  const handleAllAreasChange = (checked) => {
    if (checked) {
      // 开启时清空已选区域
      config.areas = [];
    }
  };

  // 显示区域选择器
  const showAreaSelector = () => {
    // 已选区域的ID列表
    const selectedIds = config.areas.map(area => area.areaId);
    tempSelectedAreas.value = [...selectedIds];
    areaSelectorVisible.value = true;
  };

  // 确认选择区域
  const handleAreaSelectConfirm = () => {
    // 将选中的区域ID转换为区域对象
    const findAreaInTree = (tree, areaId) => {
      for (const node of tree) {
        if (node.areaId === areaId) {
          return {
            areaId: node.areaId,
            areaName: node.areaName,
            includeSubAreas: false
          };
        }
        if (node.children) {
          const found = findAreaInTree(node.children, areaId);
          if (found) return found;
        }
      }
      return null;
    };

    // 添加新选择的区域（避免重复）
    tempSelectedAreas.value.forEach(areaId => {
      const exists = config.areas.some(area => area.areaId === areaId);
      if (!exists) {
        const area = findAreaInTree(areaTreeData.value, areaId);
        if (area) {
          config.areas.push(area);
        }
      }
    });

    areaSelectorVisible.value = false;
    message.success(`已添加 ${tempSelectedAreas.value.length} 个区域`);
  };

  // 移除区域
  const removeArea = (index) => {
    config.areas.splice(index, 1);
  };

  // 全选区域
  const selectAllAreas = () => {
    const getAllAreaIds = (tree) => {
      let ids = [];
      tree.forEach(node => {
        ids.push(node.areaId);
        if (node.children) {
          ids = ids.concat(getAllAreaIds(node.children));
        }
      });
      return ids;
    };

    const allAreaIds = getAllAreaIds(areaTreeData.value);
    allAreaIds.forEach(areaId => {
      const exists = config.areas.some(area => area.areaId === areaId);
      if (!exists) {
        const findArea = (tree, id) => {
          for (const node of tree) {
            if (node.areaId === id) {
              return {
                areaId: node.areaId,
                areaName: node.areaName,
                includeSubAreas: false
              };
            }
            if (node.children) {
              const found = findArea(node.children, id);
              if (found) return found;
            }
          }
          return null;
        };

        const area = findArea(areaTreeData.value, areaId);
        if (area) {
          config.areas.push(area);
        }
      }
    });

    message.success('已选择所有区域');
  };

  // 清空区域
  const clearAllAreas = () => {
    config.areas = [];
    message.success('已清空所有区域');
  };

  const handleUpdate = (value) => {
    emit('update:modelValue', value);
  };

  // 组件挂载时加载区域数据（实际应从API加载）
  onMounted(async () => {
    try {
      // TODO: 从API加载区域树数据
      // const res = await consumeApi.getAreaTree();
      // areaTreeData.value = res.data;
    } catch (error) {
      console.error('加载区域数据失败', error);
    }
  });
</script>

<style lang="less" scoped>
  .area-config-editor {
    .hint-text {
      margin-left: 8px;
      color: #999;
      font-size: 12px;
    }

    :deep(.ant-list-item) {
      padding: 12px 16px;
    }

    :deep(.ant-alert) {
      margin-top: 16px;

      ul {
        margin: 8px 0;
        padding-left: 20px;

        li {
          margin: 4px 0;
          color: #666;
        }
      }
    }
  }
</style>
