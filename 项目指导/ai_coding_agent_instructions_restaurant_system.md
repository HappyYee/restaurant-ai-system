# AI 编程智能体任务说明文档

## 项目名称

**基于大模型增强的餐饮门店智能点餐与经营管理系统**

## 文档用途

本文档用于交给 AI 编程智能体，例如 Codex、Claude Code、Cursor Agent、GitHub Copilot Agent 等，让其根据明确的项目目标、技术栈、功能范围、数据库设计、接口规范和开发顺序，逐步帮助开发本毕业设计项目。

AI 智能体需要优先保证：

1. 项目能稳定运行；
2. 后端接口可测试；
3. 数据库结构清晰；
4. 普通点餐业务闭环先完成；
5. AI 智能点餐和 AI 经营分析作为后期增强功能；
6. 不盲目引入过度复杂技术；
7. 每一步改动都要可解释、可运行、可回滚。

---

# 1. 当前开发者情况

开发者目前正在完成本科毕业设计，项目方向是餐饮门店管理系统。系统不是单纯的 CRUD 管理系统，而是在传统餐饮门店管理系统基础上加入大模型 API，形成具有创新点的智能点餐和经营分析系统。

开发者当前使用设备与环境：

- 主力设备：Mac mini M4，Apple Silicon，ARM64 架构；
- 操作系统：macOS；
- 已安装或正在配置的工具：
  - IntelliJ IDEA Ultimate 教育版；
  - 微信开发者工具；
  - MySQL Server；
  - MySQL Workbench；
  - Redis；
  - Postman；
  - Node.js；
  - Git；
  - VS Code；
  - JDK 21。

开发者希望 AI 智能体不要只给概念，而是直接指导和生成可执行代码、数据库 SQL、配置文件、接口、页面结构和测试步骤。

---

# 2. 项目总目标

本项目要实现一个面向小型单店餐饮门店的智能管理系统，包含以下端：

1. **Spring Boot 后端服务**  
   提供 RESTful API，负责业务逻辑、数据库操作、订单事务、Redis 会话管理和大模型 API 调用。

2. **Vue 3 Web 管理后台**  
   管理员通过 PC 浏览器管理菜品、订单、库存和经营数据。

3. **微信小程序用户端**  
   顾客通过小程序浏览菜单、加入购物车、普通下单、使用 AI 智能点餐和查看历史订单。

4. **MySQL 数据库**  
   存储用户、管理员、菜品、订单、订单明细和 AI 对话日志。

5. **Redis 缓存**  
   保存 AI 点餐多轮对话上下文，使用 30 分钟 TTL。

6. **DeepSeek API 或兼容大模型 API**  
   实现 AI 点餐推荐和 AI 经营分析报告。

---

# 3. 项目核心创新点

普通餐饮门店系统已经非常常见，因此本项目的重点创新不是简单的菜品增删改查，而是：

## 3.1 AI 智能点餐

用户可以自然语言描述点餐需求，例如：

```text
我想吃清淡一点的，两个人，预算 60 元以内，最好出餐快一点。
```

系统需要结合：

- 当前上架菜品；
- 当前库存；
- 菜品价格；
- 菜品口味标签；
- 预计出餐时间；
- 用户最近历史订单；
- 用户本轮自然语言需求；

生成 2 到 3 个推荐方案。

每个方案必须包含：

- 方案名称；
- 菜品列表；
- 数量；
- 数据库真实价格；
- 总价；
- 推荐理由。

AI 不能直接下单。用户必须点击确认后，后端再次校验库存并创建正式订单。

## 3.2 AI 经营分析

管理员在后台点击“AI 经营分析”按钮后，系统后端先统计经营数据，再交给大模型生成自然语言报告。

报告包括：

- 今日营业额与昨日对比；
- 今日订单数与昨日对比；
- 今日订单时段分布；
- 热销菜品分析；
- 库存预警提醒；
- 2 到 3 条经营建议。

AI 不直接查询数据库，AI 只解读后端准备好的统计数据。

---

# 4. 技术栈

## 4.1 后端

- Java 21；
- Spring Boot 3.x；
- Maven；
- MyBatis-Plus；
- MySQL Driver；
- Spring Web；
- Spring Data Redis；
- Lombok；
- JWT 或自定义 Token；
- Jackson；
- DeepSeek API HTTP Client。

