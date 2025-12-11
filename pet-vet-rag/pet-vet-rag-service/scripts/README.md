# RAG 查询历史记录表 SQL 脚本说明

## 脚本文件

1. **create-rag-query-history-table.sql** - 完整的建表脚本（新建数据库时使用）
2. **alter-rag-query-history-table.sql** - 字段更新脚本（更新现有表结构时使用）

## 使用场景

### 场景1：新建数据库

如果数据库是新建的，直接执行 `create-rag-query-history-table.sql`：

```bash
mysql -u root -p < create-rag-query-history-table.sql
```

或者在 MySQL 客户端中执行：

```sql
source create-rag-query-history-table.sql;
```

### 场景2：更新现有表结构

如果表已经存在，需要添加新字段，执行 `alter-rag-query-history-table.sql`：

```bash
mysql -u root -p < alter-rag-query-history-table.sql
```

或者在 MySQL 客户端中执行：

```sql
source alter-rag-query-history-table.sql;
```

## 表结构说明

### 新增字段

1. **retrieved_documents** (JSON)
   - 类型：JSON
   - 说明：存储检索到的文档列表（JSON格式）
   - 默认值：NULL

2. **conversation_context** (JSON)
   - 类型：JSON
   - 说明：存储对话上下文（历史对话摘要，JSON格式）
   - 默认值：NULL

3. **confidence** (DECIMAL)
   - 类型：DECIMAL(3,2)
   - 说明：置信度分数（0.0-1.0）
   - 默认值：0.00

### 字段更新

- **user_id**: 改为 NOT NULL（新表），现有表需要先处理NULL值
- **model_name**: 默认值更新为 'deepseek'
- **retrieved_count**: 默认值更新为 0
- **enable_generation**: 默认值更新为 1
- **query_time**: 默认值更新为 0

## 索引说明

表包含以下索引：

- `PRIMARY KEY` (`id`) - 主键
- `idx_user_id` (`user_id`) - 用户ID索引
- `idx_session_id` (`session_id`) - 会话ID索引
- `idx_create_time` (`create_time`) - 创建时间索引
- `idx_user_create_time` (`user_id`, `create_time`) - 用户ID和创建时间联合索引

## 注意事项

1. **user_id 字段**：如果现有表中 user_id 字段允许 NULL，需要先处理数据后再执行 NOT NULL 约束
2. **JSON 字段**：MySQL 5.7+ 支持 JSON 类型，确保数据库版本符合要求
3. **字符集**：表使用 utf8mb4 字符集，支持 emoji 等特殊字符
4. **迁移脚本**：alter 脚本使用动态 SQL，会自动检查字段是否存在，可以安全地重复执行

## 验证

执行脚本后，可以验证表结构：

```sql
USE pet_vet_rag;
DESC rag_query_history;
SHOW CREATE TABLE rag_query_history;
```

## 回滚（如果需要）

如果需要回滚，可以执行以下 SQL：

```sql
ALTER TABLE `rag_query_history` 
    DROP COLUMN IF EXISTS `retrieved_documents`,
    DROP COLUMN IF EXISTS `conversation_context`,
    DROP COLUMN IF EXISTS `confidence`;
```

**注意**：回滚会丢失这些字段的数据，请谨慎操作！
