<script setup lang="ts">
defineOptions({
  name: 'Register'
});

const { toggleLoginModule } = useRouterPush();
const sxhHomeUrl = import.meta.env.VITE_SXH_HOME_URL || 'http://localhost:5173';
</script>

<template>
  <NSpace vertical :size="18" class="w-full">
    <NAlert type="info" :show-icon="false">
      思享汇 RAG 不单独维护注册用户，账号体系与思享汇社区 `user` 表共用。
    </NAlert>
    <NSpace vertical :size="18" class="w-full">
      <NButton type="primary" size="large" round block tag="a" :href="sxhHomeUrl">
        前往思享汇注册
      </NButton>
      <NButton block @click="toggleLoginModule('pwd-login')">
        返回登录
      </NButton>
    </NSpace>

    <div class="mt-4 text-center">
      注册完成后，返回本页即可直接使用思享汇账号登录。
    </div>
  </NSpace>
</template>

<style scoped></style>
