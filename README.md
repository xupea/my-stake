# MyStake MonoRepo

这是一个包含 TypeScript 和 Java 的混合 monorepo，由三个项目组成：

- **client/** - Next.js 前端应用
- **ws-gateway/** - WebSocket 网关（Node.js + TypeScript）
- **server/** - 后端 API 服务（Java + Spring Boot）

## 🎯 项目结构

```
my-stake/
├── client/              # Next.js 前端
├── ws-gateway/          # WebSocket 网关
├── server/              # Java 后端
├── package.json         # 根项目配置（pnpm workspaces）
├── pnpm-workspace.yaml  # pnpm 工作空间配置
└── .gitignore          # Git 忽略配置
```

## 📦 安装依赖

> **前置条件**: 确保已安装 Node.js、pnpm、Java 11+ 和 Maven

```bash
# 安装所有依赖（包括 Java 依赖）
pnpm install:all

# 仅安装 Node.js 依赖
pnpm install
```

## 🚀 开发

### 启动所有服务
```bash
pnpm dev:all
```

### 启动单个服务
```bash
pnpm dev:client      # 前端 (localhost:3000)
pnpm dev:gateway     # 网关 (localhost:8080)
pnpm dev:server      # 后端 (localhost:8080)
```

## 🔨 构建

### 构建所有项目
```bash
pnpm build
```

### 构建单个项目
```bash
pnpm build:client
pnpm build:gateway
pnpm build:server    # 生成 JAR 包
```

## 🧹 清理

```bash
# 删除所有构建输出和 node_modules
pnpm clean
```

## 📋 可用命令

| 命令 | 描述 |
|------|------|
| `pnpm install:all` | 安装所有依赖（Node + Java） |
| `pnpm build` | 构建所有项目 |
| `pnpm dev:all` | 同时启动所有服务 |
| `pnpm dev:client` | 启动前端开发服务器 |
| `pnpm dev:gateway` | 启动网关服务 |
| `pnpm dev:server` | 启动后端服务 |
| `pnpm lint` | 执行所有 lint 检查 |
| `pnpm clean` | 清理所有构建输出 |

## 🔗 端口配置

- **Client**: http://localhost:3000
- **Server**: http://localhost:8080
- **WebSocket Gateway**: ws://localhost:8080

## 📝 工作空间依赖

pnpm workspaces 管理 `client` 和 `ws-gateway` 的依赖  
`server` 使用 Maven 独立管理，启用共享 Maven 缓存以加快构建速度

### 更新特定包
```bash
# 更新 client 中的依赖
pnpm --filter client add package-name

# 更新 ws-gateway 中的依赖
pnpm --filter ws-gateway add package-name

# 更新 server 依赖（修改 pom.xml）
cd server && mvn dependency:tree
```

## 🐛 故障排除

### 缓存问题
```bash
# 清理 pnpm 缓存
pnpm store prune

# 完全重装
pnpm clean && pnpm install:all
```

### Maven 缓存问题
```bash
cd server
mvn clean
rm -rf ~/.m2/repository
mvn clean install
```

## 📚 更多信息

- [pnpm Workspaces](https://pnpm.io/workspaces)
- [Maven 官方文档](https://maven.apache.org/)
