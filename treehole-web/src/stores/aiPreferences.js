import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'treehole_ai_preferences'

const DEFAULTS = {
  aiName: '树洞陪伴者',
  userNickname: '',
  persona: '温柔、耐心、共情，像一个会认真倾听的朋友。',
  tone: 'gentle',
  replyLength: 'balanced',
  useEmoji: false,
  proactiveCare: true
}

function readStorage() {
  if (typeof window === 'undefined') return { ...DEFAULTS }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return { ...DEFAULTS }
    return { ...DEFAULTS, ...JSON.parse(raw) }
  } catch {
    return { ...DEFAULTS }
  }
}

export const useAiPreferencesStore = defineStore('aiPreferences', () => {
  const settings = ref(readStorage())

  const aiName = computed(() => settings.value.aiName || DEFAULTS.aiName)
  const userNickname = computed(() => settings.value.userNickname || '')
  const persona = computed(() => settings.value.persona || DEFAULTS.persona)
  const tone = computed(() => settings.value.tone || DEFAULTS.tone)
  const replyLength = computed(() => settings.value.replyLength || DEFAULTS.replyLength)
  const useEmoji = computed(() => Boolean(settings.value.useEmoji))
  const proactiveCare = computed(() => Boolean(settings.value.proactiveCare))

  const subtitle = computed(() => {
    const toneMap = {
      gentle: '慢慢说，我会认真听。',
      rational: '我们可以把感受和问题一起理清。',
      lively: '我会陪你把情绪一点点说开。'
    }
    return toneMap[tone.value] || toneMap.gentle
  })

  function persist() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(settings.value))
  }

  function patchSettings(payload) {
    settings.value = { ...settings.value, ...payload }
    persist()
  }

  function resetSettings() {
    settings.value = { ...DEFAULTS }
    persist()
  }

  function buildCustomSystemPrompt() {
    const toneText = {
      gentle: '语气温柔、稳定、让人有安全感',
      rational: '语气理性、清晰、结构化，但保持尊重与共情',
      lively: '语气轻快、鼓励感更强，但不要轻浮'
    }[tone.value]

    const lengthText = {
      short: '回复尽量简洁，优先 1 到 3 句，除非用户明确要求展开',
      balanced: '回复控制在适中长度，既有共情也有一点具体建议',
      detailed: '回复可以更详细一些，但避免空泛和重复'
    }[replyLength.value]

    const lines = [
      `你的名字是“${aiName.value}”。`,
      userNickname.value ? `称呼用户时可优先使用“${userNickname.value}”，但不要每句都重复称呼。` : '称呼用户时自然即可，不要频繁重复称呼。',
      `你的人设偏好：${persona.value}`,
      `回复风格要求：${toneText}。`,
      `${lengthText}。`,
      useEmoji.value ? '可以极少量使用自然的中文语境表情，但不要过度使用。' : '不要使用表情符号，保持自然、干净的表达。',
      proactiveCare.value
        ? '在合适时可以主动多追问一句，帮助用户继续表达。'
        : '尽量减少主动追问，除非用户明显需要引导。'
    ]

    return lines.join('\n')
  }

  return {
    settings,
    aiName,
    userNickname,
    persona,
    tone,
    replyLength,
    useEmoji,
    proactiveCare,
    subtitle,
    patchSettings,
    resetSettings,
    buildCustomSystemPrompt
  }
})
