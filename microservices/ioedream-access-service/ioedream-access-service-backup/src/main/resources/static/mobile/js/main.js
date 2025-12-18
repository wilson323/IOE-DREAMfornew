// =============================================
// 移动端主要JavaScript文件
// =============================================

// 路由配置
const routes = [
    { path: '/home', component: HomeComponent },
    { path: '/login', component: LoginComponent },
    { path: '/bluetooth', component: BluetoothComponent },
    { path: '/device', component: DeviceManagementComponent },
    { path: '/ai-analysis', component: AIAnalysisComponent },
    { path: '/video', component: VideoMonitoringComponent },
    { path: '/profile', component: PersonalCenterComponent },
    { path: '/', redirect: '/home' }
];

// Vue Router配置
const router = VueRouter.createRouter({
    history: VueRouter.createWebHistory('/mobile'),
    routes
});

// Vue应用扩展
const app = createApp({
    data() {
        return {
            // 已有的数据...
            routes: routes,
            router: router
        };
    },

    methods: {
        // 已有的方法...

        // 挂载Vue Router
        setupRouter() {
            this.$router = router;

            // 路由守卫
            router.beforeEach((to, from, next) => {
                const token = localStorage.getItem('token');

                // 登录页面
                if (to.path === '/login') {
                    if (token) {
                        next('/home');
                    } else {
                        next();
                    }
                    return;
                }

                // 其他页面需要登录
                if (!token) {
                    next('/login');
                    return;
                }

                next();
            });
        },

        // 跳转到页面
        navigateTo(path) {
            this.$router.push(path);
        },

        // 返回上一页
        goBack() {
            this.$router.go(-1);
        },

        // 获取当前路由信息
        getCurrentRoute() {
            return this.$router.currentRoute.value;
        },

        // 检查是否在指定页面
        isInPage(path) {
            return this.getCurrentRoute().path.startsWith(path);
        },

        // 获取查询参数
        getQueryParams() {
            return this.getCurrentRoute().query;
        },

        // 获取路由参数
        getRouteParams() {
            return this.getCurrentRoute().params;
        }
    }
});

// 注册Vue Router
app.use(router);

// 创建移动端组件
const HomeComponent = {
    template: `
        <div class="page-container">
            <!-- 蓝牙状态卡片 -->
            <div class="card bluetooth-status-card" :class="getBluetoothStatusClass()">
                <div class="bluetooth-status-header">
                    <div class="bluetooth-status-icon">
                        <van-icon name="phone-o" size="24" />
                    </div>
                    <div class="bluetooth-status-text">
                        <h3>蓝牙门禁</h3>
                        <p>{{ getBluetoothStatusText() }}</p>
                    </div>
                </div>

                <!-- 无感通行状态 -->
                <div v-if="seamlessAccessEnabled" class="seamless-access-card">
                    <div class="seamless-access-header">
                        <div class="seamless-access-icon">
                            <van-icon name="shield-o" size="20" />
                        </div>
                        <div class="seamless-access-status">
                            <span v-if="seamlessAccessStatus">无感通行已启用</span>
                            <span v-else>无感通行已禁用</span>
                        </div>
                    </div>
                    <div class="seamless-access-title">无感通行</div>
                    <div class="seamless-access-desc">
                        {{ seamlessAccessDesc }}
                    </div>
                    <div class="seamless-access-actions">
                        <button class="seamless-access-btn" @click="toggleSeamlessAccess">
                            {{ seamlessAccessEnabled ? '禁用' : '启用' }}
                        </button>
                        <button class="seamless-access-btn" @click="showSeamlessSettings">
                            设置
                        </button>
                    </div>
                </div>
            </div>

            <!-- 快捷操作 -->
            <div class="quick-actions">
                <div class="quick-action-btn" @click="scanBluetoothDevices">
                    <div class="quick-action-icon">
                        <van-icon name="scan" size="20" />
                    </div>
                    <span class="quick-action-text">扫描设备</span>
                </div>
                <div class="quick-action-btn" @click="showConnectedDevices">
                    <div class="quick-action-icon">
                        <van-icon name="link-o" size="20" />
                    </div>
                    <span class="quick-action-text">已连接</span>
                </div>
                <div class="quick-action-btn" @click="showAccessHistory">
                    <div class="quick-action-icon">
                        <van-icon name="history" size="20" />
                    </div>
                    <span class="quick-action-text">通行记录</span>
                </div>
                <div class="quick-action-btn" @click="showSettings">
                    <div class="quick-action-icon">
                        <van-icon name="setting-o" size="20" />
                    </div>
                    <span class="quick-action-text">设置</span>
                </div>
            </div>

            <!-- 统计信息 -->
            <div class="grid grid-2">
                <div class="stats-card">
                    <div class="stats-value">{{ todayAccessCount }}</div>
                    <div class="stats-label">今日通行</div>
                </div>
                <div class="stats-card">
                    <div class="stats-value">{{ connectedDeviceCount }}</div>
                    <div class="stats-label">已连接设备</div>
                </div>
            </div>

            <!-- 最近通行记录 -->
            <div class="card">
                <div class="card-header">
                    <span>最近通行记录</span>
                    <span @click="showAllRecords" style="color: #1989fa; font-size: 14px;">
                        查看全部 →
                    </span>
                </div>
                <div class="card-body">
                    <div v-if="recentRecords.length === 0" class="empty-container">
                        <div class="empty-icon">📱</div>
                        <div class="empty-text">暂无通行记录</div>
                        <div class="empty-desc">使用蓝牙设备或门禁卡进行通行</div>
                    </div>
                    <div v-else class="list">
                        <div v-for="record in recentRecords" :key="record.id" class="list-item">
                            <div class="list-item-icon">
                                <van-icon name="logistics" />
                            </div>
                            <div class="list-item-content">
                                <div class="list-item-title">{{ record.deviceName }}</div>
                                <div class="list-item-desc">{{ formatTime(record.accessTime) }}</div>
                            </div>
                            <div class="list-item-arrow">
                                <van-icon name="arrow" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            bluetoothEnabled: false,
            bluetoothConnected: false,
            seamlessAccessEnabled: true,
            seamlessAccessStatus: true,
            seamlessAccessDesc: '靠近门禁设备即可自动完成通行验证',
            todayAccessCount: 0,
            connectedDeviceCount: 0,
            recentRecords: []
        };
    },
    methods: {
        getBluetoothStatusClass() {
            if (!this.bluetoothEnabled) return 'bluetooth-status-disconnected';
            if (this.bluetoothConnected) return 'bluetooth-status-connected';
            return 'bluetooth-status-connecting';
        },

        getBluetoothStatusText() {
            if (!this.bluetoothEnabled) return '蓝牙未开启';
            if (this.bluetoothConnected) return '蓝牙已连接';
            return '蓝牙连接中...';
        },

        async scanBluetoothDevices() {
            try {
                this.$root.showLoading('扫描设备中...');
                const response = await this.$root.$http.post('/api/v1/mobile/bluetooth/scan', {
                    scanDuration: 30
                });

                this.$root.hideLoading();

                if (response.data.code === 200) {
                    this.$root.showNotify('扫描完成', 'success');
                    this.$root.navigateTo('/bluetooth');
                } else {
                    this.$root.showNotify(response.data.message, 'error');
                }
            } catch (error) {
                this.$root.hideLoading();
                this.$root.showNotify('扫描失败', 'error');
            }
        },

        showConnectedDevices() {
            this.$root.navigateTo('/bluetooth');
        },

        showAccessHistory() {
            this.$root.navigateTo('/profile?tab=history');
        },

        showSettings() {
            this.$root.navigateTo('/profile?tab=settings');
        },

        showAllRecords() {
            this.$root.navigateTo('/profile?tab=history');
        },

        toggleSeamlessAccess() {
            this.seamlessAccessEnabled = !this.seamlessAccessEnabled;
            this.seamlessAccessStatus = !this.seamlessAccessEnabled;
            this.$root.showNotify(
                this.seamlessAccessEnabled ? '无感通行已启用' : '无感通行已禁用',
                'success'
            );
        },

        showSeamlessSettings() {
            // 显示无感通行设置
            this.$root.showNotify('设置功能开发中', 'info');
        },

        loadHomeData() {
            this.loadTodayAccessCount();
            this.loadConnectedDevices();
            this.loadRecentRecords();
            this.checkBluetoothStatus();
        },

        async loadTodayAccessCount() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/statistics/today');
                if (response.data.code === 200) {
                    this.todayAccessCount = response.data.data.accessCount || 0;
                }
            } catch (error) {
                console.error('加载今日通行数量失败:', error);
            }
        },

        async loadConnectedDevices() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/bluetooth/devices');
                if (response.data.code === 200) {
                    this.connectedDeviceCount = response.data.data.filter(device =>
                        device.connectionStatus === 'CONNECTED'
                    ).length;
                }
            } catch (error) {
                console.error('加载连接设备数量失败:', error);
            }
        },

        async loadRecentRecords() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/record/recent?limit=5');
                if (response.data.code === 200) {
                    this.recentRecords = response.data.data || [];
                }
            } catch (error) {
                console.error('加载最近通行记录失败:', error);
            }
        },

        async checkBluetoothStatus() {
            // 检查蓝牙状态
            if (window.BluetoothAPI && BluetoothAPI.isSupported()) {
                try {
                    const enabled = await BluetoothAPI.isEnabled();
                    this.bluetoothEnabled = enabled;

                    if (enabled) {
                        // 获取已连接的设备
                        const connected = await BluetoothAPI.getConnectedDevices();
                        this.bluetoothConnected = connected.length > 0;
                    }
                } catch (error) {
                    console.error('检查蓝牙状态失败:', error);
                }
            }
        },

        formatTime(time) {
            return this.$root.formatTime(time);
        }
    },
    mounted() {
        this.loadHomeData();
        // 定期刷新数据
        setInterval(() => {
            this.loadHomeData();
        }, 30000); // 30秒刷新一次
    }
};

// 登录组件
const LoginComponent = {
    template: `
        <div class="login-container">
            <div class="login-header">
                <h1>IOE-DREAM</h1>
                <p>智慧园区一卡通管理</p>
            </div>

            <div class="login-form">
                <div class="form-group">
                    <label class="form-label">手机号</label>
                    <input
                        v-model="loginForm.phone"
                        type="tel"
                        class="form-control"
                        placeholder="请输入手机号"
                        @input="onPhoneInput"
                    />
                </div>

                <div class="form-group">
                    <label class="form-label">密码</label>
                    <input
                        v-model="loginForm.password"
                        type="password"
                        class="form-control"
                        placeholder="请输入密码"
                    />
                </div>

                <button
                    class="btn btn-primary btn-block btn-large"
                    @click="handleLogin"
                    :disabled="loading"
                >
                    {{ loading ? '登录中...' : '登录' }}
                </button>

                <div class="login-footer">
                    <span>还没有账号？</span>
                    <a href="#" @click="showRegister">立即注册</a>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            loading: false,
            loginForm: {
                phone: '',
                password: ''
            }
        };
    },
    methods: {
        onPhoneInput() {
            // 手机号格式化
            this.loginForm.phone = this.loginForm.phone.replace(/[^\d]/g, '');
        },

        async handleLogin() {
            if (!this.loginForm.phone || !this.loginForm.password) {
                this.$root.showNotify('请输入手机号和密码', 'warning');
                return;
            }

            try {
                this.loading = true;

                const response = await this.$root.$http.post('/api/v1/mobile/login', {
                    phone: this.loginForm.phone,
                    password: this.loginForm.password
                });

                if (response.data.code === 200) {
                    const { token, user } = response.data.data;
                    localStorage.setItem('token', token);
                    localStorage.setItem('user', JSON.stringify(user));

                    this.$root.showNotify('登录成功', 'success');
                    this.$root.navigateTo('/home');
                } else {
                    this.$root.showNotify(response.data.message, 'error');
                }
            } catch (error) {
                this.$root.showNotify('登录失败', 'error');
            } finally {
                this.loading = false;
            }
        },

        showRegister() {
            this.$root.showNotify('注册功能开发中', 'info');
        }
    }
};

