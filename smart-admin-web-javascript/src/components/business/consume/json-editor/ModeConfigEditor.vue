<template>
  <div class="mode-config-editor">
    <JsonBaseEditor ref="baseEditor" :model-value="config" @update:model-value="handleUpdate">
      <template #editor>
        <a-tabs v-model:activeKey="activeMode" type="card">
          <!-- 固定金额模式 -->
          <a-tab-pane key="FIXED_AMOUNT" tab="固定金额模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.FIXED_AMOUNT.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.FIXED_AMOUNT.enabled">
                <a-form-item label="子类型">
                  <a-radio-group
                    v-model:value="config.FIXED_AMOUNT.subType"
                    :disabled="readonly"
                  >
                    <a-radio value="SIMPLE">简单模式</a-radio>
                    <a-radio value="KEY_VALUE">键值对模式</a-radio>
                    <a-radio value="SECTION">分段模式</a-radio>
                  </a-radio-group>
                </a-form-item>

                <!-- 简单模式 -->
                <div v-if="config.FIXED_AMOUNT.subType === 'SIMPLE'" class="config-section">
                  <a-form-item label="固定金额(分)">
                    <a-input-number
                      v-model:value="config.FIXED_AMOUNT.amount"
                      :min="0"
                      :disabled="readonly"
                      style="width: 200px"
                    />
                    <span class="unit-hint">单位：分，例如 1000 = 10.00元</span>
                  </a-form-item>
                </div>

                <!-- 键值对模式 -->
                <div v-if="config.FIXED_AMOUNT.subType === 'KEY_VALUE'" class="config-section">
                  <a-button
                    type="dashed"
                    @click="addKeyValue"
                    :disabled="readonly"
                    style="margin-bottom: 12px"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加键值对
                  </a-button>

                  <a-list
                    :data-source="config.FIXED_AMOUNT.keyValues"
                    bordered
                  >
                    <template #renderItem="{ item, index }">
                      <a-list-item>
                        <a-space style="width: 100%">
                          <a-input
                            v-model:value="item.key"
                            placeholder="键值"
                            :disabled="readonly"
                            style="width: 150px"
                          />
                          <a-input-number
                            v-model:value="item.amount"
                            placeholder="金额(分)"
                            :min="0"
                            :disabled="readonly"
                            style="width: 150px"
                          />
                          <a-button
                            danger
                            size="small"
                            @click="removeKeyValue(index)"
                            :disabled="readonly"
                          >
                            <template #icon><DeleteOutlined /></template>
                          </a-button>
                        </a-space>
                      </a-list-item>
                    </template>
                  </a-list>
                </div>

                <!-- 分段模式 -->
                <div v-if="config.FIXED_AMOUNT.subType === 'SECTION'" class="config-section">
                  <a-button
                    type="dashed"
                    @click="addSection"
                    :disabled="readonly"
                    style="margin-bottom: 12px"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加时间段
                  </a-button>

                  <a-list
                    :data-source="config.FIXED_AMOUNT.values"
                    bordered
                  >
                    <template #renderItem="{ item, index }">
                      <a-list-item>
                        <a-card size="small" style="width: 100%">
                          <a-form layout="vertical">
                            <a-row :gutter="16">
                              <a-col :span="6">
                                <a-form-item label="键值">
                                  <a-input
                                    v-model:value="item.key"
                                    placeholder="breakfast"
                                    :disabled="readonly"
                                  />
                                </a-form-item>
                              </a-col>
                              <a-col :span="6">
                                <a-form-item label="名称">
                                  <a-input
                                    v-model:value="item.name"
                                    placeholder="早餐"
                                    :disabled="readonly"
                                  />
                                </a-form-item>
                              </a-col>
                              <a-col :span="6">
                                <a-form-item label="金额(分)">
                                  <a-input-number
                                    v-model:value="item.amount"
                                    :min="0"
                                    :disabled="readonly"
                                    style="width: 100%"
                                  />
                                </a-form-item>
                              </a-col>
                              <a-col :span="6">
                                <a-form-item label="操作">
                                  <a-button
                                    danger
                                    size="small"
                                    @click="removeSection(index)"
                                    :disabled="readonly"
                                  >
                                    <template #icon><DeleteOutlined /></template>
                                    删除
                                  </a-button>
                                </a-form-item>
                              </a-col>
                            </a-row>

                            <a-row :gutter="16">
                              <a-col :span="12">
                                <a-form-item label="时间段">
                                  <a-time-range
                                    v-model:value="item.timeRange"
                                    format="HH:mm"
                                    :disabled="readonly"
                                    style="width: 100%"
                                  />
                                </a-form-item>
                              </a-col>
                              <a-col :span="12">
                                <a-form-item label="适用餐别">
                                  <a-select
                                    v-model:value="item.mealTypes"
                                    mode="multiple"
                                    placeholder="选择餐别"
                                    :disabled="readonly"
                                  >
                                    <a-select-option value="BREAKFAST">早餐</a-select-option>
                                    <a-select-option value="LUNCH">午餐</a-select-option>
                                    <a-select-option value="DINNER">晚餐</a-select-option>
                                    <a-select-option value="SNACK">零食</a-select-option>
                                  </a-select>
                                </a-form-item>
                              </a-col>
                            </a-row>
                          </a-form>
                        </a-card>
                      </a-list-item>
                    </template>
                  </a-list>
                </div>
              </template>
            </a-form>
          </a-tab-pane>

          <!-- 自由金额模式 -->
          <a-tab-pane key="FREE_AMOUNT" tab="自由金额模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.FREE_AMOUNT.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.FREE_AMOUNT.enabled">
                <a-form-item label="单次限额(分)">
                  <a-input-number
                    v-model:value="config.FREE_AMOUNT.maxAmount"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">单次消费最大金额，0表示不限制</span>
                </a-form-item>

                <a-form-item label="每日限额(分)">
                  <a-input-number
                    v-model:value="config.FREE_AMOUNT.dailyLimit"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">每日累计消费最大金额，0表示不限制</span>
                </a-form-item>
              </template>
            </a-form>
          </a-tab-pane>

          <!-- 计量计费模式 -->
          <a-tab-pane key="METERED" tab="计量计费模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.METERED.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.METERED.enabled">
                <a-form-item label="子类型">
                  <a-radio-group
                    v-model:value="config.METERED.subType"
                    :disabled="readonly"
                  >
                    <a-radio value="TIMING">计时模式</a-radio>
                    <a-radio value="COUNT">计数模式</a-radio>
                  </a-radio-group>
                </a-form-item>

                <a-form-item
                  v-if="config.METERED.subType === 'TIMING'"
                  label="单价(分/分钟)"
                >
                  <a-input-number
                    v-model:value="config.METERED.unitPrice"
                    :min="0"
                    :precision="2"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">每分钟收费金额</span>
                </a-form-item>

                <a-form-item
                  v-if="config.METERED.subType === 'COUNT'"
                  label="单价(分/次)"
                >
                  <a-input-number
                    v-model:value="config.METERED.unitPrice"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">每次收费金额</span>
                </a-form-item>

                <a-form-item label="计费精度(秒)">
                  <a-input-number
                    v-model:value="config.METERED.precision"
                    :min="1"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">计时模式下，计费最小单位（秒）</span>
                </a-form-item>
              </template>
            </a-form>
          </a-tab-pane>

          <!-- 商品模式 -->
          <a-tab-pane key="PRODUCT" tab="商品模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.PRODUCT.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.PRODUCT.enabled">
                <a-form-item label="允许透支">
                  <a-switch
                    v-model:checked="config.PRODUCT.allowOverdraw"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">允许余额不足时消费</span>
                </a-form-item>

                <a-form-item
                  v-if="config.PRODUCT.allowOverdraw"
                  label="透支限额(分)"
                >
                  <a-input-number
                    v-model:value="config.PRODUCT.overdrawLimit"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">最大透支金额</span>
                </a-form-item>

                <a-form-item label="需要输入数量">
                  <a-switch
                    v-model:checked="config.PRODUCT.requireQuantity"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">是否需要输入商品数量</span>
                </a-form-item>
              </template>
            </a-form>
          </a-tab-pane>

          <!-- 订餐模式 -->
          <a-tab-pane key="ORDER" tab="订餐模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.ORDER.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.ORDER.enabled">
                <a-form-item label="订餐截止时间(分钟)">
                  <a-input-number
                    v-model:value="config.ORDER.orderDeadline"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">餐别开始前多少分钟停止订餐</span>
                </a-form-item>

                <a-form-item label="允许取消">
                  <a-switch
                    v-model:checked="config.ORDER.allowCancel"
                    :disabled="readonly"
                  />
                </a-form-item>

                <a-form-item
                  v-if="config.ORDER.allowCancel"
                  label="取消截止时间(分钟)"
                >
                  <a-input-number
                    v-model:value="config.ORDER.cancelDeadline"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="unit-hint">餐别开始前多少分钟停止取消</span>
                </a-form-item>
              </template>
            </a-form>
          </a-tab-pane>

          <!-- 智能模式 -->
          <a-tab-pane key="INTELLIGENCE" tab="智能模式">
            <a-form layout="vertical">
              <a-form-item label="启用">
                <a-switch
                  v-model:checked="config.INTELLIGENCE.enabled"
                  :disabled="readonly"
                />
              </a-form-item>

              <template v-if="config.INTELLIGENCE.enabled">
                <a-form-item label="智能规则类型">
                  <a-select
                    v-model:value="config.INTELLIGENCE.ruleType"
                    :disabled="readonly"
                  >
                    <a-select-option value="TIME_BASED">基于时间</a-select-option>
                    <a-select-option value="AMOUNT_BASED">基于金额</a-select-option>
                    <a-select-option value="FREQUENCY_BASED">基于频率</a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item label="规则配置(JSON)">
                  <a-textarea
                    v-model:value="config.INTELLIGENCE.ruleConfig"
                    :disabled="readonly"
                    :rows="6"
                    placeholder='{"morning": {"maxAmount": 2000, "discount": 0.8}}'
                  />
                  <span class="unit-hint">JSON格式的智能规则配置</span>
                </a-form-item>

                <a-button
                  type="primary"
                  @click="applyIntelligenceTemplate"
                  :disabled="readonly"
                >
                  应用模板
                </a-button>
              </template>
            </a-form>
          </a-tab-pane>
        </a-tabs>
      </template>
    </JsonBaseEditor>

    <!-- 配置模板选择 -->
    <a-card title="快速配置" size="small" style="margin-top: 16px">
      <a-space>
        <span>选择模板:</span>
        <a-select
          v-model:value="selectedTemplate"
          placeholder="选择配置模板"
          style="width: 200px"
          @change="applyTemplate"
          :disabled="readonly"
        >
          <a-select-option value="canteen_fixed">食堂-固定金额</a-select-option>
          <a-select-option value="canteen_free">食堂-自由金额</a-select-option>
          <a-select-option value="supermarket">超市-商品模式</a-select-option>
          <a-select-option value="timing">计时收费</a-select-option>
        </a-select>
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
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
  const activeMode = ref('FIXED_AMOUNT');
  const selectedTemplate = ref(null);

  // 初始化配置
  const config = reactive({
    FIXED_AMOUNT: {
      enabled: false,
      subType: 'SECTION',
      amount: 0,
      keyValues: [],
      values: []
    },
    FREE_AMOUNT: {
      enabled: false,
      maxAmount: 0,
      dailyLimit: 0
    },
    METERED: {
      enabled: false,
      subType: 'TIMING',
      unitPrice: 0,
      precision: 60
    },
    PRODUCT: {
      enabled: false,
      allowOverdraw: false,
      overdrawLimit: 0,
      requireQuantity: true
    },
    ORDER: {
      enabled: false,
      orderDeadline: 30,
      allowCancel: true,
      cancelDeadline: 60
    },
    INTELLIGENCE: {
      enabled: false,
      ruleType: 'TIME_BASED',
      ruleConfig: '{}'
    }
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

  // 固定金额模式 - 键值对操作
  const addKeyValue = () => {
    config.FIXED_AMOUNT.keyValues.push({
      key: '',
      amount: 0
    });
  };

  const removeKeyValue = (index) => {
    config.FIXED_AMOUNT.keyValues.splice(index, 1);
  };

  // 固定金额模式 - 分段操作
  const addSection = () => {
    config.FIXED_AMOUNT.values.push({
      key: '',
      name: '',
      amount: 0,
      timeRange: null,
      mealTypes: []
    });
  };

  const removeSection = (index) => {
    config.FIXED_AMOUNT.values.splice(index, 1);
  };

  // 智能模式模板
  const applyIntelligenceTemplate = () => {
    config.INTELLIGENCE.ruleConfig = JSON.stringify({
      morning: {
        maxAmount: 2000,
        discount: 0.8,
        description: '早市优惠'
      },
      noon: {
        maxAmount: 5000,
        discount: 1.0,
        description: '正餐正常价格'
      },
      evening: {
        maxAmount: 3000,
        discount: 0.9,
        description: '晚市优惠'
      }
    }, null, 2);
    message.success('智能规则模板已应用');
  };

  // 快速配置模板
  const templates = {
    canteen_fixed: {
      FIXED_AMOUNT: {
        enabled: true,
        subType: 'SECTION',
        values: [
          {
            key: 'breakfast',
            name: '早餐',
            amount: 1000,
            timeRange: ['06:00', '09:00'],
            mealTypes: ['BREAKFAST']
          },
          {
            key: 'lunch',
            name: '午餐',
            amount: 2000,
            timeRange: ['11:00', '13:00'],
            mealTypes: ['LUNCH']
          },
          {
            key: 'dinner',
            name: '晚餐',
            amount: 1500,
            timeRange: ['17:00', '19:00'],
            mealTypes: ['DINNER']
          }
        ]
      }
    },
    canteen_free: {
      FREE_AMOUNT: {
        enabled: true,
        maxAmount: 5000,
        dailyLimit: 30000
      }
    },
    supermarket: {
      PRODUCT: {
        enabled: true,
        allowOverdraw: false,
        requireQuantity: true
      }
    },
    timing: {
      METERED: {
        enabled: true,
        subType: 'TIMING',
        unitPrice: 100,
        precision: 60
      }
    }
  };

  const applyTemplate = (templateKey) => {
    if (templates[templateKey]) {
      // 重置所有模式
      Object.keys(config).forEach(key => {
        if (config[key].enabled !== undefined) {
          config[key].enabled = false;
        }
      });

      // 应用模板
      Object.assign(config, templates[templateKey]);
      message.success('配置模板已应用');
    }
  };

  const handleUpdate = (value) => {
    emit('update:modelValue', value);
  };
</script>

<style lang="less" scoped>
  .mode-config-editor {
    .config-section {
      margin-top: 16px;
      padding: 16px;
      background: #fafafa;
      border-radius: 4px;
    }

    .unit-hint {
      margin-left: 8px;
      color: #999;
      font-size: 12px;
    }

    :deep(.ant-tabs-card) {
      .ant-tabs-tab {
        border-radius: 4px 4px 0 0;
      }
    }
  }
</style>
