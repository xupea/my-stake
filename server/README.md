# stake-server

一个基于 Java Spring Boot 的 MVP 服务端，当前包含两部分能力：

- 基础认证接口：注册、登录、token 校验
- RabbitMQ 推送接口：定时发送消息，以及按 websocket topic 发布消息

这个项目当前定位是最小可用版本，适合先把 Spring Boot + Node.js WebSocket + RabbitMQ 的链路跑通。

当前 RabbitMQ 消息体已经对齐 Node.js 消费端的 `PushMessage` 协议，可直接按 `message.type` 分发。

## 技术栈

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Validation
- Spring AMQP
- RabbitMQ

## 当前能力

### 认证接口

已提供接口：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/verify`

说明：

- 用户和 token 当前都存放在内存中
- 重启服务后，用户和 token 会丢失
- 这是 MVP 方案，不是生产级认证实现

## RabbitMQ 推送

当前 RabbitMQ 结构：

- exchange: `stake.topic`
- queue: `ws.push`
- binding pattern: `push.#`

含义：

- Spring 把消息发送到 `stake.topic`
- 只要 routing key 以 `push.` 开头，就会被路由到 `ws.push`
- Node.js WebSocket 服务直接消费 `ws.push` 即可

### 默认定时消息

项目启动后会定时发送消息到 RabbitMQ。

默认配置：

- default topic: `ws.announcements`
- interval: `5000ms`

定时任务发送的消息格式：

```json
{
  "type": "topic",
  "topic": "ws.announcements",
  "userId": "",
  "timestamp": 1774800000000,
  "payload": "scheduled message at 1774800000000"
}
```

## 支持的 websocket topics

当前只允许以下 topic：

- `ws.available-balances`
- `ws.vault-balances`
- `ws.highroller-house-bets`
- `ws.announcements`
- `ws.race-status`
- `ws.feature-flag`
- `ws.notifications`
- `ws.house-bets`
- `ws.deposit-bonus-transaction`

如果发送未支持的 topic，接口会返回 `400 Bad Request`。

## 手动发布 websocket 消息

接口：

- `POST /api/ws/publish`

请求体：

```json
{
  "type": "topic",
  "topic": "ws.notifications",
  "payload": "{\"title\":\"hello\",\"content\":\"world\"}"
}
```

成功响应示例：

```json
{
  "ok": true,
  "message": "Publish success",
  "username": "topic",
  "token": null
}
```

说明：

- 这里复用了现有的 `AuthResponse` 结构
- `username` 字段当前实际返回的是 message type
- 这是为了保持 MVP 简单，后续可以单独定义发布响应结构

支持的 `type`：

- `topic`: 必须传 `topic`
- `user`: 必须传 `userId`
- `broadcast`: 不需要 `topic` 和 `userId`

## Node.js WebSocket 侧建议

Node.js 消费端期望的消息结构：

```json
{
  "type": "topic",
  "topic": "ws.notifications",
  "userId": "",
  "timestamp": 1774800000000,
  "payload": "{\"title\":\"hello\",\"content\":\"world\"}"
}
```

推荐链路：

1. Node.js 消费 RabbitMQ 队列 `ws.push`
2. 收到消息后根据 `type` 分发：
   - `broadcast`
   - `user`
   - `topic`
3. `type = topic` 时再根据 JSON 中的 `topic` 做 websocket 订阅分发

建议 websocket 客户端订阅消息格式：

```json
{
  "action": "subscribe",
  "topics": [
    "ws.notifications",
    "ws.race-status"
  ]
}
```

建议不要为每个前端订阅单独创建 RabbitMQ queue。MVP 保持一个消费队列 `ws.push`，Node.js 在内存中维护 websocket 订阅关系即可。

## 本地配置

`src/main/resources/application.properties`:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

app.rabbitmq.exchange=stake.topic
app.rabbitmq.queue=ws.push
app.rabbitmq.binding-pattern=push.#
app.rabbitmq.default-topic=ws.announcements
app.rabbitmq.publish-interval-ms=5000
```

请确保本地 RabbitMQ 运行在 `localhost:5672`。

## 启动项目

```bash
./mvnw spring-boot:run
```

## 运行测试

```bash
./mvnw test
```

## 当前限制

- 用户数据未持久化
- token 未持久化
- 没有真正的 session 共享能力
- 还没有 JWT
- `/api/ws/publish` 当前为了 MVP 已放开访问控制
- 还没有 Node.js WebSocket 示例代码

## 后续建议

后续如果继续演进，优先级建议如下：

1. 把用户和 token 存到数据库或 Redis
2. 用 JWT 或可持久化 token 替代当前内存 token
3. 给 `/api/ws/publish` 增加鉴权
4. 增加 Node.js 消费 `ws.push` 并分发 websocket 的实现
5. 根据业务把 payload 从字符串升级为结构化 JSON
