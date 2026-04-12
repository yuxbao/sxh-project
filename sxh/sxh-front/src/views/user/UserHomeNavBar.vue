<template>
  <el-tabs v-model="activeName" type="card" class="demo-tabs" @tab-click="handleClick" @tab-change="handleChange">
    <el-tab-pane label="文章" name="articlesTab" lazy>
      <template #default>
        <UserHomeNavBarArticleList :articles="articles" empty-description="暂无发布的文章"></UserHomeNavBarArticleList>
        <!--        分页组件-->
        <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentArticlesPage"
          v-model:page-size="articlesPageSize" layout="sizes, prev, pager, next" :page-count="totalArticlesPage"
          :default-current-page="1" @update:page-size="onArticlesPageSizeChange"
          @update:current-page="onArticlesCurrentPageChange" />
      </template>
    </el-tab-pane>
    <el-tab-pane v-if="global.user.id == route.params.userId" label="浏览记录" name="historyTab" lazy>
      <UserHomeNavBarArticleList :articles="historyArticles" empty-description="暂无浏览记录"></UserHomeNavBarArticleList>
      <!--        分页组件-->
      <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentHistoryArticlesPage"
        v-model:page-size="historyArticlesPageSize" layout="sizes, prev, pager, next"
        :page-count="totalHistoryArticlesPage" :default-current-page="1"
        @update:page-size="onHistoryArticlesPageSizeChange" @update:current-page="onHistoryArticlesCurrentPageChange" />
    </el-tab-pane>
    <el-tab-pane label="关注列表" name="followTab" lazy>
      <UserFollowedList :user="followUsers" empty-description="暂无关注的用户"></UserFollowedList>
      <!--        分页组件-->
      <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentFollowersPage"
        v-model:page-size="followersPageSize" layout="sizes, prev, pager, next" :page-count="totalFollowersPage"
        :default-current-page="1" @update:page-size="onFollowersPageSizeChange"
        @update:current-page="onFollowersCurrentPageChange" />
    </el-tab-pane>
    <el-tab-pane label="粉丝列表" name="fansTab" lazy>
      <UserFollowedList :user="fans" empty-description="暂无粉丝"></UserFollowedList>
      <!--        分页组件-->
      <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentFansPage"
        v-model:page-size="fansPageSize" layout="sizes, prev, pager, next" :page-count="totalFansPage"
        :default-current-page="1" @update:page-size="onFansPageSizeChange"
        @update:current-page="onFansCurrentPageChange">
      </el-pagination>
    </el-tab-pane>
    <el-tab-pane label="收藏" name="starsTab" lazy>
      <UserHomeNavBarArticleList :articles="starsArticles" empty-description="暂无收藏的文章"></UserHomeNavBarArticleList>
      <!--        分页组件-->
      <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentStarArticlesPage"
        v-model:page-size="starArticlesPageSize" layout="sizes, prev, pager, next" :page-count="totalStarArticlesPage"
        :default-current-page="1" @update:page-size="onStarArticlesPageSizeChange"
        @update:current-page="onStarArticlesCurrentPageChange" />
    </el-tab-pane>
  </el-tabs>

</template>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import UserHomeNavBarArticleList from '@/views/user/nav-bar/UserHomeNavBarArticleList.vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import {
  USER_ARTICLE_LIST_URL,
  USER_FANS_LIST_URL,
  USER_FOLLOW_LIST_URL,
  USER_HISTORY_LIST_URL,
  USER_STAR_LIST_URL
} from '@/http/URL'
import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { useGlobalStore } from '@/stores/global'
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import UserFollowedList from '@/views/user/UserFollowedList.vue'
const route = useRoute()
const router = useRouter()
const globalStore = useGlobalStore()
const global = globalStore.global

const activeName = ref(route.params.typeName || 'articlesTab')

watch(() => route.params.userId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    currentArticlesPage.value = 1
    currentHistoryArticlesPage.value = 1
    currentStarArticlesPage.value = 1
    currentFollowersPage.value = 1
    currentFansPage.value = 1
    if (activeName.value == 'articlesTab') {
      getArticles()
    } else if (activeName.value == 'historyTab') {
      getHistoryArticles()
    } else if (activeName.value == 'starsTab') {
      getStarsArticles()
    } else if (activeName.value == 'followTab') {
      getFollowUsers()
    } else if (activeName.value == 'fansTab') {
      getFans()
    }
  }
})

