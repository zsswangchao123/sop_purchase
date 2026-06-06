# Purchase Support List Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot + Vue 3 backend management system that configures product support rules and generates purchase support lists with quantities, editable unit prices, totals, status tracking, Excel import, and Excel export.

**Architecture:** The backend owns persistence, rule calculation, validation, Excel parsing, and export generation. The frontend is a Vue 3 admin UI that calls JSON APIs and keeps calculation authority on the server. MySQL stores configurable products, support items, rules, generated list snapshots, and source tracing records.

**Tech Stack:** Java 17, Spring Boot 3.x, Maven, MySQL 8, MyBatis-Plus or Spring Data JPA, Flyway, JUnit 5, Vue 3, Vite, TypeScript, Element Plus, Axios, Vitest.

---

## File Structure

Create this project layout:

```text
backend/
  pom.xml
  src/main/java/com/sop/purchase/
    PurchaseApplication.java
    common/
      ApiResponse.java
      BusinessException.java
      GlobalExceptionHandler.java
    product/
      ProductController.java
      ProductService.java
      ProductRepository.java
      Product.java
      dto/ProductDtos.java
    supportitem/
      SupportItemController.java
      SupportItemService.java
      SupportItemRepository.java
      SupportItem.java
      dto/SupportItemDtos.java
    rule/
      ProductSupportRuleController.java
      ProductSupportRuleService.java
      ProductSupportRuleRepository.java
      ProductSupportRule.java
      CalcType.java
      RoundingMode.java
      dto/RuleDtos.java
    purchaselist/
      PurchaseListController.java
      PurchaseListService.java
      PurchaseListCalculator.java
      PurchaseListRepository.java
      PurchaseListProductRepository.java
      PurchaseListItemRepository.java
      PurchaseListItemSourceRepository.java
      PurchaseList.java
      PurchaseListProduct.java
      PurchaseListItem.java
      PurchaseListItemSource.java
      PurchaseListStatus.java
      SourceType.java
      dto/PurchaseListDtos.java
    excel/
      ExcelImportService.java
      ExcelExportService.java
      ImportTemplateController.java
  src/main/resources/
    application.yml
    db/migration/V1__init_purchase_support_schema.sql
  src/test/java/com/sop/purchase/
    purchaselist/PurchaseListCalculatorTest.java
    purchaselist/PurchaseListServiceTest.java
    excel/ExcelImportServiceTest.java

frontend/
  package.json
  vite.config.ts
  src/
    main.ts
    App.vue
    router/index.ts
    api/http.ts
    api/product.ts
    api/supportItem.ts
    api/rule.ts
    api/purchaseList.ts
    views/ProductView.vue
    views/SupportItemView.vue
    views/RuleView.vue
    views/PurchaseGenerateView.vue
    views/PurchaseListView.vue
    views/PurchaseDetailView.vue
```

## Task 1: Scaffold Backend

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/sop/purchase/PurchaseApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/sop/purchase/common/ApiResponse.java`
- Create: `backend/src/main/java/com/sop/purchase/common/BusinessException.java`
- Create: `backend/src/main/java/com/sop/purchase/common/GlobalExceptionHandler.java`

- [ ] **Step 1: Create Maven project file**

Create `backend/pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.sop</groupId>
  <artifactId>purchase-support</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>jar</packaging>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.6</version>
    <relativePath/>
  </parent>

  <properties>
    <java.version>17</java.version>
  </properties>

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
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-mysql</artifactId>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <version>5.3.0</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Add application entrypoint**

Create `backend/src/main/java/com/sop/purchase/PurchaseApplication.java`:

```java
package com.sop.purchase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PurchaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PurchaseApplication.class, args);
    }
}
```

- [ ] **Step 3: Add backend config**

Create `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sop_purchase?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

- [ ] **Step 4: Add common response and error handling**

Create `ApiResponse.java`, `BusinessException.java`, and `GlobalExceptionHandler.java` with these responsibilities:

```java
package com.sop.purchase.common;

public record ApiResponse<T>(boolean success, T data, String message) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
```

```java
package com.sop.purchase.common;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

