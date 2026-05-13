# 基于大模型增强的餐饮门店智能点餐与经营管理系统

这是一个本科毕业设计项目，面向小型单店餐饮门店，包含微信小程序点餐端、Vue 管理后台、Spring Boot 后端和 MySQL 数据库脚本。

## 项目结构

```text
.
├── admin-web/     # Vue 3 管理后台
├── backend/       # Spring Boot 后端服务
├── database/      # MySQL 建表和演示数据
├── miniapp/       # 原生微信小程序用户端
└── 项目指导/       # 项目目标、技术栈和开发说明
```

## 已完成能力

- 管理后台：经营看板、人员管理、会员运营、财务看板、AI 经营分析、菜品/订单/库存等页面。
- 后端服务：管理员登录、微信模拟登录、菜品查询、会员资料、会员价下单、我的订单、后台订单、会员统计、AI 经营分析和 AI 点餐接口。
- 微信小程序：点餐首页、会员价菜品、菜品详情、购物车确认、提交订单、订单列表、会员中心、AI 点餐推荐页。
- 数据库：菜品、用户、管理员、订单、订单明细、人员、财务记录、AI 对话日志等表。

## 会员积分规则

- 积分获得：普通会员实付 1 元得 1 积分，银卡会员得 1.2 积分，金卡会员得 1.5 积分；积分按实付金额向下取整。
- 积分抵扣：100 积分抵 1 元，50 积分起用，可抵 0.5 元。
- 经营保护：单笔抵扣同时受会员等级上限、订单金额 10% 上限和毛利保护限制。普通会员单笔最多抵 5 元，银卡最多抵 10 元，金卡最多抵 15 元；后端会再次校验，避免抵扣后低于合理毛利。

## 后端启动

先导入数据库：

```bash
mysql -uroot -p < database/schema.sql
mysql -uroot -p < database/init_data.sql
```

启动 Spring Boot：

```bash
cd backend
export MYSQL_USERNAME=root
export MYSQL_PASSWORD="你的 MySQL 密码"
export DEEPSEEK_API_KEY="你的 DeepSeek API Key"
export DEEPSEEK_MODEL="deepseek-v4-pro"
./mvnw spring-boot:run
```

接口默认运行在：

```text
http://localhost:8080/api
```

## 管理后台启动

```bash
cd admin-web
npm install
npm run dev
```

默认开发地址通常是：

```text
http://127.0.0.1:5173
```

## 微信小程序

使用微信开发者工具导入 `miniapp` 目录。小程序默认开发环境请求 `http://127.0.0.1:8080/api`；真机调试时不要改源码，可以在开发者工具 Storage 中写入：

```js
restaurant_api_base_url = 'http://你的Mac局域网IP:8080/api'
```

本地开发阶段可在微信开发者工具中关闭“校验合法域名”；正式版需要配置 HTTPS 业务域名。

## 说明

DeepSeek 默认模型通过 `DEEPSEEK_MODEL` 配置，未配置时使用 `deepseek-v4-pro`。真实 API Key 不应提交到仓库，应通过环境变量配置；如果未配置 Key，系统会使用本地规则兜底，保证演示流程可继续。

## Windows 演示部署

另一台 Windows 电脑可以直接从 GitHub 拉取项目运行演示。推荐按文档操作：

```text
docs/WINDOWS_DEMO.md
```

快速启动脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\windows\start-demo.ps1
```

首次运行前需要安装 JDK 21+、Node.js 20+、MySQL 8、微信开发者工具，并复制 `scripts/windows/env.demo.ps1.example` 为 `scripts/windows/env.demo.ps1` 填写本机 MySQL 密码。DeepSeek Key 不会上传 GitHub，换电脑后如需真实 AI 调用，也要在这个本地配置文件里填写。
