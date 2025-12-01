/**
 * 消费管理系统 - 主要JavaScript文件
 * 老王我写的核心功能，别他妈乱动！
 */

// 全局配置
const APP_CONFIG = {
    apiBaseUrl: '/api',
    version: '2.0.0',
    debug: true,
    animations: {
        duration: 300,
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
    }
};

// 工具函数集合
const Utils = {
    /**
     * 格式化货币显示
     */
    formatCurrency: (amount, currency = '¥') => {
        return `${currency}${parseFloat(amount).toLocaleString('zh-CN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`;
    },

    /**
     * 格式化数字显示
     */
    formatNumber: (num) => {
        return parseInt(num).toLocaleString('zh-CN');
    },

    /**
     * 格式化时间显示
     */
    formatTime: (timestamp) => {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;

        if (diff < 60000) { // 小于1分钟
            return '刚刚';
        } else if (diff < 3600000) { // 小于1小时
            return `${Math.floor(diff / 60000)}分钟前`;
        } else if (diff < 86400000) { // 小于1天
            return `${Math.floor(diff / 3600000)}小时前`;
        } else {
            return date.toLocaleDateString('zh-CN');
        }
    },

    /**
     * 防抖函数
     */
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * 节流函数
     */
    throttle: (func, limit) => {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },

    /**
     * 显示加载状态
     */
    showLoading: (element, text = '加载中...') => {
        if (typeof element === 'string') {
            element = document.querySelector(element);
        }
        if (element) {
            element.disabled = true;
            element.dataset.originalText = element.textContent;
            element.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${text}`;
        }
    },

    /**
     * 隐藏加载状态
     */
    hideLoading: (element) => {
        if (typeof element === 'string') {
            element = document.querySelector(element);
        }
        if (element && element.dataset.originalText) {
            element.disabled = false;
            element.textContent = element.dataset.originalText;
            delete element.dataset.originalText;
        }
    },

    /**
     * 显示通知消息
     */
    showNotification: (message, type = 'info', duration = 5000) => {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type} slide-in`;

        const iconMap = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas ${iconMap[type]}"></i>
                <span>${message}</span>
                <button class="notification-close">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        // 添加通知样式
        if (!document.getElementById('notification-styles')) {
            const style = document.createElement('style');
            style.id = 'notification-styles';
            style.textContent = `
                .notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 9999;
                    max-width: 400px;
                    background: white;
                    border-radius: 12px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.15);
                    border-left: 4px solid;
                    animation: slideIn 0.3s ease-out;
                }

                .notification-success { border-left-color: var(--success); }
                .notification-error { border-left-color: var(--error); }
                .notification-warning { border-left-color: var(--warning); }
                .notification-info { border-left-color: var(--info); }

                .notification-content {
                    padding: 16px 20px;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                }

                .notification-content i:first-child {
                    font-size: 20px;
                    flex-shrink: 0;
                }

                .notification-success i:first-child { color: var(--success); }
                .notification-error i:first-child { color: var(--error); }
                .notification-warning i:first-child { color: var(--warning); }
                .notification-info i:first-child { color: var(--info); }

                .notification-content span {
                    flex: 1;
                    color: var(--gray-800);
                    font-size: 14px;
                }

                .notification-close {
                    background: none;
                    border: none;
                    color: var(--gray-400);
                    cursor: pointer;
                    padding: 4px;
                    border-radius: 4px;
                    transition: all 0.2s;
                }

                .notification-close:hover {
                    background: var(--gray-100);
                    color: var(--gray-600);
                }

                @keyframes slideIn {
                    from {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                    to {
                        transform: translateX(0);
                        opacity: 1;
                    }
                }

                @keyframes slideOut {
                    from {
                        transform: translateX(0);
                        opacity: 1;
                    }
                    to {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                }

                .notification.slide-out {
                    animation: slideOut 0.3s ease-out forwards;
                }
            `;
            document.head.appendChild(style);
        }

        document.body.appendChild(notification);

        // 关闭按钮事件
        const closeBtn = notification.querySelector('.notification-close');
        closeBtn.addEventListener('click', () => {
            notification.classList.add('slide-out');
            setTimeout(() => notification.remove(), 300);
        });

        // 自动关闭
        if (duration > 0) {
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.classList.add('slide-out');
                    setTimeout(() => notification.remove(), 300);
                }
            }, duration);
        }

        return notification;
    }
};