```java
package com.sop.purchase.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.fail(message);
    }
}
```

- [ ] **Step 5: Verify backend compiles**

Run:

```powershell
mvn -f backend/pom.xml test
```

Expected: build succeeds with zero tests or empty test suite.

- [ ] **Step 6: Commit**

```powershell
git add backend
git commit -m "chore: scaffold backend service"
```

## Task 2: Add Database Schema and Entities

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init_purchase_support_schema.sql`
- Create entities and repositories under `product`, `supportitem`, `rule`, and `purchaselist` packages.
- Test: `backend/src/test/java/com/sop/purchase/purchaselist/PurchaseListCalculatorTest.java`

- [ ] **Step 1: Add Flyway schema**

Create `V1__init_purchase_support_schema.sql` with the seven tables from the spec:

```sql
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  specification VARCHAR(128),
  unit VARCHAR(32) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE support_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  unit VARCHAR(32) NOT NULL,
  default_price DECIMAL(12,2) NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE product_support_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  support_item_id BIGINT NOT NULL,
  calc_type VARCHAR(16) NOT NULL,
  base_quantity DECIMAL(12,2),
  support_quantity DECIMAL(12,2) NOT NULL,
  rounding_mode VARCHAR(16) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_rule_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_rule_support_item FOREIGN KEY (support_item_id) REFERENCES support_item(id)
);

CREATE TABLE purchase_list (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  list_no VARCHAR(64) NOT NULL UNIQUE,
  status VARCHAR(16) NOT NULL,
  source_type VARCHAR(16) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  remark VARCHAR(512),
  created_by VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  confirmed_at DATETIME,
  purchased_at DATETIME
);

CREATE TABLE purchase_list_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_code_snapshot VARCHAR(64) NOT NULL,
  product_name_snapshot VARCHAR(128) NOT NULL,
  unit_snapshot VARCHAR(32) NOT NULL,
  quantity DECIMAL(12,2) NOT NULL,
  remark VARCHAR(512),
  CONSTRAINT fk_list_product_list FOREIGN KEY (purchase_list_id) REFERENCES purchase_list(id),
  CONSTRAINT fk_list_product_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE purchase_list_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_id BIGINT NOT NULL,
  support_item_id BIGINT NOT NULL,
  support_item_code_snapshot VARCHAR(64) NOT NULL,
  support_item_name_snapshot VARCHAR(128) NOT NULL,
  unit_snapshot VARCHAR(32) NOT NULL,
  quantity DECIMAL(12,2) NOT NULL,
  default_unit_price_snapshot DECIMAL(12,2) NOT NULL,
  actual_unit_price DECIMAL(12,2) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  remark VARCHAR(512),
  CONSTRAINT fk_list_item_list FOREIGN KEY (purchase_list_id) REFERENCES purchase_list(id),
  CONSTRAINT fk_list_item_support_item FOREIGN KEY (support_item_id) REFERENCES support_item(id)
);

