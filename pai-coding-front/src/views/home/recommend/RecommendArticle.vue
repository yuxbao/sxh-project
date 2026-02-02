<template>
  <div class="home-carouse-wrap" :style="{ backgroundColor: backgroundColor }">
    <div class="home-carouse-inter-wrap hidden-when-screen-small p-0">
      <div class="home-carouse-item" v-for="(article, iter) in topArticles" :key="article.articleId">
        <RouterLink :to="'/article/detail/' + article.articleId">
          <img :src="article.cover" :id="'cover' + iter" @load="setColor(iter)" />
          <div class="home-carouse-item-body">
            <div class="home-carouse-item-title" :title="article.title">{{ article.title }}</div>
            <div class="home-carouse-item-tag hover-background"
              :title="article.tags.map(tagItem => tagItem.tag).join(',')">
              <span class="home-carouse-item-dot tag"></span>

              <div v-for="(tag, index) in article.tags.slice(0, 3)" :key="index">
                <span class="home-carouse-item-first-text">{{ tag.tag }}</span>
              </div>
              <div v-if="article.tags.length > 3">.....</div>
            </div>
            <!-- <object> -->
            <div class="home-carouse-item-tag flex hover-background">
              <span class="home-carouse-item-dot"></span>
              <!-- <a th:href="${'/user/' + article.author}"> -->
              <span class="ml-2">{{ article.authorName }}</span>
              <!-- </a> -->
              <span class="ml-2">{{ format(new Date(Number(article.createTime)), "yyyy/MM/dd") }}</span>
            </div>
            <!-- </object> -->
          </div>
        </RouterLink>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
import { format } from 'date-fns'
import { ref } from 'vue'
import Vibrant from 'node-vibrant/lib/bundle'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps<{
  topArticles: ArticleType[]
}>()

const backgroundColor = ref<string>('')


function setColor(index: number) {
  if (index !== 0) return
  const img = document.getElementById(`cover${index}`) as HTMLImageElement
  if (img) {
    // @ts-ignore
    if (img.complete) {
      applyColor(img)
    } else {
      img.addEventListener("load", function () {
        applyColor(img)
      })
    }
  }
}

function applyColor(img: any) {
  // Vibrant.from 如果传入 URL 字符串，可以直接由 Vibrant 处理下载。
  // 如果传入 HTMLImageElement，则该元素必须在加载时就有了 crossOrigin="anonymous" 属性，
  // 否则 Canvas 就会被污染。
  // 为了安全起见，这里直接使用图片 src 进行提取，让 Vibrant 自己去处理加载（它内部也会处理跨域）
  // 或者确保 img 标签在 HTML 中渲染时就有 crossorigin 属性

  Vibrant.from(img.src)
    .getPalette()
    .then((palette) => {
      let rgb = palette.Vibrant?.getHex()
      // console.log("rgb", rgb)
      backgroundColor.value = rgb || ''
    }).catch(e => {
      console.warn("Vibrant extract color failed:", e)
    })
}





import { RouterLink } from 'vue-router'
</script>

<style scoped></style>
