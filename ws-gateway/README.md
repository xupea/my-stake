# ws-gateway

基于 Node.js + uWebSockets.js 实现的高性能 WebSocket 推送网关，支持横向扩展。

## 架构

```
Java 服务端
    │
    │  发消息（RocketMQ）
    ▼
┌─────────────────────────────────────────┐
│              RocketMQ                   │
│         Topic: stake-ws-push            │
└────────────┬────────────────────────────┘
             │ 每个节点各自消费（fanout 模式）
    ┌────────┼────────┐
    ▼        ▼        ▼
  Node1    Node2    Node3      ← 多节点横向扩展
    │        │        │
    └────────┼────────┘
             │ 节点间路由（Redis Pub/Sub）
             ▼
          Redis
    （记录 userId → 节点映射）
             │
             ▼
          客户端
```

**消息路由逻辑：**
1. 用户连接时，Redis 记录 `userId → nodeId`
2. 每个节点独立消费 RocketMQ，收到消息后检查本节点是否有目标用户/订阅者
3. 有 `userId`：在本节点 → 直接推送；不在 → 查 Redis 找目标节点，通过 Redis Pub/Sub 转发
4. 无 `userId`：推送给本节点上所有订阅了该 topic 的连接

## 技术栈

| 模块 | 选型 |
|------|------|
| WebSocket 服务器 | uWebSockets.js |
| 消息队列 | RocketMQ（`@rocketmq/rocketmq-client-nodejs`） |
| 节点路由 | Redis Pub/Sub（ioredis） |
| 语言 | TypeScript |

## 目录结构

```
ws-gateway/
├── src/
│   ├── types/index.ts          # 公共类型定义
│   ├── config/index.ts         # 配置（读环境变量）
│   ├── mq/
│   │   ├── index.ts            # MQ 抽象类（换实现只改这里）
│   │   ├── rabbitmq.ts         # RabbitMQ 实现（保留备用）
│   │   └── rocketmq.ts         # RocketMQ 实现（当前使用）
│   ├── redis/index.ts          # Redis 连接 + 路由逻辑
│   ├── auth/index.ts           # 认证（调 Java HTTP 接口）
│   ├── connection/manager.ts   # 本节点连接管理
│   ├── subscription/manager.ts # 订阅管理（topic → subscriptionId → ws）
│   ├── dispatcher/index.ts     # 消息分发核心逻辑
│   ├── ws/server.ts            # WebSocket 服务器
│   └── index.ts                # 启动入口
├── Dockerfile
├── docker-compose.yml
└── .env.example
```

## 环境变量

复制 `.env.example` 为 `.env` 并按需修改：

```bash
cp .env.example .env
```

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | `3000` | 监听端口 |
| `NODE_ID` | `node-{pid}` | 节点唯一标识，多节点部署时必须各不相同 |
| `AUTH_TOKEN_VALIDATE_URL` | `http://localhost:8080/internal/token/validate` | Java 认证接口（推荐，token 方式） |
| `AUTH_VALIDATE_URL` | `http://localhost:8080/internal/session/validate` | Java 认证接口（备用，Cookie 方式） |
| `SESSION_COOKIE_NAME` | `session` | session cookie 名称 |
| `AUTH_TIMEOUT_MS` | `3000` | 认证请求超时（ms） |
| `DEV_AUTH_BYPASS` | `false` | **仅开发用**，跳过认证，直接用 token 值作为 userId |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | — | Redis 密码（可选） |
| `REDIS_DB` | `0` | Redis 数据库编号 |
| `ROCKETMQ_ENDPOINTS` | `localhost:8081` | RocketMQ Proxy gRPC 地址（5.x 默认 8081） |
| `ROCKETMQ_CONSUMER_GROUP` | `ws-gateway-group` | 消费者组名 |
| `ROCKETMQ_TOPIC` | `stake-ws-push` | 消费的 Topic |
| `ROCKETMQ_TAG` | `*` | Tag 过滤表达式，`*` 表示全部 |
| `ROCKETMQ_ACCESS_KEY` | — | 鉴权 AccessKey（生产环境必填） |
| `ROCKETMQ_ACCESS_SECRET` | — | 鉴权 AccessSecret（生产环境必填） |

## 本地开发

### 前置条件

- Node.js 20+
- Docker（用于启动 Redis + RocketMQ）

### 启动

**1. 安装依赖**

```bash
npm install
```

**2. 启动 Redis 和 RocketMQ**

```bash
docker compose up redis rocketmq-namesrv rocketmq-broker -d
```

> RocketMQ 5.x 端口说明：
> - `9876` — NameServer（Java 端 `rocketmq.name-server` 连接此端口）
> - `10911` — Broker
> - `8081` — Proxy gRPC（Node.js 客户端 `ROCKETMQ_ENDPOINTS` 连接此端口）
> - `8080` — Proxy HTTP（管理/调试用）