// 用户发表的文章的列表
const currentArticlesPage = ref(1)
const totalArticlesPage = ref(0)
const articlesPageSize = ref(10)

const articles = ref<BasicPageType<ArticleType>>({ ...defaultBasicPage })
const getArticles = () => {
  doGet<CommonResponse>(USER_ARTICLE_LIST_URL, {
    userId: route.params.userId,
    currentPage: currentArticlesPage.value,
    pageSize: articlesPageSize.value
  })
    .then((res) => {
      Object.assign(articles.value, res.data.result)
      currentArticlesPage.value = Number(res.data.result.current)
      totalArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => {
      console.log(err)
    })
}
const onArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentArticlesPage.value = newCurrentPage
  getArticles()
}
const onArticlesPageSizeChange = (newPageSize: number) => {
  articlesPageSize.value = newPageSize
  getArticles()
}
// 用户浏览历史列表
const currentHistoryArticlesPage = ref(1)
const totalHistoryArticlesPage = ref(0)
const historyArticlesPageSize = ref(10)
const historyArticles = ref<BasicPageType<ArticleType>>({ ...defaultBasicPage })
const getHistoryArticles = () => {
  doGet<CommonResponse>(USER_HISTORY_LIST_URL, {
    userId: route.params.userId,
    currentPage: currentHistoryArticlesPage.value,
    pageSize: historyArticlesPageSize.value
  })
    .then((res) => {
      Object.assign(historyArticles.value, res.data.result)
      currentHistoryArticlesPage.value = Number(res.data.result.current)
      totalHistoryArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => {
      console.log(err)
    })
}
const onHistoryArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentHistoryArticlesPage.value = newCurrentPage
  getHistoryArticles()
}
const onHistoryArticlesPageSizeChange = (newPageSize: number) => {
  historyArticlesPageSize.value = newPageSize
  getHistoryArticles()
}
// 用户收藏列表
const currentStarArticlesPage = ref(1)
const totalStarArticlesPage = ref(0)
const starArticlesPageSize = ref(10)
const starsArticles = ref<BasicPageType<ArticleType>>({ ...defaultBasicPage })
const getStarsArticles = () => {
  doGet<CommonResponse>(USER_STAR_LIST_URL, {
    userId: route.params.userId,
    currentPage: currentStarArticlesPage.value,
    pageSize: starArticlesPageSize.value
  })
    .then((res) => {
      Object.assign(starsArticles.value, res.data.result)
      currentStarArticlesPage.value = Number(res.data.result.current)
      totalStarArticlesPage.value = Number(res.data.result.pages)
    })
    .catch((err) => {
      console.log(err)
    })
}
const onStarArticlesCurrentPageChange = (newCurrentPage: number) => {
  currentStarArticlesPage.value = newCurrentPage
  getStarsArticles()
}
const onStarArticlesPageSizeChange = (newPageSize: number) => {
  starArticlesPageSize.value = newPageSize
  getStarsArticles()
}

// 用户关注的用户列表
const currentFollowersPage = ref(1)
const totalFollowersPage = ref(0)
const followersPageSize = ref(10)
const followUsers = ref<BasicPageType<FollowUserInfoType>>({ ...defaultBasicPage })
const getFollowUsers = () => {
  doGet<CommonResponse>(USER_FOLLOW_LIST_URL, {
    userId: route.params.userId,
    currentPage: currentFollowersPage.value,
    pageSize: followersPageSize.value
  })
    .then((res) => {
      console.log(res.data.result)
      Object.assign(followUsers.value, res.data.result)
      currentFollowersPage.value = Number(res.data.result.current)
      totalFollowersPage.value = Number(res.data.result.pages)
    })
    .catch((err) => {
      console.log(err)
    })
}

const onFollowersCurrentPageChange = (newCurrentPage: number) => {
  currentFollowersPage.value = newCurrentPage
  getFollowUsers()
}
const onFollowersPageSizeChange = (newPageSize: number) => {
  followersPageSize.value = newPageSize
  getFollowUsers()
}

// 用户的粉丝列表
const currentFansPage = ref(1)
const totalFansPage = ref(0)
const fansPageSize = ref(10)
const fans = ref<BasicPageType<FollowUserInfoType>>({ ...defaultBasicPage })
const getFans = () => {
  doGet<CommonResponse>(USER_FANS_LIST_URL, {
    userId: route.params.userId,
    currentPage: currentFansPage.value,
    pageSize: fansPageSize.value
  })
    .then((res) => {
      Object.assign(fans.value, res.data.result)
      currentFansPage.value = Number(res.data.result.current)
      totalFansPage.value = Number(res.data.result.pages)
    })
    .catch((err) => {
      console.log(err)
    })
}

