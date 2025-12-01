# IOE-DREAM 项目结构文档

## 目录组织结构

### 根目录结构

```
IOE-DREAM/
├── smart-admin-api-java17-springboot3/     # 后端项目 (Java 17 + Spring Boot 3)
├── smart-admin-web-javascript/             # 前端项目 (Vue 3 + JavaScript)
├── smart-app/                              # 移动端项目 (uni-app)
├── 数据库SQL脚本/                           # 数据库初始化脚本
├── docs/                                   # 项目文档
├── openspec/                               # OpenSpec规范文档
├── .spec-workflow/                         # OpenSpec工作流配置
├── scripts/                                # 自动化脚本
├── .github/                                # GitHub Actions配置
├── CLAUDE.md                               # AI助手指导文档
└── README.md                               # 项目说明文档
```

### 后端项目结构

```
smart-admin-api-java17-springboot3/
├── sa-base/                                # 基础模块 (公共服务)
│   ├── src/main/java/net/lab1024/sa/base/
│   │   ├── common/                         # 公共组件
│   │   │   ├── config/                     # 配置类
│   │   │   ├── constant/                   # 常量定义
│   │   │   ├── domain/                     # 基础实体类
│   │   │   ├── mapper/                     # 基础数据访问
│   │   │   ├── service/                    # 基础服务接口
│   │   │   ├── util/                       # 工具类
│   │   │   └── validation/                 # 验证组件
│   │   ├── config/                         # 配置文件
│   │   └── resources/                      # 资源文件
│   └── pom.xml                            # Maven配置
│
├── sa-admin/                               # 管理模块 (业务实现)
│   ├── src/main/java/net/lab1024/sa/admin/
│   │   ├── common/                         # 管理模块公共组件
│   │   ├── config/                         # 配置类
│   │   ├── module/                         # 业务模块
│   │   │   ├── access/                     # 门禁模块
│   │   │   ├── consume/                    # 消费模块
│   │   │   ├── attendance/                 # 考勤模块
│   │   │   ├── visitor/                    # 访客模块
│   │   │   ├── video/                      # 视频模块
│   │   │   ├── hr/                         # 人事模块
│   │   │   ├── device/                     # 设备模块
│   │   │   └── notification/               # 通知模块
│   │   └── Application.java               # 启动类
│   ├── src/main/resources/
│   │   ├── mapper/                         # MyBatis映射文件
│   │   ├── static/                         # 静态资源
│   │   └── application-dev.yml            # 开发环境配置
│   └── pom.xml                            # Maven配置
│
└── pom.xml                                # 父级Maven配置
```

### 前端项目结构

```
smart-admin-web-javascript/
├── public/                                 # 公共资源
├── src/
│   ├── api/                               # API接口封装
│   │   ├── business/                      # 业务API
│   │   ├── config/                        # 配置API
│   │   └── system/                        # 系统API
│   ├── assets/                            # 静态资源
│   │   ├── images/                        # 图片资源
│   │   ├── styles/                        # 样式文件
│   │   └── fonts/                         # 字体文件
│   ├── components/                        # 通用组件
│   │   ├── common/                        # 公共组件
│   │   ├── form/                          # 表单组件
│   │   └── table/                         # 表格组件
│   ├── config/                            # 配置文件
│   │   ├── axios.js                       # HTTP配置
│   │   ├── router.js                      # 路由配置
│   │   └── vite.config.js                 # Vite配置
│   ├── constants/                         # 常量定义
│   ├── directives/                        # 自定义指令
│   ├── i18n/                             # 国际化
│   ├── layout/                           # 布局组件
│   ├── lib/                              # 第三方库
│   ├── plugins/                          # 插件配置
│   ├── router/                           # 路由配置
│   ├── store/                            # 状态管理
│   ├── theme/                            # 主题配置
│   ├── utils/                            # 工具函数
│   ├── views/                            # 页面组件
│   │   ├── business/                     # 业务页面
│   │   ├── system/                       # 系统页面
│   │   └── example/                      # 示例页面
│   ├── App.vue                           # 根组件
│   └── main.js                           # 入口文件
├── package.json                          # 依赖配置
└── vite.config.js                        # 构建配置
```

