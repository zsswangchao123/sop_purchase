# 采购配套清单

根据商品配套规则自动计算采购物品数量、价格和总金额的后台管理系统。

## 技术栈

- 后端：Spring Boot、Spring Data JPA、Flyway、MySQL
- 前端：Vue 3、TypeScript、Vite、Element Plus

## 本地启动

启动 MySQL 并创建数据库：

```sql
CREATE DATABASE sop_purchase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

配置数据库环境变量后启动后端：

```powershell
$env:DB_USERNAME='root'
$env:DB_PASSWORD='你的密码'
mvn -f backend/pom.xml spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。

## Docker 部署

先完成后端和前端构建：

```powershell
mvn -f backend/pom.xml clean package
cd frontend
npm ci
npm run build
```

在 `deploy` 目录创建 `.env`，参考 `deploy/.env.example` 配置独立数据库密码，然后运行：

```bash
docker compose up -d --build
```

默认通过服务器的 `8088` 端口访问。
