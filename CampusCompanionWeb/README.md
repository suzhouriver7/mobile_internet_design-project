## 项目概览（Project Overview）

`Web_vue` 是“校园 Companion”系统的前端应用，面向在校同学提供一站式的校园社交与约伴服务，主要功能包括：

- 账号登录 / 注册 / 忘记密码找回
- 首页聚合：快捷发布动态/订单、我的订单概览、动态流
- 订单模块：订单筛选、列表、详情、申请 / 审核 / 撤销 / 取消、部分字段编辑
- 动态模块：动态列表、详情、发布包含图片的动态、点赞与评论（含楼中楼）、删除
- AI 问询：与后端 AI 服务对话，并基于自然语言对话生成“订单预填草稿”并一键跳转到创建页
- 个人中心：查看并编辑个人基础信息、头像等

整个应用与后端 `CampusCompanionBackend` 通过 REST API 对接，前端只负责 UI 呈现和交互逻辑。

---

## 技术栈（Technical Stack）

主要技术与依赖如下（版本取自 [package.json](package.json)）：

- 框架与基础库
  - Vue 3（`^3.5.24`，组合式 API + `<script setup>`）
  - Vue Router（`^4.6.4`，基于 HTML5 History 的 SPA 路由）
  - Pinia（`^3.0.4`，状态管理，用于 auth / user / order / content 等 store）
- UI 组件与样式
  - Element Plus（`^2.13.0`，UI 组件库：表单、表格、弹窗、布局等）
  - SCSS / SASS（`^1.97.1`，局部和全局样式定制）
- 网络与工具
  - Axios（`^1.13.2`，HTTP 客户端，带统一的拦截器、日志和错误处理）
  - 自定义日志工具（`src/utils/logger.js`）
- 构建与开发工具
  - Vite（`^7.2.4`，打包与本地开发服务器）
  - `@vitejs/plugin-vue`（`^6.0.1`，Vue 单文件组件支持）

运行环境建议：

- Node.js：建议 `>= 18.x`（Vite 7 官方推荐版本），在本项目下实际使用 18+ 测试通过
- 包管理器：推荐使用 `npm`（项目脚本基于 npm 编写），也可使用兼容的 pnpm / yarn，但需自行处理 lockfile

---

## 编译与运行说明（Build & Run Instructions）

### 1. 环境准备

1. 安装 Node.js（建议 18 LTS 或以上）。
2. 在前端目录下安装依赖：

```bash
cd frontend/Web_vue
npm install
```

> 如使用国内源，可自行配置 `npm config set registry https://registry.npmmirror.com` 以加速安装。

### 2. 开发环境运行（Hot Reload）

开发环境通过 Vite 的 dev server 启动，默认端口 `5173`，并通过代理将 `/api` 转发到后端：

- 代理配置：见 [vite.config.js](vite.config.js)
  - `server.port = 5173`
  - 代理规则：`/api -> http://localhost:8080`
  - axios 默认 `baseURL = /api/v1`，开发环境下会被 Vite 代理到后端 `/api/v1`。

启动命令：

```bash
npm run dev
```

启动后访问：`http://localhost:5173/`

### 3. 生产构建与预览

- 生产构建（输出静态资源到 `dist/`）：

```bash
npm run build
```

- 本地预览构建产物：

```bash
npm run preview
```

通常生产环境会将 `dist/` 部署到 Nginx 等静态服务器，并通过反向代理将 `/api` 指向后端 Spring Boot 服务。

### 4. 运行时配置

应用用到了少量环境变量，来自 Vite 的 `import.meta.env`：

- `VITE_API_BASE_URL`（可选）：覆盖 axios 的默认 `baseURL`，例如设置为 `https://your-domain.com/api/v1`；未配置时默认为 `/api/v1`。
- `VITE_FILE_BASE_URL`（可选）：拼接用户头像、动态图片等文件 URL 的基础前缀，未配置时默认为 `http://localhost:8080`。

可通过根目录 `.env` / `.env.production` 等文件配置上述变量。

### 5. 常见问题与排查

- **前端启动 404 / 白屏**：

  - 确认已在 `frontend/Web_vue` 下执行 `npm install`；
  - 确认 Node.js 版本 >= 18；
  - 查看终端是否有 Vite 报错；
  - 打开浏览器控制台（F12）查看是否有编译或运行时错误。
