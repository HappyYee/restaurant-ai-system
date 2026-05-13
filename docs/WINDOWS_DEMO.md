# Windows 演示部署说明

这份说明用于把 GitHub 上的项目拉到另一台 Windows 电脑，并跑起后端、管理后台和微信小程序演示。

## 1. 需要安装的软件

- Git
- JDK 21 或更高版本
- Node.js 20 或更高版本
- MySQL 8.x
- 微信开发者工具

当前演示不强依赖 Redis。后端配置里保留了 Redis 地址，但 Redis 未启动时也可以运行当前功能。

## 2. 拉取项目

```powershell
git clone https://github.com/HappyYee/restaurant-ai-system.git
cd restaurant-ai-system
```

如果使用 SSH，也可以用仓库的 SSH 地址。

## 3. 配置本机环境变量

复制示例配置：

```powershell
copy scripts\windows\env.demo.ps1.example scripts\windows\env.demo.ps1
notepad scripts\windows\env.demo.ps1
```

至少要填写：

```powershell
$env:MYSQL_PASSWORD = "你的 MySQL root 密码"
```

DeepSeek Key 不会提交到 GitHub。需要真实 AI 调用时，在 `env.demo.ps1` 里填写：

```powershell
$env:DEEPSEEK_API_KEY = "你的 DeepSeek API Key"
$env:DEEPSEEK_MODEL = "deepseek-v4-pro"
```

如果不填 Key，系统仍能演示，只是 AI 经营分析和 AI 点餐会显示本地规则兜底。

## 4. 初始化数据库

确保 MySQL 服务正在运行，然后执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\windows\init-database.ps1
```

脚本会导入：

- `database/schema.sql`
- `database/init_data.sql`

数据库名为 `restaurant_ai`。

## 5. 启动后端和管理后台

一键演示：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\windows\start-demo.ps1
```

如果还没初始化数据库，也可以：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\windows\start-demo.ps1 -InitDatabase
```

脚本会打开两个 PowerShell 窗口：

- Spring Boot 后端：`http://127.0.0.1:8080`
- Vue 管理后台：`http://127.0.0.1:5173`

后台账号：

```text
admin / 123456
```

## 6. 验证接口

```powershell
curl http://127.0.0.1:8080/api/products/hot?limit=1
```

能看到 JSON 菜品数据，就说明后端已经正常。

## 7. 运行微信小程序

1. 打开微信开发者工具。
2. 导入项目目录：`miniapp`。
3. 开发者工具里关闭“校验合法域名”。
4. 如果只在微信开发者工具里演示，默认请求 `http://127.0.0.1:8080/api`，通常不需要改。
5. 如果要真机调试，手机访问不到电脑的 `127.0.0.1`，需要使用 Windows 的局域网 IP。

查看局域网 IP：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\windows\print-demo-urls.ps1
```

在微信开发者工具 Storage 里写入：

```text
key: restaurant_api_base_url
value: http://你的Windows局域网IP:8080/api
```

手机和 Windows 电脑必须在同一个局域网。

## 8. 常见问题

### 后台网页打不开

确认 `npm run dev` 窗口没有关闭，并访问：

```text
http://127.0.0.1:5173
```

### 后台登录失败

确认后端正在运行：

```powershell
curl http://127.0.0.1:8080/api/products/hot?limit=1
```

后端重启后旧 token 会失效，刷新网页后重新登录即可。

### 小程序连接不到后端

开发者工具演示优先用：

```text
http://127.0.0.1:8080/api
```

真机演示必须用：

```text
http://Windows局域网IP:8080/api
```

如果 Windows 防火墙拦截 8080，需要允许 Java 或打开 8080 端口。

### DeepSeek 没有响应

检查 `scripts/windows/env.demo.ps1` 是否配置了：

```powershell
$env:DEEPSEEK_API_KEY = "..."
```

Key 不在 GitHub 里，换电脑后必须重新配置。没配置时系统会本地兜底，不会影响基础演示。
