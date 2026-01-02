# Docker 部署与打包指南

本文档说明如何使用 Docker 对当前“校园 Companion”项目进行打包、运行和更新。

---

## 1. Docker 相关文件说明

项目中与 Docker 相关的主要文件：

- `docker-compose.yml`
  - 位于项目根目录。
  - 定义三类服务：
    - `db`：MySQL 8.0 数据库服务；
    - `backend`：后端 Spring Boot 服务（CampusCompanionBackend）；
    - `frontend`：前端 Vue 应用（frontend/Web_vue），通过 Nginx 提供静态页并反向代理到后端。
  - 管理服务之间的依赖关系、端口映射和持久化卷（`db_data`）。

- `CampusCompanionBackend/Dockerfile`
  - 后端服务的镜像构建脚本，多阶段构建：
    - 使用 `maven:3.9.9-eclipse-temurin-21` 进行 `mvn clean package -DskipTests` 打包；
    - 使用 `eclipse-temurin:21-jre` 作为运行时镜像，复制打好的 JAR 并以 `java -jar app.jar` 运行；
    - 暴露端口 `8080`。

- `frontend/Web_vue/Dockerfile`
  - 前端服务的镜像构建脚本，同样采用多阶段构建：
    - 构建阶段：基于 `node:20-alpine`，安装依赖并执行 `npm run build` 生成 `dist`；
    - 运行阶段：基于 `nginx:1.27-alpine`，将 `dist` 拷贝到 `/usr/share/nginx/html`，并使用自定义 `nginx.conf`；
    - 暴露端口 `80`，在 docker-compose 中映射为宿主 `8081`。

- `frontend/Web_vue/nginx.conf`
  - Nginx 配置文件，用于：
    - 提供前端静态资源；
    - 将前端对 `/api` 的请求反向代理到后端（`backend` 服务），确保前端 axios 使用 `/api/v1` 时无需关心后端具体地址。

- `docs/SystemAnalysis/create.sql`
  - MySQL 初始化脚本：
    - 在第一次启动数据库且数据目录为空时，自动创建 `campus_companion` 数据库及所有表（users、orders、posts、verify_code_record 等）；
    - 授权和 root 密码由 docker-compose 中的 `MYSQL_ROOT_PASSWORD` 等环境变量控制。
  - 在 `db` 服务通过 `- ./docs/SystemAnalysis/create.sql:/docker-entrypoint-initdb.d/create.sql:ro` 挂载，仅在空数据卷初始化时执行。

---

## 2. 完整打包步骤

以下所有命令均在项目根目录执行（包含 `docker-compose.yml` 的目录）。

### 2.1 前置条件

- 已安装：
  - Docker
  - Docker Compose（或 Docker Desktop 自带的 compose）。
- 端口占用：
  - 确保宿主机 8080（后端）和 8081（前端）未被其他程序长期占用。

### 2.2 一键构建镜像

项目已通过 `docker-compose.yml` 集成后端和前端的 Dockerfile，通常只需要一次命令：

```bash
cd /path/to/mobile_internet-project

docker compose build
```

- `db` 服务使用官方 `mysql:8.0` 镜像，无需构建；
- `backend` 服务：使用 `CampusCompanionBackend/Dockerfile` 构建 `mobile_internet-project-backend:latest`；
- `frontend` 服务：使用 `frontend/Web_vue/Dockerfile` 构建 `mobile_internet-project-frontend:latest`。

也可以直接在启动时自动构建：

```bash
docker compose up --build
```

### 2.3 单独构建某个服务镜像（可选）

如仅修改了后端代码：

```bash
# 仅重新构建 backend 镜像
docker compose build backend
```

如仅修改了前端代码：

```bash
# 仅重新构建 frontend 镜像
docker compose build frontend
```

---

## 3. 容器运行指南

### 3.1 启动全部服务

在项目根目录执行：

```bash
# 后台运行
cd /path/to/mobile_internet-project

docker compose up -d
```

这会启动：

