# MySQL事务

## 事务四大特性（ACID）

1. **原子性（Atomicity）**
   - 事务是最小执行单位，不可分割
   - 要么全部成功，要么全部失败回滚

2. **一致性（Consistency）**
   - 事务执行前后，数据库状态保持一致
   - 违反约束会回滚

3. **隔离性（Isolation）**
   - 并发执行的事务互不干扰
   - 通过隔离级别控制

4. **持久性（Durability）**
   - 事务提交后，修改永久保存
   - 即使系统故障也不丢失

## 事务隔离级别

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|----------|------|------------|------|
| READ UNCOMMITTED | √ | √ | √ |
| READ COMMITTED | × | √ | √ |
| REPEATABLE READ | × | × | √ |
| SERIALIZABLE | × | × | × |

### 详细说明

- **脏读**：读取到其他事务未提交的数据
- **不可重复读**：同一事务内两次读取数据不同（其他事务修改了数据）
- **幻读**：同一事务内两次查询结果集不同（其他事务新增/删除了数据）

## MySQL默认隔离级别

- MySQL InnoDB默认：**REPEATABLE READ**
- 可通过命令查看：`SELECT @@transaction_isolation`

## 事务相关命令

```sql
-- 开启事务
START TRANSACTION;

-- 提交事务
COMMIT;

-- 回滚事务
ROLLBACK;

-- 设置隔离级别
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

## 事务并发问题解决方案

1. 悲观锁：SELECT ... FOR UPDATE
2. 乐观锁：版本号字段
3. 间隙锁：防止幻读