- **接口请求 404 / CORS 报错**：

  - 开发环境下需同时启动后端服务，监听 `http://localhost:8080`；
  - 确认 `vite.config.js` 中 `/api` 代理是否生效；
  - 如后端部署在其他域名或路径，需要配置 `VITE_API_BASE_URL`，避免跨域问题。
- **登录后刷新页面回到登录页 / 闪现未登录状态**：

  - 应用通过 `localStorage.token` + Pinia `auth` store 管理登录态，并在 `main.js` 中挂载前执行一次 `getUserInfo`；
  - 若后端返回 401，前端会清理本地 token 并重定向到 `/login`。
- **AI 问询接口超时**：

  - axios 与 AI 服务 `fullChat` 均将超时设置为 300 秒，若仍有超时，请检查后端 AI 调用配置或网络环境。

---

## 功能列表（Implemented Features）

### 1. 认证与账号管理

- 登录 / 注册

  - 页面：`/login`（[src/views/Login.vue](src/views/Login.vue)）、`/register`（[src/views/RegisterView.vue](src/views/RegisterView.vue)）
  - 功能：
    - 用户使用邮箱 + 密码注册、登录；
    - 登录成功后前端保存 `token` 和 `userId` 到 `localStorage`，并写入 Pinia `auth` store；
    - 登录后刷新仍可保持会话状态，通过 `/users/{id}` 拉取最新用户信息。
- 忘记密码流程

  - 页面：`/forgot-password`（[src/views/ForgotPasswordView.vue](src/views/ForgotPasswordView.vue)）
  - 功能：
    - 验证邮箱是否已注册；
    - 发送重置验证码、校验验证码；
    - 设置新密码完成重置。
- 退出登录

  - 位置：顶部导航栏用户下拉菜单中的“退出登录”（[src/App.vue](src/App.vue)）
  - 行为：调用后端 `/auth/logout`，并在前端清理 token / user / userId 与用户相关缓存，跳转到登录页。

### 2. 首页聚合（/）

页面：`/`（[src/views/Home.vue](src/views/Home.vue)）

- 顶部快捷发布区：
  - 支持直接输入文本发布动态；
  - 提供“发布订单”快捷按钮跳转到 `/orders/create`。
- 订单状态总览：
  - “我发布的订单”：展示当前用户作为发布者的订单列表；
  - “我申请的订单”：展示当前用户申请过的订单及申请状态；
  - 支持点击卡片跳转到订单详情。
- 动态流：
  - 分页展示最新动态，包含发布者信息、内容文本、图片、点赞数、评论数；
  - 支持点赞 / 取消点赞、点击进入详情查看评论。

### 3. 订单模块

- 订单列表（/orders）

  - 页面：`/orders`（[src/views/OrdersView.vue](src/views/OrdersView.vue)）
  - 功能：
    - 通过活动类型 / 校区 / 状态筛选订单；
    - 可选择“我发布的”仅查看自己创建的订单；
    - 列表中展示发布者头像与昵称、活动类型、校区、地点、时间、人数、状态等；
    - 支持分页；
    - 操作列提供“详情”与“申请/取消订单”等按钮，按钮文案与禁用状态由复杂的业务规则控制：
      - 发布者：在状态为 `PENDING` 时可“取消订单”；
      - 非发布者：根据人数、状态和是否已申请显示“申请 / 已申请 / 已过期 / 已取消 / 人数已满”等文案。
- 创建订单（/orders/create）

  - 页面：`/orders/create`（[src/views/CreateOrderView.vue](src/views/CreateOrderView.vue)）
  - 功能：
    - 选择活动类型、校区、性别要求、开始时间、地点、人数上限、备注等；
    - 开始时间不能早于当前时间；
    - 支持从 AI 问询页跳转时通过 URL Query 预填写表单（详见“AI 问询与订单助手”）；
    - 提交后调用 `/orders` 创建订单。
- 订单详情（/orders/:id）

  - 页面：`/orders/:id`（[src/views/OrderDetailView.vue](src/views/OrderDetailView.vue)）
  - 功能：
    - 展示发布者信息（头像、昵称、ID 等）、订单基础信息（活动、时间、地点、人数、备注、状态、创建/更新时间）；
    - 发布者或管理员可在详情页编辑部分字段：地点、开始时间、人数上限、备注，并有本地“修改记录”列表；
    - 右侧“申请信息”卡片展示所有申请：申请人头像、昵称、申请时间、状态；
    - 发布者可以对待审核申请进行“通过 / 拒绝”操作；
    - 申请人可以撤销自己的申请；
    - 顶部提供“返回订单列表”按钮。

