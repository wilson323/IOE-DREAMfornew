// 消费管理系统 - 统一菜单配置
// 此文件管理所有页面的侧边栏菜单，确保菜单一致性和active状态正确

// 获取当前页面的目录深度
function getPageDepth() {
    const path = window.location.pathname;
    // 如果在pages目录下，深度为1，否则为0
    return path.includes('/pages/') ? 1 : 0;
}

// 根据当前页面深度生成正确的URL
function getUrlPath(relativePath) {
    const depth = getPageDepth();

    // 处理返回主页的特殊路径
    if (relativePath === '../index.html') {
        if (depth === 0) {
            // 在根目录下，直接访问index.html
            return 'index.html';
        } else {
            // 在pages目录下，返回上一级的index.html
            return '../index.html';
        }
    }

    if (depth === 0) {
        // 在根目录下，页面在pages子目录
        return 'pages/' + relativePath;
    } else {
        // 在pages目录下，直接访问同目录文件
        return relativePath;
    }
}

// 菜单配置数据
const menuConfig = [
    {
        sectionTitle: '系统管理',
        items: [
            {
                id: 'dashboard',
                title: '系统主页',
                icon: 'fas fa-tachometer-alt',
                url: getUrlPath('../index.html')
            }
        ]
    },
    {
        sectionTitle: '基础配置',
        items: [
            {
                id: 'accounts',
                title: '账户类别',
                icon: 'fas fa-tags',
                url: getUrlPath('accounts.html')
            },
            {
                id: 'account-management',
                title: '账户管理',
                icon: 'fas fa-user-cog',
                url: getUrlPath('account-management.html')
            },
                        {
                id: 'regions',
                title: '区域管理',
                icon: 'fas fa-map-marker-alt',
                url: getUrlPath('regions.html')
            },
            {
                id: 'meals',
                title: '餐别资料',
                icon: 'fas fa-utensils',
                url: getUrlPath('meals.html')
            },
            {
                id: 'parameters',
                title: '参数设置',
                icon: 'fas fa-cogs',
                url: getUrlPath('parameters.html')
            },
            {
                id: 'devices',
                title: '设备管理',
                icon: 'fas fa-desktop',
                url: getUrlPath('devices.html')
            },
            {
                id: 'products',
                title: '商品资料',
                icon: 'fas fa-box',
                url: getUrlPath('products.html')
            }
        ]
    },
    {
        sectionTitle: '业务管理',
        items: [
            {
                id: 'subsidy',
                title: '补贴管理',
                icon: 'fas fa-gift',
                url: getUrlPath('subsidy.html')
            },
            {
                id: 'manual-consumption',
                title: '手工补消费',
                icon: 'fas fa-hand-holding-usd',
                url: getUrlPath('manual-consumption.html')
            },
            {
                id: 'offline-consumption',
                title: '离线消费管理',
                icon: 'fas fa-plug',
                url: getUrlPath('offline-consumption.html')
            },
            {
                id: 'transfer',
                title: '转账记录',
                icon: 'fas fa-exchange-alt',
                url: getUrlPath('transfer.html')
            },
            {
                id: 'payment',
                title: '支付管理',
                icon: 'fas fa-credit-card',
                url: getUrlPath('payment.html')
            }
        ]
    },
    {
        sectionTitle: '查询统计',
        items: [
            {
                id: 'reports',
                title: '报表查询中心',
                icon: 'fas fa-chart-bar',
                url: getUrlPath('reports.html'),
                children: [
                {
                    id: 'consumption-details',
                    title: '消费明细表',
                    icon: 'fas fa-receipt',
                    url: getUrlPath('consumption-details.html')
                },
                {
                    id: 'recharge-details',
                    title: '充值明细表',
                    icon: 'fas fa-plus-circle',
                    url: getUrlPath('recharge-details.html')
                    },
                    {
                        id: 'refund-details',
                        title: '退款明细表',
                        icon: 'fas fa-minus-circle',
                        url: getUrlPath('refund-details.html')
                    },
                    {
                        id: 'balance',
                        title: '账户余额表',
                        icon: 'fas fa-wallet',
                        url: getUrlPath('balance.html')
                    },
                    {
                        id: 'subsidy-details',
                        title: '补贴明细表',
                        icon: 'fas fa-gift',
                        url: getUrlPath('subsidy-details.html')
                    },
                    {
                        id: 'account-income-expense',
                        title: '账户收支',
                        icon: 'fas fa-chart-line',
                        url: getUrlPath('account-income-expense.html')
                    }
                ]
            }
        ]
    },
    {
        sectionTitle: '系统设置',
        items: [
            {
                id: 'parameters',
                title: '参数设置',
                icon: 'fas fa-cogs',
                url: getUrlPath('parameters.html')
            }
        ]
    }
];