## 命名规范

### 文件命名

#### 后端Java文件
- **实体类 (Entity)**: `PascalCase` + `Entity` 后缀 (例: `UserEntity.java`)
- **值对象 (VO)**: `PascalCase` + `VO` 后缀 (例: `UserVO.java`)
- **数据传输对象 (DTO)**: `PascalCase` + `DTO` 后缀 (例: `UserDTO.java`)
- **表单对象 (Form)**: `PascalCase` + `Form` 后缀 (例: `UserForm.java`)
- **控制器 (Controller)**: `PascalCase` + `Controller` 后缀 (例: `UserController.java`)
- **服务接口 (Service)**: `PascalCase` + `Service` 后缀 (例: `UserService.java`)
- **服务实现 (ServiceImpl)**: `PascalCase` + `ServiceImpl` 后缀 (例: `UserServiceImpl.java`)
- **管理器 (Manager)**: `PascalCase` + `Manager` 后缀 (例: `UserManager.java`)
- **数据访问 (DAO)**: `PascalCase` + `Dao` 后缀 (例: `UserDao.java`)
- **枚举类 (Enum)**: `PascalCase` + `Enum` 后缀 (例: `UserStatusEnum.java`)
- **配置类 (Config)**: `PascalCase` + `Config` 后缀 (例: `WebConfig.java`)

#### 前端JavaScript文件
- **Vue组件**: `PascalCase` (例: `UserList.vue`)
- **JavaScript模块**: `kebab-case` (例: `user-service.js`)
- **TypeScript类型**: `kebab-case` (例: `user-types.ts`)
- **API模块**: `kebab-case` (例: `user-api.js`)
- **工具函数**: `kebab-case` (例: `date-utils.js`)
- **常量文件**: `UPPER_SNAKE_CASE` (例: `API_CONSTANTS.js`)

### 代码命名

#### Java代码
- **类名**: `PascalCase` (例: `UserService`, `UserEntity`)
- **方法名**: `camelCase` (例: `getUserById`, `validateUserForm`)
- **变量名**: `camelCase` (例: `userService`, `userId`)
- **常量**: `UPPER_SNAKE_CASE` (例: `MAX_PAGE_SIZE`, `DEFAULT_TIMEOUT`)
- **包名**: `lowercase` + 点分隔 (例: `net.lab1024.sa.admin.module.user`)

#### JavaScript/TypeScript代码
- **组件名**: `PascalCase` (例: `UserList`, `UserProfile`)
- **函数名**: `camelCase` (例: `getUserList`, `validateForm`)
- **变量名**: `camelCase` (例: `userService`, `isLoading`)
- **常量**: `UPPER_SNAKE_CASE` (例: `API_BASE_URL`, `MAX_RETRY_COUNT`)
- **接口名**: `PascalCase` + `I` 前缀 (例: `IUserService`)

## 导入模式

### Java导入顺序

```java
// 1. Jakarta EE 标准库
import jakarta.persistence.*;
import jakarta.validation.*;

// 2. Spring Framework
import org.springframework.*;
import org.springframework.boot.*;

// 3. 第三方库
import com.baomidou.mybatisplus.*;
import lombok.*;

// 4. 项目内部包
import net.lab1024.sa.base.common.entity.BaseEntity;
import net.lab1024.sa.admin.module.user.domain.entity.UserEntity;

// 5. 同包下的类
import static net.lab1024.sa.admin.common.util.ResponseUtil.*;
```

### JavaScript导入顺序

```javascript
// 1. Node.js 内置模块
import fs from 'fs';
import path from 'path';

// 2. 第三方库
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 3. 项目内部模块 (绝对路径)
import { useUserStore } from '@/store/user';
import { UserAPI } from '@/api/business/user';

// 4. 相对路径导入
import './UserList.css';
import UserForm from './components/UserForm.vue';
```