## 4.2 管理后台前端

- Vue 3；
- Vite；
- Element Plus；
- Axios；
- Pinia；
- Vue Router；
- ECharts。

## 4.3 微信小程序

- 原生微信小程序；
- wx.login；
- wx.request；
- 本地 storage 保存 token 和购物车；
- 页面包括菜品列表、菜品详情、购物车确认、AI 点餐、历史订单。

## 4.4 数据库与缓存

- MySQL 8 或 MySQL 9；
- Redis 8；
- MySQL Workbench 辅助查看数据库；
- Postman 调试接口。

---

# 5. 项目开发原则

AI 编程智能体必须遵守以下开发原则。

## 5.1 先完成基础闭环，再做 AI

开发顺序必须是：

```text
Spring Boot 基础项目
→ MySQL 连接
→ 菜品表和菜品接口
→ 普通下单事务
→ 后台菜品管理
→ 后台订单管理
→ 小程序普通点餐
→ 数据看板
→ AI 智能点餐
→ AI 经营分析
```

不要一开始就写 AI 功能。AI 是增强层，不是地基。

## 5.2 代码要适合毕业设计

代码应当：

- 简洁；
- 可运行；
- 层次清晰；
- 不过度设计；
- 方便截图写论文；
- 方便答辩演示；
- 避免过度复杂架构。

不要引入微服务、Docker Compose、Kubernetes、消息队列、复杂权限系统等超出毕设范围的内容，除非开发者明确要求。

## 5.3 后端优先

后端 API 是整个系统核心。每完成一个模块，必须能用 Postman 测试。

## 5.4 不信任前端和 AI 的价格

订单总价必须由后端根据数据库价格重新计算。

前端传入的价格无效。AI 返回的价格也无效。

## 5.5 AI 不能直接操作数据库

AI 只生成建议。任何数据库写入操作必须由后端业务代码完成。

## 5.6 所有关键业务都要有异常处理

例如：

- token 失效；
- 菜品不存在；
- 菜品下架；
- 库存不足；
- AI 返回非法 JSON；
- Redis 连接失败；
- MySQL 事务回滚；
- 大模型 API 超时。

---

# 6. 推荐项目目录结构

AI 智能体应按照以下结构创建项目。

```text
restaurant-ai-system/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/example/restaurant/
│   │   ├── RestaurantApplication.java
│   │   ├── common/
│   │   │   ├── Result.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── BusinessException.java
│   │   ├── config/
│   │   │   ├── WebConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   └── MyBatisPlusConfig.java
│   │   ├── entity/
│   │   │   ├── Product.java
│   │   │   ├── User.java
│   │   │   ├── AdminUser.java
│   │   │   ├── Orders.java
│   │   │   ├── OrderItem.java
│   │   │   └── AiChatLog.java
│   │   ├── mapper/
│   │   │   ├── ProductMapper.java
│   │   │   ├── UserMapper.java
│   │   │   ├── AdminUserMapper.java
│   │   │   ├── OrdersMapper.java
│   │   │   ├── OrderItemMapper.java
│   │   │   └── AiChatLogMapper.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── OrderService.java
│   │   │   ├── AuthService.java
│   │   │   ├── AdminAuthService.java
│   │   │   ├── AiOrderService.java
│   │   │   ├── BusinessStatsService.java
│   │   │   └── AiAnalysisService.java
│   │   ├── service/impl/
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProductController.java
│   │   │   ├── OrderController.java
│   │   │   ├── AiOrderController.java
│   │   │   ├── AdminAuthController.java
│   │   │   ├── AdminProductController.java
│   │   │   ├── AdminOrderController.java
│   │   │   ├── AdminStatsController.java
│   │   │   └── AdminAiController.java
│   │   ├── dto/
│   │   ├── vo/
│   │   ├── security/
│   │   │   ├── TokenUtil.java
│   │   │   └── LoginInterceptor.java
│   │   └── ai/
│   │       ├── DeepSeekClient.java
│   │       ├── AiPromptBuilder.java
│   │       ├── AiResponseParser.java
│   │       └── AiRecommendationValidator.java
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/
│
├── admin-web/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/
│       ├── router/
│       ├── store/
│       ├── views/
│       │   ├── Login.vue
│       │   ├── Dashboard.vue
│       │   ├── ProductManage.vue
│       │   └── OrderManage.vue
│       └── components/
│
├── miniapp/
│   ├── app.js
│   ├── app.json
│   ├── app.wxss
│   ├── pages/
│   │   ├── index/
│   │   ├── product-detail/
│   │   ├── order-confirm/
│   │   ├── order-success/
│   │   ├── ai-chat/
│   │   ├── order-list/
│   │   └── user/
│   └── utils/
│       ├── request.js
│       └── auth.js
│
└── database/
    ├── schema.sql
    └── init_data.sql
```

