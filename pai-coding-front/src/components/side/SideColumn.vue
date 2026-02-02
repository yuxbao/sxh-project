<template>
  <!-- 侧边栏 精选教程 -->
  <section class="column home-right-item-wrap relative com-2-panel J-specialTopicPanel">
    <div class="com-2-panel-title">
      <span class="com-2-panel-title-text">{{ side.title || '精选教程' }}</span>
    </div>
    <ul class="com-2-side-topics without-margin">
      <li class="com-media" v-for="(item, index) in side.items" :key="index">
        <a :href="item.url" target="_blank" class="com-side-topic">
          <div class="com-media-object">
            <img :src="item.img || ''" class="com-thumbnail">
          </div>
          <div class="com-media-body">
            <div class="com-side-topic-title">{{ item.title }}</div>
            <div class="com-side-topic-desc" v-if="item.name">{{ item.name }}</div>
          </div>
        </a>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

defineProps<{
  side: SideBarItem
}>()
</script>
