# 星禾点餐微信小程序

这是餐饮门店智能点餐系统的微信小程序用户端，使用原生微信小程序开发。

## 已实现页面

- 点餐首页：读取后端菜品列表，按分类浏览，加入购物车。
- 菜品详情：查看原价、会员价、库存、口味标签和出餐时间。
- 确认订单：按会员价结算，调整购物车数量、填写备注、提交订单。
- 下单成功：展示订单编号和金额。
- 我的订单：读取当前用户历史订单。
- 会员：会员等级、积分、累计消费、升级进度、购物车概览和常用入口。
- 智能点餐：优先请求后端 DeepSeek AI 点餐接口，失败时使用本地规则兜底。

## 后端接口

默认请求地址在 `utils/config.js`，当前配置为真机联调使用的 Mac 局域网地址：

```js
baseUrl: 'http://192.168.1.3:8080/api'
```

本地联调前需要先启动 Spring Boot 后端，并导入 `database/schema.sql` 和 `database/init_data.sql`。如果 Mac 的 Wi-Fi IP 变化，需要同步修改 `utils/config.js`。

后端启动示例：

```bash
cd /Users/a1/AI_Studio/Code/homework/backend
export MYSQL_USERNAME=root
export MYSQL_PASSWORD="你的 MySQL 密码"
export DEEPSEEK_API_KEY="你的 DeepSeek API Key"
./mvnw spring-boot:run
```

启动后可以先在浏览器或终端确认：

```bash
curl http://192.168.1.3:8080/api/products
```

只要这个接口能返回菜品 JSON，真机小程序首页就能显示后端数据。

## 微信开发者工具

1. 打开微信开发者工具。
2. 选择“导入项目”。
3. 项目目录选择 `miniapp`。
4. AppID 可以先使用测试号或替换 `project.config.json` 中的 `appid`。
5. 本地开发阶段可关闭“校验合法域名”，真机调试时手机和 Mac 需要在同一个局域网。

## 启动优化

`app.json` 已启用微信小程序按需注入：

```json
"lazyCodeLoading": "requiredComponents"
```

首页菜品卡片已抽成 `components/product-card`，并在 `pages/index/index.json` 中配置了占位组件：

```json
"componentPlaceholder": {
  "product-card": "view"
}
```

## 当前账号流程

当前后端的 `/api/auth/wx-login` 是开发期模拟登录接口，小程序调用 `wx.login` 后会把 `code` 发给后端，后端返回自定义 token。后续接真实微信登录时，只需要改后端 `AuthServiceImpl` 的 code2Session 逻辑，小程序端请求结构可以保持不变。