---

# 7. 数据库设计

请优先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS restaurant_ai
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;
```

## 7.1 product 菜品表

```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    taste_tags VARCHAR(255) DEFAULT NULL COMMENT '口味标签，逗号分隔',
    description VARCHAR(500) DEFAULT NULL COMMENT '菜品描述',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '图片地址',
    cook_time INT DEFAULT 10 COMMENT '预计出餐时间，分钟',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除'
) COMMENT='菜品表';
```

## 7.2 user 顾客表

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='顾客表';
```

## 7.3 admin_user 管理员表

```sql
CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密密码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='管理员表';
```

## 7.4 orders 订单主表

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总价',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待处理，1制作中，2已完成，3已取消',
    remark VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
    source TINYINT NOT NULL DEFAULT 0 COMMENT '来源：0普通点餐，1AI点餐',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status),
    INDEX idx_source (source)
) COMMENT='订单主表';
```

## 7.5 order_item 订单明细表

```sql
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '菜品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '下单时菜品名称快照',
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '下单时单价快照',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) COMMENT='订单明细表';
```

## 7.6 ai_chat_log AI 对话记录表

```sql
CREATE TABLE ai_chat_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL COMMENT '用户ID，经营分析可为空',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    scene VARCHAR(50) NOT NULL COMMENT '场景：order_recommend/business_analysis',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_scene (scene)
) COMMENT='AI对话记录表';
```

---

# 8. 初始化演示数据

AI 智能体应创建 `database/init_data.sql`，包含适合演示的菜品数据。

```sql
INSERT INTO product (name, category, price, stock, status, taste_tags, description, image_url, cook_time) VALUES
('番茄鸡蛋饭', '主食', 22.00, 20, 1, '清淡,不辣,实惠', '番茄鸡蛋搭配米饭，口味清淡，适合日常午餐。', '', 8),
('牛肉饭', '主食', 28.00, 5, 1, '微辣,热销,饱腹', '牛肉搭配米饭，微辣口味，门店热销菜品。', '', 10),
('鸡腿饭', '主食', 26.00, 30, 1, '咸香,饱腹,热销', '香煎鸡腿搭配米饭，适合想吃饱的顾客。', '', 12),
('鸡胸肉轻食饭', '主食', 30.00, 15, 1, '清淡,高蛋白,健康', '鸡胸肉搭配蔬菜和米饭，适合清淡健康需求。', '', 10),
('柠檬茶', '饮品', 8.00, 50, 1, '清爽,甜,饮品', '清爽柠檬茶，适合搭配主食。', '', 2),
('无糖绿茶', '饮品', 6.00, 40, 1, '清淡,无糖,饮品', '无糖绿茶，适合清淡低糖需求。', '', 1),
('香辣鸡翅', '小吃', 16.00, 8, 1, '香辣,小吃', '香辣口味鸡翅，适合加餐。', '', 8),
('薯条', '小吃', 10.00, 25, 1, '小吃,实惠', '经典薯条，可作为加餐。', '', 5);
```

管理员账号可在后端启动时初始化，或在 SQL 中插入 BCrypt 加密后的密码。开发阶段可以先使用简单密码，但最终文档和代码中应说明密码加密存储。

---

# 9. 后端配置要求

## 9.1 application.yml 示例

AI 智能体应生成类似以下配置，并提醒开发者替换密码和 API Key。

```yaml
server:
  port: 8080

spring:
  application:
    name: restaurant-ai-system

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/restaurant_ai?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_MYSQL_PASSWORD

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 5000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

ai:
  deepseek:
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:}
    model: deepseek-chat
    timeout-seconds: 20
