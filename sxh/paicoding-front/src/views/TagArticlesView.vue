<template>
  <HeaderBar></HeaderBar>

  <div class="home mt-2">
    <div class="home-wrap bg-color">
      <div class="home-inter-wrap">
        <div class="home-body">
          <div id="articleList" class="cdc-article-panel__list">
            <ArticleList :articles="articles.records"></ArticleList>
          </div>

          <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage"
            v-model:page-size="pageSize" layout="sizes, prev, pager, next" :page-count="totalPage"
            :default-current-page="1" @update:page-size="onPageSizeChange"
            @update:current-page="onCurrentPageChange" />
        </div>
      </div>
    </div>
  </div>

  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>


<script setup lang="ts">

import HeaderBar from '@/components/layout/HeaderBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { onMounted, provide, reactive, ref } from 'vue'
import { doGet } from '@/http/BackendRequests'
import { TAG_ARTICLE_LIST_URL } from '@/http/URL'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { useGlobalStore } from '@/stores/global'
import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { useRoute } from 'vue-router'
import ArticleList from '@/views/home/article/ArticleList.vue'
const globalStore = useGlobalStore()
const route = useRoute()

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

let articles = reactive<BasicPageType<ArticleType>>({ ...defaultBasicPage })

const currentPage = ref(1)
const totalPage = ref(0)
const pageSize = ref(10)

const fetchArticles = (pageNum: number, size: number) => {
  const tagId = route.params['tagId']
  if (!tagId) {
    return
  }
  doGet<CommonResponse>(TAG_ARTICLE_LIST_URL, {
    tagId: tagId,
    currentPage: pageNum,
    pageSize: size
  }).then((response) => {
    if (response.data) {
      globalStore.setGlobal(response.data.global)
      Object.assign(articles, response.data.result)
      totalPage.value = Number(response.data.result.pages)
      currentPage.value = Number(response.data.result.current)
    }
  })
}

onMounted(() => {
  fetchArticles(currentPage.value, pageSize.value)
})

const onPageSizeChange = (newPageSize: number) => {
  fetchArticles(currentPage.value, newPageSize)
}

const onCurrentPageChange = (newCurrentPage: number) => {
  fetchArticles(newCurrentPage, pageSize.value)
}
</script>

<style scoped>

</style>