**3. 配置环境变量**

```bash
cp .env.example .env
# 本地无 Java 服务时，设置 DEV_AUTH_BYPASS=true
```

**4. 启动 Gateway**

```bash
npm run dev   # 监听 :3000
```

### 测试推送消息

使用 RocketMQ Dashboard 或 CLI 向 `stake-ws-push` Topic 发送消息：

**推送给指定用户（有 userId）：**
```json
{
  "topic": "ws.available-balances",
  "userId": "user-1",
  "data": { "BTC": 1.5, "USD": 1000 }
}
```

**推送给所有订阅者（无 userId）：**
```json
{
  "topic": "ws.house-bets",
  "data": { "amount": 500, "currency": "BTC", "game": "crash" }
}
```

## Docker 部署

### 单节点

```bash
docker compose up -d
```

### 多节点（横向扩展）

```bash
docker compose up -d --scale ws-gateway=3
```

> **注意**：每个节点的 `NODE_ID` 必须唯一，否则 Redis 路由会出错。
> 生产部署建议通过 Kubernetes 或 ECS 任务 ID 自动注入。

## Java 对接说明

### 认证接口

Gateway 在收到客户端 `connection_init` 消息后，调用此接口验证 token：

```
POST {AUTH_TOKEN_VALIDATE_URL}
Content-Type: application/json

{ "accessToken": "<token>" }
```

或游客用户：

```json
{ "lockdownToken": "<token>" }
```

**成功响应（200）：**
```json
{ "userId": "123", "username": "alice" }
```

**失败响应：** HTTP 401

### MQ 消息格式

Java 向 RocketMQ Topic `stake-ws-push` 发送 JSON 消息，统一格式如下：

**推给指定用户（userId 必填）：**
```json
{
  "topic": "ws.available-balances",
  "userId": "user-123",
  "data": { "BTC": 1.5 }
}
```

**推给所有订阅者（无 userId）：**
```json
{
  "topic": "ws.house-bets",
  "data": { "amount": 100, "game": "dice" }
}
```

消息路由由 `topic` 字段决定，`userId` 存在时只推送给对应用户，不存在时推送给所有订阅了该 topic 的连接。

### 支持的 Topic 列表

| Topic | 类型 | 说明 |
|-------|------|------|
| `ws.available-balances` | user | 可用余额变动 |
| `ws.vault-balances` | user | 金库余额变动 |
| `ws.deposit-bonus-transaction` | user | 充值/奖金到账 |
| `ws.notifications` | user | 个人通知 |
| `ws.house-bets` | topic | 全站下注流水 |
| `ws.highroller-house-bets` | topic | 高额下注流水 |
| `ws.race-status` | topic | 竞赛排行榜更新 |
| `ws.announcements` | broadcast | 全站公告 |
| `ws.feature-flag` | broadcast | 功能开关下发 |

## WebSocket 协议

### 连接流程

```
客户端                          ws-gateway
  │                                 │
  ├── connection_init ─────────────→│
  │                                 ├── 调用 Java 验证 token
  │←── connection_ack ──────────────┤  验证通过，状态 → initialized
  │                                 │
  ├── subscribe ───────────────────→│  { id, type: "subscribe", payload: topic }
  │                                 │
  │←── next ────────────────────────┤  { id, type: "next", payload: { data } }
  │                                 │
  ├── complete ────────────────────→│  { id }  取消订阅
```

### 消息格式

#### `connection_init`（客户端 → 服务端）
```json
{
  "type": "connection_init",
  "payload": {
    "accessToken": "<已登录用户 token>",
    "lockdownToken": "<游客 token>",
    "language": "en"
  }
}
```

#### `subscribe`（客户端 → 服务端）
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "subscribe",
  "payload": "ws.house-bets"
}
```
`id` 由客户端生成的 UUID，贯穿订阅生命周期，服务端推送数据时会带上此 id。

#### `next`（服务端 → 客户端）
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "next",
  "payload": {
    "data": { }
  }
}
```

#### `complete`（客户端 → 服务端）
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "complete"
}
```

#### `ping` / `pong`
```json
{ "type": "ping" }
{ "type": "pong" }
```

### 健康检查

```
GET /health
```

```json
{
  "status": "ok",
  "nodeId": "node-1",
  "connections": 42,
  "subscriptions": 128
}
```

## 替换 MQ

MQ 消费逻辑通过抽象类隔离在 `src/mq/index.ts`，切换实现只需两步：

1. 新建 `src/mq/kafka.ts`，继承 `MQConsumer` 抽象类并实现 `connect` / `subscribe` / `close`
2. 在 `src/index.ts` 替换导入

其余所有业务代码无需改动。
