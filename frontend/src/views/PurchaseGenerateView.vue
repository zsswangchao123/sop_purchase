<template>
  <div class="page">
    <div class="toolbar"><div><h2>生成采购清单</h2><p class="page-subtitle">选择商品或导入 Excel，自动计算本次配套采购</p></div><div class="toolbar-right"><el-button :tag="'a'" :href="purchaseListApi.templateUrl" :icon="Download">下载模板</el-button><el-upload :auto-upload="false" :show-file-list="false" accept=".xlsx" @change="uploadExcel"><el-button :icon="Upload">Excel 导入</el-button></el-upload></div></div>
    <div class="panel table-panel">
      <el-table :data="inputs" class="input-table">
        <el-table-column label="商品">
          <template #default="{ row }"><el-select v-model="row.productId" filterable remote :remote-method="searchProducts" :loading="productLoading" placeholder="输入编码或名称搜索商品" style="width:100%" @visible-change="openProductSelect"><el-option v-for="product in products" :key="product.id" :label="`${product.code} ${product.name} · ${product.specification || '无规格'} · ${product.unit}`" :value="product.id" /></el-select></template>
        </el-table-column>
        <el-table-column label="数量" width="180"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.01" :precision="2" /></template></el-table-column>
        <el-table-column label="备注"><template #default="{ row }"><el-input v-model="row.remark" /></template></el-table-column>
        <el-table-column width="90"><template #default="{ $index }"><el-button link type="danger" @click="inputs.splice($index, 1)">删除</el-button></template></el-table-column>
      </el-table>
      <div class="toolbar" style="margin-top:14px"><el-button :icon="Plus" @click="addRow">新增一行</el-button><el-button type="primary" :icon="DocumentChecked" @click="generate">生成清单</el-button></div>
    </div>
    <div v-if="generated" class="panel" style="margin-top:16px">
      <div class="metric-grid"><div class="metric"><div class="metric-label">输入商品</div><div class="metric-value">{{ generated.products.length }}</div></div><div class="metric"><div class="metric-label">配套项</div><div class="metric-value">{{ generated.items.length }}</div></div><div class="metric"><div class="metric-label">提示</div><div class="metric-value">{{ generated.warnings.length }}</div></div></div>
      <div class="toolbar"><h3>生成结果</h3><div class="total">合计 {{ generated.totalAmount }}</div></div>
      <el-alert v-for="warning in generated.warnings" :key="warning" :title="warning" type="warning" show-icon style="margin-bottom:8px" />
      <el-table :data="generated.items"><el-table-column prop="supportItemCode" label="编码" width="140" /><el-table-column prop="supportItemName" label="配套项" /><el-table-column prop="quantity" label="数量" width="120" /><el-table-column prop="actualUnitPrice" label="本次单价" width="120" /><el-table-column prop="amount" label="金额" width="120" /></el-table>
      <el-button type="primary" :icon="View" style="margin-top:14px" @click="$router.push(`/purchase/lists/${generated?.id}`)">查看详情</el-button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { DocumentChecked, Download, Plus, Upload, View } from '@element-plus/icons-vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { onMounted, ref } from 'vue'
import { productApi } from '../api/product'
import { purchaseListApi, type GenerateItemInput } from '../api/purchaseList'
import type { Product, PurchaseList } from '../api/types'
const products = ref<Product[]>([]), productLoading = ref(false), inputs = ref<GenerateItemInput[]>([]), generated = ref<PurchaseList>()
async function searchProducts(keyword: string) { productLoading.value = true; try { const selectedIds = inputs.value.map(item => item.productId).filter(Boolean); const selected = products.value.filter(item => selectedIds.includes(item.id)); const result = await productApi.list({ keyword, enabled: true, page: 0, size: 20 }); products.value = [...selected, ...result.content.filter(item => !selectedIds.includes(item.id))] } finally { productLoading.value = false } }
function openProductSelect(visible: boolean) { if (visible) searchProducts('') }
function addRow() { inputs.value.push({ quantity: 1 }) }
async function generate() { const valid = inputs.value.filter(item => item.productId && item.quantity > 0); if (!valid.length) return void ElMessage.error('请至少选择一个商品并填写数量'); if (new Set(valid.map(item => item.productId)).size !== valid.length) return void ElMessage.error('同一商品不能重复选择'); generated.value = await purchaseListApi.manual(valid) }
async function uploadExcel(file: UploadFile) { if (file.raw) generated.value = await purchaseListApi.excel(file.raw) }
onMounted(() => { addRow(); searchProducts('') })
</script>
