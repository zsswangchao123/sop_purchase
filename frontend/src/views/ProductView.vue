<template>
  <div class="page">
    <div class="toolbar">
      <div><h2>商品管理</h2><p class="page-subtitle">维护可导入和可选择的主商品</p></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="keyword" clearable placeholder="搜索商品编码或名称" :prefix-icon="Search" @input="searchLater" @clear="searchNow" />
      <el-select v-model="enabled" clearable placeholder="全部状态" @change="searchNow">
        <el-option label="已启用" :value="true" /><el-option label="已停用" :value="false" />
      </el-select>
    </div>
    <el-table v-loading="loading" :data="rows" class="panel">
      <el-table-column prop="code" label="编码" width="140" /><el-table-column prop="name" label="名称" />
      <el-table-column prop="specification" label="规格" /><el-table-column prop="unit" label="单位" width="100" />
      <el-table-column label="启用" width="100"><template #default="{ row }"><el-switch v-model="row.enabled" @change="toggle(row)" /></template></el-table-column>
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-bar"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next" @change="load" /></div>
    <el-dialog v-model="dialogVisible" :title="editing?.id ? '编辑商品' : '新增商品'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="编码" required><el-input v-model="form.code" :disabled="!!editing?.id" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item>
        <el-form-item label="单位" required><el-input v-model="form.unit" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { productApi } from '../api/product'
import type { Product } from '../api/types'
const rows = ref<Product[]>([]), keyword = ref(''), enabled = ref<boolean>(), loading = ref(false)
const page = ref(1), size = ref(20), total = ref(0), dialogVisible = ref(false), editing = ref<Product | null>(null), form = ref<Partial<Product>>({})
let timer: ReturnType<typeof setTimeout>
async function load() { loading.value = true; try { const result = await productApi.list({ keyword: keyword.value, enabled: enabled.value, page: page.value - 1, size: size.value }); rows.value = result.content; total.value = result.totalElements } finally { loading.value = false } }
function searchNow() { page.value = 1; load() }
function searchLater() { clearTimeout(timer); timer = setTimeout(searchNow, 300) }
function openCreate() { editing.value = null; form.value = { enabled: true }; dialogVisible.value = true }
function openEdit(row: Product) { editing.value = row; form.value = { ...row }; dialogVisible.value = true }
async function save() { if (!form.value.code || !form.value.name || !form.value.unit) return void ElMessage.error('请填写编码、名称和单位'); editing.value?.id ? await productApi.update(editing.value.id, form.value) : await productApi.create(form.value); dialogVisible.value = false; await load() }
async function toggle(row: Product) { await productApi.setEnabled(row.id, row.enabled) }
onMounted(load)
</script>