## 代码结构模式

### Java类结构模式

```java
package net.lab1024.sa.admin.module.user.controller;

// 1. 导入声明
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

// 2. 类注解
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息的增删改查")
public class UserController {

    // 3. 依赖注入
    @Resource
    private UserService userService;

    // 4. 公共常量
    private static final int MAX_PAGE_SIZE = 100;

    // 5. 公共方法 (按功能分组)
    // 5.1 查询方法
    @GetMapping("/list")
    public ResponseDTO<PageResult<UserVO>> getUserList(@RequestBody UserQueryForm queryForm) {
        // 实现逻辑
    }

    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUserById(@PathVariable Long id) {
        // 实现逻辑
    }

    // 5.2 修改方法
    @PostMapping("/add")
    public ResponseDTO<Long> addUser(@Valid @RequestBody UserForm userForm) {
        // 实现逻辑
    }

    @PutMapping("/update")
    public ResponseDTO<Void> updateUser(@Valid @RequestBody UserForm userForm) {
        // 实现逻辑
    }

    // 5.3 删除方法
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> deleteUser(@PathVariable Long id) {
        // 实现逻辑
    }

    // 6. 私有方法
    private UserVO convertToVO(UserEntity entity) {
        // 转换逻辑
    }
}
```

### Vue组件结构模式

```vue
<template>
  <!-- 1. 模板内容 -->
  <div class="user-list">
    <!-- 搜索区域 -->
    <div class="search-form">
      <!-- 表单组件 -->
    </div>

    <!-- 操作区域 -->
    <div class="action-bar">
      <!-- 操作按钮 -->
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <!-- 表格组件 -->
    </div>

    <!-- 分页区域 -->
    <div class="pagination">
      <!-- 分页组件 -->
    </div>
  </div>
</template>

<script setup lang="ts">
// 2. 导入声明
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UserAPI } from '@/api/business/user';
import type { UserVO, UserQueryForm } from '@/types/user';

// 3. 组件属性
const props = defineProps<{
  departmentId?: number;
}>();

// 4. 响应式数据
const loading = ref(false);
const userList = ref<UserVO[]>([]);
const queryParams = reactive<UserQueryForm>({
  pageNum: 1,
  pageSize: 20,
  departmentId: props.departmentId,
});

// 5. 计算属性
const tableColumns = computed(() => [
  // 表格配置
]);

// 6. 生命周期钩子
onMounted(() => {
  loadUserList();
});

// 7. 方法定义
const loadUserList = async () => {
  try {
    loading.value = true;
    const response = await UserAPI.getUserList(queryParams);
    userList.value = response.data.list;
  } catch (error) {
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  // 新增逻辑
};

const handleEdit = (row: UserVO) => {
  // 编辑逻辑
};

const handleDelete = async (row: UserVO) => {
  try {
    await ElMessageBox.confirm('确定删除该用户吗？', '提示');
    await UserAPI.deleteUser(row.id);
    ElMessage.success('删除成功');
    loadUserList();
  } catch (error) {
    // 错误处理
  }
};
</script>

<style scoped lang="scss">
// 8. 样式定义
.user-list {
  padding: 20px;

  .search-form {
    margin-bottom: 20px;
  }

  .action-bar {
    margin-bottom: 20px;
  }

  .table-container {
    background: white;
    border-radius: 4px;
  }
}
</style>
```

## 代码组织原则

### 1. 单一职责原则
- 每个类应该只有一个变化的原因
- 每个文件应该专注于一个功能模块
- 避免过大的类和过长的方法

### 2. 模块化设计
- 按业务功能组织代码结构
- 明确定义模块边界和接口
- 模块间通过定义良好的接口通信

### 3. 可测试性
- 依赖注入优先于依赖查找
- 业务逻辑与外部依赖解耦
- 提供清晰的测试入口点

### 4. 一致性原则
- 遵循项目统一的编码规范
- 使用一致的设计模式和命名约定
- 保持代码风格的一致性

