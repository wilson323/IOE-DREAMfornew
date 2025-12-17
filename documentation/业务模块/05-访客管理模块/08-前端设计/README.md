# 访客管理模块前端设计

## 1. 前端架构

### 1.1 技术栈
- **框架**: Vue 3.4 + TypeScript
- **UI组件**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4.x
- **构建工具**: Vite 5.x
- **HTTP客户端**: Axios

### 1.2 移动端技术栈
- **框架**: uni-app 3.0
- **UI组件**: uni-ui
- **状态管理**: Pinia

## 2. 页面结构

### 2.1 Web管理端
```
访客管理/
├── 访客信息/
│   ├── 访客列表
│   ├── 访客详情
│   └── 黑名单管理
├── 预约管理/
│   ├── 预约列表
│   ├── 预约审批
│   └── 批量预约
├── 登记管理/
│   ├── 签到登记
│   ├── 在场访客
│   └── 签出管理
├── 物流管理/
│   ├── 司机管理
│   ├── 车辆管理
│   ├── 物流预约
│   └── 出门单管理
├── 通行记录/
│   ├── 记录查询
│   ├── 异常管理
│   └── 轨迹追踪
└── 统计分析/
    ├── 访客统计
    ├── 区域统计
    └── 报表管理
```

### 2.2 移动端
```
访客小程序/
├── 首页
├── 访客预约
├── 我的预约
├── 扫码签到
├── 访客签出
└── 个人中心
```

## 3. 核心组件

### 3.1 访客预约表单
```typescript
// VisitorReservationForm.vue
interface ReservationForm {
  visitorName: string;
  visitorPhone: string;
  visitorCompany: string;
  idCard: string;
  intervieweeId: number;
  visitPurpose: string;
  visitDate: string;
  startTime: string;
  endTime: string;
  visitAreaId: number;
  companionCount: number;
  vehicleNo?: string;
}
```

### 3.2 访客签到组件
```typescript
// VisitorCheckIn.vue
interface CheckInProps {
  mode: 'qrcode' | 'face' | 'manual';
  reservationId?: number;
}

// 签到流程
// 1. 扫描预约码/人脸识别
// 2. 核验预约信息
// 3. 拍照采集
// 4. 发放访客凭证
// 5. 打印访客卡
```

### 3.3 人脸识别组件
```typescript
// FaceRecognition.vue
interface FaceRecognitionProps {
  mode: 'capture' | 'verify';
  visitorId?: number;
  onSuccess: (result: FaceResult) => void;
  onError: (error: Error) => void;
}

interface FaceResult {
  matched: boolean;
  confidence: number;
  livenessResult: 'PASS' | 'FAIL';
  faceImage: string;
}
```

### 3.4 二维码扫描组件
```typescript
// QRCodeScanner.vue
interface QRScannerProps {
  onScan: (code: string) => void;
  onError: (error: Error) => void;
}
```

## 4. API接口封装

### 4.1 访客管理API
```typescript
// api/visitor.ts
export const visitorApi = {
  // 查询访客列表
  queryList: (params: VisitorQueryParams) => 
    request.post('/api/v1/visitor/query', params),
  
  // 添加访客
  add: (data: VisitorAddForm) => 
    request.post('/api/v1/visitor/add', data),
  
  // 加入黑名单
  addToBlacklist: (data: BlacklistForm) => 
    request.post('/api/v1/visitor/addToBlacklist', data),
  
  // 移出黑名单
  removeFromBlacklist: (visitorId: number) => 
    request.post('/api/v1/visitor/removeFromBlacklist', { visitorId }),
};
```

### 4.2 预约管理API
```typescript
// api/reservation.ts
export const reservationApi = {
  // 创建预约
  create: (data: ReservationForm) => 
    request.post('/api/v1/visitor/reservation/create', data),
  
  // 审批预约
  approve: (data: ApprovalForm) => 
    request.post('/api/v1/visitor/reservation/approve', data),
  
  // 取消预约
  cancel: (reservationId: number, reason: string) => 
    request.post('/api/v1/visitor/reservation/cancel', { reservationId, reason }),
  
  // 查询预约列表
  queryList: (params: ReservationQueryParams) => 
    request.post('/api/v1/visitor/reservation/query', params),
};
```

### 4.3 登记管理API
```typescript
// api/registration.ts
export const registrationApi = {
  // 访客签到
  checkIn: (data: CheckInForm) => 
    request.post('/api/v1/visitor/registration/checkIn', data),
  
  // 访客签出
  checkOut: (registrationId: number) => 
    request.post('/api/v1/visitor/registration/checkOut', { registrationId }),
  
  // 获取在场访客
  getOnsite: () => 
    request.get('/api/v1/visitor/registration/onsite'),
};
```

## 5. 状态管理

### 5.1 访客状态
```typescript
// stores/visitor.ts
export const useVisitorStore = defineStore('visitor', {
  state: () => ({
    currentVisitor: null as Visitor | null,
    onsiteVisitors: [] as Visitor[],
    statistics: null as VisitorStatistics | null,
  }),
  
  actions: {
    async fetchOnsiteVisitors() {
      const res = await registrationApi.getOnsite();
      this.onsiteVisitors = res.data;
    },
    
    async fetchStatistics(params: StatisticsParams) {
      const res = await statisticsApi.getVisitor(params);
      this.statistics = res.data;
    },
  },
});
```

## 6. 移动端组件

### 6.1 预约表单组件
```vue
<!-- pages/reservation/form.vue -->
<template>
  <view class="reservation-form">
    <uni-forms :model="formData" :rules="rules">
      <uni-forms-item label="姓名" name="name">
        <uni-easyinput v-model="formData.name" />
      </uni-forms-item>
      <uni-forms-item label="手机号" name="phone">
        <uni-easyinput v-model="formData.phone" type="number" />
      </uni-forms-item>
      <!-- 更多字段 -->
    </uni-forms>
    <button @click="submit">提交预约</button>
  </view>
</template>
```

### 6.2 扫码签到组件
```vue
<!-- pages/checkin/scan.vue -->
<template>
  <view class="scan-page">
    <camera 
      device-position="front"
      flash="off"
      @scancode="onScanCode"
    />
    <view class="tips">请将预约二维码对准扫描框</view>
  </view>
</template>
```

### 6.3 人脸采集组件
```vue
<!-- components/FaceCapture.vue -->
<template>
  <view class="face-capture">
    <camera 
      device-position="front"
      flash="off"
      @error="onCameraError"
    />
    <view class="face-frame" />
    <button @click="capture">拍照</button>
  </view>
</template>
```

## 7. 交互设计

### 7.1 预约流程
1. 填写访客信息
2. 选择被访人
3. 选择访问日期和时间
4. 确认提交
5. 等待审批
6. 收到结果通知

### 7.2 签到流程
1. 扫描预约二维码
2. 人脸识别验证
3. 确认预约信息
4. 发放访客凭证
5. 打印访客卡（可选）

### 7.3 签出流程
1. 扫描访客卡/二维码
2. 确认签出
3. 回收访客卡
4. 完成签出

## 8. 响应式设计

### 8.1 断点设置
```scss
// 移动端
@media (max-width: 767px) { }

// 平板
@media (min-width: 768px) and (max-width: 1023px) { }

// 桌面端
@media (min-width: 1024px) { }
```

### 8.2 自适应布局
- 列表页：移动端单列，桌面端表格
- 表单页：移动端全宽，桌面端居中
- 详情页：移动端垂直，桌面端左右布局

