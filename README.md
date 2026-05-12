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

使用微信开发者工具导入 `miniapp` 目录。真机调试时，需要把 `miniapp/utils/config.js` 中的 `baseUrl` 改为 Mac 当前局域网 IP，例如：

```js
baseUrl: 'http://192.168.1.3:8080/api'
```

本地开发阶段可在微信开发者工具中关闭“校验合法域名”。

## 说明

DeepSeek 默认模型通过 `DEEPSEEK_MODEL` 配置，未配置时使用 `deepseek-v4-flash`。真实 API Key 不应提交到仓库，应通过环境变量配置；如果未配置 Key，系统会使用本地规则兜底，保证演示流程可继续。
