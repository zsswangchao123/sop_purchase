<template>
  <div class="page" v-if="detail">
    <div class="toolbar">
      <div><h2>{{ detail.listNo }}</h2><el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag></div>
      <div class="toolbar-right">
        <el-button :tag="'a'" :href="purchaseListApi.exportUrl(detail.id)" :icon="Download">导出 Excel</el-button>
        <el-button type="primary" :icon="CircleCheck" :disabled="detail.status !== 'DRAFT'" @click="confirm">确认清单</el-button>
        <el-button type="success" :icon="Finished" :disabled="detail.status !== 'CONFIRMED'" @click="markPurchased">标记已采购</el-button>
      </div>
    </div>
    <div class="metric-grid">
      <div class="metric"><div class="metric-label">输入商品</div><div class="metric-value">{{ detail.products.length }}</div></div>
      <div class="metric"><div class="metric-label">配套项</div><div class="metric-value">{{ detail.items.length }}</div></div>
      <div class="metric"><div class="metric-label">总金额</div><div class="metric-value">{{ detail.totalAmount }}</div></div>
    </div>
    <div class="panel">
      <div class="toolbar"><h3>配套项明细</h3><div class="total">合计 {{ detail.totalAmount }}</div></div>
      <el-table :data="detail.items">
        <el-table-column prop="supportItemCode" label="编码" width="140" /><el-table-column prop="supportItemName" label="配套项" /><el-table-column prop="quantity" label="数量" width="110" />
        <el-table-column label="本次单价" width="180"><template #default="{ row }"><el-input-number v-model="row.actualUnitPrice" :disabled="detail?.status !== 'DRAFT'" :min="0" :precision="2" @change="updatePrice(row)" /></template></el-table-column>
        <el-table-column prop="amount" label="金额" width="120" />
      </el-table>
    </div>
    <div class="panel" style="margin-top:16px">
      <h3>输入商品</h3>
      <el-table :data="detail.products"><el-table-column prop="productCode" label="编码" width="140" /><el-table-column prop="productName" label="商品" /><el-table-column prop="quantity" label="数量" width="120" /><el-table-column prop="unit" label="单位" width="100" /><el-table-column prop="remark" label="备注" /></el-table>
    </div>
  </div>
</template>
<script setup lang="ts">
import { CircleCheck, Download, Finished } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { purchaseListApi } from '../api/purchaseList'
import type { PurchaseList, PurchaseListItem } from '../api/types'
const route = useRoute(), detail = ref<PurchaseList>()
function statusLabel(value: PurchaseList['status']) { return { DRAFT: '草稿', CONFIRMED: '已确认', PURCHASED: '已采购' }[value] }
function statusTagType(value: PurchaseList['status']) { return ({ DRAFT: 'info', CONFIRMED: 'warning', PURCHASED: 'success' } as const)[value] }
async function load() { detail.value = await purchaseListApi.detail(Number(route.params.id)) }
async function updatePrice(row: PurchaseListItem) { if (detail.value) detail.value = await purchaseListApi.updatePrice(detail.value.id, row.id, row.actualUnitPrice, row.remark) }
async function confirm() { if (detail.value) detail.value = await purchaseListApi.confirm(detail.value.id) }
async function markPurchased() { if (detail.value) detail.value = await purchaseListApi.markPurchased(detail.value.id) }
onMounted(load)
</script>
