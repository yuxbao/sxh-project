# RAG 数据库迁移与收敛设计

## 目标

- 保留 `PaiSmart` 作为 `sxh-rag` 独立库。
- `sxh-rag` 只共享 `pai_coding.user` 做登录认证，不迁移 `sxh` 主库业务表。
- 为 `PaiSmart` 提供可重复执行的一键迁移命令。
- 只保留 `sxh-rag` 当前真实依赖的表，删除无关测试表和脏表。

## 方案

采用“固定 SQL + 白名单清理 + 启动校验”三段式收敛：

1. 固定 SQL
   使用 `docs/databases/migrate-rag-schema.sql` 作为权威结构定义，负责建表、补列、补索引、补外键。
2. 白名单清理
   使用 `scripts/migrate-rag-db.sh` 在备份后清理 `PaiSmart` 中不属于 RAG 的表。
3. 启动校验
   将 Hibernate 从 `ddl-auto=update` 改为 `ddl-auto=validate`，避免运行时悄悄改表。

## 白名单表

- `users`
- `organization_tags`
- `file_upload`
- `chunk_info`
- `document_vectors`
- `conversations`
- `article_knowledge`

## 一键命令

```bash
./scripts/migrate-rag-db.sh
```

支持环境变量覆盖：

- `RAG_DB_HOST`
- `RAG_DB_PORT`
- `RAG_DB_NAME`
- `RAG_DB_USER`
- `RAG_DB_PASSWORD`
- `RAG_DB_DROP_UNKNOWN`

## 风险控制

- 迁移前自动执行 `mysqldump` 备份。
- 只清理 `PaiSmart` 当前库，不触碰 `pai_coding`。
- 删除范围基于白名单，不做模糊匹配。
