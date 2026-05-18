<template>
  <div class="th-page">
    <h1 class="th-page-title">预警规则配置</h1>

    <div class="th-card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="160px"
        label-position="right"
        class="rule-form"
      >
        <el-divider content-position="left">情绪分值阈值</el-divider>

        <el-form-item label="中风险阈值" prop="mediumSentimentThreshold">
          <el-input-number
            v-model="form.mediumSentimentThreshold"
            :min="-1"
            :max="1"
            :step="0.1"
            :precision="2"
          />
          <span class="form-tip">
            情绪分值 ≤ 此值且 > 高风险线时判定为中风险（范围 -1 ~ 1）
          </span>
        </el-form-item>

        <el-form-item label="高风险阈值" prop="highSentimentThreshold">
          <el-input-number
            v-model="form.highSentimentThreshold"
            :min="-1"
            :max="1"
            :step="0.1"
            :precision="2"
          />
          <span class="form-tip">
            情绪分值 ≤ 此值时判定为高风险（范围 -1 ~ 1）
          </span>
        </el-form-item>

        <el-divider content-position="left">关键词检测</el-divider>

        <el-form-item label="关键词直判高风险">
          <el-switch
            v-model="form.keywordHighEnabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
          <span class="form-tip">
            启用后，消息中包含高危关键词（如自杀、自残等）将直接判定为高风险
          </span>
        </el-form-item>

        <el-divider content-position="left">备注</el-divider>

        <el-form-item label="规则备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="可选备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            保存配置
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="th-card info-card">
      <h3>说明</h3>
      <ul>
        <li><strong>情绪分值范围</strong>：-1（极度消极）到 1（极度积极），0 为中性</li>
        <li><strong>中风险</strong>：情绪分值 ≤ 中风险阈值且 > 高风险阈值，系统会记录预警</li>
        <li><strong>高风险</strong>：情绪分值 ≤ 高风险阈值，系统会触发安全回复并通知辅导员</li>
        <li><strong>关键词检测</strong>：启用后，包含自杀、自残等关键词的消息将直接判定为高风险，不受情绪分值影响</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlertRule, updateAlertRule } from '../../api/admin'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  mediumSentimentThreshold: -0.3,
  highSentimentThreshold: -0.6,
  keywordHighEnabled: 1,
  remark: ''
})

const rules = {
  mediumSentimentThreshold: [
    { required: true, message: '请输入中风险阈值', trigger: 'blur' }
  ],
  highSentimentThreshold: [
    { required: true, message: '请输入高风险阈值', trigger: 'blur' }
  ],
  keywordHighEnabled: [
    { required: true, message: '请选择是否启用关键词检测', trigger: 'change' }
  ]
}

async function fetchRule() {
  loading.value = true
  try {
    const res = await getAlertRule()
    const data = res.data.data
    if (data) {
      form.mediumSentimentThreshold = data.mediumSentimentThreshold ?? -0.3
      form.highSentimentThreshold = data.highSentimentThreshold ?? -0.6
      form.keywordHighEnabled = data.keywordHighEnabled ?? 1
      form.remark = data.remark || ''
    }
  } catch (e) {
    ElMessage.error('加载预警规则失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 验证阈值逻辑
  if (form.mediumSentimentThreshold <= form.highSentimentThreshold) {
    ElMessage.warning('中风险阈值必须大于高风险阈值')
    return
  }

  submitting.value = true
  try {
    await updateAlertRule({
      mediumSentimentThreshold: form.mediumSentimentThreshold,
      highSentimentThreshold: form.highSentimentThreshold,
      keywordHighEnabled: form.keywordHighEnabled,
      remark: form.remark
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  fetchRule()
}

onMounted(() => {
  fetchRule()
})
</script>

<style scoped>
.rule-form {
  max-width: 700px;
}

.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.info-card {
  margin-top: 16px;
}

.info-card h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

.info-card ul {
  margin: 0;
  padding-left: 20px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.8;
}

.info-card li {
  margin-bottom: 4px;
}
</style>
