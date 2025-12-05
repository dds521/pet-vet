# PetVetAI - AI 驱动的宠物医疗咨询平台

## 项目简介

PetVetAI 是一个基于 Spring Boot 3.3.5 和 Spring AI 的宠物医疗咨询平台，集成了 Seata 分布式事务、Sentinel 流量控制、RocketMQ 消息队列等技术。

## 技术栈

- **Spring Boot**: 3.3.5
- **Spring AI**: 1.0.0 (OpenAI)
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Seata**: 分布式事务解决方案
- **Sentinel**: 流量控制和服务降级
- **RocketMQ**: 消息队列
- **MyBatis Plus**: 3.5.5
- **MySQL**: 数据库
- **Redis**: 缓存

## Seata 分布式事务集成

本项目集成了 Seata 分布式事务，提供了三种模式的完整演示：

### 1. AT 模式（自动模式）- 最简单

**特点**：
- 只需要在方法上添加 `@GlobalTransactional` 注解
- Seata 自动处理事务的提交和回滚
- 无需手动编写补偿逻辑
- 适合大多数业务场景

**使用示例**：
```java
@GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
public void createOrderAndDeductBalance(Long petId, Long userId, BigDecimal amount) {
    // 创建订单
    Order order = new Order(petId, orderNo, amount);
    orderMapper.insert(order);
    
    // 扣减余额
    Account account = accountMapper.selectById(userId);
    account.setBalance(account.getBalance().subtract(amount));
    accountMapper.updateById(account);
}
```

**工作原理**：
1. 业务方法执行前，Seata 拦截 SQL，解析语义，保存数据快照（before image）
2. 业务方法执行后，保存数据快照（after image），生成行锁
3. 如果全局事务提交，释放行锁；如果回滚，使用 before image 恢复数据

**测试接口**：
- `GET /api/seata/at/create-order?petId=1&userId=1&amount=100` - 正常创建订单
- `GET /api/seata/at/create-order-with-exception?petId=1&userId=1&amount=100` - 测试异常回滚

### 2. TCC 模式（手动模式）- 精确控制

**特点**：
- 需要手动实现 Try、Confirm、Cancel 三个阶段
- 精确控制事务的提交和回滚
- 性能较高，但实现复杂
- 适合需要精确控制、性能要求高的场景

**使用示例**：
```java
@TwoPhaseBusinessAction(
    name = "tccCreateOrder",
    commitMethod = "confirmCreateOrder",
    rollbackMethod = "cancelCreateOrder"
)
public boolean tryCreateOrder(BusinessActionContext context, Long petId, BigDecimal amount) {
    // Try 阶段：完成业务检查，预留资源
    // 1. 检查账户余额
    // 2. 冻结余额（预留资源）
    // 3. 创建订单（状态为 PENDING）
    return true;
}

public boolean confirmCreateOrder(BusinessActionContext context) {
    // Confirm 阶段：确认执行，使用 Try 阶段预留的资源
    // 1. 更新订单状态为已支付
    // 2. 扣减账户余额
    return true;
}

public boolean cancelCreateOrder(BusinessActionContext context) {
    // Cancel 阶段：取消执行，释放 Try 阶段预留的资源
    // 1. 更新订单状态为已取消
    // 2. 释放冻结的余额
    return true;
}
```

**工作原理**：
1. **Try 阶段**：尝试执行，完成所有业务检查，预留业务资源
2. **Confirm 阶段**：确认执行，使用 Try 阶段预留的资源，执行业务
3. **Cancel 阶段**：取消执行，释放 Try 阶段预留的资源

**测试接口**：
- `GET /api/seata/tcc/create-order?petId=1&userId=1&amount=100` - TCC 模式创建订单

### 3. Saga 模式（状态机模式）- 长事务

**特点**：
- 通过状态机管理长事务
- 每个服务实现正向操作和补偿操作
- 支持长时间运行的事务
- 最终一致性
- 适合业务流程长、步骤多的场景

**使用示例**：
```java
public void createOrderWithSaga(Long petId, BigDecimal amount) {
    // Step 1: 创建订单
    String orderNo = createOrderStep(petId, amount);
    
    // Step 2: 扣减库存
    // deductInventoryStep(orderNo);
    
    // Step 3: 扣减余额
    // deductBalanceStep(orderNo);
    
    // 如果某个步骤失败，会按照相反顺序执行补偿操作
}
```

**工作原理**：
1. 将长事务拆分为多个本地事务
2. 每个本地事务都有对应的补偿操作
3. 如果某个步骤失败，会按照相反顺序执行补偿操作

**测试接口**：
- `GET /api/seata/saga/create-order?petId=1&amount=100` - Saga 模式创建订单

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+
- Redis 6.0+
- Seata Server 1.8.0（已部署在本地）

### 2. 数据库初始化

执行 SQL 脚本创建必要的表：

```bash
mysql -u root -p pet_vet_ai < src/main/resources/sql/seata_demo_tables.sql
```

或者直接在 MySQL 中执行 `src/main/resources/sql/seata_demo_tables.sql` 文件。

### 3. 配置 Seata Server

确保本地 Seata Server 已启动：

```bash
# 使用 tool-services.sh 启动 Seata Server
cd /path/to/workspace-service/cursor-AI
./tool-services.sh start tool-seata-docker
```

Seata Server 配置：
- 服务地址：`127.0.0.1:8091`
- 控制台地址：`http://127.0.0.1:7091`
- 默认账户：`seata/seata`

