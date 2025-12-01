/*
 * 登录用户
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-06 20:55:09
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import _ from 'lodash';
import { defineStore } from 'pinia';
import localKey from '/@/constants/local-storage-key-const';
import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
import { messageApi } from '/@/api/support/message-api.js';
import { smartSentry } from '/@/lib/smart-sentry.js';
import { localRead, localSave, localRemove } from '/@/utils/local-util';


export const useUserStore = defineStore({
  id: 'userStore',
  state: () => ({
    token: '',
    //员工id
    employeeId: '',
    // 头像
    avatar: '',
    //登录名
    loginName: '',
    //姓名
    actualName: '',
    //手机号
    phone: '',
    //部门id
    departmentId: '',
    //部门名词
    departmentName: '',
    //是否需要修改密码
    needUpdatePwdFlag: false,
    //是否为超级管理员
    administratorFlag: true,
    //上次登录ip
    lastLoginIp: '',
    //上次登录ip地区
    lastLoginIpRegion: '',
    //上次登录 设备
    lastLoginUserAgent: '',
    //上次登录时间
    lastLoginTime: '',
    //左侧菜单树形结构
    menuTree: [],
    //存在页面路由的菜单集合
    menuRouterList: [],
    //是否完成menuRouter初始化
    menuRouterInitFlag: false,
    //父类菜单集合
    menuParentIdListMap: new Map(),
    // 功能点集合
    pointsList: [],
    // 标签页
    tagNav: null,
    // 缓存
    keepAliveIncludes: [],
    // 未读消息数量
    unreadMessageCount: 0,
    // 待办工作数
    toBeDoneCount: 0,
  }),
  getters: {
    getToken(state) {
      if (state.token) {
        return state.token;
      }
      return localRead(localKey.USER_TOKEN);
    },
    getNeedUpdatePwdFlag(state){
      return state.needUpdatePwdFlag;
    },
    //是否初始化了 路由
    getMenuRouterInitFlag(state) {
      return state.menuRouterInitFlag;
    },
    //菜单树
    getMenuTree(state) {
      return state.menuTree;
    },
    //菜单的路由
    getMenuRouterList(state) {
      return state.menuRouterList;
    },
    //菜单的父级id
    getMenuParentIdListMap(state) {
      return state.menuParentIdListMap;
    },
    //功能点
    getPointList(state) {
      if (_.isEmpty(state.pointsList)) {
        let localUserPoints = localRead(localKey.USER_POINTS) || '';
        state.pointsList = localUserPoints ? JSON.parse(localUserPoints) : [];
      }
      return state.pointsList;
    },
    //标签页
    getTagNav(state) {
      if (_.isNull(state.tagNav)) {
        let localTagNav = localRead(localKey.USER_TAG_NAV) || '';
        state.tagNav = localTagNav ? JSON.parse(localTagNav) : [];
      }
      let tagNavList = _.cloneDeep(state.tagNav) || [];
      tagNavList.unshift({
        menuName: HOME_PAGE_NAME,
        menuTitle: '首页',
      });
      return tagNavList;
    },
  },

  actions: {
    logout() {
      this.token = '';
      this.menuList = [];
      this.tagNav = [];
      this.unreadMessageCount = 0;
      localRemove(localKey.USER_TOKEN);
      localRemove(localKey.USER_POINTS);
      localRemove(localKey.USER_TAG_NAV);
      localRemove(localKey.APP_CONFIG);
      localRemove(localKey.HOME_QUICK_ENTRY);
      localRemove(localKey.NOTICE_READ);
      localRemove(localKey.TO_BE_DONE);
    },
    // 查询未读消息数量
    async queryUnreadMessageCount() {
      try {
        let result = await messageApi.queryUnreadCount();
        this.unreadMessageCount = result.data;
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
    async queryToBeDoneList() {
      try {
        let localToBeDoneList = localRead(localKey.TO_BE_DONE);
        if (localToBeDoneList) {
          this.toBeDoneCount = JSON.parse(localToBeDoneList).filter((e) => !e.doneFlag).length;
        }
      } catch (err) {
        smartSentry.captureError(err);
      }
    },
    //设置登录信息
    setUserLoginInfo(data) {
      // 用户基本信息
      this.token = data.token;
      this.employeeId = data.employeeId;
      this.avatar = data.avatar;
      this.loginName = data.loginName;
      this.actualName = data.actualName;
      this.phone = data.phone;
      this.departmentId = data.departmentId;
      this.departmentName = data.departmentName;
      this.needUpdatePwdFlag = data.needUpdatePwdFlag;
      this.administratorFlag = data.administratorFlag;
      this.lastLoginIp = data.lastLoginIp;
      this.lastLoginIpRegion = data.lastLoginIpRegion;
      this.lastLoginUserAgent = data.lastLoginUserAgent;
      this.lastLoginTime = data.lastLoginTime;

      // 添加智能视频菜单（用于开发测试）
      let menuList = [...data.menuList];
      const smartVideoMenuId = addSmartVideoMenu(menuList);

      //菜单权限
      this.menuTree = buildMenuTree(menuList);

      //拥有路由的菜单
      this.menuRouterList = menuList.filter((e) => e.path || e.frameUrl);

      //父级菜单集合
      this.menuParentIdListMap = buildMenuParentIdListMap(this.menuTree);

      //功能点
      this.pointsList = menuList.filter((menu) => menu.menuType === MENU_TYPE_ENUM.POINTS.value && menu.visibleFlag && !menu.disabledFlag);

      // 获取用户未读消息
      this.queryUnreadMessageCount();
      // 获取待办工作数
      this.queryToBeDoneList();
    },

    setToken(token) {
      this.token = token;
    },

    //设置标签页
    setTagNav(route, from) {
      if (_.isNull(this.tagNav)) {
        let localTagNav = localRead(localKey.USER_TAG_NAV) || '';
        this.tagNav = localTagNav ? JSON.parse(localTagNav) : [];
      }
      // name唯一标识
      let name = route.name;
      if (!name || name === HOME_PAGE_NAME || name === '403' || name === '404') {
        return;
      }
      let findTag = (this.tagNav || []).find((e) => e.menuName === name);
      if (findTag) {
        // @ts-ignore
        findTag.fromMenuName = from.name;
        findTag.fromMenuQuery = from.query;
        findTag.menuQuery = route.query;
      } else {
        // @ts-ignore
        this.tagNav.push({
          // @ts-ignore
          menuName: name,
          // @ts-ignore
          menuTitle: route.meta.title,
          menuQuery: route.query,
          menuIcon:route.meta.icon,
          // @ts-ignore
          fromMenuName: from.name,
          fromMenuQuery: from.query,
        });
      }
      localSave(localKey.USER_TAG_NAV, JSON.stringify(this.tagNav));
    },
    //关闭标签页
    closeTagNav(menuName, closeAll) {
      if (_.isEmpty(this.getTagNav)) return;
      if (closeAll && !menuName) {
        this.tagNav = [];
        this.clearKeepAliveIncludes();
      } else {
        let findIndex = (this.tagNav || []).findIndex((e) => e.menuName === menuName);
        if (closeAll) {
          if (findIndex === -1) {
            this.tagNav = [];
            this.clearKeepAliveIncludes();
          } else {
            let tagNavElement = (this.tagNav || [])[findIndex];
            this.tagNav = [tagNavElement];
            this.clearKeepAliveIncludes(tagNavElement.menuName);
          }
        } else {
          (this.tagNav || []).splice(findIndex, 1);
          this.deleteKeepAliveIncludes(menuName);
        }
      }
      localSave(localKey.USER_TAG_NAV, JSON.stringify(this.tagNav));
    },
    //关闭页面
    closePage(route, router, path) {
      if (!this.getTagNav || _.isEmpty(this.getTagNav)) return;
      if (path) {
        router.push({ path });
      } else {
        // 寻找tagNav
        let index = this.getTagNav.findIndex((e) => e.menuName === route.name);
        if (index === -1) {
          router.push({ name: HOME_PAGE_NAME });
        } else {
          let tagNav = this.getTagNav[index];
          if (tagNav.fromMenuName && this.getTagNav.some((e) => e.menuName === tagNav.fromMenuName)) {
            router.push({ name: tagNav.fromMenuName, query: tagNav.fromMenuQuery });
          } else {
            // 查询左侧tag
            let leftTagNav = this.getTagNav[index - 1];
            router.push({ name: leftTagNav.menuName, query: leftTagNav.menuQuery });
          }
        }
      }
      this.closeTagNav(route.name, false);
    },
    // 加入缓存
    pushKeepAliveIncludes(val) {
      if (!val) {
        return;
      }
      if (!this.keepAliveIncludes) {
        this.keepAliveIncludes = [];
      }
      if (this.keepAliveIncludes.length < 30) {
        let number = this.keepAliveIncludes.findIndex((e) => e === val);
        if (number === -1) {
          this.keepAliveIncludes.push(val);
        }
      }
    },
    // 删除缓存
    deleteKeepAliveIncludes(val) {
      if (!this.keepAliveIncludes || !val) {
        return;
      }
      let number = this.keepAliveIncludes.findIndex((e) => e === val);
      if (number !== -1) {
        this.keepAliveIncludes.splice(number, 1);
      }
    },
    // 清空缓存
    clearKeepAliveIncludes(val) {
      if (!val || !this.keepAliveIncludes.includes(val)) {
        this.keepAliveIncludes = [];
        return;
      }
      this.keepAliveIncludes = [val];
    },
  },
});

/**
 * 构建菜单父级集合
 */
