<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { request } from '@/service/request';
import { localStg } from '@/utils/storage';

const route = useRoute();
const loading = ref(true);
const errorMessage = ref('');
const sxhHomeUrl = import.meta.env.VITE_SXH_HOME_URL || 'http://localhost:5173';

const code = computed(() => {
  const queryCode = typeof route.query.code === 'string' ? route.query.code : '';
  if (queryCode) {
    return queryCode;
  }

  const searchCode = new URLSearchParams(window.location.search).get('code') || '';
  if (searchCode) {
    return searchCode;
  }

  const hash = window.location.hash || '';
  const queryIndex = hash.indexOf('?');
  if (queryIndex >= 0) {
    return new URLSearchParams(hash.slice(queryIndex + 1)).get('code') || '';
  }

  return '';
});

async function exchangeBridgeCode() {
  if (!code.value) {
    const { data, error } = await request<Api.Auth.UserInfo>({
      url: '/users/me',
      method: 'GET'
    });

    if (!error && data?.id) {
      window.location.replace('/#/chat');
      return;
    }

    errorMessage.value = '缺少登录桥接参数，请返回思享汇重新进入智能助手。';
    loading.value = false;
    return;
  }

  const { data, error } = await request<Api.Auth.LoginToken>({
    url: '/auth/sxh/exchange',
    method: 'POST',
    data: { code: code.value }
  });

  if (error || !data?.token) {
    const fallback = await request<Api.Auth.UserInfo>({
      url: '/users/me',
      method: 'GET'
    });

    if (!fallback.error && fallback.data?.id) {
      window.location.replace('/#/chat');
      return;
    }

    errorMessage.value = error?.response?.data?.message || '智能助手登录失败，请稍后重试。';
    loading.value = false;
    return;
  }

  localStg.remove('token');
  localStg.remove('refreshToken');
  localStg.set('token', data.token);
  localStg.set('refreshToken', data.refreshToken);

  window.location.replace('/chat');
}

onMounted(() => {
  exchangeBridgeCode();
});
</script>

<template>
  <div class="min-h-screen flex-center bg-[#f6f8fb] px-4">
    <NCard class="w-full max-w-120 rounded-4 shadow-sm">
      <div class="flex-col items-center gap-4 text-center">
        <SystemLogo class="text-14 text-primary" />
        <h1 class="m-0 text-6 font-semibold text-[#1f2937]">正在同步 sxh 登录状态</h1>
        <p v-if="loading" class="m-0 text-4 text-[#6b7280]">马上进入智能助手聊天页，请稍候。</p>
        <p v-else class="m-0 text-4 text-[#ef4444]">{{ errorMessage }}</p>
        <NSpin v-if="loading" size="large" />
        <div v-else class="flex items-center justify-center gap-3">
          <NButton type="primary" tag="a" href="/login">
            前往 sxh-rag 登录页
          </NButton>
          <NButton quaternary tag="a" :href="sxhHomeUrl">
            返回思享汇
          </NButton>
        </div>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
