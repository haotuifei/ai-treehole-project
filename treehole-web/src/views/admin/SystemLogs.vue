<template>
  <div class="th-page">
    <h1 class="th-page-title">系统日志</h1>

    <!-- 搜索栏 -->
    <div class="th-card search-bar">
      <el-form :model="query" inline>
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width: 130px">
            <el-option label="认证" value="AUTH" />
            <el-option label="用户" value="USER" />
            <el-option label="聊天" value="CHAT" />
            <el-option label="情绪" value="EMOTION" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="操作/URI/IP"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志表格 -->
    <div class="th-card">
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="module" label="模块" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="moduleTagType(row.module)">
              {{ row.module || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" min-width="150" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="70">
          <template #default="{ row }">
            <el-tag size="small" :type="methodTagType(row.requestMethod)">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求URI" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'slow-request': row.durationMs > 1000 }">
              {{ row.durationMs != null ? row.durationMs + 'ms' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="70" />
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="60" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.errorMsg || row.method || row.userAgent"
              type="primary"
              link
              size="small"
              @click="showDetail(row)"
            >
              查看
            </el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求方法">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URI">{{ detail.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="方法签名">
          <span class="method-text">{{ detail.method || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">
          <span class="ua-text">{{ detail.userAgent || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="detail.errorMsg">
          <span class="error-text">{{ detail.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageSystemLogs } from '../../api/admin'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref({})

const query = reactive({
  pageNum: 1,
  pageSize: 20,
  module: '',
  status: null,
  keyword: ''
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

const moduleTagMap = {
  AUTH: 'warning',
  USER: '',
  CHAT: 'success',
  EMOTION: 'danger',
  SYSTEM: 'info'
}

function moduleTagType(module) {
  return moduleTagMap[module] || 'info'
}

function methodTagType(method) {
  const map = { GET: '', POST: 'success', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

async function fetchLogs() {
  loading.value = true
  try {
    const res = await pageSystemLogs(query)
    const data = res.data.data
    logs.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchLogs()
}

function handleReset() {
  query.module = ''
  query.status = null
  query.keyword = ''
  query.pageNum = 1
  fetchLogs()
}

function showDetail(row) {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.search-bar :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 4px;
}

.slow-request {
  color: var(--el-color-danger);
  font-weight: 600;
}

.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.method-text {
  font-family: monospace;
  font-size: 13px;
  word-break: break-all;
}

.ua-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  word-break: break-all;
}

.error-text {
  color: var(--el-color-danger);
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
