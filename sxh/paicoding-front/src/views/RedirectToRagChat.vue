<template>
  <HeaderBar />

  <div class="redirect-page">
    <div class="redirect-card">
      <div class="redirect-title">正在跳转到智能助手</div>
      <div class="redirect-desc">{{ descText }}</div>
      <a v-if="redirectUrl" class="redirect-button" :href="redirectUrl">打开 sxh-rag 聊天页</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'

const redirectUrl = ref('')
const loading = ref(true)
const errorMessage = ref('')

const descText = computed(() => {
  if (loading.value) {
    return '正在同步当前 sxh 登录用户到智能助手，请稍候。'
  }
  if (errorMessage.value) {
    return errorMessage.value
  }
  return '如果没有自动跳转，请点击下方按钮。'
})

const loadRedirectUrl = async () => {
  try {
    const response = await doGet<CommonResponse<string>>('/chatv2/api/rag-login-url', {})
    const url = response.data.result
    if (!url) {
      throw new Error('智能助手跳转地址为空')
    }
    redirectUrl.value = url
    window.location.replace(url)
  } catch (error) {
    console.error('Failed to load rag redirect url', error)
    errorMessage.value = '智能助手登录同步失败，请刷新页面重试。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRedirectUrl()
})
</script>

<style scoped>
.redirect-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pai-neu-bg);
  padding: 24px;
}

.redirect-card {
  width: min(520px, 100%);
  padding: 32px 28px;
  border-radius: 18px;
  background: var(--pai-neu-surface);
  border: 1px solid var(--pai-neu-border);
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.08);
  text-align: center;
}

.redirect-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--pai-color-3-black);
}

.redirect-desc {
  margin-top: 10px;
  color: var(--pai-color-3-gray);
  line-height: 1.7;
}

.redirect-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  padding: 12px 18px;
  border-radius: 10px;
  background: var(--pai-brand-1-normal);
  color: #fff;
  text-decoration: none;
  font-weight: 600;
}
</style>
