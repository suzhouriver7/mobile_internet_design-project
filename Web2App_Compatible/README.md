# 校园约伴系统 - uni-app 移动端

基于 uni-app 开发的校园约伴系统移动端应用，支持 H5、小程序和 App 多端运行。

## 项目结构

```
Web2App_Compatible/
├── api/                    # API 接口封装
│   ├── auth.js            # 认证相关接口
│   ├── order.js           # 订单相关接口
│   ├── content.js         # 动态相关接口
│   ├── user.js            # 用户相关接口
│   ├── file.js            # 文件上传接口
│   └── index.js           # 统一导出
├── components/             # 公共组件
├── pages/                 # 页面文件
│   ├── auth/              # 认证页面
│   │   ├── login.vue      # 登录页
│   │   └── register.vue   # 注册页
│   ├── index/             # 首页
│   │   └── index.vue      # 首页
│   ├── order/             # 订单相关页面
│   │   ├── list.vue       # 订单列表
│   │   ├── create.vue     # 创建订单
│   │   ├── index.vue      # 订单详情
│   │   └── messages.vue   # 订单消息
│   ├── content/           # 动态相关页面
│   │   ├── list.vue       # 动态列表
│   │   ├── index.vue      # 动态详情
│   │   └── comments.vue   # 评论页面
│   └── user/              # 用户相关页面
│       ├── info.vue        # 用户信息
│       └── edit.vue        # 编辑信息
├── store/                 # Vuex 状态管理
│   ├── index.js           # 主 store
│   ├── user.js            # 用户模块
│   └── order.js           # 订单模块
├── utils/                 # 工具函数
│   ├── request.js         # 网络请求封装
│   ├── config.js          # 配置文件
│   ├── constants.js       # 常量定义
│   └── util.js            # 工具函数
├── static/                # 静态资源
├── App.vue                # 应用入口
├── main.js                # 主入口文件
├── manifest.json          # 应用配置
├── pages.json             # 页面配置
└── package.json          # 依赖配置
```

## 技术栈

- **框架**: uni-app (Vue 3)
- **状态管理**: Vuex 4
- **UI 组件库**: uview-plus
- **HTTP 请求**: uni.request (封装)
- **时间处理**: dayjs

## 快速开始

### 1. 安装依赖

```bash
cd Web2App_Compatible
npm install
```

### 2. 配置 API 地址

编辑 `utils/config.js`，修改 API 基础地址：

```javascript
const devConfig = {
  baseURL: 'http://localhost:8080/api/v1',  // 开发环境
  fileBaseURL: 'http://localhost:8080',
  debug: true
}
```

### 3. 运行项目

#### H5 端
```bash
npm run dev:h5
```

#### 微信小程序
```bash
npm run dev:mp-weixin
```

#### App 端
```bash
npm run dev:app
```

## 功能模块

### 1. 用户认证
- 用户登录（邮箱/学号 + 密码）
- 用户注册
- 自动登录（记住账号）
- Token 刷新

### 2. 订单管理
- 浏览活动列表（支持筛选）
- 发布活动
- 查看活动详情
- 申请加入活动
- 审核申请
- 订单消息聊天

### 3. 动态社区
- 浏览动态列表
- 发布动态（支持图片/视频）
- 查看动态详情
- 点赞/取消点赞
- 评论/回复

### 4. 用户中心
- 查看个人信息
- 编辑个人信息
- 修改密码
- 上传头像

## API 接口说明

所有 API 接口统一封装在 `api/` 目录下，使用统一的请求拦截器处理：

- 自动添加 `X-User-Id` 请求头（从 Vuex store 获取）
- 统一处理响应格式（ApiResponse）
- 统一错误处理（BusinessException）
- Token 过期自动跳转登录

### 使用示例

```javascript
import { orderApi } from '@/api'

// 获取订单列表
const orders = await orderApi.getOrders({
  page: 1,
  size: 10,
  status: 0,
  activityType: 1
})

// 创建订单
const orderId = await orderApi.createOrder({
  activityType: 1,
  genderRequire: 2,
  campus: 0,
  location: '天街海底捞',
  startTime: '2024-03-15 18:30:00',
  maxPeople: 4,
  note: '找人一起吃火锅，AA制'
})
```

## 状态管理

使用 Vuex 4 进行状态管理，模块化设计：

- **user 模块**: 用户信息、登录状态
- **order 模块**: 订单列表、当前订单、筛选条件

### 使用示例

```javascript
import { useStore } from 'vuex'

const store = useStore()

// 获取用户信息
const userInfo = computed(() => store.getters.userInfo)
const isLogin = computed(() => store.getters.isLogin)

// 登录
await store.dispatch('login', loginResponse)

// 退出登录
await store.dispatch('logout')
```

## 页面路由

### TabBar 页面（底部导航）
- `/pages/index/index` - 首页
- `/pages/order/list` - 活动广场
- `/pages/content/list` - 动态社区
- `/pages/user/info` - 我的

### 普通页面
- `/pages/auth/login` - 登录
- `/pages/auth/register` - 注册
- `/pages/order/create` - 发布活动
- `/pages/order/index` - 订单详情
- `/pages/order/messages` - 订单聊天
- `/pages/content/index` - 动态详情
- `/pages/content/comments` - 评论
- `/pages/user/edit` - 编辑信息

## 常量定义

所有枚举常量定义在 `utils/constants.js`：

- `ACTIVITY_TYPE` - 活动类型
- `CAMPUS` - 校区
- `ORDER_STATUS` - 订单状态
- `GENDER_REQUIRE` - 性别要求
- `APPLY_STATUS` - 申请状态

## 注意事项

1. **API 地址配置**: 确保 `utils/config.js` 中的 API 地址正确
2. **用户认证**: 当前使用 `X-User-Id` 请求头进行身份认证
3. **图片资源**: 静态图片放在 `static/images/` 目录
4. **TabBar 图标**: 需要准备 tabBar 图标（未选中/选中状态）
5. **环境变量**: 生产环境需要修改 `config.js` 中的配置

## 开发规范

1. **代码风格**: 使用 ESLint 进行代码检查
2. **组件命名**: 使用 PascalCase
3. **页面命名**: 使用 kebab-case
4. **API 调用**: 统一使用 `api/` 目录下的封装
5. **状态管理**: 统一使用 Vuex，避免直接操作 localStorage

## 常见问题

### 1. 网络请求失败
- 检查后端服务是否启动
- 检查 `config.js` 中的 API 地址是否正确
- 检查是否有跨域问题（H5 端）

### 2. 登录状态丢失
- 检查 `store/index.js` 中的 `initUserState` 是否正确调用
- 检查 localStorage 中是否有用户信息

### 3. 页面跳转失败
- 检查 `pages.json` 中是否配置了对应页面
- 检查页面路径是否正确

## 后续开发建议

1. **完善页面**: 补充订单详情、动态详情、用户信息等页面
2. **优化体验**: 添加加载动画、骨架屏、下拉刷新等
3. **错误处理**: 完善错误提示和异常处理
4. **性能优化**: 图片懒加载、列表虚拟滚动等
5. **功能扩展**: 消息推送、分享功能、搜索功能等

## 联系支持

如有问题，请联系开发团队或查看项目文档。

