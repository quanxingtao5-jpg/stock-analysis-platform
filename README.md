# stock-analysis-platform

上市公司财报与行情分析平台的微服务基础工程。目前提供一条股票资料示例链路，业务分析能力可在此基础上扩展。

## 技术栈

- Java 17、Maven 多模块
- Spring Boot 3.2.12、Spring Cloud 2023.0.6、Spring Cloud Alibaba 2023.0.3.4
- Nacos 服务注册与发现、Spring Cloud Gateway
- MyBatis-Plus、MySQL、Redis、RabbitMQ

## 模块

| 模块 | 用途 |
| --- | --- |
| `common` | 共享 API 响应结构 |
| `gateway` | 8082 端口入口，通过 Nacos 将 `/api/stocks/**` 转发给 `stock-service` |
| `stock-service` | 8081 端口示例服务，MySQL 持久化、Redis 读取缓存、RabbitMQ 创建事件 |

## 本地启动（不用 Docker）

1. 安装 JDK 17 和 Maven 3.8+，并确认 `java -version`、`mvn -version` 指向它们。
2. 启动已有的 Nacos 2.4.x、MySQL 8、Redis 和 RabbitMQ 服务。默认连接地址见下表。Nacos 请仅在本地开发环境关闭鉴权；其他环境需要通过环境变量或外部配置提供鉴权信息。
3. 执行 `mysql -u root -p < sql/init.sql` 创建数据库和示例表。
4. 设置实际连接参数，然后在仓库根目录运行 `mvn clean package`。
5. 分别运行 `java -jar stock-service/target/stock-service-1.0.0-SNAPSHOT.jar` 与 `java -jar gateway/target/gateway-1.0.0-SNAPSHOT.jar`。

## 环境配置

两个服务默认启用 `local`。分别在各模块的 `application-local.yml`、`application-test.yml`、`application-prod.yml` 中维护本地、测试、生产环境配置；通用配置留在 `application.yml`。目前三套环境的中间件地址都预填本机 `127.0.0.1`，部署到其他机器前需修改相应 profile 或设置环境变量。

在 IDEA 的运行配置中设置 Active profiles 为 `test` 或 `prod`，也可以设置环境变量 `SPRING_PROFILES_ACTIVE=test` / `prod`。运行 JAR 时同样可设置该环境变量，例如 `SPRING_PROFILES_ACTIVE=test java -jar stock-service/target/stock-service-1.0.0-SNAPSHOT.jar`。

本地 MySQL 密码保存在被 Git 忽略的 `stock-service/config/application-local-private.yml`，不会打进 JAR。本地从项目根目录或 `stock-service` 目录启动都可读取该文件；从其他目录启动时请设置 `MYSQL_PASSWORD` 环境变量。测试和生产环境也通过 `MYSQL_PASSWORD` 提供密码。

| 环境变量 | 默认值 |
| --- | --- |
| `NACOS_ADDR` | `127.0.0.1:8848` |
| `MYSQL_URL` | `jdbc:mysql://127.0.0.1:3306/stock_analysis?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / 空 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / 空 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `guest` / `guest` |
| `GATEWAY_PORT` / `STOCK_SERVICE_PORT` | `8082` / `8081` |

通过网关验证：

```bash
curl -X POST http://localhost:8082/api/stocks \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"AAPL","name":"Apple Inc.","price":250.0000}'
curl http://localhost:8082/api/stocks/1
```

首次读取从 MySQL 查询并写入 Redis，缓存有效期 5 分钟。创建时向 RabbitMQ 的 `stock.events` exchange 发布 `stock.created` 消息，内容是新记录 ID。此示例没有实现跨 MySQL 和 RabbitMQ 的可靠事务消息；正式业务若需要不丢事件，应增加 outbox 等机制。

## AI 代码审查

同仓库的非草稿 PR 会触发 `.github/workflows/ai-review.yml`。审查规则在 `.github/ai-review-rules.md`，包含 Java 多行控制结构、4 空格缩进，以及 POM XML 分行和 2 空格缩进。审查脚本只允许对 PR 新增或修改的行发表行内评论；没有可确认的问题时不发表评论。

仓库需配置 Actions secret `AI_API_KEY` 和变量 `AI_MODEL`；`AI_BASE_URL` 可选，默认使用 OpenAI API 的 `/v1` 地址。接口需兼容 Chat Completions。工作流只处理同仓库的非草稿 PR，并从 PR 当前提交读取审查脚本与规则。