CREATE TABLE purchase_list_item_source (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_item_id BIGINT NOT NULL,
  purchase_list_product_id BIGINT NOT NULL,
  product_support_rule_id BIGINT NOT NULL,
  calculated_quantity DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_item_source_item FOREIGN KEY (purchase_list_item_id) REFERENCES purchase_list_item(id),
  CONSTRAINT fk_item_source_product FOREIGN KEY (purchase_list_product_id) REFERENCES purchase_list_product(id),
  CONSTRAINT fk_item_source_rule FOREIGN KEY (product_support_rule_id) REFERENCES product_support_rule(id)
);
```

- [ ] **Step 2: Add enums**

Create:

```java
public enum CalcType { FIXED, RATIO }
public enum RoundingMode { CEIL, DECIMAL }
public enum PurchaseListStatus { DRAFT, CONFIRMED, PURCHASED }
public enum SourceType { MANUAL, EXCEL }
```

- [ ] **Step 3: Add JPA entities and repositories**

Create one entity per table using `BigDecimal` for quantities and money, `LocalDateTime` for timestamps, and repository interfaces extending `JpaRepository<Entity, Long>`.

Repository methods required:

```java
Optional<Product> findByCode(String code);
List<Product> findByName(String name);
List<ProductSupportRule> findByProductIdAndEnabledTrue(Long productId);
List<PurchaseList> findByStatus(PurchaseListStatus status);
```

- [ ] **Step 4: Run schema/entity verification**

Run:

```powershell
mvn -f backend/pom.xml test
```

Expected: Flyway migration and JPA validation pass in tests after adding test datasource config.

- [ ] **Step 5: Commit**

```powershell
git add backend
git commit -m "feat: add purchase support data model"
```

## Task 3: Implement Product, Support Item, and Rule CRUD

**Files:**
- Create/modify controllers, services, DTOs, and repositories under `product`, `supportitem`, and `rule`.
- Test: service tests for validation rules.

- [ ] **Step 1: Add DTOs**

Each module should expose request and response records. Example for product:

```java
public class ProductDtos {
    public record CreateProductRequest(String code, String name, String specification, String unit, String remark) {}
    public record UpdateProductRequest(String name, String specification, String unit, Boolean enabled, String remark) {}
    public record ProductResponse(Long id, String code, String name, String specification, String unit, Boolean enabled, String remark) {}
}
```

- [ ] **Step 2: Add validation**

Rules:

```text
code, name, unit cannot be blank.
default_price cannot be negative.
support_quantity must be greater than 0.
RATIO rules require base_quantity greater than 0.
FIXED rules ignore base_quantity.
```

- [ ] **Step 3: Add REST endpoints**

Implement:

```text
GET    /api/products
POST   /api/products
PUT    /api/products/{id}
PATCH  /api/products/{id}/enabled

GET    /api/support-items
POST   /api/support-items
PUT    /api/support-items/{id}
PATCH  /api/support-items/{id}/enabled

GET    /api/products/{productId}/support-rules
POST   /api/products/{productId}/support-rules
PUT    /api/support-rules/{id}
PATCH  /api/support-rules/{id}/enabled
DELETE /api/support-rules/{id}
```

- [ ] **Step 4: Run tests**

Run:

```powershell
mvn -f backend/pom.xml test
```

Expected: CRUD validation tests pass.

- [ ] **Step 5: Commit**

```powershell
git add backend
git commit -m "feat: add product support rule management"
```

## Task 4: Implement Purchase Calculation Engine

**Files:**
- Create: `backend/src/main/java/com/sop/purchase/purchaselist/PurchaseListCalculator.java`
- Modify: `backend/src/main/java/com/sop/purchase/purchaselist/dto/PurchaseListDtos.java`
- Test: `backend/src/test/java/com/sop/purchase/purchaselist/PurchaseListCalculatorTest.java`

- [ ] **Step 1: Write calculator tests**

Cover these exact cases:

```java
@Test
void fixedRuleAddsSupportQuantityOnceWhenProductExists() {}

@Test
void ratioRuleCalculatesByProductQuantity() {}

@Test
void ceilRoundingRoundsUpToWholeUnit() {}

@Test
void decimalRoundingKeepsTwoDecimals() {}

@Test
void sameSupportItemFromMultipleProductsIsMerged() {}
```

- [ ] **Step 2: Implement calculation contract**

Use input records like:

```java
public record GenerateProductInput(Long productId, String productCode, String productName, String unit, BigDecimal quantity, String remark) {}
public record CalculatedSupportItem(Long supportItemId, String code, String name, String unit, BigDecimal quantity, BigDecimal defaultPrice, BigDecimal actualPrice, BigDecimal amount, List<CalculatedSource> sources) {}
public record CalculatedSource(Long productId, Long ruleId, BigDecimal calculatedQuantity) {}
```

- [ ] **Step 3: Implement formula**

The calculator must implement:

```java
if (rule.calcType() == CalcType.FIXED) {
    rawQuantity = rule.supportQuantity();
} else {
    rawQuantity = productQuantity.divide(rule.baseQuantity(), 8, HALF_UP).multiply(rule.supportQuantity());
}

