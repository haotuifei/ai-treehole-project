<template>
  <div class="th-page">
    <div class="page-header">
      <h1 class="th-page-title">用户管理</h1>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="th-card search-bar">
      <el-form :model="query" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="用户名/姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleCode" placeholder="全部" clearable style="width: 130px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="辅导员" value="COUNSELOR" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户表格 -->
    <div class="th-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            {{ row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roleCodes"
              :key="role"
              :type="roleTagType(role)"
              size="small"
              class="role-tag"
            >
              {{ roleLabel(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.username !== 'admin'"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            :disabled="isEdit"
            placeholder="3-64 位字符"
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="新密码">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="学号" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" placeholder="班级名称" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { pageUsers, createUser, updateUser, deleteUser, listRoles } from '../../api/admin'

const loading = ref(false)
const submitting = ref(false)
const users = ref([])
const total = ref(0)
const allRoles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  roleCode: '',
  status: null
})

const getDefaultForm = () => ({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  studentNo: '',
  className: '',
  roleIds: [],
  status: 1
})

const form = reactive(getDefaultForm())

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '长度 3-64', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '长度 6-64', trigger: 'blur' }
  ],
  roleIds: [
    { required: true, type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change' }
  ]
}

const roleLabelMap = { ADMIN: '管理员', COUNSELOR: '辅导员', STUDENT: '学生' }
const roleTagMap = { ADMIN: 'danger', COUNSELOR: 'warning', STUDENT: '' }

function roleLabel(code) {
  return roleLabelMap[code] || code
}

function roleTagType(code) {
  return roleTagMap[code] || 'info'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await pageUsers(query)
    const data = res.data.data
    users.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const res = await listRoles()
    allRoles.value = res.data.data || []
  } catch (e) {
    console.error('加载角色失败', e)
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchUsers()
}

function handleReset() {
  query.keyword = ''
  query.roleCode = ''
  query.status = null
  query.pageNum = 1
  fetchUsers()
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    realName: row.realName || '',
    phone: row.phone || '',
    email: row.email || '',
    studentNo: row.studentNo || '',
    className: row.className || '',
    roleIds: [],
    status: row.status
  })
  const codeToId = {}
  allRoles.value.forEach(r => { codeToId[r.roleCode] = r.id })
  form.roleIds = (row.roleCodes || []).map(c => codeToId[c]).filter(Boolean)
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
    if (isEdit.value) {
      const payload = { ...form }
      if (!payload.password) delete payload.password
      await updateUser(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.username}」吗？此操作不可撤销。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await deleteUser(row.id)
    ElMessage.success('已删除')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-bar {
  margin-bottom: 16px;
}

.search-bar :deep(.el-form-item) {
  margin-bottom: 0;
}

.role-tag {
  margin-right: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 4px;
}
</style>
