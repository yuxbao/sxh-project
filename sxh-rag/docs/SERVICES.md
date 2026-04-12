# 服务控制台登录信息

## 本地开发环境

| 服务 | 控制台地址 | 用户名 | 密码 |
|------|-----------|--------|------|
| MinIO | http://localhost:9001 | minioadmin | minioadmin |
| Elasticsearch | https://localhost:9200 | elastic | mRj-QzairyPuVMFaA4Q+ |
| Redis | - | - | 273511 |
| MySQL | localhost:3306 | root | Baoyu273511a |

## Docker 环境 Web 控制台

| 服务 | 控制台地址 | 用户名 | 密码 |
|------|-----------|--------|------|
| MinIO | http://localhost:19001 | admin | SxhRag2025 |
| Kafka UI | http://localhost:9002 | - | - |
| Kibana | http://localhost:5601 | - | - |
| Elasticsearch | http://localhost:9200 | elastic | SxhRag2025 |
| MySQL | localhost:3306 | root | SxhRag2025 |
| Redis | localhost:6379 | - | SxhRag2025 |

### 启动 Docker 服务

```bash
cd docs

# 启动所有服务
docker-compose up -d

# 只启动 Kafka UI 和 Kibana（依赖 kafka 和 es）
docker-compose up -d kafka-ui kibana

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f kafka-ui
docker-compose logs -f kibana
```

## Kafka 命令行

```bash
# 设置 Kafka 路径
export KAFKA_HOME=~/dev/env/kafka/kafka_2.13-3.9.0

# 查看所有 topic
$KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# 消费消息
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic file-processing-topic1 --from-beginning

# 查看 consumer group
$KAFKA_HOME/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

## Elasticsearch curl 命令

```bash
# 查看集群状态
curl -k -u "elastic:mRj-QzairyPuVMFaA4Q+" https://localhost:9200/_cluster/health?pretty

# 查看所有索引
curl -k -u "elastic:mRj-QzairyPuVMFaA4Q+" https://localhost:9200/_cat/indices?v

# 查看节点信息
curl -k -u "elastic:mRj-QzairyPuVMFaA4Q+" https://localhost:9200/_cat/nodes?v
```

## 日志文件

```bash
# 查看服务日志
tail -f logs/local/elasticsearch.log
tail -f logs/local/kafka.log
tail -f logs/local/minio.log
tail -f logs/local/redis.log
```

你的 ES enrollment token（我刚用本机 Elasticsearch 8.10.0 重新生成的 kibana scope）是：

eyJ2ZXIiOiI4LjEwLjAiLCJhZHIiOlsiMTcyLjE2LjYuNjQ6OTIwMCJdLCJmZ3IiOiI1MjViNmJjZTFmNDA5ODExZDljM
WM4NjQ5YmVjYThhNGZjZDJkNjZjMzIzYWVlMjk0ZmRiOGE2NGMxNTdlODY4Iiwia2V5IjoiZmxrZFBwMEJ3Nm9fcnV3bj
V6Uy06cDRGQVJLc3JUaktMYWt5cGhtOC1QQSJ9