if (rule.roundingMode() == RoundingMode.CEIL) {
    finalQuantity = rawQuantity.setScale(0, CEILING);
} else {
    finalQuantity = rawQuantity.setScale(2, HALF_UP);
}
```

- [ ] **Step 4: Merge by support item**

Group calculated rows by `supportItemId`. Sum quantity, recalculate amount with the current support item default price, and append all source records.

- [ ] **Step 5: Run calculator tests**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=PurchaseListCalculatorTest
```

Expected: all calculator tests pass.

- [ ] **Step 6: Commit**

```powershell
git add backend
git commit -m "feat: add purchase support calculator"
```

## Task 5: Implement Purchase List Generation and Status Flow

**Files:**
- Modify: purchase list service, controller, DTOs, repositories.
- Test: `backend/src/test/java/com/sop/purchase/purchaselist/PurchaseListServiceTest.java`

- [ ] **Step 1: Add service tests**

Test:

```text
manual generation saves draft purchase list.
product without rules is accepted and returned as warning.
disabled support item is skipped and returned as warning.
draft list allows actual unit price changes.
confirmed list rejects price changes.
confirmed list can become purchased.
purchased list cannot return to draft.
```

- [ ] **Step 2: Implement manual generation endpoint**

Endpoint:

```text
POST /api/purchase-lists/manual
```

Request:

```json
{
  "items": [
    { "productId": 1, "quantity": 20, "remark": "本次采购" }
  ],
  "remark": "6月采购"
}
```

Response includes list header, product inputs, support item rows, warnings, and total amount.

- [ ] **Step 3: Implement draft price update**

Endpoint:

```text
PATCH /api/purchase-lists/{id}/items/{itemId}/price
```

Reject when list status is not `DRAFT`. Recalculate item amount and list total after saving.

- [ ] **Step 4: Implement status endpoints**

Endpoints:

```text
POST /api/purchase-lists/{id}/confirm
POST /api/purchase-lists/{id}/mark-purchased
```

Only allow:

```text
DRAFT -> CONFIRMED
CONFIRMED -> PURCHASED
```

- [ ] **Step 5: Run service tests**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=PurchaseListServiceTest
```

Expected: all service tests pass.

- [ ] **Step 6: Commit**

```powershell
git add backend
git commit -m "feat: add purchase list generation workflow"
```

## Task 6: Implement Excel Import, Template, and Export

**Files:**
- Create: `backend/src/main/java/com/sop/purchase/excel/ExcelImportService.java`
- Create: `backend/src/main/java/com/sop/purchase/excel/ExcelExportService.java`
- Create: `backend/src/main/java/com/sop/purchase/excel/ImportTemplateController.java`
- Modify: `PurchaseListController.java`
- Test: `backend/src/test/java/com/sop/purchase/excel/ExcelImportServiceTest.java`

- [ ] **Step 1: Write Excel import tests**

Test:

```text
code match succeeds.
blank code falls back to unique product name.
duplicate name fails and asks for product code.
unknown code fails with row number.
negative quantity fails with row number.
```

- [ ] **Step 2: Implement template download**

Endpoint:

```text
GET /api/import-templates/products
```

Workbook headers:

```text
商品编码 | 商品名称 | 数量 | 备注
```

- [ ] **Step 3: Implement Excel import generation**

Endpoint:

```text
POST /api/purchase-lists/excel
```

Use multipart file upload. Parse `.xlsx`, validate all rows, and only save a draft purchase list when all blocking validation passes.

- [ ] **Step 4: Implement purchase list export**

Endpoint:

```text
GET /api/purchase-lists/{id}/export
```

Workbook sheets:

```text
采购配套项明细
输入商品清单
来源明细
```

- [ ] **Step 5: Run Excel tests**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=ExcelImportServiceTest
```

Expected: all Excel import tests pass.

- [ ] **Step 6: Commit**

```powershell
git add backend
git commit -m "feat: add purchase list excel import export"
```

