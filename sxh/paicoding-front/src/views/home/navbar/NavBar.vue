<template>
  <div class="mb-1">
    <div v-if="!ifSearchActive" class="card-body d-flex category-wrap type-box">
      <div class="d-flex align-content-start w-full category-list">
        <div v-for="subCategory in categories" :key="subCategory.categoryId">
          <RouterLink :to="{ path: '/', query: { category: subCategory.category } }" class="category--item pt-2 pr-5"
            :class="{ 'category--active': subCategory.category === activeCategory }">
            {{ subCategory.category }}
          </RouterLink>
        </div>
      </div>
      <button @click="ifSearchActive = true" class="right-8 px-3 ml-4 category-search-btn">
        <svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="search"
          class="svg-inline--fa fa-search fa-w-16 " role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
          <path fill="currentColor"
            d="M505 442.7L405.3 343c-4.5-4.5-10.6-7-17-7H372c27.6-35.3 44-79.7 44-128C416 93.1 322.9 0 208 0S0 93.1 0 208s93.1 208 208 208c48.3 0 92.7-16.4 128-44v16.3c0 6.4 2.5 12.5 7 17l99.7 99.7c9.4 9.4 24.6 9.4 33.9 0l28.3-28.3c9.4-9.4 9.4-24.6.1-34zM208 336c-70.7 0-128-57.2-128-128 0-70.7 57.2-128 128-128 70.7 0 128 57.2 128 128 0 70.7-57.2 128-128 128z">
          </path>
        </svg>
      </button>
    </div>
    <div v-if="ifSearchActive" class="center-content card-body d-flex category-wrap type-box">
      <div class="d-flex justify-center">
        <div class="relative lg:max-w-lg w-full">
          <el-select v-model="searchInput" filterable remote reserve-keyword placeholder="搜文章，或试试自然语言描述..."
            :loading="loading" :remote-method="fetchSearchData" :no-data-text="'没有找到合适内容，换个关键词或换种说法试试'" fit-input-width
            @change="handleSelect">
            <el-option v-for="item in options" :key="item.id" :value="item.id" :label="item.title">
              <template #default>
                <div class="search-option">
                  <span class="search-option-title" v-html="highlightKeyword(item.title)"></span>
                  <span v-if="item.column" class="search-option-tag">{{ item.column }}</span>
                </div>
              </template>
            </el-option>
          </el-select>

          <div class="hidden search-result-block">
            <div class="ais-Hits Hits search-has-result">
              <ul class="ais-Hits-list">
                <li class="ais-Hits-item">
                  <RouterLink class="my-0 border-b py-1 px-2 border-gray-400 hover:bg-gray-400 block"
                    to="/article/detail/">
                    <span class="text-white text-sm hover:text-primary-200">
                      <span class="title pre">编程汇</span>
                      <mark class="text-primary-300 bg-transparent">关键字</mark>
                      <span class="title last">就是牛</span>
                    </span>
                  </RouterLink>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <el-button class="center-content" @click="ifSearchActive = false"><el-icon size="25">
            <Close />
          </el-icon></el-button>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'

const route = useRoute()
const router = useRouter()

const ifSearchActive = ref(false)

const props = defineProps<{
  categories: ArticleCategoryType[]
}>()

const activeCategory = ref('全部')

onMounted(() => {
  activeCategory.value = route.query.category as string || '全部'
})

// ==================== 搜索输入框 ====================
const loading = ref(false)
const searchInput = ref<string | number>('')
const keyWord = ref('')
interface ListItem {
  column?: string,
  columnId?: number,
  id: number,
  readType?: string,
  sort?: string,
  title: string,
}
const options = ref<ListItem[]>([])

const handleSelect = (val: string | number) => {
  if (val) {
    router.push(`/article/detail/${val}`)
  }
}

const fetchSearchData = (query: string) => {
  keyWord.value = query
  loading.value = true
  doGet<CommonResponse>('/search/api/hint', { key: query })
    .then((res) => {
      options.value = res.data.result?.items
    })
    .catch((err) => {
      console.log(err)
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 高亮搜索得到的文章的结果
 * @param text
 */
const highlightKeyword = (text: string) => {
  if (!keyWord.value) return text
  const regex = new RegExp(`(${keyWord.value})`, 'gi')
  return text.replace(regex, '<b class="highlight" style="font-size: large">$1</b>')
}
</script>

<style scoped>
.el-select {
  width: 300px
}

.search-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.search-option-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-option-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  font-size: 12px;
  line-height: 18px;
}

/* 当屏幕宽度大于等于 768px 时，应用此样式 */
@media (min-width: 768px) {
  .el-select {
    width: 500px
  }
}
</style>
