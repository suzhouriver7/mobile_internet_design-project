# 校园约伴系统 - Campus Companion

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![uni-app](https://img.shields.io/badge/uni--app-latest-blue.svg)](https://uniapp.dcloud.io/)

> 本项目为北京理工大学2023级计算机学院本科生**谷奕辰、孙健柏、伍奕涛、张元宏**四位同学的专业限选课《移动互联分析与设计》的结课作业项目

## 📋 项目概述

校园约伴系统（Campus Companion）是一款面向校园用户的综合性社交平台，旨在解决校园内学生之间约伴进行各类活动（如运动、聚餐、学习、娱乐等）的需求。系统采用前后端分离架构，提供**移动端App**、**Web前端**和**后端服务**三端支持，为用户提供便捷的约伴服务和丰富的社交体验。

### 核心功能

- 🎯 **活动约伴**：发布和参与各类校园活动（运动、聚餐、学习、娱乐等）
- 📱 **动态社区**：发布动态、评论互动、点赞分享
- 🤖 **AI助手**：智能对话助手，提供个性化建议
- 👤 **用户系统**：完整的用户认证、信息管理功能
- 💬 **实时聊天**：活动参与者之间的实时消息交流
- 📊 **活动管理**：活动申请、状态管理、历史记录

## 🏗️ 项目架构

本项目采用前后端分离架构，包含以下三个部分：

```
mobile_internet_design-project/
├── CampusCompanionApp/          # 移动端App（uni-app）
├── CampusCompanionBackend/      # 后端服务（Spring Boot）
├── frontend/                    # Web前端（Vue 3）
│   └── Web_vue/
└── docs/                        # 项目文档
    ├── DemandAnalysis/          # 需求分析文档
    └── SystemAnalysis/          # 系统分析文档
```

### 技术栈

#### 移动端App
- **框架**：uni-app (Vue 3)
- **状态管理**：Vuex 4
- **时间处理**：dayjs
- **开发工具**：HBuilderX
- **支持平台**：Android、iOS、H5

#### Web前端
- **框架**：Vue 3
- **构建工具**：Vite
- **UI组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **HTTP客户端**：Axios

#### 后端服务
- **框架**：Spring Boot 4.0.0
- **Java版本**：21
- **数据库**：MySQL
- **ORM**：Spring Data JPA / Hibernate
- **安全认证**：Spring Security + JWT
- **邮件服务**：Spring Mail
- **API文档**：RESTful API

## 🚀 快速开始

### 环境要求

- **Java**：JDK 21+
- **Node.js**：v16+
- **MySQL**：8.0+
- **Maven**：3.6+
- **HBuilderX**（仅移动端开发需要）

### 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE campus_companion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据库连接信息（`CampusCompanionBackend/src/main/resources/application.properties`）：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campus_companion?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. 数据库初始化：
   - 后端使用JPA自动建表（`spring.jpa.hibernate.ddl-auto=update`）
   - 或使用 `docs/SystemAnalysis/create.sql` 手动导入

### 后端启动

```bash
cd CampusCompanionBackend

# 使用Maven运行
./mvnw spring-boot:run

# 或使用IDE直接运行 CampusCompanionBackendApplication.java
```

后端服务默认运行在：`http://localhost:8080`

### 移动端App启动

```bash
cd CampusCompanionApp

# 安装依赖
npm install
```

在HBuilderX中：
1. 文件 → 打开目录 → 选择 `CampusCompanionApp`
2. 运行 → 运行到手机或模拟器 → 运行到Android App基座（或浏览器）

**API地址配置**：
- 夜神模拟器：`http://10.0.2.2:8080/api/v1`
- 浏览器：`http://localhost:8080/api/v1`
- 真机：`http://你的电脑IP:8080/api/v1`

### Web前端启动

```bash
cd frontend/Web_vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

Web前端默认运行在：`http://localhost:5173`

## 📁 项目结构

### 后端结构
```
CampusCompanionBackend/
├── src/main/java/dev/campuscompanionbackend/
│   ├── config/              # 配置类（安全、跨域等）
│   ├── controller/          # REST控制器
│   ├── service/             # 业务逻辑层
│   ├── repository/          # 数据访问层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── enums/               # 枚举类
│   ├── exception/           # 异常处理
│   └── utils/               # 工具类
└── src/main/resources/
    └── application.properties  # 应用配置
```

### 移动端结构
```
CampusCompanionApp/
├── api/                     # API接口层
├── pages/                   # 页面目录
│   ├── index/              # 首页
│   ├── auth/               # 认证页面
│   ├── order/              # 活动相关页面
│   ├── content/            # 动态相关页面
│   ├── user/               # 用户相关页面
│   └── ai/                 # AI助手页面
├── store/                   # 状态管理
├── utils/                   # 工具函数
├── App.vue                  # 应用入口
├── main.js                  # 主入口文件
├── manifest.json            # 应用配置
└── pages.json               # 页面路由配置
```

### Web前端结构
```
frontend/Web_vue/
├── src/
│   ├── components/          # 组件
│   ├── views/              # 页面视图
│   ├── stores/             # Pinia状态管理
│   ├── services/           # API服务
│   ├── utils/              # 工具函数
│   ├── router/             # 路由配置
│   └── App.vue             # 根组件
├── public/                  # 静态资源
└── vite.config.js          # Vite配置
```

## 🔌 API接口

### 基础信息

- **基础URL**：`http://localhost:8080/api/v1`
- **认证方式**：JWT Token（请求头：`Authorization: Bearer {token}`）
- **数据格式**：JSON

### 主要接口模块

- **认证模块** (`/api/v1/auth`)：登录、注册、忘记密码
- **用户模块** (`/api/v1/users`)：用户信息管理
- **活动模块** (`/api/v1/orders`)：活动发布、申请、管理
- **动态模块** (`/api/v1/contents`)：动态发布、评论、点赞
- **AI模块** (`/api/v1/ai`)：AI对话
- **文件模块** (`/api/v1/files`)：文件上传
- **验证码模块** (`/api/v1/verify`)：验证码发送

详细的API文档请参考：`docs/SystemAnalysis/接口文档.md`

## 📚 项目文档

- [需求分析文档](docs/DemandAnalysis/系统需求文档.md)
- [系统分析文档](docs/SystemAnalysis/README.md)
- [数据库设计文档](docs/SystemAnalysis/数据库模型设计.md)
- [接口文档](docs/SystemAnalysis/接口文档.md)
- [移动端项目介绍](CampusCompanionApp/项目介绍.md)

## 🌟 功能特性

### 用户系统
- ✅ 邮箱/学号登录
- ✅ 邮箱验证码注册
- ✅ 忘记密码（三步流程）
- ✅ 用户信息管理
- ✅ 头像上传

### 活动约伴
- ✅ 活动发布与管理
- ✅ 活动列表浏览（支持筛选）
- ✅ 活动详情查看
- ✅ 活动申请与审批
- ✅ 活动状态管理
- ✅ 活动参与者聊天

### 动态社区
- ✅ 动态发布（文字、图片、视频）
- ✅ 动态列表浏览
- ✅ 动态详情查看
- ✅ 评论功能
- ✅ 点赞功能

### AI助手
- ✅ 智能对话
- ✅ 系统提示对话

### 其他功能
- ✅ 多端支持（Android、iOS、H5、Web）
- ✅ 响应式设计
- ✅ 完善的错误处理
- ✅ 统一的网络请求封装

## 🔧 配置说明

### 后端配置

主要配置项（`application.properties`）：
- **数据库连接**：URL、用户名、密码
- **JWT配置**：密钥、过期时间
- **邮件服务**：SMTP服务器配置
- **文件上传**：大小限制
- **智谱AI**：API Key配置（AI功能需要）

### 前端配置

- **API地址**：在 `utils/config.js`（移动端）或 `src/utils/axios.js`（Web端）中配置
- **环境变量**：根据开发/生产环境切换

## ⚠️ 注意事项

1. **数据库**：确保MySQL服务已启动，数据库已创建
2. **端口占用**：确保8080端口未被占用（后端），5173端口未被占用（Web前端）
3. **网络配置**：
   - 移动端在模拟器中访问后端：使用 `10.0.2.2` 代替 `localhost`
   - 真机调试：需要使用电脑的实际局域网IP地址
4. **邮件服务**：忘记密码功能需要配置SMTP邮件服务器
5. **AI功能**：需要配置智谱AI的API Key（在 `application.properties` 中）

## 🛠️ 开发建议

1. **首次运行**：建议先运行后端，确保数据库连接正常
2. **调试顺序**：后端 → Web前端 → 移动端
3. **数据测试**：可以通过后端接口或直接在数据库中插入测试数据
4. **功能测试**：按照用户流程：注册 → 登录 → 发布活动 → 发布动态
5. **代码规范**：遵循各框架的代码规范，保持代码整洁

## 📊 项目状态

- ✅ 后端服务：已完成，所有接口正常运行
- ✅ 移动端App：已完成，与后端API完全对接
- ✅ Web前端：已完成，功能完整
- ✅ 数据库设计：已完成，支持所有功能
- ✅ API文档：已完成，接口文档完善
- ✅ 项目文档：已完成，文档齐全

## 👥 开发团队

- **谷奕辰**
- **孙健柏**
- **伍奕涛**
- **张元宏**

**所属院校**：北京理工大学 计算机学院  
**课程**：移动互联分析与设计  
**项目版本**：1.0.0

## 📄 许可证

本项目为课程作业项目，仅供学习交流使用。

---

**如有问题或建议，欢迎提出Issue或Pull Request！**