```

## 9.2 敏感信息规则

不得把真实 DeepSeek API Key 写死到代码仓库中。

推荐用环境变量：

```bash
export DEEPSEEK_API_KEY="你的真实key"
```

`.gitignore` 必须忽略：

```text
.env
application-local.yml
*.log
target/
node_modules/
```

---

# 10. 后端依赖要求

请在 `backend/pom.xml` 中加入以下依赖。

必须包含：

- spring-boot-starter-web；
- mysql-connector-j；
- mybatis-plus-spring-boot3-starter；
- spring-boot-starter-data-redis；
- lombok；
- spring-boot-starter-validation；
- jjwt 或其他 token 工具；
- okhttp 或 spring webclient，用于调用 DeepSeek API。

示意：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.9</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

如版本冲突，请优先保证项目能启动，再调整依赖版本。

---

# 11. 通用接口返回格式

所有后端接口必须统一返回格式。

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误示例：

```json
{
  "code": 400,
  "message": "库存不足",
  "data": null
}
```

建议创建：

```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

常用状态码：

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 业务错误或参数错误 |
| 401 | 未登录或 token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器异常 |
| 600 | AI 服务调用失败 |

---

# 12. 用户端接口要求

## 12.1 微信登录

```http
POST /api/auth/login
```

开发阶段可以先使用模拟登录，避免微信 openid 接口阻塞开发。

模拟登录请求：

```json
{
  "code": "test-code"
}
```

返回：

```json
{
  "token": "user-token",
  "userId": 1,
  "nickname": "测试用户"
}
```

正式版本再接入 wx.login 和 openid 换取逻辑。

## 12.2 菜品列表

```http
GET /api/products
GET /api/products?category=主食
```

规则：

- 只返回未删除且上架菜品；
- stock = 0 可以返回，但前端显示已售罄；
- AI 推荐候选必须要求 stock > 0。

## 12.3 菜品详情

```http
GET /api/products/{id}
```

## 12.4 创建普通订单

```http
POST /api/orders
Authorization: Bearer {token}
```

请求：

```json
{
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 5, "quantity": 2}
  ],
  "remark": "不要香菜"
}
```

返回：

```json
{
  "orderId": 1001,
  "orderNo": "202605090001",
  "totalAmount": 60.00
}
```

## 12.5 我的订单

```http
GET /api/orders/my
Authorization: Bearer {token}
```

只返回当前用户订单，不能返回其他用户订单。

## 12.6 AI 点餐对话

```http
POST /api/ai/chat
Authorization: Bearer {token}
```

请求：

```json
{
  "sessionId": "uuid-session-id",
  "message": "我想吃清淡一点的，两个人，预算60元以内"
}
```

返回：

```json
{
  "sessionId": "uuid-session-id",
  "reply": "根据你的需求，我推荐以下方案。",
  "plans": [
    {
      "planId": "plan_1",
      "planName": "清淡实惠方案",
      "items": [
        {
          "productId": 1,
          "productName": "番茄鸡蛋饭",
          "quantity": 2,
          "unitPrice": 22.00,
          "subtotal": 44.00
        },
        {
          "productId": 6,
          "productName": "无糖绿茶",
          "quantity": 2,
          "unitPrice": 6.00,
          "subtotal": 12.00
        }
      ],
      "totalAmount": 56.00,
      "reason": "该方案口味清淡，适合两人，价格低于60元，出餐时间较短。"
    }
  ]
}
```

## 12.7 确认 AI 推荐方案

```http
POST /api/ai/confirm
Authorization: Bearer {token}
```

请求：

```json
{
  "sessionId": "uuid-session-id",
  "planId": "plan_1",
  "remark": "不要香菜"
}
```

返回：

```json
{
  "orderId": 1002,
  "orderNo": "202605090002",
  "totalAmount": 56.00
}
```

---

# 13. 管理端接口要求

## 13.1 管理员登录