function buildMenuParentIdListMap(menuTree) {
  let menuParentIdListMap = new Map();
  recursiveBuildMenuParentIdListMap(menuTree, [], menuParentIdListMap);
  return menuParentIdListMap;
}

function recursiveBuildMenuParentIdListMap(menuList, parentMenuList, menuParentIdListMap) {
  for (const e of menuList) {
    // 顶级parentMenuList清空
    if (e.parentId === 0) {
      parentMenuList = [];
    }
    let menuIdStr = e.menuId.toString();
    let cloneParentMenuList = _.cloneDeep(parentMenuList);
    if (!_.isEmpty(e.children) && e.menuName) {
      // 递归
      cloneParentMenuList.push({ name: menuIdStr, title: e.menuName });
      recursiveBuildMenuParentIdListMap(e.children, cloneParentMenuList, menuParentIdListMap);
    } else {
      // 对于叶子节点（没有子菜单的菜单），存储其父级路径
      menuParentIdListMap.set(menuIdStr, cloneParentMenuList);
    }
  }
}

/**
 * 构建菜单树
 *
 * @param  menuList
 * @returns
 */
function buildMenuTree(menuList) {
  //1 获取所有 有效的 目录和菜单
  let catalogAndMenuList = menuList.filter((menu) => menu.menuType !== MENU_TYPE_ENUM.POINTS.value && menu.visibleFlag && !menu.disabledFlag);

  //2 获取顶级目录
  let topCatalogList = catalogAndMenuList.filter((menu) => menu.parentId === 0);
  for (const topCatalog of topCatalogList) {
    buildMenuChildren(topCatalog, catalogAndMenuList);
  }
  return topCatalogList;
}

