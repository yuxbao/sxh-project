<template>
  <HeaderBar />

  <div class="home mt-2">
    <div class="home-wrap bg-color">
      <div class="home-inter-wrap">
        <div class="home-body w-full">
          <div class="bg-white rounded shadow-sm p-6 mb-6">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h1 class="text-2xl font-bold text-gray-800">用户活跃度排行榜</h1>
                <p class="text-sm text-gray-500 mt-1">按日榜/ 月榜展示用户活跃度</p>
              </div>
              <div class="flex gap-2">
                <button
                  class="px-4 py-2 rounded text-sm border transition-colors"
                  :class="activeTime === 'day' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-200 hover:border-blue-400'"
                  @click="switchTime('day')"
                >
                  日榜
                </button>
                <button
                  class="px-4 py-2 rounded text-sm border transition-colors"
                  :class="activeTime === 'month' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-200 hover:border-blue-400'"
                  @click="switchTime('month')"
                >
                  月榜
                </button>
              </div>
            </div>
          </div>

          <el-skeleton :loading="loading" animated :throttle="200">
            <template #template>
              <div v-for="i in 8" :key="i" class="bg-white rounded shadow-sm p-4 mb-3">
                <el-skeleton-item variant="text" style="width: 60%" />
                <el-skeleton-item variant="text" style="width: 40%" class="mt-2" />
              </div>
            </template>
            <template #default>
              <div v-if="rankItems.length > 0" class="space-y-3">
                <div
                  v-for="item in rankItems"
                  :key="item.rank"
                  class="bg-white rounded shadow-sm p-4 flex flex-wrap items-center justify-between gap-4"
                >
                  <div class="flex items-center gap-4 min-w-0">
                    <div
                      class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
                      :class="rankBadgeClass(item.rank)"
                    >
                      {{ item.rank }}
                    </div>
                    <img
                      :src="item.user.avatar"
                      alt="avatar"
                      class="w-12 h-12 rounded-full object-cover border border-gray-100"
                    />
                    <div class="min-w-0">
                      <div class="text-base font-semibold text-gray-800 truncate">
                        {{ item.user.name }}
                      </div>
                      <div class="text-sm text-gray-500 truncate">
                        {{ item.user.profile || '这个人很懒，没有留下介绍~' }}
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-4">
                    <div class="text-sm text-gray-500">
                      活跃度 <span class="text-red-500 font-semibold">{{ item.score }}</span>
                    </div>
                    <a class="text-blue-600 text-sm hover:underline" :href="`/user/${item.user.userId}`">去主页</a>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无排行榜数据" />
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { ACTIVITY_RANK_URL } from '@/http/URL'
import type { RankInfoResponse, RankItem } from '@/http/ResponseTypes/RankType'
import { useGlobalStore } from '@/stores/global'

const router = useRouter()
const route = useRoute()
const globalStore = useGlobalStore()

const loading = ref(true)
const rankItems = ref<RankItem[]>([])
const activeTime = ref<'day' | 'month'>('month')

const normalizeTime = (val: string | { desc?: string } | null | undefined) => {
  if (!val) return 'month'
  if (typeof val === 'string') {
    return val.toLowerCase() === 'day' ? 'day' : 'month'
  }
  if (val.desc && val.desc.toLowerCase() === 'day') {
    return 'day'
  }
  return 'month'
}

const loadRank = () => {
  loading.value = true
  doGet<CommonResponse<RankInfoResponse>>(`${ACTIVITY_RANK_URL}/${activeTime.value}`, {})
    .then((response) => {
      if (response.data) {
        globalStore.setGlobal(response.data.global)
        const result = response.data.result
        activeTime.value = normalizeTime(result.time) as 'day' | 'month'
        rankItems.value = result.items || []
      }
    })
    .finally(() => {
      loading.value = false
    })
}

const switchTime = (time: 'day' | 'month') => {
  if (activeTime.value === time) return
  router.push(`/rank/${time}`)
}

const rankBadgeClass = (rank: number) => {
  if (rank === 1) return 'bg-red-500 text-white'
  if (rank === 2) return 'bg-orange-500 text-white'
  if (rank === 3) return 'bg-yellow-500 text-white'
  return 'bg-gray-100 text-gray-600'
}

onMounted(() => {
  const routeTime = (route.params.time as string | undefined) || 'month'
  activeTime.value = routeTime === 'day' ? 'day' : 'month'
  loadRank()
})

watch(
  () => route.params.time,
  (newVal) => {
    const next = (newVal as string | undefined) || 'month'
    const normalized = next === 'day' ? 'day' : 'month'
    if (normalized !== activeTime.value) {
      activeTime.value = normalized
    }
    loadRank()
  }
)
</script>

<style scoped></style>
