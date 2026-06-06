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