### 4. 动态模块

- 动态列表（/contents）

  - 页面：`/contents`（[src/views/ContentsView.vue](src/views/ContentsView.vue)）
  - 功能：
    - 分页展示所有动态，包括文本和图片；
    - 支持点赞 / 取消点赞、进入详情页查看完整内容和评论；
    - 管理员 / 作者可删除动态，删除后会同步删除后端记录和关联媒体。
- 发布动态（/contents/create）

  - 页面：`/contents/create`（[src/views/CreateContentView.vue](src/views/CreateContentView.vue)）
  - 功能：
    - 输入文本内容，上传多张图片；
    - 调用 `/contents` 创建动态，随后一个或多个 `/contents/{id}/media` 上传图片；
    - 成功后跳转到首页或动态列表。
- 动态详情（/contents/:id）

  - 页面：`/contents/:id`（[src/views/ContentDetailView.vue](src/views/ContentDetailView.vue)）
  - 功能：
    - 展示单条动态的全部内容、发布者信息、图片；
    - 支持树形评论（楼中楼），通过 [src/components/CommentItem.vue](src/components/CommentItem.vue) 递归渲染；
    - 作者或管理员可删除动态；
    - 评论作者或管理员可删除评论及其子评论（前端调用 `/contents/comments/{commentId}`，后端递归删除）。

### 5. AI 问询与订单助手

- AI 问询页（/ai）

  - 页面：`/ai`（[src/views/AIView.vue](src/views/AIView.vue)）
  - 功能：
    - 基于后端 `/ai/chat/full` 服务的完整对话：消息列表、Markdown 渲染、输入框支持 Enter 发送 / Shift+Enter 换行；
    - 支持较长上下文对话，前端与后端都将超时调高以适配大模型回答；
    - 统一错误处理与加载状态展示。
- AI 驱动的“订单预填草稿”

  - 技术位置：
    - 本地解析服务：`src/mcp/orderAssistant.js`
    - MCP 注册与工具：`src/utils/mcp.js`
    - 入口：`src/views/AIView.vue`
  - 交互流程：
    1. 用户在 AI 问询中输入“帮我约/帮我生成订单/帮我预填订单”等自然语言；
    2. 前端先正常向后端发送 AI 对话请求并展示自然语言回复；
    3. 若输入命中触发词，前端会调用本地 `orderAssistantService`：
       - 依据关键词和正则（活动类型、校区、人数、性别、地点、时间等）从用户输入文本中抽取一个“订单草稿”；
       - 构造用于 `/orders/create` 的 URL Query 参数；
    4. AIView 在对话中追加一条特殊类型消息 `type: 'order-draft-suggestion'`：
       - 展示解析出的活动类型、校区、地点、时间、性别要求、人数上限的中文预览；
       - 提供“点击跳转并预填写订单”按钮；
    5. 用户点击按钮后，路由跳转到 `/orders/create` 并携带 query，创建订单页从 query 中读取并预填表单。

> 当前解析能力主要依赖本地规则，对“明天/后天 上午/下午”等自然语言时间支持基础，但仍有提升空间；未来可通过让后端 AI 输出结构化字段来完善这一能力。

### 6. 个人中心

- 页面：`/user/profile`（[src/views/UserProfileView.vue](src/views/UserProfileView.vue)）
- 功能：
  - 展示当前用户头像、昵称、邮箱等基础信息；
  - 支持修改昵称、性别、签名、头像等（具体能力视后端接口而定）；
  - 管理员用户会在 UI 上获得更多操作入口（如删除动态、评论、订单等）。

---

## 路由-功能-文件映射（Routes & Mapping）

### 1. 路由表概览

路由定义见 [src/router/index.js](src/router/index.js)：

