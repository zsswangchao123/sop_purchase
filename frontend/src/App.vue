<template>
  <el-container class="shell">
    <el-aside width="248px" class="aside">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><Goods /></el-icon>
        </div>
        <div>
          <div class="brand-title">采购配套清单</div>
          <div class="brand-subtitle">采购运营中心</div>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item index="/products"><el-icon><Box /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/support-items"><el-icon><TakeawayBox /></el-icon><span>配套项管理</span></el-menu-item>
        <el-menu-item index="/rules"><el-icon><SetUp /></el-icon><span>配套规则</span></el-menu-item>
        <el-menu-item index="/purchase/generate"><el-icon><DocumentAdd /></el-icon><span>生成清单</span></el-menu-item>
        <el-menu-item index="/purchase/lists"><el-icon><Tickets /></el-icon><span>清单历史</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="workspace">
      <el-header class="header">
        <div>
          <div class="header-title">后台管理</div>
          <div class="header-subtitle">商品配套规则、采购清单和价格核算</div>
        </div>
        <div class="header-badge">试用版</div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { Box, DocumentAdd, Goods, SetUp, TakeawayBox, Tickets } from '@element-plus/icons-vue'
</script>

<style scoped>
.shell {
  min-height: 100vh;
  background: #eef2f7;
}

.aside {
  position: sticky;
  top: 0;
  height: 100vh;
  background: linear-gradient(180deg, #10231f 0%, #172b28 48%, #1e2f2b 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 18px 0 44px rgba(15, 35, 31, 0.12);
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  min-height: 76px;
  padding: 18px;
  color: #f8fafc;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  color: #064e3b;
  background: linear-gradient(135deg, #d9f99d, #5eead4);
  border-radius: 8px;
  box-shadow: 0 14px 30px rgba(94, 234, 212, 0.18);
}

.brand-title {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0;
}

.brand-subtitle {
  margin-top: 3px;
  color: #a7f3d0;
  font-size: 12px;
}

.menu {
  border-right: 0;
  background: transparent;
  padding: 12px;
}

.menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 6px;
  color: #cbd5e1;
  border-radius: 8px;
  transition: background-color 180ms ease, color 180ms ease;
}

.menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.menu :deep(.el-menu-item.is-active) {
  color: #ecfdf5;
  background: rgba(16, 185, 129, 0.22);
}

.workspace {
  min-width: 0;
}

.header {
  display: flex;
  height: 76px;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.86);
  border-bottom: 1px solid #e5e7eb;
  backdrop-filter: blur(14px);
}

.header-title {
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
}

.header-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.header-badge {
  padding: 6px 10px;
  color: #065f46;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.main {
  padding: 0;
}

@media (max-width: 780px) {
  .shell {
    display: block;
  }

  .aside {
    position: static;
    width: 100% !important;
    height: auto;
  }

  .menu {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .header {
    height: auto;
    gap: 12px;
    align-items: flex-start;
    padding: 16px;
  }
}
</style>
