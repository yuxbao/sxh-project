# sxh-rag 启动说明

本文档基于当前仓库配置整理，适合本地开发启动。

## 一键启动

项目根目录直接执行：

```bash
./scripts/start-all.sh
```

这个命令会完成以下动作：

- 启动 MySQL、Redis、MinIO、Kafka、Elasticsearch
- 等待依赖服务就绪
- 自动创建 `sxh_rag` 数据库
- 自动创建 MinIO 的 `uploads` bucket
- 在同一个终端里同时输出后端和前端日志

停止方式：

- 停止前后端：当前终端按 `Ctrl-C`
- 停止 Docker 依赖服务：

```bash
./scripts/stop-deps.sh
```

## 本机一键启动

如果你已经在本机安装了 MySQL、Redis、MinIO、Kafka、Elasticsearch，可以直接执行：

```bash
./scripts/start-local-all.sh
```

这个命令会尝试：

- 复用已在运行的本机服务
- 自动启动本机 MySQL、Redis、MinIO、Kafka、Elasticsearch
- 自动创建 `sxh_rag` 数据库
- 自动创建 MinIO 的 `uploads` bucket
- 自动创建 Kafka topics
- 在同一个终端里同时输出后端和前端日志

停止方式：

- 停止前后端：当前终端按 `Ctrl-C`
- 停止由脚本拉起的本机依赖：

```bash
./scripts/stop-local-deps.sh
```

默认使用的本机路径和账号：

- `KAFKA_HOME=$HOME/dev/env/kafka/kafka_2.13-3.9.0`
- `ES_HOME=$HOME/dev/env/elasticsearch/elasticsearch-8.10.0`
- `MINIO_DATA_DIR=$HOME/minio/data`
- MySQL 密码按 [`src/main/resources/application.yml`](src/main/resources/application.yml)
- Redis 密码按 [`src/main/resources/application.yml`](src/main/resources/application.yml)
- Elasticsearch 密码按 [`src/main/resources/application.yml`](src/main/resources/application.yml)

如果你的本机路径不同，可以这样覆盖：

```bash
KAFKA_HOME=/your/kafka/path ES_HOME=/your/es/path MINIO_DATA_DIR=/your/minio/data ./scripts/start-local-all.sh
```

## 推荐启动方式

推荐使用一键脚本。原因是当前仓库里的 [`docs/docker-compose.yaml`](docs/docker-compose.yaml) 与 [`src/main/resources/application-docker.yml`](src/main/resources/application-docker.yml) 是对应的，脚本已经把初始化动作一起处理了。

## 1. 启动依赖服务

在项目根目录执行：

```bash
docker compose -f docs/docker-compose.yaml up -d
```

如果你的环境还在使用旧版命令，也可以执行：

```bash
docker-compose -f docs/docker-compose.yaml up -d
```

## 2. 创建 MySQL 数据库

`docker-compose` 里只启动了 MySQL，没有自动创建 `sxh_rag` 数据库，所以第一次启动前需要手动创建一次：

```bash
docker exec -it mysql mysql -uroot -pSxhRag2025 -e "CREATE DATABASE IF NOT EXISTS sxh_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

## 3. 创建 MinIO Bucket

项目代码里默认使用的 bucket 名称是 `uploads`，仓库中没有自动建 bucket 的逻辑，所以第一次使用前需要创建。

方式一：打开 MinIO 控制台手动创建

```text
http://localhost:19001
用户名：admin
密码：SxhRag2025
Bucket：uploads
```

方式二：如果你本机装了 `mc`，可以直接执行：

```bash
mc alias set local http://localhost:19000 admin SxhRag2025
mc mb local/uploads
```

## 4. 启动后端

在项目根目录执行：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

说明：

- 后端端口：`8081`
- 使用配置文件：[`src/main/resources/application-docker.yml`](src/main/resources/application-docker.yml)

## 5. 启动前端

在另一个终端执行：

```bash
cd frontend
pnpm install
pnpm dev
```

说明：

- 前端开发端口：`9527`
- 前端开发模式使用的是 [`frontend/.env.test`](frontend/.env.test)
- 前端会把接口代理到 `http://localhost:8081/api/v1`

## 6. 访问地址

- 前端首页：`http://localhost:9527`
- 后端接口基址：`http://localhost:8081/api/v1`
- MinIO 控制台：`http://localhost:19001`
- Elasticsearch：`http://localhost:9200`

## 一次性执行顺序

如果你想直接按顺序启动，可以照下面执行：

```bash
./scripts/start-deps.sh
./scripts/start-backend.sh
```

然后新开一个终端执行：

```bash
./scripts/start-frontend.sh
```

## 停止服务

停止 Docker 依赖服务：

```bash
./scripts/stop-deps.sh
```

## 补充说明

- 当前仓库的默认配置文件 [`src/main/resources/application.yml`](src/main/resources/application.yml) 和开发配置 [`src/main/resources/application-dev.yml`](src/main/resources/application-dev.yml) 使用的是另一套本地账号密码，不建议和 `docs/docker-compose.yaml` 混用。
- 如果你需要调用 AI 能力，仍需检查 `DeepSeek` 和 `Embedding` 相关配置是否可用。
- 管理员初始化账号按当前配置为：`admin / admin123`
