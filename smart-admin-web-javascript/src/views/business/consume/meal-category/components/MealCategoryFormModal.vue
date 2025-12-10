<!--
 * Meal Category Form Modal
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="isEdit ? 'Edit Category' : 'New Category'"
    width="700px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirm-loading="submitLoading"
    destroy-on-close
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-divider orientation="left">Basic Info</a-divider>

      <a-form-item label="Name" name="categoryName">
        <a-input
          v-model:value="formData.categoryName"
          placeholder="Enter category name"
          :maxlength="50"
        />
      </a-form-item>

      <a-form-item label="Type" name="mealType">
        <a-select
          v-model:value="formData.mealType"
          placeholder="Select type"
          @change="handleMealTypeChange"
        >
          <a-select-option value="BREAKFAST">Breakfast</a-select-option>
          <a-select-option value="LUNCH">Lunch</a-select-option>
          <a-select-option value="DINNER">Dinner</a-select-option>
          <a-select-option value="SNACK">Snack</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="Description" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="Enter description"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <a-divider orientation="left">Time Range</a-divider>

      <a-form-item label="Start Time" name="startTime">
        <a-time-picker
          v-model:value="formData.startTime"
          placeholder="Select start"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="End Time" name="endTime">
        <a-time-picker
          v-model:value="formData.endTime"
          placeholder="Select end"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 100%"
        />
      </a-form-item>

      <a-divider orientation="left">Price</a-divider>

      <a-form-item label="Min Price" name="minPrice">
        <a-input-number
          v-model:value="formData.minPrice"
          placeholder="Enter min price"
          style="width: 100%"
          :min="0"
          :max="9999.99"
          :precision="2"
          addon-before="¥"
          @change="handlePriceChange"
        />
      </a-form-item>

      <a-form-item label="Max Price" name="maxPrice">
        <a-input-number
          v-model:value="formData.maxPrice"
          placeholder="Enter max price"
          style="width: 100%"
          :min="0"
          :max="9999.99"
          :precision="2"
          addon-before="¥"
          @change="handlePriceChange"
        />
      </a-form-item>

      <a-form-item label="Price Range">
        <div class="price-range-display">
          <span class="min-price">¥{{ formatMoney(formData.minPrice || 0) }}</span>
          <span class="separator">~</span>
          <span class="max-price">¥{{ formatMoney(formData.maxPrice || 0) }}</span>
        </div>
      </a-form-item>

      <a-divider orientation="left">Area</a-divider>

      <a-form-item label="Available Areas" name="areaIds">
        <a-select
          v-model:value="formData.areaIds"
          mode="multiple"
          placeholder="Select areas"
          :options="areaOptions"
          allow-clear
          show-search
          :filter-option="filterOption"
        />
      </a-form-item>

      <a-divider orientation="left">Rules</a-divider>

      <a-form-item label="Daily Limit" name="dailyLimit">
        <a-input-number
          v-model:value="formData.dailyLimit"
          placeholder="Enter daily limit"
          style="width: 100%"
          :min="0"
          :max="999"
          addon-after="份"
        />
      </a-form-item>

      <a-form-item label="Advance Booking" name="advanceBooking">
        <a-switch
          v-model:checked="formData.advanceBooking"
          :checked-children="'On'"
          :un-checked-children="'Off'"
        />
      </a-form-item>

      <a-form-item label="Booking Time" name="bookingTime" v-if="formData.advanceBooking">
        <a-input-number
          v-model:value="formData.bookingTime"
          placeholder="Enter minutes"
          style="width: 100%"
          :min="1"
          :max="1440"
          addon-after="minutes"
        />
      </a-form-item>

      <a-form-item label="Status" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">Enabled</a-radio>
          <a-radio :value="0">Disabled</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-divider orientation="left">Remark</a-divider>

      <a-form-item label="Remark" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="Enter remark"
          :rows="2"
          :maxlength="200"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref();
const submitLoading = ref(false);