| 路径                 | 名称               | 组件                             | 说明                        | `requiresAuth` |
| -------------------- | ------------------ | -------------------------------- | --------------------------- | ---------------- |
| `/`                | `Home`           | `views/Home.vue`               | 首页聚合，订单概览 + 动态流 | ✅               |
| `/login`           | `Login`          | `views/Login.vue`              | 登录页                      | ❌               |
| `/forgot-password` | `ForgotPassword` | `views/ForgotPasswordView.vue` | 忘记密码流程                | ❌               |
| `/register`        | `Register`       | `views/RegisterView.vue`       | 注册页                      | ❌               |
| `/orders`          | `Orders`         | `views/OrdersView.vue`         | 订单列表与筛选              | ✅               |
| `/orders/create`   | `CreateOrder`    | `views/CreateOrderView.vue`    | 创建订单页                  | ✅               |
| `/orders/:id`      | `OrderDetail`    | `views/OrderDetailView.vue`    | 订单详情、审核、编辑        | ✅               |
| `/contents`        | `Contents`       | `views/ContentsView.vue`       | 动态列表                    | ✅               |
| `/contents/create` | `CreateContent`  | `views/CreateContentView.vue`  | 发布动态                    | ✅               |
| `/contents/:id`    | `ContentDetail`  | `views/ContentDetailView.vue`  | 动态详情和评论              | ✅               |
| `/ai`              | `AI`             | `views/AIView.vue`             | AI 问询与订单助手           | ✅               |
| `/user/profile`    | `UserProfile`    | `views/UserProfileView.vue`    | 个人中心                    | ✅               |

路由守卫逻辑：

- 在 `beforeEach` 中：
  - 根据 `to.meta.title` 设置页面标题 `${title} - 校园 Companion`；
  - 若 `to.meta.requiresAuth === true` 且 `localStorage` 中无 `token`，则重定向到 `/login`，并带上 `redirect=to.fullPath` 以便登录后回跳；
  - 否则允许访问。
- 在 `afterEach` 中，通过 `logger.event('ROUTE_NAVIGATE', ...)` 记录路由跳转日志。

### 2. 目录结构与关键文件说明

前端目录结构（节选）：

- `src/main.js`：应用入口，创建 Vue 实例，注册 Element Plus / Pinia / Router，并在挂载前执行登录态初始化与全局错误监听。
- `src/App.vue`：根组件，包含全局导航栏（顶部菜单 + 用户信息）、路由出口和页脚。
- `src/router/`：路由配置与导航守卫。
- `src/views/`：页面级组件：
  - `Home.vue`：首页聚合；
  - `Login.vue`、`RegisterView.vue`、`ForgotPasswordView.vue`：认证相关页面；
  - `OrdersView.vue`、`CreateOrderView.vue`、`OrderDetailView.vue`：订单模块页面；
  - `ContentsView.vue`、`CreateContentView.vue`、`ContentDetailView.vue`：动态模块页面；
  - `AIView.vue`：AI 问询与订单助手页面；
  - `UserProfileView.vue`：个人中心页面。
- `src/stores/`：Pinia store：
  - `auth.js`：登录态、token、用户信息；
  - `user.js`：用户详情、列表等（供个人中心使用）；
  - `order.js`：订单相关 API 封装与临时状态；
  - `content.js`：动态 / 评论 / 点赞相关 API 封装与状态；
- `src/services/`：对后端 REST 接口的薄封装：
  - `auth.js`：登录、注册、邮箱验证码、密码重置等接口；
  - `ai.js`：简单对话与完整 `fullChat` 接口；
- `src/utils/`：工具函数与通用封装：
  - `axios.js`：axios 实例、拦截器与统一错误处理；
  - `logger.js`：统一日志工具（`info` / `error` / `event` 等）；
  - `mcp.js`：轻量级本地“服务注册中心”，供 `orderAssistant` 等前端服务注册与调用。
- `src/mcp/`：本地“代理服务”：
  - `orderAssistant.js`：分析自然语言文本，生成订单草稿和 URL Query 参数。
- `src/components/`：可复用组件：
  - `CommentItem.vue`：递归评论组件，支持多级回复和删除；
  - `ThumbFilled.vue` / `ThumbOutline.vue`：自定义点赞图标组件。

---

## 技术亮点与架构设计（Technical Highlights）

### 1. 登录态与权限控制

- 登录态管理：

  - `auth` store 统一持有 `user` 与 `token`；
  - `main.js` 在应用挂载前读取 `localStorage.token`，如存在则调用 `/users/{id}` 获取最新用户信息；
  - 若获取失败（例如 token 过期），会自动调用 `logout()` 清理本地状态，避免“假登录”状态。
- 路由级权限校验：

  - 所有需要登录的页面都通过 `meta.requiresAuth = true` 声明；
  - `router.beforeEach` 根据 `token` 拦截未登录访问，并附带 `redirect` 回跳参数。

