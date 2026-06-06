# QA Report - 采购配套清单

Date: 2026-06-05
Target: http://localhost:5173
Mode: Standard QA with fixes
Framework: Vue 3 SPA + Spring Boot API

## Summary

Baseline health score: 78/100
Final health score: 96/100

Pages tested:

- 生成采购清单
- 商品管理
- 配套项管理
- 配套规则
- 清单历史
- 清单详情
- Mobile viewport at 375px

Core workflow tested:

- Create QA product/support item/rule through API setup.
- Select product in UI.
- Generate purchase list.
- Verify calculated support quantity and total.
- Edit actual unit price.
- Confirm list.
- Mark list purchased.
- Verify console remains clean.

Screenshots were displayed inline in the Codex session because the browser automation process could not write image files directly to the workspace.

## Issues Found And Fixed

### ISSUE-001 - Rule management page returned 500 and showed empty table

Severity: High
Category: Functional / Console

Repro:

1. Open `/rules`.
2. Select a product with an existing support rule.
3. Observe the table shows `No Data`.
4. Browser console logs an unhandled promise error.
5. API `/api/products/{id}/support-rules` returns `could not initialize proxy ... no Session`.

Root cause:

`ProductSupportRuleService.listByProduct` mapped lazy JPA relations outside a transactional read boundary.

Fix status: verified

Files changed:

- `backend/src/main/java/com/sop/purchase/rule/ProductSupportRuleService.java`
- `backend/src/main/java/com/sop/purchase/rule/ProductSupportRuleRepository.java`
- `backend/src/test/java/com/sop/purchase/rule/ProductSupportRuleServiceTest.java`

Verification:

- Added regression test for lazy mapping and disabled-rule visibility.
- Browser retest showed rule row renders.
- Disabling and re-enabling the rule keeps it visible.
- Console errors: 0.

### ISSUE-002 - Disabled support rules disappeared from rule management

Severity: Medium
Category: Functional / UX

Repro:

1. Open `/rules`.
2. Select a product with a rule.
3. Disable the rule.
4. The rule should remain visible so the user can re-enable it.

Root cause:

The rule list endpoint only returned `enabled=true` rules.

Fix status: verified

Files changed:

- `backend/src/main/java/com/sop/purchase/rule/ProductSupportRuleRepository.java`
- `backend/src/main/java/com/sop/purchase/rule/ProductSupportRuleService.java`
- `backend/src/test/java/com/sop/purchase/rule/ProductSupportRuleServiceTest.java`

Verification:

- Browser retest showed disabled rule remains in the table.
- Re-enable works from the same row.

### ISSUE-003 - Generate page product selector displayed `0` before selection

Severity: Medium
Category: UX

Repro:

1. Open `/purchase/generate`.
2. Observe first product selector before choosing any product.
3. It displays `0`, which looks like a real value.

Root cause:

The default input row used `productId: 0`.

Fix status: verified

Files changed:

- `frontend/src/api/purchaseList.ts`
- `frontend/src/views/PurchaseGenerateView.vue`

Verification:

- Browser retest showed no standalone `0`.
- Placeholder now shows `请选择商品`.

### ISSUE-004 - Mobile viewport rendered as desktop width

Severity: Medium
Category: Responsive / Accessibility

Repro:

1. Open the app with a 375px mobile viewport.
2. `window.innerWidth` reports about 980px.
3. Mobile CSS does not apply correctly.

Root cause:

`frontend/index.html` did not include a viewport meta tag.

Fix status: verified

Files changed:

- `frontend/index.html`

Verification:

- Mobile retest reports `innerWidth: 375`.
- Page document scroll width remains 375.
- Console errors: 0.

### ISSUE-005 - Mobile product selector text was squeezed

Severity: Low
Category: Responsive / Visual

Repro:

1. Open `/purchase/generate` at 375px after viewport fix.
2. The product selector area compresses too much inside the table.

Fix status: verified

Files changed:

- `frontend/src/views/PurchaseGenerateView.vue`
- `frontend/src/style.css`

Verification:

- Input table now uses internal horizontal scrolling on mobile.
- Page itself does not horizontally overflow.
- Product selector placeholder remains readable.

## Final Verification

Automated checks:

```powershell
mvn -f backend/pom.xml test
cd E:\sop\frontend
npm run build
npm run test
```

Results:

- Backend: 18 tests passed.
- Frontend build: passed.
- Frontend tests: 1 passed.
- Browser console during final flow: 0 errors.

Known non-blocking warnings:

- Vite reports large chunk size because Element Plus is bundled into the main app chunk.
- Rollup strips two `#__PURE__` annotations from a dependency. This does not affect runtime behavior.

## Ship Readiness

Status: Ready for local demo / MVP review.

Remaining recommended follow-ups:

- Add route-level code splitting later to reduce Vite chunk warning.
- Add more frontend component tests for form validation once the UI stabilizes.