```http
POST /api/admin/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

返回：

```json
{
  "token": "admin-token",
  "username": "admin"
}
```

## 13.2 菜品管理

```http
GET    /api/admin/products
POST   /api/admin/products
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
```

管理端菜品列表需要包括下架菜品。

支持：

- 按名称搜索；
- 按分类筛选；
- 上架；
- 下架；
- 修改库存；
- 逻辑删除。

## 13.3 订单管理

```http
GET /api/admin/orders
GET /api/admin/orders/{id}
PUT /api/admin/orders/{id}/status
```

状态流转：

```text
待处理(0) → 制作中(1) → 已完成(2)
待处理(0) → 已取消(3)
制作中(1) → 已取消(3)
```

取消订单时需要恢复库存。已完成订单不能取消。

## 13.4 数据看板

```http
GET /api/admin/stats/overview
GET /api/admin/stats/trend
GET /api/admin/stats/hot-products
GET /api/admin/stats/hourly-distribution
```

overview 返回：

```json
{
  "todaySales": 2300.00,
  "todayOrderCount": 87,
  "todayAiOrderCount": 12,
  "lowStockCount": 3
}
```

## 13.5 AI 经营分析

```http
GET /api/admin/ai/analysis
Authorization: Bearer {adminToken}
```

返回：

```json
{
  "report": "## 今日经营概况\n今日营业额为...",
  "generatedAt": "2026-05-09 10:30:00"
}
```

---

# 14. 订单事务规则

AI 智能体必须重点实现订单创建事务。

订单创建步骤：

1. 校验用户登录；
2. 校验 items 非空；
3. 遍历每个 productId；
4. 查询菜品是否存在、上架、未删除；
5. 使用数据库真实价格计算 subtotal；
6. 使用条件更新扣减库存；
7. 若任意菜品扣减失败，抛异常并回滚；
8. 创建 orders 记录；
9. 创建 order_item 记录；
10. 返回订单编号和总价。

防超卖 SQL：

```sql
UPDATE product
SET stock = stock - #{quantity}
WHERE id = #{productId}
  AND stock >= #{quantity}
  AND status = 1
  AND deleted = 0;
```

如果影响行数为 0，则说明库存不足或菜品不可售，订单创建失败。

---

# 15. AI 点餐实现规则

## 15.1 AI 点餐候选数据

调用大模型前，后端必须查询：

- status = 1；
- deleted = 0；
- stock > 0；

的菜品作为候选。

同时查询当前用户最近 5 条订单作为偏好参考。

## 15.2 AI 输出格式

大模型必须输出 JSON。

```json
{
  "reply": "一句简短说明",
  "plans": [
    {
      "planName": "方案名称",
      "items": [
        {"productId": 1, "quantity": 1}
      ],
      "reason": "推荐理由"
    }
  ]
}
```

## 15.3 Prompt 模板

```text
你是一个餐饮门店的智能点餐助手。你的任务是根据用户需求、当前可售菜品、库存、价格、口味标签、预计出餐时间和用户历史订单，推荐 2 到 3 个点餐方案。

必须遵守以下规则：
1. 只能推荐提供的菜品列表中的菜品；
2. 必须使用菜品 id，不要编造菜品；
3. 不要推荐库存不足的菜品；
4. 每个方案包含 1 到 4 个菜品；
5. 每个方案必须包含推荐理由；
6. 不要直接替用户下单；
7. 输出必须是合法 JSON；
8. 不要输出 Markdown；
9. 不要输出 JSON 之外的解释。

当前可售菜品：
{availableProducts}

用户最近订单：
{recentOrders}

历史对话：
{conversationHistory}

本轮用户需求：
{userMessage}

请按以下格式输出：
{
  "reply": "一句简短说明",
  "plans": [
    {
      "planName": "方案名称",
      "items": [
        {"productId": 1, "quantity": 1}
      ],
      "reason": "推荐理由"
    }
  ]
}
```

## 15.4 AI 结果校验

AI 返回后，后端必须：

1. 解析 JSON；
2. 校验 plans 是否存在；
3. 校验 productId 是否真实存在；
4. 校验菜品是否上架；
5. 校验菜品是否未删除；
6. 校验库存是否足够；
7. 校验 quantity 是否为正整数；
8. 使用数据库价格重新计算总价；
9. 移除不合法方案；
10. 如果所有方案都失败，返回错误，让用户重新描述。

## 15.5 Redis 会话规则

Redis key：

```text
ai:session:{sessionId}
```

TTL：

```text
30 分钟
```

保存内容：

```json
[
  {"role": "user", "content": "我想吃清淡一点的"},
  {"role": "assistant", "content": "已推荐清淡方案"}
]
```

下单成功后删除该 session。

---

# 16. AI 经营分析实现规则

## 16.1 后端统计数据包

后端先统计：

```json
{
  "todaySales": 2300.00,
  "yesterdaySales": 2800.00,
  "todayOrderCount": 87,
  "yesterdayOrderCount": 103,
  "timeDistribution": {
    "breakfast": 10,
    "lunch": 42,
    "afternoon": 8,
    "dinner": 25,
    "night": 2
  },
  "topProducts": [
    {"name": "牛肉饭", "quantity": 32},
    {"name": "柠檬茶", "quantity": 28},
    {"name": "鸡腿饭", "quantity": 20}
  ],
  "lowStockProducts": [
    {"name": "鸡腿饭", "stock": 6},
    {"name": "柠檬茶", "stock": 4}
  ]
}
```

## 16.2 AI 分析 Prompt

```text
你是一个餐饮门店经营分析助手。以下是系统后端已经统计好的门店经营数据。请根据数据生成一份简洁、专业、可执行的经营分析报告。

