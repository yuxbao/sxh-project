<template>
    <div class="hot-article home-right-item-wrap relative bg-white rounded shadow-sm p-4 mb-4">
        <div class="flex justify-between items-center border-b pb-2 mb-3">
            <h3 class="text-lg font-bold text-gray-800">{{ side.title }}</h3>
            <span class="text-gray-500 text-sm">{{ side.subTitle }}</span>
        </div>
        <ul class="flex flex-col gap-3">
            <li v-for="(item, index) in side.items" :key="index" class="group">
                <a :href="item.url" class="flex items-start gap-3">
                    <!-- 序号 -->
                    <div class="flex-shrink-0 w-6 h-6 flex items-center justify-center rounded text-xs font-bold"
                        :class="[
                            index === 0 ? 'bg-red-500 text-white' :
                                index === 1 ? 'bg-orange-500 text-white' :
                                    index === 2 ? 'bg-yellow-500 text-white' :
                                        'bg-gray-100 text-gray-500'
                        ]">
                        {{ index + 1 }}
                    </div>
                    <div class="min-w-0">
                        <!-- 文章名称 -->
                        <div
                            class="text-gray-700 hover:text-blue-600 transition-colors text-sm line-clamp-2 leading-relaxed">
                            {{ item.title }}
                        </div>
                        <div class="flex items-center text-xs text-gray-400 mt-1 space-x-2">
                            <span v-if="item.visit !== null" class="flex items-center">
                                <span class="mr-1">🔥</span> {{ resolveVisit(item) }}
                            </span>
                        </div>
                    </div>
                </a>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import type { SideBarItem, SideBarVisit } from '@/http/ResponseTypes/SideBarItemType'

defineProps<{
    side: SideBarItem
}>()

const resolveVisit = (item: { visit: number | SideBarVisit | null }) => {
    if (item.visit === null || item.visit === undefined) {
        return '-';
    }
    if (typeof item.visit === 'number') {
        return item.visit;
    }
    return item.visit.visit ?? '-';
}
</script>

<style scoped>
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>