// 蓝牙门禁组件
const BluetoothComponent = {
    template: `
        <div class="page-container">
            <!-- 蓝牙状态卡片 -->
            <div class="card bluetooth-status-card" :class="getBluetoothStatusClass()">
                <div class="bluetooth-status-header">
                    <div class="bluetooth-status-icon">
                        <van-icon name="phone-o" size="24" />
                    </div>
                    <div class="bluetooth-status-text">
                        <h3>蓝牙门禁</h3>
                        <p>{{ getBluetoothStatusText() }}</p>
                    </div>
                </div>
            </div>

            <!-- 扫描区域 -->
            <div v-if="scanning" class="card">
                <div class="scanning-animation">
                    <div class="scanning-icon">
                        <div class="scanning-circle">
                            <div class="scanning-icon-text">
                                <van-icon name="scan" size="24" />
                            </div>
                        </div>
                    </div>
                    <div class="scanning-text">正在扫描设备...</div>
                    <div class="scanning-desc">请确保蓝牙设备已开启并在附近</div>
                </div>
            </div>

            <!-- 快捷操作 -->
            <div class="quick-actions">
                <button class="quick-action-btn" @click="startScan" :disabled="scanning">
                    <div class="quick-action-icon">
                        <van-icon name="scan" size="20" />
                    </div>
                    <span class="quick-action-text">扫描设备</span>
                </button>
                <button class="quick-action-btn" @click="refreshDevices">
                    <div class="quick-action-icon">
                        <van-icon name="replay" size="20" />
                    </div>
                    <span class="quick-action-text">刷新</span>
                </button>
                <button class="quick-action-btn" @click="showSeamlessAccess">
                    <div class="quick-action-icon">
                        <van-icon name="shield-o" size="20" />
                    </div>
                    <span class="quick-action-text">无感通行</span>
                </button>
                <button class="quick-action-btn" @click="showSettings">
                    <div class="quick-action-icon">
                        <van-icon name="setting-o" size="20" />
                    </div>
                    <span class="quick-action-text">设置</span>
                </button>
            </div>

            <!-- 设备列表 -->
            <div class="device-list">
                <div class="device-list-header">
                    <span class="device-list-title">设备列表</span>
                    <div class="device-list-actions">
                        <span @click="showFilter">筛选</span>
                        <span @click="sortBySignal">信号</span>
                    </div>
                </div>

                <div v-if="devices.length === 0" class="empty-container">
                    <div class="empty-icon">📱</div>
                    <div class="empty-text">未发现设备</div>
                    <div class="empty-desc">点击"扫描设备"按钮开始搜索附近的蓝牙设备</div>
                </div>

                <div v-else>
                    <div
                        v-for="device in devices"
                        :key="device.deviceId"
                        class="device-item"
                        :class="getDeviceItemClass(device)"
                        @click="connectDevice(device)"
                    >
                        <div class="device-icon" :class="getDeviceIconClass(device.deviceType)">
                            <van-icon :name="getDeviceIcon(device.deviceType)" size="20" />
                        </div>
                        <div class="device-info">
                            <div class="device-name">
                                {{ device.deviceName }}
                                <div class="device-signal">
                                    <div class="signal-bars" :class="getSignalClass(device.signalStrength)">
                                        <div class="signal-bar"></div>
                                        <div class="signal-bar"></div>
                                        <div class="signal-bar"></div>
                                        <div class="signal-bar"></div>
                                    </div>
                                </div>
                            </div>
                            <div class="device-desc">{{ device.deviceType }} - {{ device.deviceCode }}</div>
                            <div class="device-meta">
                                <div class="device-meta-item">
                                    <van-icon name="location-o" size="12" />
                                    <span>{{ device.areaName || '未知区域' }}</span>
                                </div>
                                <div class="device-meta-item">
                                    <van-icon name="clock-o" size="12" />
                                    <span>{{ getDeviceLastSeen(device) }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="device-status" :class="device.connectionStatus.toLowerCase()">
                            {{ getConnectionStatusText(device.connectionStatus) }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            scanning: false,
            devices: [],
            bluetoothEnabled: false,
            bluetoothConnected: false
        };
    },
    methods: {
        getBluetoothStatusClass() {
            if (!this.bluetoothEnabled) return 'bluetooth-status-disconnected';
            if (this.bluetoothConnected) return 'bluetooth-status-connected';
            return 'bluetooth-status-connecting';
        },

        getBluetoothStatusText() {
            if (!this.bluetoothEnabled) return '蓝牙未开启';
            if (this.bluetoothConnected) return '蓝牙已连接';
            return '蓝牙连接中...';
        },

        getDeviceItemClass(device) {
            return device.connectionStatus.toLowerCase();
        },

        getDeviceIconClass(deviceType) {
            const iconMap = {
                'SMART_LOCK': 'smart-lock',
                'ACCESS_CONTROL': 'access-control',
                'DOOR_LOCK': 'smart-lock'
            };
            return iconMap[deviceType] || '';
        },

        getDeviceIcon(deviceType) {
            const iconMap = {
                'SMART_LOCK': 'lock',
                'ACCESS_CONTROL': 'door-o',
                'DOOR_LOCK': 'lock'
            };
            return iconMap[deviceType] || 'phone-o';
        },

        getSignalClass(signal) {
            if (signal >= 80) return 'signal-strong';
            if (signal >= 50) return 'signal-medium';
            return 'signal-weak';
        },

        getConnectionStatusText(status) {
            const statusMap = {
                'CONNECTED': '已连接',
                'CONNECTING': '连接中',
                'DISCONNECTED': '未连接',
                'FAILED': '连接失败'
            };
            return statusMap[status] || '未知状态';
        },

        getDeviceLastSeen(device) {
            const now = new Date();
            const lastSeen = new Date(device.lastSeen);
            const diff = now - lastSeen;

            if (diff < 60000) return '刚刚';
            if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
            if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
            return '很久之前';
        },

        async startScan() {
            try {
                this.scanning = true;
                this.$root.showLoading('扫描设备中...');

                const response = await this.$root.$http.post('/api/v1/mobile/bluetooth/scan', {
                    scanDuration: 30,
                    deviceTypes: ['SMART_LOCK', 'ACCESS_CONTROL', 'DOOR_LOCK'],
                    signalStrengthThreshold: -80
                });

                if (response.data.code === 200) {
                    this.devices = response.data.data || [];
                    this.$root.showNotify(`发现 ${this.devices.length} 个设备`, 'success');
                } else {
                    this.$root.showNotify(response.data.message, 'error');
                }
            } catch (error) {
                this.$root.showNotify('扫描失败', 'error');
            } finally {
                this.scanning = false;
                this.$root.hideLoading();
            }
        },

        async refreshDevices() {
            await this.startScan();
        },

        async connectDevice(device) {
            try {
                this.$root.showLoading('连接设备中...');

                const response = await this.$root.$http.post('/api/v1/mobile/bluetooth/connect', {
                    deviceId: device.deviceId,
                    deviceCode: device.deviceCode,
                    connectionType: 'BLUETOOTH'
                });

                if (response.data.code === 200) {
                    this.$root.showNotify('设备连接成功', 'success');
                    // 更新设备状态
                    const index = this.devices.findIndex(d => d.deviceId === device.deviceId);
                    if (index !== -1) {
                        this.devices[index].connectionStatus = 'CONNECTED';
                    }
                } else {
                    this.$root.showNotify(response.data.message, 'error');
                }
            } catch (error) {
                this.$root.showNotify('连接失败', 'error');
            } finally {
                this.$root.hideLoading();
            }
        },

        showSeamlessAccess() {
            this.$root.showNotify('无感通行功能开发中', 'info');
        },

        showSettings() {
            this.$root.showNotify('设置功能开发中', 'info');
        },

        showFilter() {
            this.$root.showNotify('筛选功能开发中', 'info');
        },

        sortBySignal() {
            this.devices.sort((a, b) => b.signalStrength - a.signalStrength);
        },

        async checkBluetoothStatus() {
            if (window.BluetoothAPI && BluetoothAPI.isSupported()) {
                try {
                    const enabled = await BluetoothAPI.isEnabled();
                    this.bluetoothEnabled = enabled;

                    if (enabled) {
                        const connected = await BluetoothAPI.getConnectedDevices();
                        this.bluetoothConnected = connected.length > 0;
                    }
                } catch (error) {
                    console.error('检查蓝牙状态失败:', error);
                }
            }
        },

        loadDevices() {
            this.startScan();
        }
    },
    mounted() {
        this.checkBluetoothStatus();
        this.loadDevices();
    }
};

// AI分析组件
const AIAnalysisComponent = {
    template: `
        <div class="page-container">
            <!-- AI分析头部卡片 -->
            <div class="card ai-analysis-header">
                <div class="ai-analysis-header-content">
                    <div class="ai-analysis-title">
                        <van-icon name="chart-trending-o" size="24" />
                        智能分析
                    </div>
                    <div class="ai-analysis-desc">
                        基于AI算法的智能行为分析和异常检测
                    </div>
                </div>
            </div>

            <!-- AI统计 -->
            <div class="ai-stats-grid">
                <div class="ai-stat-card anomalies">
                    <div class="ai-stat-value">
                        <span>{{ todayAnomalies }}</span>
                        <span class="ai-stat-unit">个</span>
                    </div>
                    <div class="ai-stat-trend">
                        <van-icon name="arrow-up" />
                        <span class="ai-stat-trend up">12%</span>
                    </div>
                    <div class="ai-stat-label">今日异常</div>
                </div>
                <div class="ai-stat-card risk">
                    <div class="ai-stat-value">
                        <span>{{ riskScore }}</span>
                        <span class="ai-stat-unit">分</span>
                    </div>
                    <div class="ai-stat-trend">
                        <van-icon name="arrow-down" />
                        <span class="ai-stat-trend down">5%</span>
                    </div>
                    <div class="ai-stat-label">风险评分</div>
                </div>
                <div class="ai-stat-card accuracy">
                    <div class="ai-stat-value">
                        <span>{{ accuracy }}%</span>
                    </div>
                    <div class="ai-stat-trend">
                        <van-icon name="arrow-up" />
                        <span class="ai-stat-trend up">3%</span>
                    </div>
                    <div class="ai-stat-label">识别准确率</div>
                </div>
                <div class="ai-stat-card efficiency">
                    <div class="ai-stat-value">
                        <span>{{ efficiency }}%</span>
                    </div>
                    <div class="ai-stat-trend">
                        <van-icon name="minus" />
                        <span class="ai-stat-trend">0%</span>
                    </div>
                    <div class="ai-stat-label">处理效率</div>
                </div>
            </div>

            <!-- 时间范围选择 -->
            <div class="time-range-selector">
                <div class="time-range-selector-header">分析时间范围</div>
                <div class="time-range-options">
                    <div
                        v-for="range in timeRanges"
                        :key="range.value"
                        class="time-range-option"
                        :class="{ active: selectedTimeRange === range.value }"
                        @click="selectTimeRange(range.value)"
                    >
                        {{ range.label }}
                    </div>
                </div>
            </div>

            <!-- 行为分析 -->
            <div class="behavior-analysis-card">
                <div class="behavior-analysis-header">
                    <span class="behavior-analysis-title">行为分析</span>
                    <div class="behavior-analysis-actions">
                        <span class="behavior-analysis-action" @click="refreshBehaviorAnalysis">刷新</span>
                    </div>
                </div>
                <div class="behavior-analysis-body">
                    <div v-for="pattern in behaviorPatterns" :key="pattern.id" class="behavior-pattern">
                        <div class="behavior-pattern-header">
                            <div class="behavior-pattern-title">
                                <van-icon :name="pattern.icon" size="16" />
                                {{ pattern.name }}
                            </div>
                            <div class="behavior-pattern-score" :class="pattern.scoreClass">
                                <van-icon name="fire" size="12" />
                                {{ pattern.score }}分
                            </div>
                        </div>
                        <div class="behavior-pattern-chart">
                            <div class="behavior-chart-line"></div>
                            <div
                                v-for="point in pattern.dataPoints"
                                :key="point.time"
                                class="behavior-chart-dot"
                                :style="{ left: point.position + '%', top: point.level + '%' }"
                            ></div>
                        </div>
                        <div class="behavior-pattern-stats">
                            <div class="behavior-pattern-stat">
                                <div class="behavior-pattern-stat-value">{{ pattern.count }}</div>
                                <div class="behavior-pattern-stat-label">检测次数</div>
                            </div>
                            <div class="behavior-pattern-stat">
                                <div class="behavior-pattern-stat-value">{{ pattern.accuracy }}%</div>
                                <div class="behavior-pattern-stat-label">准确率</div>
                            </div>
                            <div class="behavior-pattern-stat">
                                <div class="behavior-pattern-stat-value">{{ pattern.avgDuration }}s</div>
                                <div class="behavior-pattern-stat-label">平均时长</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 异常检测 -->
            <div class="anomaly-detection-card">
                <div class="anomaly-detection-header">
                    <div class="anomaly-detection-title">
                        <van-icon name="warning-o" size="16" />
                        异常检测
                    </div>
                    <div class="anomaly-detection-actions">
                        <span class="anomaly-detection-action" @click="viewAllAnomalies">查看全部</span>
                    </div>
                </div>
                <div class="anomaly-detection-body">
                    <div v-if="anomalies.length === 0" class="empty-container">
                        <div class="empty-icon">🔍</div>
                        <div class="empty-text">暂无异常检测</div>
                        <div class="empty-desc">系统运行正常，未检测到异常行为</div>
                    </div>
                    <div v-else>
                        <div v-for="anomaly in anomalies" :key="anomaly.id" class="anomaly-item" :class="getAnomalyRiskClass(anomaly.riskLevel)">
                            <div class="anomaly-item-icon" :class="getAnomalyRiskClass(anomaly.riskLevel)">
                                <van-icon :name="getAnomalyIcon(anomaly.type)" size="16" />
                            </div>
                            <div class="anomaly-item-content">
                                <div class="anomaly-item-title">{{ anomaly.title }}</div>
                                <div class="anomaly-item-desc">{{ anomaly.description }}</div>
                                <div class="anomaly-item-time">
                                    <van-icon name="clock-o" size="12" />
                                    {{ formatTime(anomaly.detectedTime) }}
                                </div>
                            </div>
                            <div class="anomaly-item-actions">
                                <button class="anomaly-action-btn" @click="viewAnomalyDetail(anomaly)">详情</button>
                                <button class="anomaly-action-btn" @click="handleAnomaly(anomaly)">处理</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 风险评估 -->
            <div class="risk-assessment-card">
                <div class="risk-assessment-header">风险评估</div>
                <div class="risk-score-display">
                    <div class="risk-score-circle">
                        <svg width="120" height="120">
                            <circle class="risk-score-circle-bg" cx="60" cy="60" r="52" />
                            <circle
                                class="risk-score-circle-progress"
                                :class="getRiskScoreClass(riskScore)"
                                cx="60" cy="60" r="52"
                                :stroke-dasharray="getCircleProgress(riskScore)"
                                stroke-dashoffset="0"
                            />
                        </svg>
                        <div class="risk-score-circle-text">{{ riskScore }}</div>
                    </div>
                    <div class="risk-score-details">
                        <div class="risk-score-level" :class="getRiskScoreClass(riskScore)">
                            {{ getRiskLevelText(riskScore) }}
                        </div>
                        <div class="risk-score-description">{{ getRiskDescription(riskScore) }}</div>
                    </div>
                </div>
                <div class="risk-factors">
                    <div v-for="factor in riskFactors" :key="factor.name" class="risk-factor-item">
                        <span class="risk-factor-name">{{ factor.name }}</span>
                        <span class="risk-factor-score" :class="factor.levelClass">{{ factor.score }}分</span>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            todayAnomalies: 3,
            riskScore: 85,
            accuracy: 95,
            efficiency: 88,
            selectedTimeRange: 'today',
            timeRanges: [
                { value: 'today', label: '今日' },
                { value: 'week', label: '本周' },
                { value: 'month', label: '本月' },
                { value: 'quarter', label: '本季度' }
            ],
            behaviorPatterns: [],
            anomalies: [],
            riskFactors: []
        };
    },
    methods: {
        selectTimeRange(range) {
            this.selectedTimeRange = range;
            this.loadAIAnalysisData();
        },

        refreshBehaviorAnalysis() {
            this.loadBehaviorPatterns();
        },

        viewAllAnomalies() {
            this.$root.navigateTo('/profile?tab=alerts');
        },

        getAnomalyRiskClass(riskLevel) {
            const classMap = {
                'HIGH': 'high-risk',
                'MEDIUM': 'medium-risk',
                'LOW': 'low-risk'
            };
            return classMap[riskLevel] || '';
        },

        getAnomalyIcon(type) {
            const iconMap = {
                'UNAUTHORIZED_ACCESS': 'warning-o',
                'STRANGE_BEHAVIOR': 'info-o',
                'SYSTEM_ANOMALY': 'setting-o',
                'PERFORMANCE_ISSUE': 'chart-trending-o'
            };
            return iconMap[type] || 'warning-o';
        },

        getRiskScoreClass(score) {
            if (score >= 90) return 'low';
            if (score >= 70) return 'medium';
            return 'high';
        },

        getRiskLevelText(score) {
            if (score >= 90) return '低风险';
            if (score >= 70) return '中等风险';
            return '高风险';
        },

        getRiskDescription(score) {
            if (score >= 90) return '系统运行状态良好，风险较低';
            if (score >= 70) return '系统存在一定风险，需要关注';
            return '系统风险较高，建议立即处理';
        },

        getCircleProgress(score) {
            const circumference = 2 * Math.PI * 52;
            const progress = (score / 100) * circumference;
            return `${progress} ${circumference}`;
        },

        formatTime(time) {
            return this.$root.formatTime(time);
        },

        viewAnomalyDetail(anomaly) {
            this.$root.showNotify('查看异常详情功能开发中', 'info');
        },

        handleAnomaly(anomaly) {
            this.$root.showNotify('处理异常功能开发中', 'info');
        },

        async loadAIAnalysisData() {
            await Promise.all([
                this.loadBehaviorPatterns(),
                this.loadAnomalies(),
                this.loadRiskFactors()
            ]);
        },

        async loadBehaviorPatterns() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/ai/behavior-patterns', {
                    params: { timeRange: this.selectedTimeRange }
                });
                if (response.data.code === 200) {
                    this.behaviorPatterns = response.data.data || [];
                }
            } catch (error) {
                console.error('加载行为模式失败:', error);
                // 模拟数据
                this.behaviorPatterns = [
                    {
                        id: 1,
                        name: '正常通行',
                        icon: 'logistics',
                        score: 95,
                        scoreClass: 'high',
                        count: 156,
                        accuracy: 98,
                        avgDuration: 2.5,
                        dataPoints: [
                            { time: '08:00', position: 10, level: 30 },
                            { time: '12:00', position: 50, level: 40 },
                            { time: '18:00', position: 90, level: 25 }
                        ]
                    },
                    {
                        id: 2,
                        name: '异常徘徊',
                        icon: 'warning-o',
                        score: 78,
                        scoreClass: 'medium',
                        count: 3,
                        accuracy: 85,
                        avgDuration: 15.2,
                        dataPoints: [
                            { time: '14:00', position: 25, level: 60 },
                            { time: '14:30', position: 45, level: 70 },
                            { time: '15:00', position: 75, level: 65 }
                        ]
                    }
                ];
            }
        },

        async loadAnomalies() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/ai/anomalies', {
                    params: { timeRange: this.selectedTimeRange, limit: 5 }
                });
                if (response.data.code === 200) {
                    this.anomalies = response.data.data || [];
                }
            } catch (error) {
                console.error('加载异常数据失败:', error);
                // 模拟数据
                this.anomalies = [
                    {
                        id: 1,
                        title: '未授权访问尝试',
                        description: '检测到未授权人员在东门尝试刷卡通行',
                        type: 'UNAUTHORIZED_ACCESS',
                        riskLevel: 'HIGH',
                        detectedTime: new Date(Date.now() - 3600000).toISOString()
                    },
                    {
                        id: 2,
                        title: '设备异常离线',
                        description: '3号门禁控制器意外断开连接',
                        type: 'SYSTEM_ANOMALY',
                        riskLevel: 'MEDIUM',
                        detectedTime: new Date(Date.now() - 7200000).toISOString()
                    }
                ];
            }
        },

        async loadRiskFactors() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/ai/risk-factors');
                if (response.data.code === 200) {
                    this.riskFactors = response.data.data || [];
                }
            } catch (error) {
                console.error('加载风险因素失败:', error);
                // 模拟数据
                this.riskFactors = [
                    {
                        name: '设备状态',
                        score: 88,
                        levelClass: 'low'
                    },
                    {
                        name: '网络连接',
                        score: 92,
                        levelClass: 'low'
                    },
                    {
                        name: '系统性能',
                        score: 75,
                        levelClass: 'medium'
                    },
                    {
                        name: '安全防护',
                        score: 95,
                        levelClass: 'low'
                    }
                ];
            }
        }
    },
    mounted() {
        this.loadAIAnalysisData();
    }
};