const onFansCurrentPageChange = (newCurrentPage: number) => {
  currentFansPage.value = newCurrentPage
  getFans()
}

const onFansPageSizeChange = (newPageSize: number) => {
  fansPageSize.value = newPageSize
  getFans()
}

const handleClick = (tab: TabsPaneContext, event: Event) => {
  // console.log(tab, event)
}

const handleChange = (val: string) => {
  if (val == 'articlesTab') {
    router.push(`/user/${route.params.userId}/articlesTab`)
    getArticles()
  } else if (val == 'historyTab') {
    router.push(`/user/${route.params.userId}/historyTab`)
    getHistoryArticles()
  } else if (val == 'starsTab') {
    router.push(`/user/${route.params.userId}/starsTab`)
    getStarsArticles()
  } else if (val == 'followTab') {
    router.push(`/user/${route.params.userId}/followTab`)
    getFollowUsers()
  } else if (val == 'fansTab') {
    router.push(`/user/${route.params.userId}/fansTab`)
    getFans()
  }
}

// 用户发表的文章的列表
onMounted(() => {
  if (activeName.value === 'articlesTab') {
    getArticles()
  } else if (activeName.value === 'historyTab') {
    getHistoryArticles()
  } else if (activeName.value === 'starsTab') {
    getStarsArticles()
  } else if (activeName.value === 'followTab') {
    getFollowUsers()
  } else if (activeName.value === 'fansTab') {
    getFans()
  }

})

</script>

<style>
.demo-tabs {
  --el-border-radius-base: 16px;
}

.demo-tabs > .el-tabs__header {
  margin: 0 0 12px;
}

.demo-tabs .el-tabs__nav-wrap::after {
  height: 0;
}

.demo-tabs .el-tabs__nav {
  border: 1px solid var(--pai-neu-border);
  background: var(--pai-neu-surface);
  box-shadow: var(--pai-neu-shadow-out);
  border-radius: 999px;
  padding: 6px;
  gap: 6px;
}

.demo-tabs .el-tabs__item {
  border-radius: 999px;
  padding: 0 18px;
  height: 36px;
  line-height: 36px;
  font-size: 14px;
  color: var(--pai-color-4-gray);
  transition: all 0.2s ease;
}

.demo-tabs .el-tabs__item.is-active {
  color: var(--pai-color-3-black);
  background: var(--pai-neu-surface-soft);
  box-shadow: var(--pai-neu-shadow-inset);
}

.demo-tabs .el-tabs__content {
  padding: 8px 4px 0;
  color: #6b778c;
}

.demo-tabs .el-pagination {
  margin-top: 18px;
  justify-content: center;
}

html.dark .demo-tabs .el-tabs__nav {
  background: var(--pai-neu-surface);
  border-color: var(--pai-neu-border);
  box-shadow: var(--pai-neu-shadow-out);
}

html.dark .demo-tabs .el-tabs__item.is-active {
  background: var(--pai-neu-surface-soft);
  box-shadow: var(--pai-neu-shadow-inset);
}

html.dark .demo-tabs .el-tabs__content {
  color: var(--pai-color-4-gray);
}

html.dark .demo-tabs .el-pagination button,
html.dark .demo-tabs .el-pager li {
  background-color: var(--pai-neu-surface-soft);
  color: var(--pai-color-3-black);
}

@media (max-width: 768px) {
  .demo-tabs > .el-tabs__header {
    margin: 0 0 10px;
  }

  .demo-tabs .el-tabs__nav-wrap {
    overflow-x: auto;
    scrollbar-width: none;
  }

  .demo-tabs .el-tabs__nav-wrap::-webkit-scrollbar {
    display: none;
  }

  .demo-tabs .el-tabs__nav-scroll {
    width: max-content;
  }

  .demo-tabs .el-tabs__nav {
    width: max-content;
    flex-wrap: nowrap;
    padding: 4px;
    gap: 4px;
  }

  .demo-tabs .el-tabs__item {
    height: 32px;
    line-height: 32px;
    padding: 0 14px;
    font-size: 13px;
  }

  .demo-tabs .el-tabs__content {
    padding: 6px 0 0;
  }

  .demo-tabs .el-pagination {
    margin-top: 14px;
    flex-wrap: wrap;
    row-gap: 8px;
  }
}
</style>