- `campus_db`（MySQL，端口映射：`33306:3306`）
- `campus_backend`（后端，端口映射：`8080:8080`）
- `campus_frontend`（前端，端口映射：`8081:80`）

访问地址：

- 前端：`http://localhost:8081/`
- 后端 API：`http://localhost:8080/api/v1/...`
- MySQL（本机连接）：`localhost:33306`，用户名 `root`，密码 `879879`，数据库 `campus_companion`。

### 3.2 环境变量说明

在 `docker-compose.yml` 中：

- `db` 服务：
  - `MYSQL_ROOT_PASSWORD`: MySQL root 用户密码（当前为 `879879`）；
  - `MYSQL_DATABASE`: 初始数据库名（`campus_companion`）。

- `backend` 服务：
  - `SPRING_DATASOURCE_URL`: `jdbc:mysql://db:3306/campus_companion?...`，指向 `db` 服务；
  - `SPRING_DATASOURCE_USERNAME`: `root`；
  - `SPRING_DATASOURCE_PASSWORD`: `879879`；
  - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `none`，表示不自动建表，由 `create.sql` 负责；
  - `JWT_SECRET`: JWT 密钥（示例为 `your-secret-key`，应在实际环境中修改为安全值）；
  - `ZHIPU_API_KEY`: 智谱 AI 的 API Key（应替换为你自己的）；
  - `SPRING_MAIL_*`: 邮件服务器配置（目前配置为 163 邮箱，需要对应账号支持）。

> 如需修改数据库密码或 JWT 等敏感值，建议同时修改：
> - `docker-compose.yml` 中的环境变量；
> - 后端开发环境的 `application.properties` 或本地运行时的环境变量。

### 3.3 查看日志与停止容器

- 查看所有服务的日志：

```bash
docker compose logs -f
```

- 查看单个服务日志：

```bash
# 仅查看后端日志
docker compose logs -f backend

# 仅查看数据库日志
docker compose logs -f db
```

- 停止并保留数据卷：

```bash
docker compose down
```

- 停止并删除数据卷（包括数据库数据）：

```bash
docker compose down -v
```

> 注意：执行 `down -v` 会清空数据库数据和所有表，请谨慎使用。

---

## 4. 项目更新后的重新打包与更新流程

### 4.1 仅更新后端代码

1. 修改后端代码（`CampusCompanionBackend/src/...`）；
2. 在项目根目录执行：

```bash
docker compose build backend

docker compose up -d backend
```

- `build backend` 只重建后端镜像，不影响前端和数据库；
- `up -d backend` 会以新镜像重启后端容器，前端仍然指向 `http://backend:8080`，无需改动。

### 4.2 仅更新前端代码

1. 修改前端代码（`frontend/Web_vue/src/...`）；
2. 在项目根目录执行：

```bash
docker compose build frontend

docker compose up -d frontend
```

- 前端镜像会重新构建（执行 `npm install` + `npm run build`），然后 Nginx 容器以新镜像重启；
- 后端与数据库不受影响。

### 4.3 同时更新前后端

如前后端均有修改：

```bash
docker compose build

docker compose up -d
```

- 推荐在开发阶段使用 `docker compose up --build`，在生产/测试环境中先 `build` 再 `up -d`。

### 4.4 避免重复构建的小技巧

- 使用 `docker compose build backend` 或 `frontend` 定向构建，减少不必要的镜像重建；
- 充分利用 Docker 的多阶段构建与缓存机制，尽量避免频繁修改 `Dockerfile` 顶部（例如基础镜像版本），以便缓存 `npm install` / `mvn dependency:go-offline` 步骤；
- 在仅调整配置（如 `docker-compose.yml` 的环境变量）时，无需重新 build 镜像，直接 `docker compose up -d` 即可生效。

---

## 5. 常见问题排查与解决

### 5.1 MySQL 初始化时报错 "Can't create database 'campus_companion'; database exists"

**现象**：

- `db` 日志中出现：
  - `ERROR 1007 (HY000): Can't create database 'campus_companion'; database exists`

