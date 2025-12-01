# SmartAdmin 前端 Dockerfile
# 多阶段构建：构建阶段 + Nginx运行阶段
FROM node:18-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制package文件
COPY package*.json ./

# 设置npm源（可选，提高下载速度）
RUN npm config set registry https://registry.npmmirror.com

# 安装所有依赖（包括devDependencies，因为构建需要vite）
RUN npm ci

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build:prod

# 运行阶段：使用Nginx提供静态文件服务
FROM nginx:1.25-alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建nginx用户目录（如果不存在）
RUN mkdir -p /var/cache/nginx/client_temp && \
    mkdir -p /var/cache/nginx/proxy_temp && \
    mkdir -p /var/cache/nginx/fastcgi_temp && \
    mkdir -p /var/cache/nginx/uwsgi_temp && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]