// 获取当前页面的文件名（不含路径）
function getCurrentPage() {
    const path = window.location.pathname;
    const page = path.substring(path.lastIndexOf('/') + 1);
    return page || 'index.html';
}

// 根据URL查找对应的菜单项ID
function findActiveMenuId(currentPage) {
    for (const section of menuConfig) {
        for (const item of section.items) {
            if (item.url === currentPage) {
                return item.id;
            }
            // 检查子菜单
            if (item.children) {
                for (const child of item.children) {
                    if (child.url === currentPage) {
                        return child.id;
                    }
                }
            }
        }
    }
    return null;
}

// 查找父菜单ID
function findParentMenuId(currentPage) {
    for (const section of menuConfig) {
        for (const item of section.items) {
            if (item.children) {
                for (const child of item.children) {
                    if (child.url === currentPage) {
                        return item.id;
                    }
                }
            }
        }
    }
    return null;
}

// 生成侧边栏HTML
function generateSidebarMenu() {
    const currentPage = getCurrentPage();
    const activeMenuId = findActiveMenuId(currentPage);
    const parentMenuId = findParentMenuId(currentPage);

    let html = '<div class="sidebar-menu">';

    menuConfig.forEach(section => {
        html += `
            <div class="menu-section">
                <div class="menu-section-title">${section.sectionTitle}</div>
        `;

        section.items.forEach(item => {
            const hasChildren = item.children && item.children.length > 0;
            const isParent = item.id === parentMenuId;

            // 只有当前页面直接是这个菜单项时才标记为active
            // 如果是子页面，父菜单不应该标记为active，只应该展开
            const isActive = (item.id === activeMenuId && !isParent) ? 'active' : '';

            if (hasChildren) {
                // 父菜单项（带子菜单）
                // 如果当前页面是子页面，父菜单应该展开但不应该active
                const isExpanded = isParent || isActive;
                html += `
                    <div class="menu-item-with-children ${isExpanded ? 'expanded' : ''}">
                        <a href="${item.url}" class="menu-item ${isActive}" data-menu-id="${item.id}">
                            <i class="${item.icon}"></i>
                            <span class="sidebar-text">${item.title}</span>
                            <i class="fas fa-chevron-down submenu-arrow"></i>
                        </a>
                        <div class="submenu ${isExpanded ? 'show' : ''}">
                `;

                // 渲染子菜单
                item.children.forEach(child => {
                    const childActive = child.id === activeMenuId ? 'active' : '';
                    html += `
                        <a href="${child.url}" class="submenu-item ${childActive}" data-menu-id="${child.id}">
                            <i class="${child.icon}"></i>
                            <span class="sidebar-text">${child.title}</span>
                        </a>
                    `;
                });

                html += `
                        </div>
                    </div>
                `;
            } else {
                // 普通菜单项（无子菜单）
                html += `
                    <a href="${item.url}" class="menu-item ${isActive}" data-menu-id="${item.id}">
                        <i class="${item.icon}"></i>
                        <span class="sidebar-text">${item.title}</span>
                    </a>
                `;
            }
        });

        html += '</div>';
    });

    html += '</div>';
    return html;
}

// 初始化侧边栏菜单
function initSidebarMenu() {
    const sidebar = document.querySelector('.sidebar');
    if (!sidebar) {
        console.error('找不到侧边栏元素');
        return;
    }

    // 注入子菜单样式
    injectSubmenuStyles();

    // 查找侧边栏菜单容器
    const existingMenu = sidebar.querySelector('.sidebar-menu');
    if (existingMenu) {
        // 如果存在旧菜单，替换它
        const newMenu = generateSidebarMenu();
        existingMenu.outerHTML = newMenu;
    } else {
        // 如果不存在，在header后面插入
        const header = sidebar.querySelector('.sidebar-header');
        if (header) {
            header.insertAdjacentHTML('afterend', generateSidebarMenu());
        }
    }

    // 只在首次初始化时绑定事件（使用标记防止重复绑定）
    if (!sidebar.hasAttribute('data-submenu-initialized')) {
        initSubmenuToggle();
        sidebar.setAttribute('data-submenu-initialized', 'true');
    }

    console.log('侧边栏菜单已初始化，当前页面:', getCurrentPage());
}

