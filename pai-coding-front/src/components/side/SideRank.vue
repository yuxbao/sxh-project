<template>
    <div class="side-rank home-right-item-wrap relative bg-white rounded shadow-sm p-4 mb-4">
        <div class="flex justify-between items-center border-b pb-2 mb-3">
            <h3 class="text-lg font-bold text-gray-800">{{ side.title }}</h3>
            <span class="text-gray-500 text-sm">{{ side.subTitle }}</span>
        </div>
        <ul class="flex flex-col gap-3">
            <li v-for="(item, index) in side.items" :key="index" class="flex items-center justify-between">
                <div class="flex items-center gap-3 overflow-hidden">
                    <div class="flex-shrink-0 w-6 h-6 flex items-center justify-center rounded text-xs font-bold"
                        :class="[
                            index === 0 ? 'bg-red-500 text-white' :
                                index === 1 ? 'bg-orange-500 text-white' :
                                    index === 2 ? 'bg-yellow-500 text-white' :
                                        'bg-gray-100 text-gray-500'
                        ]">
                        {{ index + 1 }}
                    </div>
                    <a :href="item.url" class="flex items-center gap-2 hover:opacity-80 overflow-hidden">
                        <img v-if="item.img" :src="item.img" alt="Avatar"
                            class="w-8 h-8 rounded-full object-cover border border-gray-100" />
                        <div v-else
                            class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center text-xs text-gray-500">
                            {{ item.name?.charAt(0) || 'U' }}
                        </div>
                        <span class="text-sm font-medium text-gray-700 truncate max-w-[120px]" :title="item.name || ''">
                            {{ item.name }}
                        </span>
                    </a>
                </div>
                <div class="text-xs text-gray-500 font-mono whitespace-nowrap">
                    {{ resolveVisit(item) }}
                </div>
            </li>
        </ul>
        <div class="mt-4 flex justify-end">
            <a href="/rank/month"
               class="text-sm text-blue-600 hover:text-blue-700 hover:underline">
                查看详情
            </a>
        </div>
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
/* Scoped styles if needed */
</style>
