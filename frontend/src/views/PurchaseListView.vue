<template>
  <div class="page">
    <div class="toolbar">
      <div><h2>清单历史</h2><p class="page-subtitle">追踪草稿、已确认和已采购清单</p></div>
    </div>
    <div class="filter-bar">
      <el-input v-model="keyword" clearable placeholder="搜索清单编号或备注" :prefix-icon="Search" @input="searchLater" @clear="searchNow" />
      <el-select v-model="status" clearable placeholder="全部状态" @change="searchNow">
        <el-option label="草稿" value="DRAFT" /><el-option label="已确认" value="CONFIRMED" /><el-option label="已采购" value="PURCHASED" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" unlink-panels @change="searchNow" />
    </div>
    <el-table v-loading="loading" :data="rows" class="panel">
      <el-table-column prop="listNo" label="清单编号" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="创建时间" width="180"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="130" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" :icon="View" @click="$router.push(`/purchase/lists/${row.id}`)">详情</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-bar"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next" @change="load" /></div>
  </div>
</template>
<script setup lang="ts">
import { Search, View } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { purchaseListApi } from '../api/purchaseList'
import type { PurchaseList } from '../api/types'
const rows = ref<PurchaseList[]>([]), keyword = ref(''), status = ref<string>(), dateRange = ref<[string, string]>(), loading = ref(false), page = ref(1), size = ref(20), total = ref(0)
let timer: ReturnType<typeof setTimeout>
function statusLabel(value: PurchaseList['status']) { return { DRAFT: '草稿', CONFIRMED: '已确认', PURCHASED: '已采购' }[value] }
function statusTagType(value: PurchaseList['status']) { return ({ DRAFT: 'info', CONFIRMED: 'warning', PURCHASED: 'success' } as const)[value] }
function formatDateTime(value: string) { return value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date(value)) : '-' }
async function load() { loading.value = true; try { const result = await purchaseListApi.list({ status: status.value, keyword: keyword.value, startDate: dateRange.value?.[0], endDate: dateRange.value?.[1], page: page.value - 1, size: size.value }); rows.value = result.content; total.value = result.totalElements } finally { loading.value = false } }
function searchNow() { page.value = 1; load() }
function searchLater() { clearTimeout(timer); timer = setTimeout(searchNow, 300) }
onMounted(load)
</script>
