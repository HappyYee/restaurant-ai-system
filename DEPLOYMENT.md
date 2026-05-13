# 星禾点餐系统公网部署说明

本文档用于把项目部署到一台 Linux 服务器，并通过公网域名演示管理后台、后端 API 和微信小程序预览版。

## 1. 服务器环境

推荐服务器配置：

- Ubuntu 22.04 LTS 或更新版本
- 2GB 内存以上
- Docker Engine 24+
- Docker Compose v2
- 已开放安全组端口：`22`、`80`、`443`

项目目录约定：

```bash
/opt/restaurant-project
```

## 2. 域名规划

部署完成后使用三个域名：

```text
https://yee.earth          管理后台网站
https://www.yee.earth      管理后台网站
https://api.yee.earth      后端 API
```

后端 API 统一基础地址：

```text
https://api.yee.earth/api
```

## 3. Cloudflare DNS 设置

在 Cloudflare 中进入 `yee.earth` 的 DNS 页面，添加或确认以下记录：

```text
类型    名称    内容
A       @       服务器公网 IPv4
A       www     服务器公网 IPv4
A       api     服务器公网 IPv4
```

建议：

- 首次部署时可以先关闭代理云朵，确认 Caddy 能正常签发 HTTPS 证书。
- 如果开启 Cloudflare 代理，SSL/TLS 模式建议使用 `Full` 或 `Full (strict)`。
- DNS 生效后再执行首次部署。

## 4. Docker 权限

安装 Docker 后，把当前用户加入 docker 组：

```bash
sudo usermod -aG docker $USER
newgrp docker
docker version
docker compose version
```

如果仍然没有权限，可以临时在命令前加 `sudo`。

## 5. 首次部署

进入服务器项目目录：

```bash
cd /opt/restaurant-project
git pull
```

创建生产环境变量文件：

```bash
cp .env.production.example .env.production
nano .env.production
```

必须修改：

```text
MYSQL_ROOT_PASSWORD
MYSQL_PASSWORD
DEFAULT_ADMIN_PASSWORD
DEEPSEEK_API_KEY
```

说明：

- `.env.production` 不要提交到 GitHub。
- `DEEPSEEK_API_KEY` 可以暂时留空，系统会使用本地规则兜底，演示流程不会中断。
- `DEFAULT_ADMIN_PASSWORD` 是后台管理员 `admin` 首次初始化密码。

启动全部服务：

```bash
docker compose --env-file .env.production up -d --build
```

首次启动时 MySQL 会自动执行：

```text
database/schema.sql
database/init_data.sql
```

数据库名为：

```text
restaurant_ai
```

管理员账号由后端初始化器创建：

```text
username: admin
password: .env.production 中的 DEFAULT_ADMIN_PASSWORD
```

## 6. 更新部署

代码更新后，在服务器执行：

```bash
cd /opt/restaurant-project
git pull
docker compose --env-file .env.production up -d --build
```

如果只更新了前端或 Caddyfile，也可以直接重建 Caddy：

```bash
docker compose --env-file .env.production up -d --build caddy
```

如果只更新了后端：

```bash
docker compose --env-file .env.production up -d --build backend
```

## 7. 查看状态和日志

查看容器状态：

```bash
docker compose --env-file .env.production ps
```

查看后端日志：

```bash
docker compose --env-file .env.production logs -f backend
```

查看 Caddy 日志：

```bash
docker compose --env-file .env.production logs -f caddy
```

查看 MySQL 日志：

```bash
docker compose --env-file .env.production logs -f mysql
```

## 8. 数据库备份

备份数据库到当前目录：

```bash
docker compose --env-file .env.production exec mysql \
  sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" restaurant_ai' \
  > restaurant_ai_$(date +%Y%m%d_%H%M%S).sql
```

建议把备份文件下载到本地保存。

## 9. 数据库恢复

把备份文件上传到服务器，例如：

```text
/opt/restaurant-project/backup.sql
```

恢复数据库：

```bash
cat backup.sql | docker compose --env-file .env.production exec -T mysql \
  sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" restaurant_ai'
```

恢复后重启后端：

```bash
docker compose --env-file .env.production restart backend
```

## 10. 网站和 API 测试

部署后执行：

```bash
docker compose --env-file .env.production ps
docker compose --env-file .env.production logs -f backend
docker compose --env-file .env.production logs -f caddy
curl http://localhost:8080/health
curl https://api.yee.earth/health
curl https://api.yee.earth/api/health
curl -I https://yee.earth
curl -I https://www.yee.earth
```