// API请求封装
const API = {
    /**
     * 基础请求方法
     */
    request: async (url, options = {}) => {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
        };

        try {
            const response = await fetch(`${APP_CONFIG.apiBaseUrl}${url}`, {
                ...defaultOptions,
                ...options,
                headers: {
                    ...defaultOptions.headers,
                    ...options.headers,
                },
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API请求失败:', error);
            Utils.showNotification('请求失败，请稍后重试', 'error');
            throw error;
        }
    },

    /**
     * GET请求
     */
    get: (url, params = {}) => {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return API.request(fullUrl);
    },

    /**
     * POST请求
     */
    post: (url, data = {}) => {
        return API.request(url, {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    /**
     * PUT请求
     */
    put: (url, data = {}) => {
        return API.request(url, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    },

    /**
     * DELETE请求
     */
    delete: (url) => {
        return API.request(url, {
            method: 'DELETE',
        });
    }
};

// 页面管理器
const PageManager = {
    currentPage: '',
    pageInstances: {},

    /**
     * 注册页面实例
     */
    registerPage: (pageName, pageInstance) => {
        PageManager.pageInstances[pageName] = pageInstance;
    },

    /**
     * 初始化当前页面
     */
    initPage: (pageName) => {
        PageManager.currentPage = pageName;

        // 更新菜单状态
        PageManager.updateMenuState(pageName);

        // 调用页面特定的初始化方法
        const pageInstance = PageManager.pageInstances[pageName];
        if (pageInstance && typeof pageInstance.init === 'function') {
            pageInstance.init();
        }
    },

    /**
     * 更新菜单激活状态
     */
    updateMenuState: (pageName) => {
        // 移除所有active类
        document.querySelectorAll('.menu-item').forEach(item => {
            item.classList.remove('active');
        });

        // 添加当前页面的active类
        const currentMenuItem = document.querySelector(`[data-page="${pageName}"]`);
        if (currentMenuItem) {
            currentMenuItem.classList.add('active');
        }
    }
};

// 侧边栏管理器
const SidebarManager = {
    isCollapsed: false,

    /**
     * 切换侧边栏状态
     */
    toggle: () => {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            SidebarManager.isCollapsed = !SidebarManager.isCollapsed;
            sidebar.classList.toggle('collapsed', SidebarManager.isCollapsed);
        }
    },

    /**
     * 设置侧边栏状态
     */
    setCollapsed: (collapsed) => {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            SidebarManager.isCollapsed = collapsed;
            sidebar.classList.toggle('collapsed', collapsed);
        }
    },

    /**
     * 初始化侧边栏
     */
    init: () => {
        // 添加响应式处理
        const handleResize = Utils.throttle(() => {
            if (window.innerWidth <= 768) {
                SidebarManager.setCollapsed(true);
            } else {
                SidebarManager.setCollapsed(false);
            }
        }, 250);

        window.addEventListener('resize', handleResize);
        handleResize(); // 初始检查
    }
};

// 主题管理器
const ThemeManager = {
    currentTheme: 'light',

    /**
     * 切换主题
     */
    toggleTheme: () => {
        ThemeManager.currentTheme = ThemeManager.currentTheme === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', ThemeManager.currentTheme);
        localStorage.setItem('theme', ThemeManager.currentTheme);
    },

    /**
     * 初始化主题
     */
    init: () => {
        const savedTheme = localStorage.getItem('theme') || 'light';
        ThemeManager.currentTheme = savedTheme;
        document.documentElement.setAttribute('data-theme', savedTheme);
    }
};

// 数据模拟器（用于演示）
const DataSimulator = {
    /**
     * 生成随机统计数据
     */
    generateStats: () => {
        return {
            revenue: Math.floor(Math.random() * 50000) + 10000,
            users: Math.floor(Math.random() * 2000) + 500,
            orders: Math.floor(Math.random() * 5000) + 1000,
            deviceOnlineRate: (Math.random() * 5 + 95).toFixed(1)
        };
    },

    /**
     * 生成活动信息
     */
    generateActivities: () => {
        const activities = [
            { type: 'success', title: '新用户注册', description: '张三完成账户注册', time: '2分钟前' },
            { type: 'info', title: '批量补贴发放', description: '向员工账户发放补贴', time: '15分钟前' },
            { type: 'warning', title: '设备离线提醒', description: '消费机连接异常', time: '1小时前' },
            { type: 'success', title: '数据同步完成', description: '所有分店数据已同步', time: '2小时前' }
        ];
        return activities[Math.floor(Math.random() * activities.length)];
    }
};

// 应用初始化
class App {
    constructor() {
        this.isInitialized = false;
    }

    /**
     * 初始化应用
     */
    async init() {
        if (this.isInitialized) return;

        try {
            console.log('🚀 正在初始化消费管理系统...');

            // 初始化各个管理器
            SidebarManager.init();
            ThemeManager.init();

            // 初始化全局事件
            this.initGlobalEvents();

            // 检测当前页面并初始化
            const currentPage = this.detectCurrentPage();
            PageManager.initPage(currentPage);

            // 显示欢迎消息
            this.showWelcomeMessage();

            this.isInitialized = true;
            console.log('✅ 系统初始化完成');

        } catch (error) {
            console.error('❌ 系统初始化失败:', error);
            Utils.showNotification('系统初始化失败，请刷新页面重试', 'error');
        }
    }

    /**
     * 检测当前页面
     */
    detectCurrentPage() {
        const path = window.location.pathname;
        const page = path.substring(path.lastIndexOf('/') + 1);
        return page || 'dashboard';
    }

    /**
     * 初始化全局事件
     */
    initGlobalEvents() {
        // 键盘快捷键
        document.addEventListener('keydown', (e) => {
            // Ctrl + K 打开搜索
            if (e.ctrlKey && e.key === 'k') {
                e.preventDefault();
                this.openSearch();
            }

            // Ctrl + / 显示快捷键帮助
            if (e.ctrlKey && e.key === '/') {
                e.preventDefault();
                this.showShortcuts();
            }
        });

        // 全局错误处理
        window.addEventListener('error', (e) => {
            console.error('全局错误:', e.error);
            if (APP_CONFIG.debug) {
                Utils.showNotification('发生了一个错误，请查看控制台', 'error');
            }
        });
    }

    /**
     * 打开搜索
     */
    openSearch() {
        console.log('打开全局搜索');
        // TODO: 实现全局搜索功能
        Utils.showNotification('搜索功能开发中...', 'info');
    }

    /**
     * 显示快捷键帮助
     */
    showShortcuts() {
        Utils.showNotification('快捷键: Ctrl+K 搜索, Ctrl+/ 帮助', 'info', 3000);
    }

    /**
     * 显示欢迎消息
     */
    showWelcomeMessage() {
        const hour = new Date().getHours();
        let greeting = '早上好';

        if (hour >= 12 && hour < 18) {
            greeting = '下午好';
        } else if (hour >= 18) {
            greeting = '晚上好';
        }

        setTimeout(() => {
            Utils.showNotification(`${greeting}！欢迎使用消费管理系统`, 'success', 4000);
        }, 1000);
    }
}

// 创建应用实例
const app = new App();

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});

// 导出全局对象
window.App = {
    Utils,
    API,
    PageManager,
    SidebarManager,
    ThemeManager,
    DataSimulator,
    config: APP_CONFIG
};