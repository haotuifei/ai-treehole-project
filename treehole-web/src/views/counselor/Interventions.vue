<template>
  <div class="th-page">
    <h1 class="th-page-title">干预记录</h1>

    <!-- 搜索栏 -->
    <div class="th-card search-bar">
      <el-input
        v-model="studentKeyword"
        placeholder="搜索学生姓名/用户名"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 记录列表 -->
    <div class="th-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="学生" width="130">
          <template #default="{ row }">
            <div>{{ row.studentName || '-' }}</div>
            <div class="sub-text">{{ row.studentUsername }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="关联预警" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.warningRiskLevel" :type="riskTagType(row.warningRiskLevel)" size="small">
              {{ riskLabel(row.warningRiskLevel) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningTextSummary" label="预警摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="干预内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="干预时间" width="160">
          <template #default="{ row }">{{ formatTime(row.interventionTime) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > pageSize"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInterventions } from '../../api/counselor'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const studentKeyword = ref('')

async function fetchList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (studentKeyword.value.trim()) params.studentKeyword = studentKeyword.value.trim()
    const res = await getInterventions(params)
    const d = res.data.data
    list.value = d.records || []
    total.value = d.total || 0
  } catch {
    ElMessage.error('加载干预记录失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchList()
}

function handlePageChange(page) {
  pageNum.value = page
  fetchList()
}

function riskTagType(level) {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  return 'success'
}

function riskLabel(level) {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  return '低风险'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.sub-text {
  font-size: 12px;
  color: var(--th-text-muted);
}

.text-muted {
  color: var(--th-text-muted);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
