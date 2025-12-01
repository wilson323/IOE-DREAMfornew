<!--
  * 时间段配置弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-11-19
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:visible="visible"
    title="时间段配置"
    width="800px"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-alert
        message="时间段配置说明"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #description>
          <div>
            <p>时间段类型说明：</p>
            <ul style="margin: 8px 0; padding-left: 20px">
              <li><strong>工作日</strong>：周一到周五的指定时间段</li>
              <li><strong>周末</strong>：周六和周日的指定时间段</li>
              <li><strong>节假日</strong>：指定节假日的指定时间段</li>
              <li><strong>特定日期</strong>：指定具体日期的指定时间段</li>
            </ul>
            <p style="margin-top: 8px">
              可以配置多个时间段，只要当前时间匹配任意一个时间段即可通行。
            </p>
          </div>
        </template>
      </a-alert>

      <a-form-item label="时间段列表" name="timeSlots">
        <div class="time-slot-list">
          <div
            v-for="(slot, index) in formData.timeSlots"
            :key="index"
            class="time-slot-item"
          >
            <a-card size="small" :bordered="true">
              <template #title>
                <a-space>
                  <span>时间段 {{ index + 1 }}</span>
                  <a-tag :color="getTypeColor(slot.type)">
                    {{ getTypeLabel(slot.type) }}
                  </a-tag>
                </a-space>
              </template>
              <template #extra>
                <a-button
                  type="text"
                  danger
                  size="small"
                  @click="handleRemoveSlot(index)"
                >
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </template>

              <a-form-item
                label="时间段类型"
                :name="['timeSlots', index, 'type']"
                :rules="[{ required: true, message: '请选择时间段类型' }]"
              >
                <a-select
                  v-model:value="slot.type"
                  placeholder="请选择时间段类型"
                  @change="handleTypeChange(index)"
                >
                  <a-select-option value="weekday">工作日</a-select-option>
                  <a-select-option value="weekend">周末</a-select-option>
                  <a-select-option value="holiday">节假日</a-select-option>
                  <a-select-option value="specific">特定日期</a-select-option>
                </a-select>
              </a-form-item>

              <!-- 工作日/周末：选择星期几 -->
              <a-form-item
                v-if="slot.type === 'weekday' || slot.type === 'weekend'"
                label="选择星期"
                :name="['timeSlots', index, 'days']"
                :rules="[{ required: true, message: '请选择星期' }]"
              >
                <a-checkbox-group v-model:value="slot.days">
                  <a-checkbox :value="1">周一</a-checkbox>
                  <a-checkbox :value="2">周二</a-checkbox>
                  <a-checkbox :value="3">周三</a-checkbox>
                  <a-checkbox :value="4">周四</a-checkbox>
                  <a-checkbox :value="5">周五</a-checkbox>
                  <a-checkbox v-if="slot.type === 'weekend'" :value="6">周六</a-checkbox>
                  <a-checkbox v-if="slot.type === 'weekend'" :value="7">周日</a-checkbox>
                </a-checkbox-group>
              </a-form-item>

              <!-- 节假日/特定日期：选择日期 -->
              <a-form-item
                v-if="slot.type === 'holiday' || slot.type === 'specific'"
                label="选择日期"
                :name="['timeSlots', index, 'dates']"
                :rules="[{ required: true, message: '请选择日期' }]"
              >
                <a-select
                  v-model:value="slot.dates"
                  mode="tags"
                  placeholder="请输入日期（格式：yyyy-MM-dd），按回车添加"
                  :token-separators="[',']"
                  style="width: 100%"
                >
                </a-select>
                <div class="form-item-tip">
                  格式：yyyy-MM-dd，例如：2025-01-01，多个日期用逗号分隔
                </div>
              </a-form-item>

              <!-- 时间范围列表 -->
              <a-form-item
                label="时间范围"
                :name="['timeSlots', index, 'timeRanges']"
                :rules="[{ required: true, message: '请至少添加一个时间范围' }]"
              >
                <div class="time-range-list">
                  <div
                    v-for="(range, rangeIndex) in slot.timeRanges"
                    :key="rangeIndex"
                    class="time-range-item"
                  >
                    <a-space>
                      <a-time-picker
                        v-model:value="range.startTime"
                        format="HH:mm"
                        placeholder="开始时间"
                        style="width: 120px"
                        @change="handleTimeRangeChange(index, rangeIndex)"
                      />
                      <span>至</span>
                      <a-time-picker
                        v-model:value="range.endTime"
                        format="HH:mm"
                        placeholder="结束时间"
                        style="width: 120px"
                        @change="handleTimeRangeChange(index, rangeIndex)"
                      />
                      <a-button
                        type="text"
                        danger
                        size="small"
                        @click="handleRemoveTimeRange(index, rangeIndex)"
                        :disabled="slot.timeRanges.length === 1"
                      >
                        <template #icon><DeleteOutlined /></template>
                      </a-button>
                    </a-space>
                  </div>
                  <a-button
                    type="dashed"
                    block
                    @click="handleAddTimeRange(index)"
                    style="margin-top: 8px"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加时间范围
                  </a-button>
                </div>
              </a-form-item>
            </a-card>
          </div>

          <a-button
            type="dashed"
            block
            @click="handleAddSlot"
            style="margin-top: 16px"
          >
            <template #icon><PlusOutlined /></template>
            添加时间段
          </a-button>
        </div>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, watch, computed } from 'vue';
  import { message } from 'ant-design-vue';
  import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import dayjs from 'dayjs';

  /**
   * 组件属性
   */
  const props = defineProps({
    /**
     * 是否显示弹窗
     */
    visible: {
      type: Boolean,
      default: false,
    },
    /**
     * 初始时间段配置JSON字符串
     */
    value: {
      type: String,
      default: '',
    },
  });

  /**
   * 组件事件
   */
  const emit = defineEmits(['update:visible', 'submit', 'cancel']);

  /**
   * 表单引用
   */
  const formRef = ref(null);

  /**
   * 加载状态
   */
  const loading = ref(false);

  /**
   * 表单数据
   */
  const formData = reactive({
    timeSlots: [],
  });

  /**
   * 表单验证规则
   */
  const rules = {
    timeSlots: [
      {
        validator: (rule, value) => {
          if (!value || value.length === 0) {
            return Promise.reject('请至少添加一个时间段配置');
          }
          return Promise.resolve();
        },
      },
    ],
  };

  /**
   * 初始化表单数据
   */
  const initFormData = () => {
    if (props.value) {
      try {
        const config = JSON.parse(props.value);
        formData.timeSlots = config.timeSlots || [];
        // 转换时间字符串为dayjs对象
        formData.timeSlots.forEach((slot) => {
          if (slot.timeRanges) {
            slot.timeRanges = slot.timeRanges.map((range) => ({
              start: range.start,
              end: range.end,
              startTime: range.start ? dayjs(range.start, 'HH:mm') : null,
              endTime: range.end ? dayjs(range.end, 'HH:mm') : null,
            }));
          }
        });
      } catch (error) {
        console.error('解析时间段配置失败:', error);
        formData.timeSlots = [];
      }
    } else {
      formData.timeSlots = [];
    }
  };

  /**
   * 监听visible变化
   */
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        initFormData();
      }
    },
    { immediate: true }
  );

  /**
   * 监听value变化
   */
  watch(
    () => props.value,
    () => {
      if (props.visible) {
        initFormData();
      }
    }
  );

  /**
   * 获取时间段类型标签
   */
  const getTypeLabel = (type) => {
    const labelMap = {
      weekday: '工作日',
      weekend: '周末',
      holiday: '节假日',
      specific: '特定日期',
    };
    return labelMap[type] || type;
  };

  /**
   * 获取时间段类型颜色
   */
  const getTypeColor = (type) => {
    const colorMap = {
      weekday: 'blue',
      weekend: 'green',
      holiday: 'orange',
      specific: 'purple',
    };
    return colorMap[type] || 'default';
  };

  /**
   * 添加时间段
   */
  const handleAddSlot = () => {
    formData.timeSlots.push({
      type: 'weekday',
      days: [],
      dates: [],
      timeRanges: [
        {
          start: '',
          end: '',
          startTime: null,
          endTime: null,
        },
      ],
    });
  };

  /**
   * 删除时间段
   */
  const handleRemoveSlot = (index) => {
    formData.timeSlots.splice(index, 1);
  };

  /**
   * 时间段类型变化
   */
  const handleTypeChange = (index) => {
    const slot = formData.timeSlots[index];
    // 重置相关字段
    if (slot.type === 'weekday' || slot.type === 'weekend') {
      slot.days = [];
      slot.dates = [];
    } else {
      slot.days = [];
      if (!slot.dates) {
        slot.dates = [];
      }
    }
  };

  /**
   * 添加时间范围
   */
  const handleAddTimeRange = (slotIndex) => {
    const slot = formData.timeSlots[slotIndex];
    if (!slot.timeRanges) {
      slot.timeRanges = [];
    }
    slot.timeRanges.push({
      start: '',
      end: '',
      startTime: null,
      endTime: null,
    });
  };

  /**
   * 删除时间范围
   */
  const handleRemoveTimeRange = (slotIndex, rangeIndex) => {
    const slot = formData.timeSlots[slotIndex];
    slot.timeRanges.splice(rangeIndex, 1);
  };

  /**
   * 时间范围变化
   */
  const handleTimeRangeChange = (slotIndex, rangeIndex) => {
    const range = formData.timeSlots[slotIndex].timeRanges[rangeIndex];
    if (range.startTime) {
      range.start = range.startTime.format('HH:mm');
    }
    if (range.endTime) {
      range.end = range.endTime.format('HH:mm');
    }
  };

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    try {
      await formRef.value.validate();

      // 验证时间范围
      for (let i = 0; i < formData.timeSlots.length; i++) {
        const slot = formData.timeSlots[i];
        if (!slot.timeRanges || slot.timeRanges.length === 0) {
          message.error(`时间段 ${i + 1} 至少需要一个时间范围`);
          return;
        }

        for (let j = 0; j < slot.timeRanges.length; j++) {
          const range = slot.timeRanges[j];
          if (!range.start || !range.end) {
            message.error(`时间段 ${i + 1} 的时间范围 ${j + 1} 不完整`);
            return;
          }

          if (range.start >= range.end) {
            message.error(`时间段 ${i + 1} 的时间范围 ${j + 1} 开始时间必须小于结束时间`);
            return;
          }
        }
      }

      // 构建输出JSON
      const output = {
        timeSlots: formData.timeSlots.map((slot) => {
          const result = {
            type: slot.type,
            timeRanges: slot.timeRanges.map((range) => ({
              start: range.start,
              end: range.end,
            })),
          };

          if (slot.type === 'weekday' || slot.type === 'weekend') {
            result.days = slot.days || [];
          } else {
            result.dates = slot.dates || [];
          }

          return result;
        }),
      };

      const jsonString = JSON.stringify(output);
      emit('submit', jsonString);
      handleCancel();
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  /**
   * 取消操作
   */
  const handleCancel = () => {
    emit('update:visible', false);
    emit('cancel');
  };
</script>

<style lang="less" scoped>
  .time-slot-list {
    .time-slot-item {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .time-range-list {
    .time-range-item {
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .form-item-tip {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }
</style>

