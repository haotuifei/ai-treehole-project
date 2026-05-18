<template>
  <div class="th-page">
    <h1 class="th-page-title">辅导员班级管理</h1>

    <div class="th-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="realName" label="辅导员" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="管辖班级" min-width="300">
          <template #default="{ row }">
            <div class="class-tags">
              <el-tag
                v-for="cls in row.classes"
                :key="cls"
                closable
                type="success"
                size="small"
                style="margin-right: 8px; margin-bottom: 4px"
                @close="handleRemove(row.counselorUserId, cls)"
              >
                {{ cls }}
              </el-tag>
              <span v-if="!row.classes?.length" class="text-muted">未分配班级</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openAssign(row)">分配班级</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分配班级对话框 -->
    <el-dialog v-model="dialogVisible" title="分配班级" width="400px" destroy-on-close>
      <div v-if="currentCounselor">
        <p>辅导员：<strong>{{ currentCounselor.realName }}</strong>（{{ currentCounselor.username }}）</p>
        <el-input v-model="newClassName" placeholder="请输入班级名称" clearable @keyup.enter="handleAssign" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" :disabled="!newClassName.trim()" @click="handleAssign">
          确认分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCounselorClasses, assignCounselorClass, removeCounselorClass } from '../../api/admin'

const loading = ref(false)
const list = ref([])

async function fetchList() {
  loading.value = true
  try {
    const res = await listCounselorClasses()
    list.value = res.data.data || []
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 分配班级
const dialogVisible = ref(false)
const currentCounselor = ref(null)
const newClassName = ref('')
const assignLoading = ref(false)

function openAssign(row) {
  currentCounselor.value = row
  newClassName.value = ''
  dialogVisible.value = true
}

async function handleAssign() {
  if (!newClassName.value.trim()) return
  assignLoading.value = true
  try {
    await assignCounselorClass({
      counselorUserId: currentCounselor.value.counselorUserId,
      className: newClassName.value.trim()
    })
    ElMessage.success('分配成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '分配失败')
  } finally {
    assignLoading.value = false
  }
}

// 移除班级
async function handleRemove(counselorUserId, className) {
  try {
    await ElMessageBox.confirm(`确认移除班级「${className}」？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeCounselorClass({ counselorUserId, className })
    ElMessage.success('已移除')
    fetchList()
  } catch {
    // 取消或失败
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.class-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.text-muted {
  color: var(--th-text-muted);
}
</style>