function buildMenuChildren(menu, allMenuList) {
  let children = allMenuList.filter((e) => e.parentId === menu.menuId);
  if (children.length === 0) {
    return;
  }
  menu.children = children;
  for (const item of children) {
    buildMenuChildren(item, allMenuList);
  }
}

/**
 * 添加智能视频菜单到菜单列表中
 * 此函数用于开发测试阶段，在监控服务和代码生成之间插入智能视频菜单
 *
 * 重要：使用固定的 menuId (9000-9999 范围) 以避免与动态菜单冲突
 * 并确保路由的 name 与 menuId 一致
 */
function addSmartVideoMenu(menuList) {
  // 使用固定的 menuId 范围（9000-9999），避免与现有菜单冲突
  const SMART_VIDEO_MENU_ID_BASE = 9000;

  // 智能视频主模块
  const smartVideoMenu = {
    menuId: SMART_VIDEO_MENU_ID_BASE,
    menuName: '智能视频',
    menuTitle: '智能视频',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: 0,
    path: '',
    component: '',
    icon: 'VideoCameraOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 35,
    updateTime: new Date().toISOString(),
  };

  // 1. 系统概览 - menuId: 9001（无子菜单）
  const systemOverviewMenu = {
    menuId: 9001,
    menuName: '系统概览',
    menuTitle: '系统概览',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: smartVideoMenu.menuId,
    path: '/business/smart-video/system-overview',
    component: '/business/smart-video/system-overview.vue',
    icon: 'DashboardOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 2. 设备管理（目录）- menuId: 9100
  const deviceManagementCatalog = {
    menuId: 9100,
    menuName: '设备管理',
    menuTitle: '设备管理',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'DatabaseOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 2.1 设备 - menuId: 9101
  const deviceMenu = {
    menuId: 9101,
    menuName: '设备',
    menuTitle: '设备',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: deviceManagementCatalog.menuId,
    path: '/business/smart-video/device-list',
    component: '/business/smart-video/device-list.vue',
    icon: 'VideoCameraOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 2.2 分组 - menuId: 9102
  const deviceGroupMenu = {
    menuId: 9102,
    menuName: '分组',
    menuTitle: '分组',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: deviceManagementCatalog.menuId,
    path: '/business/smart-video/device-group',
    component: '/business/smart-video/device-group.vue',
    icon: 'ApartmentOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 3. 视频监控（目录）- menuId: 9200
  const videoMonitorCatalog = {
    menuId: 9200,
    menuName: '视频监控',
    menuTitle: '视频监控',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'PlayCircleOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 3,
    updateTime: new Date().toISOString(),
  };

  // 3.1 监控预览 - menuId: 9201
  const monitorPreviewMenu = {
    menuId: 9201,
    menuName: '监控预览',
    menuTitle: '监控预览',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: videoMonitorCatalog.menuId,
    path: '/business/smart-video/monitor-preview',
    component: '/business/smart-video/monitor-preview.vue',
    icon: 'EyeOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 3.2 录像回放 - menuId: 9202
  const videoPlaybackMenu = {
    menuId: 9202,
    menuName: '录像回放',
    menuTitle: '录像回放',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: videoMonitorCatalog.menuId,
    path: '/business/smart-video/video-playback',
    component: '/business/smart-video/video-playback.vue',
    icon: 'PlaySquareOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 4. 解码上墙（目录）- menuId: 9300
  const decoderWallCatalog = {
    menuId: 9300,
    menuName: '解码上墙',
    menuTitle: '解码上墙',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'AppstoreOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 4,
    updateTime: new Date().toISOString(),
  };

  // 4.1 解码器 - menuId: 9301
  const decoderMenu = {
    menuId: 9301,
    menuName: '解码器',
    menuTitle: '解码器',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: decoderWallCatalog.menuId,
    path: '/business/smart-video/decoder-management',
    component: '/business/smart-video/decoder-management.vue',
    icon: 'ApiOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 4.2 电视墙 - menuId: 9302
  const tvWallMenu = {
    menuId: 9302,
    menuName: '电视墙',
    menuTitle: '电视墙',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: decoderWallCatalog.menuId,
    path: '/business/smart-video/tv-wall',
    component: '/business/smart-video/tv-wall.vue',
    icon: 'DesktopOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 4.3 大屏管控 - menuId: 9303
  const screenControlMenu = {
    menuId: 9303,
    menuName: '大屏管控',
    menuTitle: '大屏管控',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: decoderWallCatalog.menuId,
    path: '/business/smart-video/screen-control',
    component: '/business/smart-video/screen-control.vue',
    icon: 'MonitorOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 3,
    updateTime: new Date().toISOString(),
  };

  // 5. 智能分析（目录）- menuId: 9400
  const intelligentAnalysisCatalog = {
    menuId: 9400,
    menuName: '智能分析',
    menuTitle: '智能分析',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'ExperimentOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 5,
    updateTime: new Date().toISOString(),
  };

  // 5.1 算法模式 - menuId: 9401
  const algorithmModeMenu = {
    menuId: 9401,
    menuName: '算法模式',
    menuTitle: '算法模式',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: intelligentAnalysisCatalog.menuId,
    path: '/business/smart-video/algorithm-mode',
    component: '/business/smart-video/algorithm-mode.vue',
    icon: 'FunctionOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 5.2 目标智能 - menuId: 9402
  const targetIntelligenceMenu = {
    menuId: 9402,
    menuName: '目标智能',
    menuTitle: '目标智能',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: intelligentAnalysisCatalog.menuId,
    path: '/business/smart-video/target-intelligence',
    component: '/business/smart-video/target-intelligence.vue',
    icon: 'AimOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 5.3 行为分析 - menuId: 9403
  const behaviorAnalysisMenu = {
    menuId: 9403,
    menuName: '行为分析',
    menuTitle: '行为分析',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: intelligentAnalysisCatalog.menuId,
    path: '/business/smart-video/behavior-analysis',
    component: '/business/smart-video/behavior-analysis.vue',
    icon: 'BranchesOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 3,
    updateTime: new Date().toISOString(),
  };

  // 5.4 人群态势 - menuId: 9404
  const crowdSituationMenu = {
    menuId: 9404,
    menuName: '人群态势',
    menuTitle: '人群态势',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: intelligentAnalysisCatalog.menuId,
    path: '/business/smart-video/crowd-situation',
    component: '/business/smart-video/crowd-situation.vue',
    icon: 'TeamOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 4,
    updateTime: new Date().toISOString(),
  };

  // 6. 检索功能（目录）- menuId: 9500
  const searchFunctionCatalog = {
    menuId: 9500,
    menuName: '检索功能',
    menuTitle: '检索功能',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'SearchOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 6,
    updateTime: new Date().toISOString(),
  };

  // 6.1 目标检索 - menuId: 9501
  const targetSearchMenu = {
    menuId: 9501,
    menuName: '目标检索',
    menuTitle: '目标检索',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: searchFunctionCatalog.menuId,
    path: '/business/smart-video/target-search',
    component: '/business/smart-video/target-search.vue',
    icon: 'ScanOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 6.2 以图搜图 - menuId: 9502
  const imageSearchMenu = {
    menuId: 9502,
    menuName: '以图搜图',
    menuTitle: '以图搜图',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: searchFunctionCatalog.menuId,
    path: '/business/smart-video/image-search',
    component: '/business/smart-video/image-search.vue',
    icon: 'PictureOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 7. 联动管理（目录）- menuId: 9600
  const linkageManagementCatalog = {
    menuId: 9600,
    menuName: '联动管理',
    menuTitle: '联动管理',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'LinkOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 7,
    updateTime: new Date().toISOString(),
  };

  // 7.1 联动设置 - menuId: 9601
  const linkageSettingsMenu = {
    menuId: 9601,
    menuName: '联动设置',
    menuTitle: '联动设置',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: linkageManagementCatalog.menuId,
    path: '/business/smart-video/linkage-settings',
    component: '/business/smart-video/linkage-settings.vue',
    icon: 'SettingOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 7.2 联动记录 - menuId: 9602
  const linkageRecordsMenu = {
    menuId: 9602,
    menuName: '联动记录',
    menuTitle: '联动记录',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: linkageManagementCatalog.menuId,
    path: '/business/smart-video/linkage-records',
    component: '/business/smart-video/linkage-records.vue',
    icon: 'HistoryOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 8. 系统分析（目录）- menuId: 9700
  const systemAnalysisCatalog = {
    menuId: 9700,
    menuName: '系统分析',
    menuTitle: '系统分析',
    menuType: MENU_TYPE_ENUM.CATALOG.value,
    parentId: smartVideoMenu.menuId,
    path: '',
    component: '',
    icon: 'BarChartOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 8,
    updateTime: new Date().toISOString(),
  };

  // 8.1 过热图 - menuId: 9701
  const heatmapMenu = {
    menuId: 9701,
    menuName: '过热图',
    menuTitle: '过热图',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: systemAnalysisCatalog.menuId,
    path: '/business/smart-video/heatmap',
    component: '/business/smart-video/heatmap.vue',
    icon: 'HeatMapOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 1,
    updateTime: new Date().toISOString(),
  };

  // 8.2 过热统计 - menuId: 9702
  const heatStatisticsMenu = {
    menuId: 9702,
    menuName: '过热统计',
    menuTitle: '过热统计',
    menuType: MENU_TYPE_ENUM.MENU.value,
    parentId: systemAnalysisCatalog.menuId,
    path: '/business/smart-video/heat-statistics',
    component: '/business/smart-video/heat-statistics.vue',
    icon: 'DotChartOutlined',
    visibleFlag: true,
    disabledFlag: false,
    cacheFlag: true,
    frameFlag: false,
    frameUrl: '',
    sort: 2,
    updateTime: new Date().toISOString(),
  };

  // 将所有菜单添加到列表中
  menuList.push(
    smartVideoMenu,
    systemOverviewMenu,
    deviceManagementCatalog,
    deviceMenu,
    deviceGroupMenu,
    videoMonitorCatalog,
    monitorPreviewMenu,
    videoPlaybackMenu,
    decoderWallCatalog,
    decoderMenu,
    tvWallMenu,
    screenControlMenu,
    intelligentAnalysisCatalog,
    algorithmModeMenu,
    targetIntelligenceMenu,
    behaviorAnalysisMenu,
    crowdSituationMenu,
    searchFunctionCatalog,
    targetSearchMenu,
    imageSearchMenu,
    linkageManagementCatalog,
    linkageSettingsMenu,
    linkageRecordsMenu,
    systemAnalysisCatalog,
    heatmapMenu,
    heatStatisticsMenu
  );

  console.log('✅ 智能视频菜单已添加 - 包含8个目录和18个菜单项');

  // 返回智能视频主菜单的ID，用于后续处理
  return smartVideoMenu.menuId;
}