const areaOptions = ref([
  { label: 'Building A', value: 1 },
  { label: 'Building B', value: 2 },
  { label: 'Canteen', value: 3 },
  { label: 'VIP Hall', value: 4 },
]);

const formState = {
  categoryName: '',
  mealType: undefined,
  description: '',
  startTime: null,
  endTime: null,
  minPrice: null,
  maxPrice: null,
  areaIds: [],
  dailyLimit: 0,
  advanceBooking: false,
  bookingTime: 30,
  status: 1,
  remark: '',
};

const formDataLocal = reactive({ ...formState });

const formRules = {
  categoryName: [
    { required: true, message: 'Please enter category name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length 2-50', trigger: 'blur' },
  ],
  mealType: [{ required: true, message: 'Please select type', trigger: 'change' }],
  startTime: [{ required: true, message: 'Please select start', trigger: 'change' }],
  endTime: [{ required: true, message: 'Please select end', trigger: 'change' }],
  minPrice: [{ required: true, message: 'Please enter min price', trigger: 'blur' }],
  maxPrice: [{ required: true, message: 'Please enter max price', trigger: 'blur' }],
  status: [{ required: true, message: 'Please select status', trigger: 'change' }],
};

const filterOption = (input, option) => option.label.toLowerCase().includes(input.toLowerCase());

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.isEdit && props.formData) {
        Object.assign(formDataLocal, { ...formState, ...props.formData });
      } else {
        Object.assign(formDataLocal, { ...formState });
      }
      formRef.value?.resetFields();
    }
  }
);

const validateTimeRange = () => {
  if (formDataLocal.startTime && formDataLocal.endTime) {
    const start = dayjs(formDataLocal.startTime, 'HH:mm');
    const end = dayjs(formDataLocal.endTime, 'HH:mm');
    if (!end.isAfter(start)) {
      formRef.value.setFields({
        endTime: {
          value: formDataLocal.endTime,
          errors: [new Error('End time must be after start time')],
        },
      });
      return false;
    }
  }
  return true;
};

const validatePriceRange = () => {
  if (formDataLocal.minPrice !== null && formDataLocal.maxPrice !== null) {
    if (formDataLocal.minPrice > formDataLocal.maxPrice) {
      formRef.value.setFields({
        maxPrice: {
          value: formDataLocal.maxPrice,
          errors: [new Error('Max price must be greater than min price')],
        },
      });
      return false;
    }
  }
  return true;
};

const handleMealTypeChange = (value) => {
  const defaults = {
    BREAKFAST: { start: '06:00', end: '09:00' },
    LUNCH: { start: '11:00', end: '14:00' },
    DINNER: { start: '17:00', end: '20:00' },
    SNACK: { start: '14:00', end: '16:00' },
  };
  const times = defaults[value];
  if (times) {
    formDataLocal.startTime = times.start;
    formDataLocal.endTime = times.end;
  }
};

const handlePriceChange = () => {
  if (formDataLocal.minPrice !== null && formDataLocal.maxPrice !== null) {
    validatePriceRange();
  }
};

const formatMoney = (amount) => Number(amount || 0).toFixed(2);

const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    if (!validateTimeRange() || !validatePriceRange()) return;

    submitLoading.value = true;
    message.success(props.isEdit ? 'Updated' : 'Created');
    emit('success', { ...formDataLocal });
    handleCancel();
  } catch (error) {
    // handled by form
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => {
  emit('update:visible', false);
  formRef.value?.resetFields();
};
</script>

<style lang="less" scoped>
:deep(.ant-divider-horizontal.ant-divider-with-text-left) {
  margin: 16px 0;
  font-weight: 500;
}

.price-range-display {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-weight: 500;

  .min-price,
  .max-price {
    color: #1890ff;
    font-size: 16px;
  }

  .separator {
    margin: 0 12px;
    color: #8c8c8c;
    font-size: 14px;
  }
}
</style>
