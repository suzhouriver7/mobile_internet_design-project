# 校园约伴 App

完整的uni-app移动端项目，与后端API完全对接。

## 快速开始

### 1. 在HBuilderX中打开项目

1. 打开HBuilderX
2. 文件 -> 打开目录
3. 选择 `CampusCompanionApp` 文件夹

### 2. 安装依赖

在HBuilderX终端执行：
```bash
npm install
```

### 3. 运行项目

#### 运行到夜神模拟器：
1. 启动夜神模拟器
2. HBuilderX：运行 -> 运行到手机或模拟器 -> 运行到Android App基座
3. 选择夜神模拟器

#### 运行到浏览器（H5）：
1. HBuilderX：运行 -> 运行到浏览器 -> Chrome

## 配置说明

### API地址配置

默认配置：
- 夜神模拟器：`10.0.2.2:8080`
- 浏览器：`localhost:8080`

如需修改，编辑 `utils/config.js` 或运行时设置：
```javascript
uni.setStorageSync('apiServerIP', '你的IP地址')
```

## 功能列表

- ✅ 用户登录/注册
- ✅ 忘记密码
- ✅ 活动广场（订单列表）
- ✅ 动态社区（内容列表）
- ✅ 发布活动
- ✅ 发布动态
- ✅ 个人中心
- ✅ AI助手
- ✅ 订单详情
- ✅ 动态详情
- ✅ 编辑资料
- ✅ 修改密码

## 项目结构

```
CampusCompanionApp/
├── api/              # API接口
├── pages/            # 页面
├── store/            # 状态管理
├── utils/            # 工具函数
├── App.vue           # 应用入口
├── main.js           # 主入口
├── manifest.json     # 应用配置
├── pages.json        # 页面配置
└── package.json      # 依赖配置
```

## 注意事项

1. **确保后端服务运行**：后端需要运行在 `localhost:8080`
2. **网络配置**：夜神模拟器使用 `10.0.2.2`，真机使用电脑实际IP
3. **数据库**：确保数据库已创建并连接成功

## 技术栈

- uni-app (Vue 3)
- Vuex 4
- dayjs

## 开发

项目已配置完整，可以直接在HBuilderX中运行。

如果遇到构建错误：
1. 清理项目缓存（项目 -> 清理 -> 清理项目缓存）
2. 删除 `unpackage` 文件夹
3. 重新运行
