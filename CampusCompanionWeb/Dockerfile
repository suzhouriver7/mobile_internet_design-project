# 前端构建阶段
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
COPY . .

# 使用默认的相对路径 /api/v1，与 nginx 反向代理保持一致
# 为规避 npm 关于可选依赖的已知 bug，先清理锁文件和 node_modules 再安装并构建
RUN rm -rf package-lock.json node_modules \
 && npm install --legacy-peer-deps \
 && npm run build

# 运行阶段：使用 nginx 提供静态资源
FROM nginx:1.27-alpine

# 拷贝自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷贝前端构建产物
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx","-g","daemon off;"]