## 模块边界

### 分层架构边界

```
前端层 (Vue Components)
    ↓ API调用 (HTTP/REST)
控制器层 (Controllers)
    ↓ 方法调用
业务逻辑层 (Services)
    ↓ 事务边界
管理器层 (Managers)
    ↓ 数据访问
数据访问层 (DAOs)
    ↓ SQL执行
数据存储层 (MySQL/Redis)
```

### 业务模块边界

#### 核心业务模块
- **用户权限模块**: 用户管理、角色管理、权限管理
- **门禁管理模块**: 门禁设备、通行记录、区域权限
- **消费管理模块**: 账户管理、交易记录、结算管理
- **考勤管理模块**: 考勤记录、排班管理、统计分析
- **访客管理模块**: 访客预约、审批流程、通行管理
- **视频监控模块**: 设备管理、录像存储、智能分析
- **设备管理模块**: 设备注册、状态监控、维护管理
- **通知服务模块**: 消息推送、邮件通知、短信服务

#### 模块依赖规则
- 核心模块不能依赖具体业务模块
- 业务模块间通过服务接口通信
- 避免循环依赖，必要时提取公共服务

## 代码大小指南

### 文件大小限制
- **Java类文件**: 最大500行代码 (超过考虑拆分)
- **Vue组件文件**: 最大400行代码 (模板+脚本+样式)
- **JavaScript模块**: 最大300行代码 (超过考虑拆分)

### 方法/函数大小限制
- **Java方法**: 最大50行代码 (复杂逻辑建议拆分)
- **JavaScript函数**: 最大30行代码 (保持简洁明了)
- **SQL查询**: 最大100行字符 (复杂查询考虑视图)

### 复杂度限制
- **圈复杂度**: 单个方法不超过10
- **嵌套深度**: 最大4层嵌套
- **参数数量**: 单个方法最多5个参数 (超过考虑对象封装)

## 前后端分离架构

### 前后端通信模式

```
前端 (Vue 3 + TypeScript)
    ↓ RESTful API (HTTP/JSON)
后端网关 (Spring Cloud Gateway)
    ↓ 路由转发
微服务集群
├── 用户权限服务
├── 门禁管理服务
├── 消费管理服务
├── 考勤管理服务
├── 访客管理服务
├── 视频监控服务
├── 设备管理服务
└── 通知服务
```

### 数据流向规范

#### 请求数据流
1. 前端组件发起HTTP请求
2. 网关层进行路由和认证
3. 微服务处理业务逻辑
4. 数据访问层操作数据库
5. 响应数据原路返回

#### 响应数据格式
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体业务数据
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 文档标准

### API文档
- 所有REST API必须有OpenAPI/Swagger文档
- 使用标准HTTP状态码
- 提供完整的请求/响应示例
- 包含错误码和处理说明

### 代码注释标准

#### Java类注释
```java
/**
 * 用户管理服务
 *
 * <p>提供用户信息的增删改查功能，包括用户基本信息管理、
 * 权限分配、状态管理等功能。</p>
 *
 * @author 开发团队
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID，不能为null
     * @return 用户信息，如果用户不存在返回null
     * @throws IllegalArgumentException 当userId为null时抛出
     */
    public UserVO getUserById(Long userId) {
        // 实现逻辑
    }
}
```

#### Vue组件注释
```vue
<!--
  用户列表组件

  功能：
  - 展示用户列表数据
  - 支持分页查询
  - 支持用户增删改操作

  @props:
    - departmentId: 部门ID，用于过滤用户

  @events:
    - user-selected: 用户选择事件
    - user-updated: 用户更新事件
-->
<script setup lang="ts">
// 组件实现
</script>
```

### README文档标准
- 每个主要模块必须有README.md
- 说明模块功能和使用方法
- 提供快速开始示例
- 包含依赖说明和配置要求

通过遵循这些结构规范和组织原则，IOE-DREAM项目将保持代码的一致性、可维护性和可扩展性，为团队协作和项目长期发展奠定坚实基础。