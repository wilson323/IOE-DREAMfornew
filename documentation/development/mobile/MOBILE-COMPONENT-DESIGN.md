# IOE-DREAM 移动端组件设计文档

> **版本**: v1.0.0  
> **更新日期**: 2025-12-17  
> **技术栈**: uni-app + Vue 3 + TypeScript

---

## 1. 组件架构

```
components/
├── base/          # 基础组件
├── business/      # 业务组件
├── layout/        # 布局组件
└── feedback/      # 反馈组件
```

---

## 2. 基础组件

### 2.1 IoeButton 按钮

```vue
<template>
  <button 
    :class="['ioe-btn', `ioe-btn--${type}`, { 'ioe-btn--loading': loading }]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <uni-icons v-if="icon" :type="icon" />
    <slot />
  </button>
</template>

<script setup lang="ts">
interface Props {
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'default';
  size?: 'large' | 'medium' | 'small';
  loading?: boolean;
  disabled?: boolean;
  icon?: string;
}
</script>
```

### 2.2 IoeCard 卡片

```vue
<template>
  <view class="ioe-card">
    <view v-if="title" class="ioe-card__header">
      <text class="ioe-card__title">{{ title }}</text>
      <slot name="extra" />
    </view>
    <view class="ioe-card__body">
      <slot />
    </view>
  </view>
</template>
```

### 2.3 IoeList 列表

```vue
<template>
  <scroll-view 
    scroll-y 
    @scrolltolower="loadMore"
    refresher-enabled
    @refresherrefresh="refresh"
  >
    <view v-for="item in list" :key="item.id">
      <slot :item="item" />
    </view>
    <view v-if="loading" class="loading">加载中...</view>
    <view v-if="finished" class="finished">没有更多了</view>
  </scroll-view>
</template>
```

---

## 3. 业务组件

### 3.1 DeviceStatusCard 设备状态卡片

```vue
<template>
  <ioe-card>
    <view class="device-card">
      <view class="device-card__icon" :class="statusClass">
        <uni-icons :type="iconType" size="32" />
      </view>
      <view class="device-card__info">
        <text class="device-name">{{ device.name }}</text>
        <text class="device-status">{{ statusText }}</text>
      </view>
      <view class="device-card__action">
        <slot name="action" />
      </view>
    </view>
  </ioe-card>
</template>

<script setup lang="ts">
interface Device {
  id: number;
  name: string;
  status: 0 | 1 | 2; // 离线|在线|故障
  type: string;
}
</script>
```

### 3.2 AttendanceClockIn 打卡组件

```vue
<template>
  <view class="clock-in">
    <view class="clock-in__time">{{ currentTime }}</view>
    <view class="clock-in__location">{{ location }}</view>
    <ioe-button 
      type="primary" 
      size="large" 
      :loading="loading"
      @click="handleClockIn"
    >
      {{ buttonText }}
    </ioe-button>
    <view class="clock-in__status">
      <text>上班: {{ morningTime || '--' }}</text>
      <text>下班: {{ eveningTime || '--' }}</text>
    </view>
  </view>
</template>
```

### 3.3 AlarmItem 告警项组件

```vue
<template>
  <view class="alarm-item" @click="handleClick">
    <view class="alarm-item__level" :class="`level-${alarm.level}`" />
    <view class="alarm-item__content">
      <text class="alarm-title">{{ alarm.title }}</text>
      <text class="alarm-device">{{ alarm.deviceName }}</text>
      <text class="alarm-time">{{ formatTime(alarm.alarmTime) }}</text>
    </view>
    <view class="alarm-item__action">
      <ioe-button size="small" @click.stop="handleProcess">处理</ioe-button>
    </view>
  </view>
</template>
```

### 3.4 VisitorQRCode 访客二维码

```vue
<template>
  <view class="visitor-qr">
    <canvas canvas-id="qrcode" class="qr-canvas" />
    <view class="visitor-info">
      <text>访客: {{ visitor.name }}</text>
      <text>有效期: {{ visitor.expireTime }}</text>
    </view>
    <ioe-button @click="refresh">刷新二维码</ioe-button>
  </view>
</template>
```

### 3.5 VideoPlayer 视频播放器

```vue
<template>
  <view class="video-player">
    <video 
      :src="streamUrl"
      :autoplay="autoplay"
      :controls="controls"
      @error="handleError"
      @loadedmetadata="handleLoaded"
    />
    <view v-if="loading" class="loading-mask">
      <uni-icons type="spinner" />
    </view>
  </view>
</template>
```

---

## 4. 布局组件

### 4.1 IoeNavBar 导航栏

```vue
<template>
  <view class="ioe-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="navbar__left" @click="handleBack">
      <uni-icons v-if="showBack" type="back" />
    </view>
    <view class="navbar__title">{{ title }}</view>
    <view class="navbar__right">
      <slot name="right" />
    </view>
  </view>
</template>
```

### 4.2 IoeTabBar 底部导航

```vue
<template>
  <view class="ioe-tabbar">
    <view 
      v-for="item in tabs" 
      :key="item.path"
      class="tabbar-item"
      :class="{ active: current === item.path }"
      @click="switchTab(item.path)"
    >
      <uni-icons :type="item.icon" />
      <text>{{ item.text }}</text>
    </view>
  </view>
</template>
```

---

## 5. 反馈组件

### 5.1 IoeToast 轻提示

```typescript
// composables/useToast.ts
export function useToast() {
  const show = (message: string, type: 'success' | 'error' | 'warning' = 'success') => {
    uni.showToast({
      title: message,
      icon: type === 'success' ? 'success' : 'none',
      duration: 2000
    });
  };
  return { show };
}
```

### 5.2 IoeDialog 对话框

```vue
<template>
  <uni-popup ref="popup" type="dialog">
    <view class="ioe-dialog">
      <view class="dialog__title">{{ title }}</view>
      <view class="dialog__content">
        <slot />
      </view>
      <view class="dialog__footer">
        <ioe-button @click="handleCancel">取消</ioe-button>
        <ioe-button type="primary" @click="handleConfirm">确定</ioe-button>
      </view>
    </view>
  </uni-popup>
</template>
```

---

## 6. 组件使用规范

### 6.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件名 | PascalCase | `IoeButton` |
| 文件名 | kebab-case | `ioe-button.vue` |
| Props | camelCase | `showIcon` |
| Events | camelCase | `@onClick` |

### 6.2 样式规范

```scss
// 使用BEM命名
.ioe-button {
  &__icon { }
  &__text { }
  &--primary { }
  &--disabled { }
}

// 主题变量
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$danger-color: #ff4d4f;
```

---

**📝 文档维护**: IOE-DREAM架构团队