## Task 7: Scaffold Frontend

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.ts`
- Create: API modules under `frontend/src/api`

- [ ] **Step 1: Create Vite Vue project files**

Use Vue 3, TypeScript, Element Plus, Vue Router, and Axios.

`frontend/package.json` scripts:

```json
{
  "scripts": {
    "dev": "vite --host 0.0.0.0",
    "build": "vue-tsc -b && vite build",
    "test": "vitest run"
  }
}
```

- [ ] **Step 2: Add router**

Routes:

```text
/products
/support-items
/rules
/purchase/generate
/purchase/lists
/purchase/lists/:id
```

- [ ] **Step 3: Add HTTP wrapper**

`api/http.ts` should create an Axios client with base URL `/api` and unwrap `ApiResponse`.

- [ ] **Step 4: Run frontend build**

Run:

```powershell
cd frontend
npm install
npm run build
```

Expected: build succeeds.

- [ ] **Step 5: Commit**

```powershell
git add frontend
git commit -m "chore: scaffold frontend admin"
```

## Task 8: Implement Frontend Admin Pages

**Files:**
- Modify views under `frontend/src/views`
- Modify API modules under `frontend/src/api`

- [ ] **Step 1: Implement ProductView**

Page capabilities:

```text
table list
create dialog
edit dialog
enable/disable switch
required validation for code/name/unit
```

- [ ] **Step 2: Implement SupportItemView**

Page capabilities:

```text
table list
create dialog
edit dialog
enable/disable switch
default price input rejecting negative values
```

- [ ] **Step 3: Implement RuleView**

Page capabilities:

```text
select product
list current rules
add/edit rule
calc type selector FIXED/RATIO
show base quantity only for RATIO
rounding mode selector CEIL/DECIMAL
```

- [ ] **Step 4: Implement PurchaseGenerateView**

Page capabilities:

```text
manual product rows with product search and quantity
template download button
Excel upload
generate button
warning display
generated support item table
total amount display
```

- [ ] **Step 5: Implement PurchaseListView and PurchaseDetailView**

Page capabilities:

```text
list filters by status
detail page with product inputs and support item rows
draft price editing
confirm action
mark purchased action
export button
disable editing outside DRAFT
```

- [ ] **Step 6: Run frontend checks**

Run:

```powershell
cd frontend
npm run build
npm run test
```

Expected: build and tests pass.

- [ ] **Step 7: Commit**

```powershell
git add frontend
git commit -m "feat: add purchase support admin UI"
```

## Task 9: End-to-End Verification

**Files:**
- Modify docs if setup instructions are needed.

- [ ] **Step 1: Create local MySQL database**

Run:

```sql
CREATE DATABASE sop_purchase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

- [ ] **Step 2: Start backend**

Run:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

Expected: backend starts on `http://localhost:8080`.

- [ ] **Step 3: Start frontend**

Run:

```powershell
cd frontend
npm run dev
```

Expected: frontend starts on Vite dev URL.

- [ ] **Step 4: Verify business scenario**

Use the UI to:

```text
create product 西兰花, code VEG001, unit 箱
create support item 冰袋, code PACK001, unit 个, default price 0.50
create support item 标签, code PACK002, unit 张, default price 0.10
create RATIO rule: 西兰花 every 1 箱 needs 2 冰袋, CEIL
create FIXED rule: 西兰花 needs 10 标签, CEIL
generate manual list with 西兰花 quantity 20
verify 冰袋 quantity 40, 标签 quantity 10
change 冰袋 actual unit price to 0.60
verify total amount updates
confirm list
verify price editing is disabled
mark purchased
export Excel
```

- [ ] **Step 5: Run all automated checks**

Run:

```powershell
mvn -f backend/pom.xml test
cd frontend
npm run build
npm run test
```

Expected: all checks pass.

- [ ] **Step 6: Commit final docs or fixes**

```powershell
git add .
git commit -m "test: verify purchase support workflow"
```

## Self-Review

- Spec coverage: product management, support item management, rules, manual generation, Excel import, editable actual price, totals, status flow, export, and tests are covered.
- Placeholder scan: no intentional TBD or deferred implementation placeholders remain in task steps.
- Type consistency: backend names use `Product`, `SupportItem`, `ProductSupportRule`, `PurchaseList`, `PurchaseListProduct`, `PurchaseListItem`, and `PurchaseListItemSource`; enum values match the design spec.
- Scope check: supplier quotes, approval flow, inventory deduction, payment, and complex permissions remain out of scope for this MVP.