**原因**：

- Docker 官方 MySQL 镜像会根据 `MYSQL_DATABASE` 自动创建数据库；
- 同时 `create.sql` 中也包含 `CREATE DATABASE` 语句，若未使用 `IF NOT EXISTS`，会二次创建导致错误。

**解决方案**：

- 当前 `create.sql` 已改为：
  - `CREATE DATABASE IF NOT EXISTS campus_companion ...`；
- 如遇到旧数据卷仍有问题，可以：

```bash
# 在项目根目录
docker compose down -v

docker compose up --build
```

### 5.2 验证码发送接口 500，日志中出现 "Data too long for column 'code'"

**现象**：

- 后端日志中：
  - `MysqlDataTruncation: Data too long for column 'code' at row 1`
- 通常发生在 `/api/v1/verify/email/{email}` 接口。

**原因**：

- Java 实体 `VerifyCodeRecord.code` 长度为 100，存储的是加密后的验证码（长度约 60+）；
- 旧版 `create.sql` 中 `verify_code_record.code` 定义为 `VARCHAR(10)`，导致插入被截断。

**解决方案**：

- 当前 `create.sql` 已改为：
  - `code VARCHAR(100) NOT NULL COMMENT '验证码（存储加密后的值，长度需足够）'`；
- 对于新建的数据卷，重新启动即可使用正确的表结构：

```bash
docker compose down -v

docker compose up --build
```

- 对于已有数据库，如不想删数据，可在 MySQL 中手动执行：

```sql
ALTER TABLE verify_code_record
  MODIFY COLUMN code VARCHAR(100) NOT NULL;
```

### 5.3 前端访问接口返回 403 "Invalid CORS request"

**现象**：

- 前端控制台报错：
  - 状态码 403
  - 响应内容 `Invalid CORS request`
- 通常发生在从 `http://localhost:8081` 向后端发起请求时。

**原因**：

- 后端的 CORS / 安全配置若只允许了 `http://localhost:5173` 或 `http://localhost:8080`，在 Docker 环境下前端通过 `8081` 访问会被拦截。

**排查与解决**：

1. 检查后端 CORS 配置（例如 `SecurityConfig` 或 `WebMvcConfig` 中）：
   - 确保允许的来源包含：
     - `http://localhost:8081`
     - 如有需要，还可加入实际部署域名。
2. 修改后重新构建后端镜像并重启：

```bash
docker compose build backend

docker compose up -d backend
```

### 5.4 端口被占用导致容器无法启动

**现象**：

- Docker 报错 `port is already allocated` 或后端日志提示 `Port 8080 was already in use`。

**解决方案**：

- 检查宿主机端口占用情况（示例为 Windows PowerShell）：

```powershell
# 查看 8080 占用
netstat -ano | findstr :8080

# 查看 8081 占用
netstat -ano | findstr :8081
```

- 结束占用这些端口的进程，或在 `docker-compose.yml` 中修改映射端口（例如 `8082:8080`）。

### 5.5 数据库数据丢失或无法连接

**现象**：

- 容器重启后数据“清空”；
- 或后端启动时报无法连到数据库。

**排查步骤**：

1. 确认 `db_data` 卷存在：

```bash
docker volume ls | grep db_data
```

2. 确认 `db` 容器正常运行：

```bash
docker compose ps
```

3. 查看数据库容器日志：

```bash
docker compose logs -f db
```

4. 若数据确实已丢失，说明可能执行过 `docker compose down -v`，需重新执行初始化脚本（会自动执行 `create.sql`）。

---

## 6. 总结

- 本项目已提供完整的 Docker 化配置，推荐使用 `docker compose up --build` 一键启动；
- 日常开发时，可只针对修改的服务执行 `docker compose build backend/frontend`，减少不必要的构建时间；
- 遇到问题时，优先查看 `docker compose logs`，多数错误可通过检查数据库初始化、CORS 配置和端口占用快速定位。