要求：
1. 不要编造数据；
2. 不要使用系统未提供的信息；
3. 报告包含营业额变化、订单变化、高峰时段、热销菜品、库存预警和经营建议；
4. 建议要具体，可执行；
5. 输出中文；
6. 使用小标题分段。

经营数据：
{businessData}
```

## 16.3 报告内容必须包括

- 今日经营概况；
- 与昨日对比；
- 高峰时段分析；
- 热销菜品分析；
- 库存预警；
- 经营建议。

---

# 17. 管理后台页面要求

## 17.1 登录页

功能：

- 输入用户名；
- 输入密码；
- 登录成功保存 adminToken；
- 跳转 Dashboard。

## 17.2 数据看板 Dashboard

展示：

- 今日营业额；
- 今日订单数；
- AI 点餐订单数；
- 库存预警数量；
- 近 7 天销售趋势；
- 热销 TOP5；
- 今日时段订单分布；
- AI 经营分析按钮和报告区域。

## 17.3 菜品管理页

功能：

- 表格展示菜品；
- 搜索；
- 分类筛选；
- 新增；
- 编辑；
- 上架；
- 下架；
- 删除；
- 修改库存。

## 17.4 订单管理页

功能：

- 订单列表；
- 按状态筛选；
- 按日期筛选；
- 查看详情；
- 接单；
- 完成；
- 取消。

---

# 18. 微信小程序页面要求

## 18.1 首页 / 菜品列表

功能：

- 自动登录或模拟登录；
- 分类 Tab；
- 菜品卡片；
- 已售罄标记；
- 加入购物车；
- 底部购物车栏；
- 跳转 AI 点餐。

## 18.2 菜品详情页

展示：

- 图片；
- 名称；
- 价格；
- 标签；
- 描述；
- 出餐时间；
- 加入购物车按钮。

## 18.3 订单确认页

展示：

- 购物车菜品；
- 数量；
- 单价；
- 总价；
- 备注输入框；
- 确认下单按钮。

## 18.4 AI 点餐页

聊天式布局：

- 顶部消息区；
- 用户气泡；
- AI 气泡；
- 方案卡片；
- 每个方案有“选这个”；
- 底部输入框；
- 支持多轮追问。

## 18.5 我的订单页

展示：

- 下单时间；
- 总价；
- 状态；
- 来源：普通点餐 / AI 点餐；
- 菜品摘要；
- 点击查看详情。

---

# 19. 开发顺序和任务拆分

AI 编程智能体必须按以下顺序推进。

## 阶段 1：后端项目初始化

目标：Spring Boot 能启动，能返回 hello。

任务：

1. 创建 `backend` 项目；
2. 配置 Maven 依赖；
3. 配置 `application.yml`；
4. 创建 `Result`；
5. 创建全局异常处理；
6. 创建测试接口 `/api/test/hello`。

验收：

```http
GET http://localhost:8080/api/test/hello
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "hello"
}
```

## 阶段 2：数据库与菜品接口

目标：后端能连接 MySQL，并返回菜品列表。

任务：

1. 创建数据库；
2. 执行 schema.sql；
3. 执行 init_data.sql；
4. 创建 Product 实体；
5. 创建 ProductMapper；
6. 创建 ProductService；
7. 创建 ProductController。

验收：

```http
GET http://localhost:8080/api/products
```

能返回上架菜品。

## 阶段 3：普通订单接口

目标：普通下单闭环跑通。

任务：

1. 创建 Orders 和 OrderItem 实体；
2. 创建 OrderCreateRequest；
3. 实现订单编号生成；
4. 实现库存校验；
5. 实现库存扣减；
6. 实现订单主表和明细表写入；
7. 使用事务；
8. 实现我的订单查询。

验收：

```http
POST http://localhost:8080/api/orders
```

能创建订单，并且 product.stock 正确减少。

## 阶段 4：管理后台 API

目标：管理员可以管理菜品和订单。

任务：

1. 管理员登录；
2. 后台菜品列表；
3. 新增菜品；
4. 编辑菜品；
5. 上下架；
6. 逻辑删除；
7. 订单列表；
8. 订单详情；
9. 订单状态更新；
10. 取消订单恢复库存。

验收：

Postman 可以完整测试后台 API。

## 阶段 5：数据看板 API

目标：返回经营统计数据。

任务：

1. 今日营业额；
2. 今日订单数；
3. AI 点餐订单数；
4. 库存预警数；
5. 近 7 天销售趋势；
6. 热销 TOP5；
7. 时段订单分布。

验收：

```http
GET http://localhost:8080/api/admin/stats/overview
```

能返回正确统计数据。

## 阶段 6：Vue 管理后台

目标：后台页面能操作真实接口。

任务：

1. 创建 Vue 3 项目；
2. 配置 axios；
3. 配置路由；
4. 登录页；
5. Dashboard；
6. 菜品管理页；
7. 订单管理页。

验收：

后台能登录，能看到菜品和订单，能修改状态。

## 阶段 7：微信小程序普通点餐

目标：用户端普通点餐闭环跑通。

任务：

1. 请求封装；
2. 登录或模拟登录；
3. 菜品列表；
4. 菜品详情；
5. 购物车；
6. 订单确认；
7. 创建订单；
8. 我的订单。

验收：

小程序能完成普通点餐，后台能看到订单。

## 阶段 8：AI 智能点餐

目标：AI 能推荐方案并转为订单。

任务：

1. 封装 DeepSeekClient；
2. 编写 PromptBuilder；
3. 查询可售菜品；
4. 查询用户最近订单；
5. Redis 保存会话；
6. 解析 AI JSON；
7. 校验推荐方案；
8. 返回方案卡片数据；
9. 确认方案并创建 AI 来源订单；
10. 小程序 AI 聊天页。

验收：

用户输入自然语言，系统返回 2 到 3 个方案，用户点击方案后成功下单。

## 阶段 9：AI 经营分析

目标：后台生成经营报告。

任务：

1. 统计经营数据包；
2. 编写经营分析 Prompt；
3. 调用大模型；
4. 返回报告；
5. 前端展示报告。

验收：

后台点击按钮能生成自然语言经营分析。

---

# 20. 测试命令

开发者当前 Mac 环境中，可用以下命令检查环境。

```bash
java -version
javac -version
node -v
npm -v
git --version
mysql --version
redis-cli ping
mvn -v
```

如果 `redis-cli ping` 返回：

```text
PONG
```

说明 Redis 正常。

如果 `mysql -u root -p` 能进入：

```text
mysql>
```

说明 MySQL 正常。

---

# 21. 运行命令规范

## 21.1 后端运行

在 `backend` 目录：

```bash
./mvnw spring-boot:run
```

或在 IDEA 中运行 `RestaurantApplication`。

## 21.2 Vue 运行

在 `admin-web` 目录：

```bash
npm install
npm run dev
```

## 21.3 Redis 运行

如果 Redis 没启动：

```bash
redis-server /opt/homebrew/etc/redis.conf
```

或后台服务方式：

```bash
brew services start redis
```

## 21.4 MySQL 登录

```bash
mysql -u root -p
```

---

# 22. Git 规范

AI 智能体生成代码时，应建议开发者定期提交 Git。

推荐提交粒度：

```text
feat: initialize Spring Boot backend
feat: add product table and product APIs
feat: implement order creation transaction
feat: add admin product management APIs
feat: add dashboard statistics APIs
feat: add AI order recommendation service
feat: add Vue admin dashboard
feat: add miniapp product list and cart
fix: handle insufficient stock when creating orders
```

推荐分支：

```text
main
feature/backend-basic
feature/product-order
feature/admin-web
feature/miniapp
feature/ai-order
feature/ai-analysis
```

---

# 23. 答辩演示目标

最终系统必须能演示以下流程。

## 23.1 普通点餐流程

```text
小程序打开
→ 浏览菜品
→ 加入购物车
→ 提交订单
→ 后台出现订单
→ 库存减少
→ 管理员接单
→ 管理员完成订单
→ 数据看板营业额变化
```

## 23.2 AI 点餐流程

```text
进入 AI 点餐页
→ 输入“我想吃清淡一点的，两个人，预算60元以内”
→ AI 返回 2 到 3 个推荐方案
→ 用户选择方案
→ 确认下单
→ 后台订单来源显示 AI 点餐
```

## 23.3 库存约束演示

```text
后台将牛肉饭库存改为 0
→ 用户问“我想吃牛肉类的”
→ AI 不推荐库存为 0 的牛肉饭
→ 系统推荐替代方案或说明当前库存不足
```

## 23.4 AI 经营分析演示

```text
后台打开数据看板
→ 点击 AI 经营分析
→ 系统生成包含营业额、订单、热销、库存和建议的报告
```

---

# 24. 不要做的事情

AI 编程智能体不要主动实现以下内容，除非开发者明确要求：

1. 微服务架构；
2. Docker Compose；
3. Kubernetes；
4. 真实微信支付；
5. 真实手机号登录；
6. 复杂会员积分系统；
7. 优惠券系统；
8. 多门店系统；
9. 复杂员工排班；
10. 动态定价；
11. 语音点餐；
12. 摄像头识别；
13. 自训练大模型；
14. RAG 知识库；
15. 复杂权限 RBAC。

这些功能可以作为论文“后续展望”，但不进入第一版开发。

---

# 25. AI 智能体工作方式要求

当 AI 智能体帮助开发时，请按以下方式工作。

## 25.1 每次只完成一个明确任务

例如：

- 只创建数据库表；
- 只写 ProductController；
- 只写订单创建事务；
- 只写 AI Prompt；
- 只写 Vue 菜品管理页。

不要一次性生成大量无法验证的代码。

## 25.2 每次生成代码后必须说明

1. 修改了哪些文件；
2. 新增了哪些文件；
3. 如何运行；
4. 如何测试；
5. 可能出错的地方；
6. 下一步建议。

## 25.3 优先保证可运行

如果高级写法和简单写法冲突，优先选择简单可运行的写法。

## 25.4 不要隐藏关键配置

例如：

- MySQL 用户名密码；
- Redis 地址；
- DeepSeek API Key；
- 端口号；
- 前端 baseURL。

要明确告诉开发者在哪里改。

## 25.5 出错时先定位，不要盲目重写

例如出现错误时，应先检查：

- 日志；
- 端口；
- 数据库连接；
- Redis 是否启动；
- Maven 依赖是否下载；
- Node 版本；
- API Key 是否配置；
- 请求路径是否正确；
- token 是否携带。

---

# 26. 当前第一步任务

AI 智能体收到本文档后，第一步不要直接开发全部项目。

第一步应该执行：

## 创建后端基础项目

要求：

1. 创建 `backend` 目录；
2. 创建 Spring Boot 3.x Maven 项目；
3. Java 版本使用 21；
4. 添加 Spring Web、MySQL Driver、MyBatis-Plus、Redis、Lombok、Validation；
5. 配置 `application.yml`；
6. 创建统一返回类 `Result`；
7. 创建全局异常处理；
8. 创建测试接口：

```http
GET /api/test/hello
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "hello"
}
```

完成这个后，再进入数据库建表和菜品接口开发。

---

# 27. 最终目标

最终交付一个完整、可运行、可演示、可写论文的毕业设计项目：

```text
Spring Boot 后端
+ MySQL 数据库
+ Redis 会话缓存
+ Vue 管理后台
+ 微信小程序用户端
+ AI 智能点餐
+ AI 经营分析
```

系统的核心价值是：

1. 用普通餐饮管理系统保证工程完整性；
2. 用 AI 智能点餐体现用户端创新；
3. 用 AI 经营分析体现管理端创新；
4. 用后端校验和事务保证系统可靠性；
5. 用清晰的开发顺序保证毕设能落地完成。