期望：

- `curl http://localhost:8080/health` 返回 `{"status":"ok"}`。
- `curl https://api.yee.earth/health` 返回 `{"status":"ok"}`。
- `curl https://api.yee.earth/api/health` 返回 `{"status":"ok"}`。
- `curl -I https://yee.earth` 和 `curl -I https://www.yee.earth` 返回 `200`。

管理后台登录地址：

```text
https://yee.earth
```

后台生产构建会请求：

```text
https://api.yee.earth/api
```

并且默认关闭 mock fallback，用于证明页面数据来自服务器数据库。

## 11. 微信小程序预览测试

小程序不需要正式发布，答辩时按预览版演示：

1. 打开微信开发者工具。
2. 导入项目目录中的 `miniapp`。
3. 在开发者工具中勾选“不校验合法域名、web-view、TLS版本以及HTTPS证书”。
4. 点击“编译”。
5. 点击“预览”。
6. 用手机微信扫码打开。
7. 在调试器 Network 中确认请求地址是 `https://api.yee.earth/api`。
8. 在后台新增或修改菜品、订单、会员数据，再回到小程序刷新确认能看到同一套服务器数据。

如需临时切换 API，可在微信开发者工具 Storage 中设置：

```text
restaurant_api_base_url=https://api.yee.earth/api
```

## 12. 答辩演示步骤

建议演示顺序：

1. 打开 `https://yee.earth`，展示管理后台。
2. 使用管理员账号登录。
3. 打开浏览器开发者工具 Network，确认请求地址是 `https://api.yee.earth/api`。
4. 打开经营看板，展示营收、订单、库存、会员、人力成本、AI 经营分析。
5. 打开财务看板，展示年度、月度、自定义收入查询、成本构成、人力成本构成图表。
6. 修改一个菜品库存或新增一条人员信息。
7. 打开微信小程序预览版，确认读取的是服务器数据。
8. 在小程序完成点餐或查看会员价、积分信息。
9. 回到后台确认订单、会员、财务数据可以联动展示。
10. 展示 AI 经营分析和 AI 点餐推荐，说明 DeepSeek Key 通过服务器环境变量配置，不进入 GitHub。

## 13. 常见错误排查

### 网站打不开

检查 DNS 是否解析到服务器：

```bash
dig yee.earth
dig api.yee.earth
```

检查 Caddy：

```bash
docker compose --env-file .env.production logs -f caddy
```

确认安全组开放 `80` 和 `443`。

### HTTPS 证书申请失败

确认：

- 域名 A 记录已经指向服务器。
- 服务器 `80` 和 `443` 可以公网访问。
- Cloudflare SSL/TLS 模式不是 `Flexible`。

### 后台登录失败

检查后端是否正常：

```bash
curl https://api.yee.earth/api/health
docker compose --env-file .env.production logs -f backend
```

如果首次初始化时忘记配置 `DEFAULT_ADMIN_PASSWORD`，可以修改 `.env.production` 后重建后端：

```bash
docker compose --env-file .env.production up -d --build backend
```

### 后台显示连接失败

生产环境默认关闭 mock fallback。先检查 API：

```bash
curl https://api.yee.earth/api/admin/stats/dashboard
```

如果返回未登录，说明 API 已经连通；重新登录后台即可。

### 小程序请求不到数据

检查：

- `miniapp/utils/config.js` 中 `SERVER_BASE_URL` 是 `https://api.yee.earth/api`。
- 微信开发者工具已勾选“不校验合法域名、web-view、TLS版本以及HTTPS证书”。
- Storage 中没有旧的 `restaurant_api_base_url` 覆盖到本地地址。
- 后端健康检查正常。

### MySQL 初始化数据没有导入

MySQL 官方镜像只会在数据目录为空时执行 `/docker-entrypoint-initdb.d`。如果需要重新初始化演示库：

```bash
docker compose --env-file .env.production down
docker volume rm restaurant-project_mysql_data
docker compose --env-file .env.production up -d --build
```

执行前务必先备份数据库。

### DeepSeek 没有响应

检查 `.env.production`：

```text
DEEPSEEK_API_KEY=
DEEPSEEK_MODEL=deepseek-v4-pro
```

修改后重启后端：

```bash
docker compose --env-file .env.production restart backend
```

如果 Key 为空或网络不可用，系统会返回本地规则分析，保证演示不断流。