### 4. 配置应用

修改 `src/main/resources/application-dev.yml` 中的数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_vet_ai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 5. 启动应用

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或使用启动脚本
./start.sh
```

### 6. 测试 Seata 功能

#### AT 模式测试

```bash
# 正常创建订单
curl "http://localhost:48080/api/seata/at/create-order?petId=1&userId=1&amount=100"

# 测试异常回滚
curl "http://localhost:48080/api/seata/at/create-order-with-exception?petId=1&userId=1&amount=100"
```

#### TCC 模式测试

```bash
curl "http://localhost:48080/api/seata/tcc/create-order?petId=1&userId=1&amount=100"
```

#### Saga 模式测试

```bash
curl "http://localhost:48080/api/seata/saga/create-order?petId=1&amount=100"
```

#### 获取 Seata 模式说明

```bash
curl "http://localhost:48080/api/seata/info"
```

## 项目结构

```
pet-vet-ai/
├── src/main/java/com/petvetai/app/
│   ├── controller/
│   │   ├── SeataDemoController.java      # Seata 演示 Controller
│   │   └── PetVetController.java         # 宠物医疗 Controller
│   ├── service/
│   │   ├── seata/
│   │   │   ├── SeataAtService.java       # AT 模式服务
│   │   │   ├── SeataTccService.java      # TCC 模式服务
│   │   │   └── SeataSagaService.java     # Saga 模式服务
│   │   └── PetMedicalService.java        # 宠物医疗服务
│   ├── domain/
│   │   ├── Order.java                    # 订单实体
│   │   ├── Account.java                  # 账户实体
│   │   └── Pet.java                      # 宠物实体
│   └── mapper/
│       ├── OrderMapper.java              # 订单 Mapper
│       └── AccountMapper.java            # 账户 Mapper
├── src/main/resources/
│   ├── seata/
│   │   ├── registry.conf                 # Seata 注册中心配置
│   │   └── file.conf                     # Seata 文件配置
│   ├── sql/
│   │   └── seata_demo_tables.sql         # 数据库表结构
│   └── application-dev.yml              # 开发环境配置
└── README.md                             # 项目说明文档
```

## Seata 配置说明

### 1. Maven 依赖

已在 `pom.xml` 中添加 Seata 依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

### 2. 应用配置

在 `application-dev.yml` 中配置 Seata：

```yaml
spring:
  cloud:
    alibaba:
      seata:
        tx-service-group: default_tx_group
        enabled: true

seata:
  enabled: true
  application-id: ${spring.application.name}
  tx-service-group: default_tx_group
  config:
    type: file
    file:
      name: file.conf
  registry:
    type: file
    file:
      name: file.conf
  service:
    vgroup-mapping:
      default_tx_group: default
    grouplist:
      default: 127.0.0.1:8091
```

### 3. Seata 配置文件

配置文件位于 `src/main/resources/seata/` 目录：
- `registry.conf` - 注册中心配置（使用 file 模式）
- `file.conf` - 文件配置（使用 file 存储模式）

## 三种模式对比

| 特性 | AT 模式 | TCC 模式 | Saga 模式 |
|------|--------|----------|-----------|
| **实现复杂度** | 低（只需注解） | 高（需实现三个阶段） | 中（需定义状态机） |
| **性能** | 中等 | 高 | 中等 |
| **一致性** | 强一致性 | 强一致性 | 最终一致性 |
| **适用场景** | 大多数业务场景 | 高性能、精确控制 | 长流程、多步骤 |
| **补偿方式** | 自动回滚 | 手动 Cancel | 状态机补偿 |

## 注意事项

1. **AT 模式**：
   - 需要在数据库中创建 `undo_log` 表
   - 只支持单库事务，不支持跨库
   - 需要数据库支持行锁

2. **TCC 模式**：
   - 需要实现 Try、Confirm、Cancel 三个方法
   - 需要保证幂等性
   - 需要处理空回滚和悬挂问题

3. **Saga 模式**：
   - 需要定义状态机 JSON 配置文件
   - 需要实现每个服务的正向和补偿操作
   - 最终一致性，不保证强一致性

## 常见问题

### 1. Seata 连接失败

检查 Seata Server 是否启动：
```bash
docker ps | grep seata-server
```

检查配置中的 Seata Server 地址是否正确：
```yaml
seata:
  service:
    grouplist:
      default: 127.0.0.1:8091
```

### 2. 事务不回滚

确保：
- 方法上添加了 `@GlobalTransactional` 注解
- 异常类型在 `rollbackFor` 中指定
- 异常被正确抛出，没有被捕获

### 3. undo_log 表未创建

执行 SQL 脚本创建表：
```sql
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(128) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xid_branch_id` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 参考资源

- [Seata 官方文档](https://seata.io/zh-cn/docs/overview/what-is-seata.html)
- [Spring Cloud Alibaba Seata](https://github.com/alibaba/spring-cloud-alibaba/wiki/Seata)
- [Seata AT 模式原理](https://seata.io/zh-cn/docs/dev/mode/at-mode.html)
- [Seata TCC 模式原理](https://seata.io/zh-cn/docs/dev/mode/tcc-mode.html)
- [Seata Saga 模式原理](https://seata.io/zh-cn/docs/dev/mode/saga-mode.html)

## 许可证

本项目采用 Apache License 2.0 许可证。
