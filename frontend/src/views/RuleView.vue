<template>
  <div class="page">
    <div class="toolbar">
      <div class="toolbar-left"><div><h2>配套规则</h2><p class="page-subtitle">定义商品和配套采购项的计算关系</p></div><el-select v-model="selectedProductId" filterable remote :remote-method="searchProducts" :loading="productLoading" placeholder="输入编码或名称搜索商品" style="width:300px" @visible-change="openProductSelect" @change="loadRules"><el-option v-for="product in products" :key="product.id" :label="`${product.code} ${product.name} · ${product.unit}`" :value="product.id" /></el-select></div>
      <el-button type="primary" :icon="Plus" :disabled="!selectedProductId" @click="openCreate">新增规则</el-button>
    </div>
    <el-table :data="rules" class="panel"><el-table-column prop="supportItemName" label="配套项" /><el-table-column label="计算方式" width="110"><template #default="{ row }">{{ calcTypeLabel(row.calcType) }}</template></el-table-column><el-table-column prop="baseQuantity" label="基准数量" width="110" /><el-table-column prop="supportQuantity" label="配套数量" width="110" /><el-table-column label="取整" width="110"><template #default="{ row }">{{ roundingModeLabel(row.roundingMode) }}</template></el-table-column><el-table-column label="启用" width="100"><template #default="{ row }"><el-switch v-model="row.enabled" @change="toggle(row)" /></template></el-table-column><el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" :icon="Delete" @click="remove(row)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" title="配套规则" width="560px">
      <el-form label-width="100px">
        <el-form-item label="配套项" required><el-select v-model="form.supportItemId" filterable remote :remote-method="searchSupportItems" :loading="supportLoading" placeholder="输入编码或名称搜索配套项" style="width:100%" @visible-change="openSupportSelect"><el-option v-for="item in supportItems" :key="item.id" :label="`${item.code} ${item.name} · ${item.unit}`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="计算方式" required><el-segmented v-model="form.calcType" :options="[{label:'固定数量',value:'FIXED'},{label:'按比例',value:'RATIO'}]" /></el-form-item>
        <el-form-item v-if="form.calcType === 'RATIO'" label="基准数量" required><el-input-number v-model="form.baseQuantity" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="配套数量" required><el-input-number v-model="form.supportQuantity" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="取整方式" required><el-select v-model="form.roundingMode" style="width:160px"><el-option label="向上取整" value="CEIL" /><el-option label="保留小数" value="DECIMAL" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { productApi } from '../api/product'
import { ruleApi } from '../api/rule'
import { supportItemApi } from '../api/supportItem'
import type { Product, Rule, SupportItem } from '../api/types'
const products = ref<Product[]>([]), supportItems = ref<SupportItem[]>([]), rules = ref<Rule[]>([])
const productLoading = ref(false), supportLoading = ref(false), selectedProductId = ref<number>(), dialogVisible = ref(false), editing = ref<Rule | null>(null), form = ref<Partial<Rule>>({})
function calcTypeLabel(value: Rule['calcType']) { return value === 'FIXED' ? '固定数量' : '按比例' }
function roundingModeLabel(value: Rule['roundingMode']) { return value === 'CEIL' ? '向上取整' : '保留小数' }
async function searchProducts(keyword: string) { productLoading.value = true; try { const selected = products.value.find(item => item.id === selectedProductId.value); const result = await productApi.list({ keyword, enabled: true, page: 0, size: 20 }); products.value = selected ? [selected, ...result.content.filter(item => item.id !== selected.id)] : result.content } finally { productLoading.value = false } }
async function searchSupportItems(keyword: string) { supportLoading.value = true; try { const selected = supportItems.value.find(item => item.id === form.value.supportItemId); const result = await supportItemApi.list({ keyword, enabled: true, page: 0, size: 20 }); supportItems.value = selected ? [selected, ...result.content.filter(item => item.id !== selected.id)] : result.content } finally { supportLoading.value = false } }
function openProductSelect(visible: boolean) { if (visible) searchProducts('') }
function openSupportSelect(visible: boolean) { if (visible) searchSupportItems('') }
async function loadRules() { if (selectedProductId.value) rules.value = await ruleApi.listByProduct(selectedProductId.value) }
function openCreate() { editing.value = null; form.value = { calcType: 'RATIO', roundingMode: 'CEIL', supportQuantity: 1, baseQuantity: 1 }; dialogVisible.value = true; searchSupportItems('') }
function openEdit(row: Rule) { editing.value = row; form.value = { ...row }; dialogVisible.value = true; searchSupportItems('') }
async function save() { if (!selectedProductId.value || !form.value.supportItemId || !form.value.calcType || !form.value.supportQuantity || !form.value.roundingMode) return void ElMessage.error('请完整填写规则'); editing.value?.id ? await ruleApi.update(editing.value.id, form.value) : await ruleApi.create(selectedProductId.value, form.value); dialogVisible.value = false; await loadRules() }
async function toggle(row: Rule) { await ruleApi.setEnabled(row.id, row.enabled) }
async function remove(row: Rule) { await ruleApi.delete(row.id); await loadRules() }
onMounted(() => searchProducts(''))
</script>
