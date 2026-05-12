CREATE DATABASE IF NOT EXISTS restaurant_ai
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE restaurant_ai;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成本价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    taste_tags VARCHAR(255) DEFAULT NULL COMMENT '口味标签，逗号分隔',
    description VARCHAR(500) DEFAULT NULL COMMENT '菜品描述',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '图片地址',
    cook_time INT DEFAULT 10 COMMENT '预计出餐时间，分钟',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    INDEX idx_category (category),
    INDEX idx_status (status)
) COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像',
    member_level VARCHAR(20) NOT NULL DEFAULT '普通会员' COMMENT '会员等级',
    points INT NOT NULL DEFAULT 0 COMMENT '会员积分',
    total_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
    member_since DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入会时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='顾客表';

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS orders (
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

CREATE TABLE IF NOT EXISTS order_item (
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

CREATE TABLE IF NOT EXISTS staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(30) NOT NULL COMMENT '手机号',
    role VARCHAR(50) NOT NULL COMMENT '岗位',
    shift_name VARCHAR(50) NOT NULL COMMENT '班次',
    salary_type VARCHAR(20) NOT NULL COMMENT '月薪/时薪',
    monthly_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hourly_wage DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    work_hours_this_month DECIMAL(10,1) NOT NULL DEFAULT 0.0,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1在职，0停用',
    hire_date DATE DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_status (status)
) COMMENT='门店人员表';

CREATE TABLE IF NOT EXISTS finance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_month CHAR(7) NOT NULL UNIQUE COMMENT '月份，例如2026-05',
    dine_in_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    miniapp_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ai_order_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    delivery_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    food_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    manager_labor_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    employee_labor_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    part_time_labor_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    rent_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    utilities_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    marketing_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    platform_fee DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    equipment_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    other_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_record_month (record_month)
) COMMENT='门店月度财务记录表';

CREATE TABLE IF NOT EXISTS ai_chat_log (
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
