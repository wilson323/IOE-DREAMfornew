<template>
  <div class="fixed-value-config-editor">
    <JsonBaseEditor ref="baseEditor" :model-value="config" @update:model-value="handleUpdate">
      <template #editor>
        <a-form layout="vertical">
          <!-- 启用定额配置 -->
          <a-form-item>
            <template #label>
              <a-space>
                <span>启用定额配置</span>
                <a-tooltip title="开启后，该区域或账户的消费将受定额限制">
                  <QuestionCircleOutlined />
                </a-tooltip>
              </a-space>
            </template>
            <a-switch
              v-model:checked="config.enabled"
              :disabled="readonly"
            />
            <span class="hint-text">开启后，消费金额将受限于配置的定额</span>
          </a-form-item>

          <template v-if="config.enabled">
            <!-- 定额配置模式 -->
            <a-form-item label="配置模式">
              <a-radio-group
                v-model:value="config.mode"
                :disabled="readonly"
              >
                <a-radio value="MEAL_BASED">基于餐别</a-radio>
                <a-radio value="TIME_BASED">基于时间段</a-radio>
                <a-radio value="HYBRID">混合模式</a-radio>
              </a-radio-group>
            </a-form-item>

            <!-- 基于餐别的定额配置 -->
            <div v-if="config.mode === 'MEAL_BASED'" class="config-section">
              <a-card title="餐别定额配置" size="small">
                <a-row :gutter="[16, 16]">
                  <a-col :span="12">
                    <a-form-item label="早餐(分)">
                      <a-input-number
                        v-model:value="config.mealValues.BREAKFAST"
                        :min="0"
                        :disabled="readonly"
                        style="width: 100%"
                      />
                      <div class="amount-display">
                        = ¥{{ (config.mealValues.BREAKFAST / 100).toFixed(2) }}
                      </div>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="午餐(分)">
                      <a-input-number
                        v-model:value="config.mealValues.LUNCH"
                        :min="0"
                        :disabled="readonly"
                        style="width: 100%"
                      />
                      <div class="amount-display">
                        = ¥{{ (config.mealValues.LUNCH / 100).toFixed(2) }}
                      </div>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="晚餐(分)">
                      <a-input-number
                        v-model:value="config.mealValues.DINNER"
                        :min="0"
                        :disabled="readonly"
                        style="width: 100%"
                      />
                      <div class="amount-display">
                        = ¥{{ (config.mealValues.DINNER / 100).toFixed(2) }}
                      </div>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="零食(分)">
                      <a-input-number
                        v-model:value="config.mealValues.SNACK"
                        :min="0"
                        :disabled="readonly"
                        style="width: 100%"
                      />
                      <div class="amount-display">
                        = ¥{{ (config.mealValues.SNACK / 100).toFixed(2) }}
                      </div>
                    </a-form-item>
                  </a-col>
                </a-row>

                <!-- 批量设置 -->
                <a-divider>批量设置</a-divider>
                <a-space>
                  <span>所有餐别统一设置为:</span>
                  <a-input-number
                    v-model:value="uniformAmount"
                    :min="0"
                    placeholder="金额(分)"
                    :disabled="readonly"
                  />
                  <a-button
                    type="primary"
                    @click="applyUniformAmount"
                    :disabled="readonly"
                  >
                    应用
                  </a-button>
                </a-space>
              </a-card>
            </div>

            <!-- 基于时间段的定额配置 -->
            <div v-if="config.mode === 'TIME_BASED'" class="config-section">
              <a-card title="时间段定额配置" size="small">
                <template #extra>
                  <a-button
                    type="primary"
                    size="small"
                    @click="addTimeSlot"
                    :disabled="readonly"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加时间段
                  </a-button>
                </template>

                <a-empty
                  v-if="config.timeSlots.length === 0"
                  description="暂无时间段配置"
                  :image="false"
                />

                <a-list
                  v-else
                  :data-source="config.timeSlots"
                  bordered
                  size="small"
                >
                  <template #renderItem="{ item, index }">
                    <a-list-item>
                      <a-card size="small" style="width: 100%">
                        <a-row :gutter="16">
                          <a-col :span="6">
                            <a-form-item label="名称">
                              <a-input
                                v-model:value="item.name"
                                placeholder="如：早餐时段"
                                :disabled="readonly"
                              />
                            </a-form-item>
                          </a-col>
                          <a-col :span="8">
                            <a-form-item label="时间段">
                              <a-time-range
                                v-model:value="item.timeRange"
                                format="HH:mm"
                                :disabled="readonly"
                                style="width: 100%"
                              />
                            </a-form-item>
                          </a-col>
                          <a-col :span="6">
                            <a-form-item label="定额(分)">
                              <a-input-number
                                v-model:value="item.amount"
                                :min="0"
                                :disabled="readonly"
                                style="width: 100%"
                              />
                              <div class="amount-display">
                                = ¥{{ (item.amount / 100).toFixed(2) }}
                              </div>
                            </a-form-item>
                          </a-col>
                          <a-col :span="4">
                            <a-button
                              danger
                              size="small"
                              @click="removeTimeSlot(index)"
                              :disabled="readonly"
                              style="margin-top: 30px"
                            >
                              <template #icon><DeleteOutlined /></template>
                              删除
                            </a-button>
                          </a-col>
                        </a-row>
                      </a-card>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>
            </div>

            <!-- 混合模式 -->
            <div v-if="config.mode === 'HYBRID'" class="config-section">
              <a-card title="混合模式配置" size="small">
                <a-alert
                  type="info"
                  message="混合模式说明"
                  description="混合模式同时支持餐别定额和时间段定额，系统将优先匹配时间段定额，未匹配到时间段时使用餐别定额"
                  show-icon
                  style="margin-bottom: 16px"
                />

                <a-tabs>
                  <a-tab-pane key="meal" tab="餐别定额">
                    <!-- 复用基于餐别的配置 -->
                    <a-row :gutter="[16, 16]">
                      <a-col :span="12">
                        <a-form-item label="早餐(分)">
                          <a-input-number
                            v-model:value="config.mealValues.BREAKFAST"
                            :min="0"
                            :disabled="readonly"
                            style="width: 100%"
                          />
                          <div class="amount-display">
                            = ¥{{ (config.mealValues.BREAKFAST / 100).toFixed(2) }}
                          </div>
                        </a-form-item>
                      </a-col>
                      <a-col :span="12">
                        <a-form-item label="午餐(分)">
                          <a-input-number
                            v-model:value="config.mealValues.LUNCH"
                            :min="0"
                            :disabled="readonly"
                            style="width: 100%"
                          />
                          <div class="amount-display">
                            = ¥{{ (config.mealValues.LUNCH / 100).toFixed(2) }}
                          </div>
                        </a-form-item>
                      </a-col>
                      <a-col :span="12">
                        <a-form-item label="晚餐(分)">
                          <a-input-number
                            v-model:value="config.mealValues.DINNER"
                            :min="0"
                            :disabled="readonly"
                            style="width: 100%"
                          />
                          <div class="amount-display">
                            = ¥{{ (config.mealValues.DINNER / 100).toFixed(2) }}
                          </div>
                        </a-form-item>
                      </a-col>
                      <a-col :span="12">
                        <a-form-item label="零食(分)">
                          <a-input-number
                            v-model:value="config.mealValues.SNACK"
                            :min="0"
                            :disabled="readonly"
                            style="width: 100%"
                          />
                          <div class="amount-display">
                            = ¥{{ (config.mealValues.SNACK / 100).toFixed(2) }}
                          </div>
                        </a-form-item>
                      </a-col>
                    </a-row>
                  </a-tab-pane>
                  <a-tab-pane key="time" tab="时间段定额">
                    <!-- 复用基于时间段的配置 -->
                    <a-button
                      type="primary"
                      size="small"
                      @click="addTimeSlot"
                      :disabled="readonly"
                      style="margin-bottom: 12px"
                    >
                      <template #icon><PlusOutlined /></template>
                      添加时间段
                    </a-button>

                    <a-list
                      :data-source="config.timeSlots"
                      bordered
                      size="small"
                    >
                      <template #renderItem="{ item, index }">
                        <a-list-item>
                          <a-row :gutter="16" style="width: 100%">
                            <a-col :span="8">
                              <a-input
                                v-model:value="item.name"
                                placeholder="名称"
                                :disabled="readonly"
                              />
                            </a-col>
                            <a-col :span="8">
                              <a-time-range
                                v-model:value="item.timeRange"
                                format="HH:mm"
                                :disabled="readonly"
                              />
                            </a-col>
                            <a-col :span="6">
                              <a-input-number
                                v-model:value="item.amount"
                                :min="0"
                                :disabled="readonly"
                                placeholder="金额(分)"
                                style="width: 100%"
                              />
                            </a-col>
                            <a-col :span="2">
                              <a-button
                                danger
                                size="small"
                                @click="removeTimeSlot(index)"
                                :disabled="readonly"
                              >
                                <template #icon><DeleteOutlined /></template>
                              </a-button>
                            </a-col>
                          </a-row>
                        </a-list-item>
                      </template>
                    </a-list>
                  </a-tab-pane>
                </a-tabs>
              </a-card>
            </div>

            <!-- 高级选项 -->
            <a-collapse style="margin-top: 16px">
              <a-collapse-panel key="advanced" header="高级选项">
                <a-form-item label="超出定额处理">
                  <a-select
                    v-model:value="config.overMode"
                    :disabled="readonly"
                  >
                    <a-select-option value="FORBID">禁止消费</a-select-option>
                    <a-select-option value="ALLOW_CASH">允许现金钱包补足</a-select-option>
                    <a-select-option value="ALLOW_OVERDRAW">允许透支</a-select-option>
                  </a-select>
                  <div class="hint-text">
                    超出定额时的处理方式
                  </div>
                </a-form-item>

                <a-form-item
                  v-if="config.overMode === 'ALLOW_OVERDRAW'"
                  label="透支限额(分)"
                >
                  <a-input-number
                    v-model:value="config.overdrawLimit"
                    :min="0"
                    :disabled="readonly"
                  />
                  <span class="hint-text">允许透支的最大金额</span>
                </a-form-item>

                <a-form-item label="定额提示">
                  <a-switch
                    v-model:checked="config.showHint"
                    :disabled="readonly"
                  />
                  <span class="hint-text">消费时显示剩余定额提示</span>
                </a-form-item>
              </a-collapse-panel>
            </a-collapse>
          </template>
        </a-form>
      </template>
    </JsonBaseEditor>
  </div>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    PlusOutlined,
    DeleteOutlined,
    QuestionCircleOutlined
  } from '@ant-design/icons-vue';
  import JsonBaseEditor from './JsonBaseEditor.vue';
  import dayjs from 'dayjs';

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
  const uniformAmount = ref(null);

  // 初始化配置
  const config = reactive({
    enabled: false,
    mode: 'MEAL_BASED', // MEAL_BASED, TIME_BASED, HYBRID
    mealValues: {
      BREAKFAST: 0,
      LUNCH: 0,
      DINNER: 0,
      SNACK: 0
    },
    timeSlots: [],
    overMode: 'FORBID', // FORBID, ALLOW_CASH, ALLOW_OVERDRAW
    overdrawLimit: 0,
    showHint: true
  });

  // 监听外部传入的值
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal && Object.keys(newVal).length > 0) {
        // 合并配置，保留默认值
        if (newVal.enabled !== undefined) config.enabled = newVal.enabled;
        if (newVal.mode) config.mode = newVal.mode;
        if (newVal.mealValues) {
          Object.assign(config.mealValues, newVal.mealValues);
        }
        if (newVal.timeSlots) {
          config.timeSlots = newVal.timeSlots.map(slot => ({
            ...slot,
            timeRange: slot.timeRange ? slot.timeRange.map(t => dayjs(t, 'HH:mm')) : null
          }));
        }
        if (newVal.overMode) config.overMode = newVal.overMode;
        if (newVal.overdrawLimit !== undefined) config.overdrawLimit = newVal.overdrawLimit;
        if (newVal.showHint !== undefined) config.showHint = newVal.showHint;
      }
    },
    { immediate: true, deep: true }
  );

  // 监听内部变化，触发更新
  watch(
    config,
    (newVal) => {
      // 格式化时间范围
      const formattedTimeSlots = newVal.timeSlots.map(slot => ({
        ...slot,
        timeRange: slot.timeRange ? slot.timeRange.map(t => t.format('HH:mm')) : null
      }));

      emit('update:modelValue', {
        ...newVal,
        timeSlots: formattedTimeSlots
      });
    },
    { deep: true }
  );

  // 添加时间段
  const addTimeSlot = () => {
    config.timeSlots.push({
      name: '',
      timeRange: null,
      amount: 0
    });
  };

  // 删除时间段
  const removeTimeSlot = (index) => {
    config.timeSlots.splice(index, 1);
  };

  // 应用统一金额
  const applyUniformAmount = () => {
    if (!uniformAmount.value && uniformAmount.value !== 0) {
      message.warning('请输入统一金额');
      return;
    }

    config.mealValues.BREAKFAST = uniformAmount.value;
    config.mealValues.LUNCH = uniformAmount.value;
    config.mealValues.DINNER = uniformAmount.value;
    config.mealValues.SNACK = uniformAmount.value;

    message.success('已应用统一金额到所有餐别');
  };

  const handleUpdate = (value) => {
    emit('update:modelValue', value);
  };
</script>

<style lang="less" scoped>
  .fixed-value-config-editor {
    .config-section {
      margin-top: 16px;
    }

    .hint-text {
      margin-left: 8px;
      color: #999;
      font-size: 12px;
    }

    .amount-display {
      margin-top: 4px;
      color: #52c41a;
      font-size: 12px;
      font-weight: 500;
    }

    :deep(.ant-card) {
      .ant-card-head {
        padding: 8px 16px;
        min-height: 40px;
      }

      .ant-card-body {
        padding: 16px;
      }
    }

    :deep(.ant-list-item) {
      padding: 12px;
    }
  }
</style>
