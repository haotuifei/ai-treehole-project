<template>
  <div class="th-page">
    <div class="page-header">
      <h1 class="th-page-title">模型配置</h1>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </div>

    <div class="th-card">
      <el-table :data="configs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="provider" label="厂商" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.provider || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型名称" width="150" />
        <el-table-column prop="apiBase" label="API 地址" min-width="250" show-overflow-tooltip />
        <el-table-column label="API Key" width="120">
          <template #default="{ row }">
            <span v-if="row.apiKeyCipher">{{ maskKey(row.apiKeyCipher) }}</span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型配置' : '新增模型配置'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="right"
      >
        <el-form-item label="厂商标识" prop="provider">
          <el-select v-model="form.provider" placeholder="选择厂商" allow-create filterable style="width: 100%">
            <el-option label="MiniMax" value="minimax" />
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="Kimi" value="kimi" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="智谱" value="zhipu" />
            <el-option label="百川" value="baichuan" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如 MiniMax M2.5、deepseek-chat" />
        </el-form-item>
        <el-form-item label="API 地址" prop="apiBase">
          <el-input v-model="form.apiBase" placeholder="https://api.example.com" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKeyCipher">
          <el-input
            v-model="form.apiKeyCipher"
            type="password"
            show-password
            placeholder="留空则不修改"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="form.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
          <span class="form-tip">数值越大优先级越高</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listModelConfigs,
  createModelConfig,
  updateModelConfig,
  deleteModelConfig
} from '../../api/admin'

const loading = ref(false)
const submitting = ref(false)
const configs = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const getDefaultForm = () => ({
  provider: '',
  modelName: '',
  apiBase: '',
  apiKeyCipher: '',
  enabled: 1,
  priority: 10,
  remark: ''
})

const form = reactive(getDefaultForm())

const rules = {
  provider: [{ required: true, message: '请选择或输入厂商标识', trigger: 'change' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiBase: [{ required: true, message: '请输入 API 地址', trigger: 'blur' }]
}

function maskKey(key) {
  if (!key || key.length < 8) return '****'
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await listModelConfigs()
    configs.value = res.data.data || []
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    provider: row.provider || '',
    modelName: row.modelName || '',
    apiBase: row.apiBase || '',
    apiKeyCipher: '',
    enabled: row.enabled ?? 1,
    priority: row.priority ?? 10,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const payload = { ...form }
    if (isEdit.value && !payload.apiKeyCipher) {
      delete payload.apiKeyCipher
    }
    if (isEdit.value) {
      await updateModelConfig(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createModelConfig(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchConfigs()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.provider} - ${row.modelName}」配置吗？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await deleteModelConfig(row.id)
    ElMessage.success('已删除')
    fetchConfigs()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