// 视频监控组件
const VideoMonitoringComponent = {
    template: `
        <div class="page-container">
            <!-- 视频监控头部 -->
            <div class="video-monitor-header">
                <div class="video-monitor-content">
                    <div class="video-monitor-title">
                        <van-icon name="video-o" size="24" />
                        视频监控
                    </div>
                    <div class="video-monitor-desc">
                        实时视频监控和智能录像回放
                    </div>
                </div>
            </div>

            <!-- 视频网格布局 -->
            <div class="video-grid" :class="getVideoGridClass()">
                <div v-for="video in videos" :key="video.id" class="video-player-card">
                    <div class="video-player-container" @click="enterFullscreen(video)">
                        <video
                            class="video-player"
                            :src="video.streamUrl"
                            :poster="video.posterUrl"
                            :muted="video.muted"
                            :loop="video.loop"
                            playsinline
                            @click="togglePlay(video)"
                        ></video>
                        <div class="video-player-overlay">
                            <div class="video-player-header">
                                <div class="video-player-title">{{ video.name }}</div>
                                <div class="video-player-status">
                                    <div class="video-status-dot" :class="getVideoStatusClass(video.status)"></div>
                                    <span>{{ getVideoStatusText(video.status) }}</span>
                                </div>
                            </div>
                            <div class="video-player-controls">
                                <div class="video-control-btn" @click.stop="togglePlay(video)">
                                    <van-icon :name="video.playing ? 'pause' : 'play'" />
                                </div>
                                <div class="video-control-btn" @click.stop="toggleMute(video)">
                                    <van-icon :name="video.muted ? 'volume-off' : 'volume'" />
                                </div>
                                <div class="video-control-btn" @click.stop="toggleRecord(video)">
                                    <van-icon name="photo" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 视频控制面板 -->
            <div class="video-control-panel">
                <div class="video-control-header">
                    <span class="video-control-title">监控控制</span>
                    <div class="video-control-actions">
                        <span class="video-control-btn-text" @click="switchLayout">切换布局</span>
                        <span class="video-control-btn-text" @click="showDeviceList">设备列表</span>
                    </div>
                </div>

                <!-- PTZ控制 -->
                <div class="ptz-control">
                    <div class="ptz-btn" @click="ptzControl('up')">
                        <van-icon name="arrow-up" size="20" class="ptz-btn-icon" />
                        <span class="ptz-btn-text">上</span>
                    </div>
                    <div class="ptz-center">
                        <div class="ptz-btn" @click="ptzControl('left')">
                            <van-icon name="arrow-left" size="20" class="ptz-btn-icon" />
                            <span class="ptz-btn-text">左</span>
                        </div>
                        <div class="ptz-btn" @click="ptzControl('home')">
                            <van-icon name="home-o" size="20" class="ptz-btn-icon" />
                            <span class="ptz-btn-text">复位</span>
                        </div>
                        <div class="ptz-btn" @click="ptzControl('right')">
                            <van-icon name="arrow" size="20" class="ptz-btn-icon" />
                            <span class="ptz-btn-text">右</span>
                        </div>
                    </div>
                    <div class="ptz-btn" @click="ptzControl('down')">
                        <van-icon name="arrow-down" size="20" class="ptz-btn-icon" />
                        <span class="ptz-btn-text">下</span>
                    </div>
                </div>

                <div class="ptz-zoom">
                    <div class="ptz-btn" @click="ptzControl('zoomIn')">
                        <van-icon name="plus" size="20" class="ptz-btn-icon" />
                        <span class="ptz-btn-text">放大</span>
                    </div>
                    <div class="ptz-btn" @click="ptzControl('zoomOut')">
                        <van-icon name="minus" size="20" class="ptz-btn-icon" />
                        <span class="ptz-btn-text">缩小</span>
                    </div>
                </div>

                <!-- 视频质量选择 -->
                <div class="video-quality-selector">
                    <div
                        v-for="quality in videoQualities"
                        :key="quality.value"
                        class="quality-option"
                        :class="{ active: selectedQuality === quality.value }"
                        @click="selectQuality(quality.value)"
                    >
                        {{ quality.label }}
                    </div>
                </div>

                <!-- 录像控制 -->
                <div class="recording-control">
                    <div class="recording-status">
                        <div class="recording-indicator"></div>
                        <span class="recording-text">录像中</span>
                        <span class="recording-time">{{ formatRecordingTime() }}</span>
                    </div>
                    <div class="recording-actions">
                        <button class="recording-btn" @click="stopRecording">停止录像</button>
                        <button class="recording-btn" @click="takeSnapshot">截图</button>
                    </div>
                </div>
            </div>

            <!-- AI检测面板 -->
            <div class="ai-detection-panel">
                <div class="ai-detection-header">
                    <div class="ai-detection-title">
                        <van-icon name="eye-o" size="16" />
                        AI智能检测
                    </div>
                    <div class="ai-detection-status" :class="{ disabled: !aiDetectionEnabled }">
                        {{ aiDetectionEnabled ? '已启用' : '已禁用' }}
                    </div>
                </div>
                <div class="detection-options">
                    <div class="detection-option">
                        <div class="detection-option-label">
                            <van-icon name="user-circle-o" size="16" class="detection-option-icon" />
                            人脸识别
                        </div>
                        <van-switch v-model="detectionOptions.faceRecognition" size="16" />
                    </div>
                    <div class="detection-option">
                        <div class="detection-option-label">
                            <van-icon name="warning-o" size="16" class="detection-option-icon" />
                            异常行为检测
                        </div>
                        <van-switch v-model="detectionOptions.behaviorDetection" size="16" />
                    </div>
                    <div class="detection-option">
                        <div class="detection-option-label">
                            <van-icon name="shield-o" size="16" class="detection-option-icon" />
                            入侵检测
                        </div>
                        <van-switch v-model="detectionOptions.intrusionDetection" size="16" />
                    </div>
                    <div class="detection-option">
                        <div class="detection-option-label">
                            <van-icon name="photo-o" size="16" class="detection-option-icon" />
                            车辆识别
                        </div>
                        <van-switch v-model="detectionOptions.vehicleDetection" size="16" />
                    </div>
                </div>
            </div>

            <!-- 告警列表 -->
            <div class="alert-list">
                <div class="alert-header">
                    <span class="alert-title">实时告警</span>
                    <div class="alert-actions">
                        <span @click="clearAlerts">清空</span>
                    </div>
                </div>
                <div v-if="alerts.length === 0" class="empty-container">
                    <div class="empty-icon">🔔</div>
                    <div class="empty-text">暂无告警信息</div>
                    <div class="empty-desc">系统运行正常，未产生告警</div>
                </div>
                <div v-else>
                    <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="alert.level.toLowerCase()">
                        <div class="alert-content">
                            <div class="alert-icon" :class="alert.level.toLowerCase()">
                                <van-icon :name="getAlertIcon(alert.type)" size="16" />
                            </div>
                            <div class="alert-info">
                                <div class="alert-title">{{ alert.title }}</div>
                                <div class="alert-desc">{{ alert.description }}</div>
                                <div class="alert-meta">
                                    <div class="alert-time">
                                        <van-icon name="clock-o" size="12" />
                                        {{ formatTime(alert.timestamp) }}
                                    </div>
                                    <div class="alert-actions">
                                        <button class="alert-action-btn" @click="viewAlert(alert)">查看</button>
                                        <button class="alert-action-btn" @click="handleAlert(alert)">处理</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            videoLayout: 'quad',
            videos: [],
            videoQualities: [
                { value: 'high', label: '高清' },
                { value: 'medium', label: '标清' },
                { value: 'low', label: '流畅' }
            ],
            selectedQuality: 'high',
            recording: false,
            recordingStartTime: null,
            aiDetectionEnabled: true,
            detectionOptions: {
                faceRecognition: true,
                behaviorDetection: true,
                intrusionDetection: false,
                vehicleDetection: false
            },
            alerts: []
        };
    },
    methods: {
        getVideoGridClass() {
            return this.videoLayout;
        },

        getVideoStatusClass(status) {
            return status.toLowerCase();
        },

        getVideoStatusText(status) {
            const statusMap = {
                'ONLINE': '在线',
                'OFFLINE': '离线',
                'RECORDING': '录像中',
                'ERROR': '错误'
            };
            return statusMap[status] || '未知';
        },

        togglePlay(video) {
            video.playing = !video.playing;
            if (video.playing) {
                video.element.play();
            } else {
                video.element.pause();
            }
        },

        toggleMute(video) {
            video.muted = !video.muted;
            video.element.muted = video.muted;
        },

        toggleRecord(video) {
            this.$root.showNotify('截图功能开发中', 'info');
        },

        enterFullscreen(video) {
            this.$root.showNotify('全屏播放功能开发中', 'info');
        },

        switchLayout() {
            const layouts = ['single', 'quad', 'nine'];
            const currentIndex = layouts.indexOf(this.videoLayout);
            this.videoLayout = layouts[(currentIndex + 1) % layouts.length];
        },

        showDeviceList() {
            this.$root.navigateTo('/profile?tab=devices');
        },

        ptzControl(direction) {
            this.$root.showNotify(`PTZ控制：${direction}`, 'info');
        },

        selectQuality(quality) {
            this.selectedQuality = quality;
            this.$root.showNotify(`切换到${this.getQualityLabel(quality)}`, 'success');
        },

        getQualityLabel(quality) {
            const qualityMap = {
                'high': '高清',
                'medium': '标清',
                'low': '流畅'
            };
            return qualityMap[quality] || quality;
        },

        stopRecording() {
            this.recording = false;
            this.recordingStartTime = null;
            this.$root.showNotify('录像已停止', 'success');
        },

        takeSnapshot() {
            this.$root.showNotify('截图已保存', 'success');
        },

        formatRecordingTime() {
            if (!this.recordingStartTime) return '00:00:00';
            const now = Date.now();
            const diff = now - this.recordingStartTime;
            const hours = Math.floor(diff / 3600000);
            const minutes = Math.floor((diff % 3600000) / 60000);
            const seconds = Math.floor((diff % 60000) / 1000);
            return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        },

        clearAlerts() {
            this.alerts = [];
            this.$root.showNotify('告警已清空', 'success');
        },

        getAlertIcon(type) {
            const iconMap = {
                'FACE_DETECTION': 'user-circle-o',
                'INTRUSION': 'warning-o',
                'MOTION_DETECTION': 'photograph',
                'VEHICLE_DETECTION': 'logistics'
            };
            return iconMap[type] || 'warning-o';
        },

        viewAlert(alert) {
            this.$root.showNotify('查看告警详情功能开发中', 'info');
        },

        handleAlert(alert) {
            this.$root.showNotify('处理告警功能开发中', 'info');
        },

        formatTime(time) {
            return this.$root.formatTime(time);
        },

        async loadVideos() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/video/devices');
                if (response.data.code === 200) {
                    this.videos = response.data.data || [];
                }
            } catch (error) {
                console.error('加载视频设备失败:', error);
                // 模拟数据
                this.videos = [
                    {
                        id: 1,
                        name: '主入口',
                        streamUrl: 'rtsp://example.com/stream1',
                        posterUrl: '/images/video-poster-1.jpg',
                        status: 'ONLINE',
                        playing: false,
                        muted: false,
                        loop: true
                    },
                    {
                        id: 2,
                        name: '侧门',
                        streamUrl: 'rtsp://example.com/stream2',
                        posterUrl: '/images/video-poster-2.jpg',
                        status: 'ONLINE',
                        playing: false,
                        muted: true,
                        loop: true
                    },
                    {
                        id: 3,
                        name: '后门',
                        streamUrl: 'rtsp://example.com/stream3',
                        posterUrl: '/images/video-poster-3.jpg',
                        status: 'RECORDING',
                        playing: false,
                        muted: true,
                        loop: true
                    },
                    {
                        id: 4,
                        name: '停车场',
                        streamUrl: 'rtsp://example.com/stream4',
                        posterUrl: '/images/video-poster-4.jpg',
                        status: 'ONLINE',
                        playing: false,
                        muted: true,
                        loop: true
                    }
                ];
            }
        },

        async loadAlerts() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/video/alerts');
                if (response.data.code === 200) {
                    this.alerts = response.data.data || [];
                }
            } catch (error) {
                console.error('加载告警数据失败:', error);
                // 模拟数据
                this.alerts = [
                    {
                        id: 1,
                        title: '人脸识别成功',
                        description: '主入口检测到授权用户通过',
                        type: 'FACE_DETECTION',
                        level: 'LOW',
                        timestamp: new Date(Date.now() - 300000).toISOString()
                    },
                    {
                        id: 2,
                        title: '异常徘徊检测',
                        description: '侧门检测到可疑人员长时间徘徊',
                        type: 'INTRUSION',
                        level: 'MEDIUM',
                        timestamp: new Date(Date.now() - 600000).toISOString()
                    }
                ];
            }
        }
    },
    mounted() {
        this.loadVideos();
        this.loadAlerts();

        // 模拟开始录像
        this.recording = true;
        this.recordingStartTime = Date.now() - 45000; // 45秒前开始
    }
};

// 个人中心组件
const PersonalCenterComponent = {
    template: `
        <div class="page-container">
            <!-- 个人信息头部 -->
            <div class="profile-header">
                <div class="profile-content">
                    <div class="profile-avatar">
                        <van-icon name="user-circle-o" size="32" />
                    </div>
                    <div class="profile-info">
                        <div class="profile-name">{{ userInfo.userName || '用户' }}</div>
                        <div class="profile-role">{{ userInfo.roleName || '普通用户' }}</div>
                        <div class="profile-department">{{ userInfo.departmentName || '技术部' }}</div>
                    </div>
                    <div class="profile-status">
                        <div class="profile-status-dot"></div>
                        <span>在线</span>
                    </div>
                </div>
            </div>

            <!-- 用户统计 -->
            <div class="user-stats-grid">
                <div class="user-stat-card">
                    <div class="user-stat-value">{{ userStats.totalAccess }}</div>
                    <div class="user-stat-label">总通行次数</div>
                </div>
                <div class="user-stat-card">
                    <div class="user-stat-value">{{ userStats.thisMonth }}</div>
                    <div class="user-stat-label">本月通行</div>
                </div>
                <div class="user-stat-card">
                    <div class="user-stat-value">{{ userStats.deviceCount }}</div>
                    <div class="user-stat-label">绑定设备</div>
                </div>
            </div>

            <!-- 功能菜单 -->
            <div class="menu-list">
                <div class="menu-group">
                    <div class="menu-group-title">门禁管理</div>
                    <div class="menu-item" @click="navigateTo('/bluetooth')">
                        <div class="menu-item-icon">
                            <van-icon name="phone-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">蓝牙门禁</div>
                            <div class="menu-item-desc">管理蓝牙设备连接</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                    <div class="menu-item" @click="navigateTo('/profile?tab=history')">
                        <div class="menu-item-icon">
                            <van-icon name="history" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">通行记录</div>
                            <div class="menu-item-desc">查看历史通行记录</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                    <div class="menu-item" @click="navigateTo('/profile?tab=devices')">
                        <div class="menu-item-icon">
                            <van-icon name="phone-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">我的设备</div>
                            <div class="menu-item-desc">管理已绑定的设备</div>
                        </div>
                        <div class="menu-item-badge" v-if="userStats.deviceCount > 0">
                            {{ userStats.deviceCount }}
                        </div>
                    </div>
                </div>

                <div class="menu-group">
                    <div class="menu-group-title">系统功能</div>
                    <div class="menu-item" @click="navigateTo('/ai-analysis')">
                        <div class="menu-item-icon">
                            <van-icon name="chart-trending-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">AI分析</div>
                            <div class="menu-item-desc">智能行为分析和异常检测</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                    <div class="menu-item" @click="navigateTo('/video')">
                        <div class="menu-item-icon">
                            <van-icon name="video-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">视频监控</div>
                            <div class="menu-item-desc">实时监控和录像回放</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                    <div class="menu-item" @click="navigateTo('/profile?tab=offline')">
                        <div class="menu-item-icon">
                            <van-icon name="download" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">离线模式</div>
                            <div class="menu-item-desc">离线数据同步管理</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                </div>

                <div class="menu-group">
                    <div class="menu-group-title">个人设置</div>
                    <div class="menu-item" @click="showNotificationSettings">
                        <div class="menu-item-icon">
                            <van-icon name="bell" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">通知设置</div>
                            <div class="menu-item-desc">消息推送和提醒配置</div>
                        </div>
                        <div class="menu-item-badge new">2</div>
                    </div>
                    <div class="menu-item" @click="showPrivacySettings">
                        <div class="menu-item-icon">
                            <van-icon name="shield-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">隐私设置</div>
                            <div class="menu-item-desc">数据隐私和安全配置</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                    <div class="menu-item" @click="showAbout">
                        <div class="menu-item-icon">
                            <van-icon name="info-o" />
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-item-title">关于我们</div>
                            <div class="menu-item-desc">应用信息和帮助文档</div>
                        </div>
                        <div class="menu-item-arrow">
                            <van-icon name="arrow" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 退出登录 -->
            <div class="logout-section">
                <button class="logout-btn" @click="logout">退出登录</button>
            </div>
        </div>
    `,
    data() {
        return {
            userInfo: {},
            userStats: {
                totalAccess: 0,
                thisMonth: 0,
                deviceCount: 0
            }
        };
    },
    methods: {
        navigateTo(path) {
            this.$root.navigateTo(path);
        },

        showNotificationSettings() {
            this.$root.showNotify('通知设置功能开发中', 'info');
        },

        showPrivacySettings() {
            this.$root.showNotify('隐私设置功能开发中', 'info');
        },

        showAbout() {
            this.$root.showNotify('关于我们功能开发中', 'info');
        },

        async logout() {
            try {
                // 调用登出接口
                await this.$root.$http.post('/api/v1/mobile/logout');

                // 清除本地存储
                localStorage.removeItem('token');
                localStorage.removeItem('user');

                this.$root.showNotify('退出登录成功', 'success');
                this.$root.navigateTo('/login');
            } catch (error) {
                // 即使接口失败也要清除本地数据
                localStorage.removeItem('token');
                localStorage.removeItem('user');

                this.$root.navigateTo('/login');
            }
        },

        async loadUserInfo() {
            try {
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    this.userInfo = JSON.parse(userStr);
                } else {
                    // 从服务器获取用户信息
                    const response = await this.$root.$http.get('/api/v1/mobile/user/profile');
                    if (response.data.code === 200) {
                        this.userInfo = response.data.data;
                        localStorage.setItem('user', JSON.stringify(this.userInfo));
                    }
                }
            } catch (error) {
                console.error('加载用户信息失败:', error);
            }
        },

        async loadUserStats() {
            try {
                const response = await this.$root.$http.get('/api/v1/mobile/user/statistics');
                if (response.data.code === 200) {
                    this.userStats = response.data.data;
                }
            } catch (error) {
                console.error('加载用户统计失败:', error);
                // 模拟数据
                this.userStats = {
                    totalAccess: 156,
                    thisMonth: 23,
                    deviceCount: 3
                };
            }
        }
    },
    mounted() {
        this.loadUserInfo();
        this.loadUserStats();
    }
};

// =============================================
// 设备管理组件
// =============================================
const DeviceManagementComponent = {
    template: `
        <div class="device-management">
            <!-- 设备管理头部 -->
            <div class="device-management-header">
                <div class="device-management-content">
                    <h1 class="device-management-title">
                        <van-icon name="setting-o" class="device-management-icon"></van-icon>
                        设备管理
                    </h1>
                    <p class="device-management-desc">管理和监控门禁设备状态，实时掌握设备运行情况</p>
                </div>
            </div>

            <!-- 设备统计卡片 -->
            <div class="device-stats-grid">
                <div class="device-stat-card total">
                    <div class="device-stat-value">{{ deviceStats.total }}</div>
                    <div class="device-stat-label">设备总数</div>
                </div>
                <div class="device-stat-card online">
                    <div class="device-stat-value">{{ deviceStats.online }}</div>
                    <div class="device-stat-label">在线设备</div>
                </div>
                <div class="device-stat-card offline">
                    <div class="device-stat-value">{{ deviceStats.offline }}</div>
                    <div class="device-stat-label">离线设备</div>
                </div>
                <div class="device-stat-card maintenance">
                    <div class="device-stat-value">{{ deviceStats.maintenance }}</div>
                    <div class="device-stat-label">维护中</div>
                </div>
            </div>

            <!-- 筛选器 -->
            <div class="device-filter-panel" v-if="showFilter">
                <div class="device-filter-row">
                    <div class="device-filter-item">
                        <label class="device-filter-label">设备类型</label>
                        <select v-model="filter.deviceType" class="device-filter-select">
                            <option value="">全部</option>
                            <option value="1">门禁控制器</option>
                            <option value="2">读卡器</option>
                            <option value="3">生物识别</option>
                            <option value="4">出门按钮</option>
                        </select>
                    </div>
                    <div class="device-filter-item">
                        <label class="device-filter-label">设备状态</label>
                        <select v-model="filter.status" class="device-filter-select">
                            <option value="">全部</option>
                            <option value="1">在线</option>
                            <option value="0">离线</option>
                            <option value="2">维护中</option>
                        </select>
                    </div>
                </div>
                <div class="device-filter-row">
                    <div class="device-filter-item">
                        <label class="device-filter-label">区域</label>
                        <select v-model="filter.areaId" class="device-filter-select">
                            <option value="">全部区域</option>
                            <option v-for="area in areas" :key="area.areaId" :value="area.areaId">
                                {{ area.areaName }}
                            </option>
                        </select>
                    </div>
                    <div class="device-filter-item">
                        <label class="device-filter-label">关键词</label>
                        <input v-model="filter.keyword" type="text" class="device-filter-input"
                               placeholder="设备名称或编码">
                    </div>
                </div>
                <div class="device-filter-actions">
                    <button @click="applyFilter" class="device-filter-btn primary">应用筛选</button>
                    <button @click="resetFilter" class="device-filter-btn reset">重置</button>
                    <button @click="showFilter = false" class="device-filter-btn">取消</button>
                </div>
            </div>

            <!-- 设备列表 -->
            <div class="device-list">
                <div class="device-list-header">
                    <h3 class="device-list-title">设备列表</h3>
                    <div class="device-list-actions">
                        <button @click="showFilter = !showFilter" class="device-filter-btn">
                            <van-icon name="filter-o"></van-icon> 筛选
                        </button>
                        <button @click="showAddDevice = true" class="device-filter-btn">
                            <van-icon name="plus"></van-icon> 添加
                        </button>
                        <button @click="refreshDevices" class="device-filter-btn">
                            <van-icon name="refresh"></van-icon> 刷新
                        </button>
                    </div>
                </div>

                <div v-if="loading" class="loading-container">
                    <van-loading size="24px" vertical>加载中...</van-loading>
                </div>

                <div v-else-if="filteredDevices.length === 0" class="empty-container">
                    <van-empty description="暂无设备数据" />
                </div>

                <div v-else>
                    <div v-for="device in filteredDevices" :key="device.deviceId"
                         class="device-item"
                         :class="getStatusClass(device.status)"
                         @click="showDeviceDetail(device)">
                        <div class="device-item-content">
                            <div class="device-item-icon" :class="getStatusClass(device.status)">
                                <van-icon :name="getDeviceIcon(device.deviceType)"></van-icon>
                            </div>
                            <div class="device-item-info">
                                <div class="device-item-name">
                                    {{ device.deviceName }}
                                    <span class="device-item-code">{{ device.deviceCode }}</span>
                                </div>
                                <div class="device-item-location">
                                    <van-icon name="location-o"></van-icon>
                                    {{ device.areaName || '未分配区域' }}
                                </div>
                                <div class="device-item-status" :class="getStatusClass(device.status)">
                                    <span class="device-status-dot"></span>
                                    {{ getStatusText(device.status) }}
                                </div>
                            </div>
                            <div class="device-item-actions">
                                <button @click.stop="controlDevice(device)"
                                        class="device-action-btn primary"
                                        :disabled="device.status !== 1">
                                    <van-icon name="play"></van-icon>
                                </button>
                                <button @click.stop="editDevice(device)"
                                        class="device-action-btn">
                                    <van-icon name="edit"></van-icon>
                                </button>
                                <button @click.stop="deleteDevice(device)"
                                        class="device-action-btn">
                                    <van-icon name="delete"></van-icon>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 设备详情弹窗 -->
            <div v-if="selectedDevice" class="device-detail-modal" @click="closeDeviceDetail">
                <div class="device-detail-content" @click.stop>
                    <div class="device-detail-header">
                        <h3 class="device-detail-title">设备详情</h3>
                        <button @click="closeDeviceDetail" class="device-detail-close">
                            <van-icon name="cross"></van-icon>
                        </button>
                    </div>
                    <div class="device-detail-body">
                        <!-- 基本信息 -->
                        <div class="device-detail-section">
                            <h4 class="device-detail-section-title">
                                <van-icon name="info-o" class="device-detail-section-icon"></van-icon>
                                基本信息
                            </h4>
                            <div class="device-detail-info">
                                <div class="device-detail-item">
                                    <span class="device-detail-label">设备名称</span>
                                    <span class="device-detail-value">{{ selectedDevice.deviceName }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">设备编码</span>
                                    <span class="device-detail-value">{{ selectedDevice.deviceCode }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">设备类型</span>
                                    <span class="device-detail-value">{{ getDeviceTypeName(selectedDevice.deviceType) }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">所在区域</span>
                                    <span class="device-detail-value">{{ selectedDevice.areaName || '未分配' }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">设备状态</span>
                                    <span class="device-detail-value status" :class="getStatusClass(selectedDevice.status)">
                                        {{ getStatusText(selectedDevice.status) }}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- 运行状态 -->
                        <div class="device-detail-section">
                            <h4 class="device-detail-section-title">
                                <van-icon name="chart-trending-o" class="device-detail-section-icon"></van-icon>
                                运行状态
                            </h4>
                            <div class="device-detail-info">
                                <div class="device-detail-item">
                                    <span class="device-detail-label">最后心跳</span>
                                    <span class="device-detail-value">{{ formatTime(selectedDevice.lastHeartbeat) }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">运行时长</span>
                                    <span class="device-detail-value">{{ selectedDevice.uptime || '未知' }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">电池电量</span>
                                    <span class="device-detail-value">{{ selectedDevice.batteryLevel || '未知' }}</span>
                                </div>
                                <div class="device-detail-item">
                                    <span class="device-detail-label">信号强度</span>
                                    <span class="device-detail-value">{{ selectedDevice.signalStrength || '未知' }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- 控制面板 -->
                        <div class="device-control-panel">
                            <h4 class="device-control-title">设备控制</h4>
                            <div class="device-control-buttons">
                                <button @click="restartDevice"
                                        class="device-control-btn"
                                        :disabled="selectedDevice.status !== 1">
                                    <van-icon name="replay"></van-icon> 重启
                                </button>
                                <button @click="testDevice"
                                        class="device-control-btn"
                                        :disabled="selectedDevice.status !== 1">
                                    <van-icon name="play-circle-o"></van-icon> 测试
                                </button>
                                <button @click="maintainDevice"
                                        class="device-control-btn"
                                        :class="selectedDevice.status === 2 ? 'primary' : ''">
                                    <van-icon name="tool-o"></van-icon>
                                    {{ selectedDevice.status === 2 ? '完成维护' : '进入维护' }}
                                </button>
                                <button @click="calibrateDevice"
                                        class="device-control-btn"
                                        :disabled="selectedDevice.status !== 1">
                                    <van-icon name="aim"></van-icon> 校准
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 添加设备表单 -->
            <div v-if="showAddDevice" class="device-detail-modal" @click="showAddDevice = false">
                <div class="device-detail-content" @click.stop>
                    <div class="device-detail-header">
                        <h3 class="device-detail-title">添加设备</h3>
                        <button @click="showAddDevice = false" class="device-detail-close">
                            <van-icon name="cross"></van-icon>
                        </button>
                    </div>
                    <div class="device-detail-body">
                        <form @submit.prevent="addDevice" class="device-add-form">
                            <div class="device-form-group">
                                <label class="device-form-label">设备名称 *</label>
                                <input v-model="newDevice.deviceName"
                                       type="text"
                                       class="device-form-input"
                                       required
                                       placeholder="请输入设备名称">
                            </div>
                            <div class="device-form-group">
                                <label class="device-form-label">设备编码 *</label>
                                <input v-model="newDevice.deviceCode"
                                       type="text"
                                       class="device-form-input"
                                       required
                                       placeholder="请输入设备编码">
                            </div>
                            <div class="device-form-group">
                                <label class="device-form-label">设备类型 *</label>
                                <select v-model="newDevice.deviceType" class="device-form-select" required>
                                    <option value="">请选择设备类型</option>
                                    <option value="1">门禁控制器</option>
                                    <option value="2">读卡器</option>
                                    <option value="3">生物识别</option>
                                    <option value="4">出门按钮</option>
                                </select>
                            </div>
                            <div class="device-form-group">
                                <label class="device-form-label">所在区域</label>
                                <select v-model="newDevice.areaId" class="device-form-select">
                                    <option value="">请选择区域</option>
                                    <option v-for="area in areas" :key="area.areaId" :value="area.areaId">
                                        {{ area.areaName }}
                                    </option>
                                </select>
                            </div>
                            <div class="device-form-group">
                                <label class="device-form-label">设备描述</label>
                                <textarea v-model="newDevice.description"
                                          class="device-form-input"
                                          rows="3"
                                          placeholder="请输入设备描述"></textarea>
                            </div>
                            <div class="device-form-buttons">
                                <button type="button" @click="showAddDevice = false"
                                        class="device-form-btn secondary">取消</button>
                                <button type="submit" class="device-form-btn primary">添加</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    `,

    data() {
        return {
            loading: false,
            showFilter: false,
            showAddDevice: false,
            selectedDevice: null,
            devices: [],
            areas: [],
            deviceStats: {
                total: 0,
                online: 0,
                offline: 0,
                maintenance: 0
            },
            filter: {
                deviceType: '',
                status: '',
                areaId: '',
                keyword: ''
            },
            newDevice: {
                deviceName: '',
                deviceCode: '',
                deviceType: '',
                areaId: '',
                description: ''
            }
        };
    },

    computed: {
        filteredDevices() {
            let result = this.devices;

            if (this.filter.deviceType) {
                result = result.filter(device => device.deviceType == this.filter.deviceType);
            }

            if (this.filter.status !== '') {
                result = result.filter(device => device.status == this.filter.status);
            }

            if (this.filter.areaId) {
                result = result.filter(device => device.areaId == this.filter.areaId);
            }

            if (this.filter.keyword) {
                const keyword = this.filter.keyword.toLowerCase();
                result = result.filter(device =>
                    device.deviceName.toLowerCase().includes(keyword) ||
                    device.deviceCode.toLowerCase().includes(keyword)
                );
            }

            return result;
        }
    },

    methods: {
        async loadDevices() {
            try {
                this.loading = true;
                const response = await this.$http.get('/api/v1/mobile/device/list');
                if (response.data.code === 200) {
                    this.devices = response.data.data || [];
                    this.calculateStats();
                } else {
                    this.$root.showNotify(response.data.message || '加载设备列表失败', 'error');
                }
            } catch (error) {
                console.error('加载设备列表失败:', error);
                this.$root.showNotify('网络错误，请稍后重试', 'error');
                // 使用模拟数据
                this.devices = this.getMockDevices();
                this.calculateStats();
            } finally {
                this.loading = false;
            }
        },

        async loadAreas() {
            try {
                const response = await this.$http.get('/api/v1/mobile/area/list');
                if (response.data.code === 200) {
                    this.areas = response.data.data || [];
                }
            } catch (error) {
                console.error('加载区域列表失败:', error);
                // 使用模拟数据
                this.areas = this.getMockAreas();
            }
        },

        calculateStats() {
            this.deviceStats = {
                total: this.devices.length,
                online: this.devices.filter(d => d.status === 1).length,
                offline: this.devices.filter(d => d.status === 0).length,
                maintenance: this.devices.filter(d => d.status === 2).length
            };
        },

        getStatusClass(status) {
            switch (status) {
                case 1: return 'online';
                case 0: return 'offline';
                case 2: return 'maintenance';
                default: return 'offline';
            }
        },

        getStatusText(status) {
            switch (status) {
                case 1: return '在线';
                case 0: return '离线';
                case 2: return '维护中';
                default: return '未知';
            }
        },

        getDeviceIcon(deviceType) {
            switch (deviceType) {
                case 1: return 'computing-o';
                case 2: return 'credit-pay';
                case 3: return 'finger-print';
                case 4: return 'pointer';
                default: return 'setting-o';
            }
        },

        getDeviceTypeName(deviceType) {
            switch (deviceType) {
                case 1: return '门禁控制器';
                case 2: return '读卡器';
                case 3: return '生物识别';
                case 4: return '出门按钮';
                default: return '未知设备';
            }
        },

        formatTime(timeStr) {
            if (!timeStr) return '未知';
            const date = new Date(timeStr);
            return this.$root.formatDateTime(date);
        },

        showDeviceDetail(device) {
            this.selectedDevice = device;
        },

        closeDeviceDetail() {
            this.selectedDevice = null;
        },

        applyFilter() {
            this.showFilter = false;
        },

        resetFilter() {
            this.filter = {
                deviceType: '',
                status: '',
                areaId: '',
                keyword: ''
            };
        },

        refreshDevices() {
            this.loadDevices();
            this.$root.showNotify('刷新成功', 'success');
        },

        async controlDevice(device) {
            try {
                this.$root.showLoading('操作中...');
                const response = await this.$http.post('/api/v1/mobile/device/control', {
                    deviceId: device.deviceId,
                    action: 'open'
                });

                if (response.data.code === 200) {
                    this.$root.showNotify('操作成功', 'success');
                    this.loadDevices();
                } else {
                    this.$root.showNotify(response.data.message || '操作失败', 'error');
                }
            } catch (error) {
                console.error('设备控制失败:', error);
                this.$root.showNotify('网络错误，请稍后重试', 'error');
            } finally {
                this.$root.hideLoading();
            }
        },

        editDevice(device) {
            this.$root.showNotify('编辑功能开发中', 'info');
        },

        deleteDevice(device) {
            this.$root.showConfirm('确认删除此设备吗？', async () => {
                try {
                    this.$root.showLoading('删除中...');
                    const response = await this.$http.delete(`/api/v1/mobile/device/${device.deviceId}`);

                    if (response.data.code === 200) {
                        this.$root.showNotify('删除成功', 'success');
                        this.loadDevices();
                    } else {
                        this.$root.showNotify(response.data.message || '删除失败', 'error');
                    }
                } catch (error) {
                    console.error('删除设备失败:', error);
                    this.$root.showNotify('网络错误，请稍后重试', 'error');
                } finally {
                    this.$root.hideLoading();
                }
            });
        },

        async addDevice() {
            try {
                this.$root.showLoading('添加中...');
                const response = await this.$http.post('/api/v1/mobile/device/add', this.newDevice);

                if (response.data.code === 200) {
                    this.$root.showNotify('添加成功', 'success');
                    this.showAddDevice = false;
                    this.newDevice = {
                        deviceName: '',
                        deviceCode: '',
                        deviceType: '',
                        areaId: '',
                        description: ''
                    };
                    this.loadDevices();
                } else {
                    this.$root.showNotify(response.data.message || '添加失败', 'error');
                }
            } catch (error) {
                console.error('添加设备失败:', error);
                this.$root.showNotify('网络错误，请稍后重试', 'error');
            } finally {
                this.$root.hideLoading();
            }
        },

        async restartDevice() {
            try {
                this.$root.showLoading('重启中...');
                const response = await this.$http.post('/api/v1/mobile/device/restart', {
                    deviceId: this.selectedDevice.deviceId
                });

                if (response.data.code === 200) {
                    this.$root.showNotify('重启成功', 'success');
                    this.loadDevices();
                } else {
                    this.$root.showNotify(response.data.message || '重启失败', 'error');
                }
            } catch (error) {
                console.error('重启设备失败:', error);
                this.$root.showNotify('网络错误，请稍后重试', 'error');
            } finally {
                this.$root.hideLoading();
            }
        },

        async testDevice() {
            this.$root.showNotify('测试功能开发中', 'info');
        },

        async maintainDevice() {
            try {
                const action = this.selectedDevice.status === 2 ? 'complete' : 'start';
                this.$root.showLoading('处理中...');
                const response = await this.$http.post('/api/v1/mobile/device/maintain', {
                    deviceId: this.selectedDevice.deviceId,
                    action: action
                });

                if (response.data.code === 200) {
                    this.$root.showNotify('操作成功', 'success');
                    this.closeDeviceDetail();
                    this.loadDevices();
                } else {
                    this.$root.showNotify(response.data.message || '操作失败', 'error');
                }
            } catch (error) {
                console.error('维护操作失败:', error);
                this.$root.showNotify('网络错误，请稍后重试', 'error');
            } finally {
                this.$root.hideLoading();
            }
        },

        async calibrateDevice() {
            this.$root.showNotify('校准功能开发中', 'info');
        },

        getMockDevices() {
            return [
                {
                    deviceId: 'DEV001',
                    deviceName: '主入口门禁控制器',
                    deviceCode: 'ACCESS_CTRL_001',
                    deviceType: 1,
                    areaId: 'AREA001',
                    areaName: 'A栋1楼大厅',
                    status: 1,
                    lastHeartbeat: new Date().toISOString(),
                    uptime: '15天8小时',
                    batteryLevel: '100%',
                    signalStrength: '强'
                },
                {
                    deviceId: 'DEV002',
                    deviceName: '东侧门禁控制器',
                    deviceCode: 'ACCESS_CTRL_002',
                    deviceType: 1,
                    areaId: 'AREA001',
                    areaName: 'A栋1楼大厅',
                    status: 0,
                    lastHeartbeat: new Date(Date.now() - 300000).toISOString(),
                    uptime: '3天12小时',
                    batteryLevel: '85%',
                    signalStrength: '弱'
                },
                {
                    deviceId: 'DEV003',
                    deviceName: '指纹识别终端',
                    deviceCode: 'BIOMETRIC_001',
                    deviceType: 3,
                    areaId: 'AREA002',
                    areaName: 'B栋办公区',
                    status: 2,
                    lastHeartbeat: new Date(Date.now() - 600000).toISOString(),
                    uptime: '7天5小时',
                    batteryLevel: '92%',
                    signalStrength: '中'
                },
                {
                    deviceId: 'DEV004',
                    deviceName: '侧门读卡器',
                    deviceCode: 'CARD_READER_001',
                    deviceType: 2,
                    areaId: 'AREA001',
                    areaName: 'A栋1楼大厅',
                    status: 1,
                    lastHeartbeat: new Date().toISOString(),
                    uptime: '20天15小时',
                    batteryLevel: '100%',
                    signalStrength: '强'
                },
                {
                    deviceId: 'DEV005',
                    deviceName: '办公室出门按钮',
                    deviceCode: 'EXIT_BTN_001',
                    deviceType: 4,
                    areaId: 'AREA003',
                    areaName: 'A栋2楼办公室',
                    status: 1,
                    lastHeartbeat: new Date().toISOString(),
                    uptime: '30天2小时',
                    batteryLevel: '98%',
                    signalStrength: '强'
                }
            ];
        },

        getMockAreas() {
            return [
                { areaId: 'AREA001', areaName: 'A栋1楼大厅' },
                { areaId: 'AREA002', areaName: 'B栋办公区' },
                { areaId: 'AREA003', areaName: 'A栋2楼办公室' },
                { areaId: 'AREA004', areaName: 'C栋生产车间' }
            ];
        }
    },

    mounted() {
        this.loadDevices();
        this.loadAreas();
    }
};

// 添加HTTP方法到Vue原型
app.config.globalProperties.$http = axios;