### 2. HTTP 封装与全局错误处理

- axios 实例（`utils/axios.js`）：

  - 统一配置 `baseURL` 和 5 分钟超时，适配 AI 长请求；
  - 请求拦截器：
    - 记录开始时间；
    - 自动附加 `Authorization: Bearer <token>` 与 `X-User-Id` 头；
    - 通过 `logger.info('API_REQUEST', ...)` 记录接口请求，控制台额外打印简要信息。
  - 响应拦截器：
    - 记录耗时和响应状态；
    - 对 401/403/404/500 等状态进行分类处理；
    - 401 时自动清除 token 并跳转到 `/login`，保证前端不处于错误会话。
- 全局错误监控（`main.js`）：

  - `app.config.errorHandler` 捕获 Vue 运行时错误，统一上报 `logger.error('VUE_ERROR', ...)`；
  - 监听 `window.error` 和 `unhandledrejection`，捕获全局脚本错误与未处理的 Promise 拒绝，记录到日志系统中，便于排查线上问题。

### 3. AI 问询与“前端 MCP”模式

- AI 服务调用（`services/ai.js`）：

  - 提供 `fullChat(payload)` 方法，直接对接后端 AI 控制器，支持自定义模型、温度、最大 Token 等参数；
  - 在单个请求上再显式配置 300 秒超时，以防浏览器或代理提前中断长响应。
- 本地 MCP 风格服务（`utils/mcp.js` + `mcp/orderAssistant.js`）：

  - `utils/mcp.js` 提供轻量服务注册/调用能力，使前端可以像“调用本地微服务”一样使用复杂逻辑；
  - `orderAssistant.js` 封装了从自然语言文本解析订单草稿的全部逻辑：
    - 活动类型（篮球、羽毛球、吃饭、自习、看电影、跑步、游戏等）；
    - 校区（良乡、中关村、珠海、西山、其他）；
    - 性别要求（女生优先 / 仅限女生 / 男生优先 / 仅限男生等）；
    - 人数（“X人/位/个”）；
    - 地点（“在XXX”）；
    - 时间（“今天/明天/后天 + HH点/HH:MM” → 格式化到 `YYYY-MM-DD HH:mm:00`）；
    - 备注（整句文案）。
  - 通过这种方式，将复杂的文本解析逻辑隔离在单一模块，便于后续替换或升级为 AI 驱动的结构化输出。

### 4. 管理员与普通用户的权限差异

- 在订单与动态相关页面中，通过 `authStore.user.userType` 区分管理员（`ADMIN` 或 `1`）与普通用户：
  - 管理员在列表和详情页上视为“所有订单的拥有者”，可进行取消、审核等操作；
  - 评论与动态删除按钮只对作者或管理员可见；
  - 这些判断逻辑集中在各页面的 `isPublisher` / `canAuditApply` / `canCancelApply` 等辅助函数中，便于维护。

### 5. 交互与性能细节

- 使用 Element Plus 的 `el-skeleton`、`el-empty` 等组件优化加载体验；
- 通过分页与按需请求控制列表类页面的网络与渲染压力；
- 大部分页面将字典（状态、类型、校区等）以 Map 的形式集中定义，保证展示文案的一致性；
- 在图片展示上添加 `loading="lazy"`，减轻首屏加载压力。

---

## 其他信息（Additional Information）

### 已知限制

- AI 文本解析对自然语言时间表达（例如“明天早上”、“后天下午”）的支持有限，主要面向较标准的“今天/明天/后天 + 具体时间点”格式；
- 当前前端对 AI 订单草稿的解析仍以本地规则为主，尚未完全对齐后端 AI 润色后的结构化结果，可能出现“AI 回复文案”和“预填预览”不一致的情况；
- 管理员权限依赖后端准确返回 `userType`，如后端返回不一致可能导致前端权限展示异常。

### 未来改进方向

- 与后端约定 AI 返回结构化订单草稿字段（activityType / campus / startTime / location / gender / maxPeople / note），前端仅负责展示与预填，减少本地正则解析；
- 为核心业务流程增加更细粒度的 loading 与错误提示，例如在订单申请 / 审核的按钮上增加独立 loading 状态；
- 引入单元测试 / 端到端测试（如 Vitest + Playwright），覆盖核心交互流程；
- 对移动端样式进一步优化，使首页与详情页在小屏幕上有更好的布局与手势体验。