// 注入子菜单样式
function injectSubmenuStyles() {
    if (document.getElementById('submenu-styles')) return;

    const style = document.createElement('style');
    style.id = 'submenu-styles';
    style.textContent = `
        .sidebar .sidebar-menu {
            position: relative;
            z-index: 0;
        }

        .menu-item-with-children {
            position: relative;
            overflow: visible;
        }

        .menu-item-with-children > .menu-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            padding-right: 18px;
            cursor: pointer;
        }

        .submenu-arrow {
            font-size: 12px;
            transition: transform 0.3s ease;
            margin-left: auto;
        }

        .menu-item-with-children.expanded .submenu-arrow {
            transform: rotate(180deg);
        }

        .submenu {
            max-height: 0;
            overflow: hidden;
            transition: max-height 0.3s ease;
            background: rgba(0, 0, 0, 0.05);
            margin-top: 4px;
            padding: 4px 0;
            border-left: 1px solid rgba(0, 0, 0, 0.05);
        }

        .submenu.show {
            max-height: 480px;
        }

        .submenu-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 20px 10px 48px;
            color: var(--gray-600);
            text-decoration: none;
            transition: all 0.2s ease;
            font-size: 13px;
            position: relative;
            z-index: 1;
        }

        .submenu-item i {
            width: 18px;
            text-align: center;
            font-size: 13px;
            color: var(--gray-500);
        }

        .submenu-item:hover {
            background: var(--gray-100);
            color: var(--gray-900);
            padding-left: 52px;
        }

        .submenu-item:hover i {
            color: var(--gray-700);
        }

        .submenu-item.active {
            background: var(--primary-100);
            color: var(--primary-700);
            border-left: 3px solid var(--primary-500);
            padding-left: 50px;
        }

        .submenu-item.active i {
            color: var(--primary-600);
        }

        .menu-item-with-children .submenu,
        .menu-item-with-children .submenu * {
            pointer-events: auto;
        }
    `;
    document.head.appendChild(style);
}

// 初始化子菜单展开/折叠交互（使用事件委托）
function initSubmenuToggle() {
    const sidebar = document.querySelector('.sidebar');
    if (!sidebar) {
        console.error('找不到侧边栏元素');
        return;
    }

    console.log('开始初始化子菜单交互...');

    // 使用事件委托，监听整个sidebar的点击事件
    sidebar.addEventListener('click', function(e) {
        console.log('sidebar点击事件触发', e.target);

        // 先检查是否点击了子菜单项（优先级高）
        const submenuItem = e.target.closest('.submenu-item');
        if (submenuItem) {
            console.log('点击子菜单项:', submenuItem.textContent.trim());
            // 允许默认跳转行为，不做任何处理
            return;
        }

        // 检查是否点击了带子菜单的父菜单项
        const clickedMenuItem = e.target.closest('a.menu-item');
        if (clickedMenuItem) {
            console.log('点击了菜单项:', clickedMenuItem.textContent.trim());

            // 检查这个菜单项是否在 menu-item-with-children 容器内
            const parentContainer = clickedMenuItem.parentElement;
            if (parentContainer && parentContainer.classList.contains('menu-item-with-children')) {
                console.log('这是一个带子菜单的父菜单项');
                const submenu = parentContainer.querySelector('.submenu');

                // 如果当前是折叠状态，阻止默认跳转，只展开
                if (!parentContainer.classList.contains('expanded')) {
                    e.preventDefault();
                    e.stopPropagation();
                    parentContainer.classList.add('expanded');
                    submenu.classList.add('show');
                    console.log('✅ 展开子菜单');
                    return false;
                } else {
                    // 如果已经展开，点击其他地方时折叠它
                    parentContainer.classList.remove('expanded');
                    submenu.classList.remove('show');
                    console.log('✅ 折叠子菜单');
                    e.preventDefault();
                    return false;
                }
            } else {
                // 普通菜单项，允许跳转
                console.log('这是普通菜单项，允许跳转');
            }
        }
    });

    console.log('✅ 子菜单交互已初始化（使用事件委托）');
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initSidebarMenu();
});

// 导出配置供其他脚本使用
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { menuConfig, generateSidebarMenu, initSidebarMenu };
}