<template>
  <HeaderBar />

  <div class="home article-detail density-relaxed" v-if="ifUsualArticle">
    <div class="col-body pg-2-article" id="article-detail-body-div">
      <div class="com-3-layout">
        <div class="layout-main">

          <!-- 正文 -->
          <ArticleDetail :global="global" :articleVo="articleVo"></ArticleDetail>

          <!--  评论  -->
          <div id="comment-list-wrapper">
            <CommentList :comments="articleVo.comments" :hot-comment="articleVo.hotComment"
              :article="articleVo.article"></CommentList>
          </div>

          <div class="correlation-article bg-color-white" id="relatedRecommend">
            <!-- 关联推荐 -->
            <div class="correlation-article-header">
              <h4 class="correlation-article-title">相关推荐</h4>
              <span v-if="relatedSideItem && relatedSideItem.subTitle" class="correlation-article-sub">
                {{ relatedSideItem.subTitle }}
              </span>
            </div>
            <div class="correlation-article-body bg-color-white">
              <div id="articleList" v-if="relatedArticles.length > 0">
                <ArticleList :articles="relatedArticles"></ArticleList>
              </div>
              <div
                v-else-if="relatedSideItem && relatedSideItem.items && relatedSideItem.items.length > 0"
                class="related-compact-list"
              >
                <a
                  v-for="(item, index) in relatedSideItem.items"
                  :key="item.url || index"
                  :href="item.url"
                  class="related-compact-item"
                >
                  <div class="related-compact-rank" :class="{'is-top': index < 3}">
                    {{ index + 1 }}
                  </div>
                  <div class="related-compact-content">
                    <div class="related-compact-title">{{ item.title }}</div>
                    <div class="related-compact-meta">
                      <span v-if="item.name">{{ item.name }}</span>
                      <span v-if="item.visit !== null && item.visit !== undefined">
                        🔥 {{ resolveVisit(item) }}
                      </span>
                    </div>
                  </div>
                </a>
              </div>
              <div v-else class="related-empty">
                暂无相关推荐
              </div>
            </div>
          </div>
        </div>

        <div class="layout-side hidden-when-screen-small flex-col flex">

          <!-- 用户相关信息 -->
          <UserCard :global="global" :user="articleVo.author"></UserCard>

          <!-- 活动推荐 -->
          <SideRecommendBar :sidebar-bar-items="sideBarItemsView"></SideRecommendBar>
          <div id="toc-container-position" class="hidden-when-screen-small"></div>
          <!-- 文章菜单  -->

          <div class="sticky top-5 overflow-auto" id="content-menu">
            <el-scrollbar>
              <em>文章目录</em>
              <el-divider></el-divider>
              <MdCatalog :editor-id="'id'" :scroll-element="scrollElement"></MdCatalog>
            </el-scrollbar>
          </div>

        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <Footer></Footer>
  </div>
  <div class="home article-detail article-loading" v-else-if="pageLoading">
    <el-skeleton animated :rows="8" />
  </div>
  <div class="home article-detail article-loading" v-else>
    <p>文章加载失败，请稍后重试。</p>
  </div>
  <LoginDialog :clicked="clicked"></LoginDialog>

</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { computed, onMounted, provide, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  type CommonResponse,
} from '@/http/ResponseTypes/CommonResponseType'
import { doGet } from '@/http/BackendRequests'
import { ARTICLE_DETAIL_URL, ARTICLE_RELATED_URL } from '@/http/URL'
import {
  type ArticleDetailResponse,
  defaultArticleDetailResponse
} from '@/http/ResponseTypes/ArticleDetailResponseType'
import ArticleDetail from '@/components/article/ArticleDetail.vue'
import { MdCatalog } from 'md-editor-v3'
import UserCard from '@/components/user/UserCard.vue'
import SideRecommendBar from '@/views/article-detail/SideRecommendBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { useGlobalStore } from '@/stores/global'
import CommentList from '@/views/article-detail/CommentList.vue'
import { setTitle } from '@/util/utils'
import ArticleList from '@/views/home/article/ArticleList.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { SideBarItem, SideBarVisit } from '@/http/ResponseTypes/SideBarItemType'

const route = useRoute()
// let global = reactive<GlobalResponse>({...defaultGlobalResponse})
let articleVo = reactive<ArticleDetailResponse>({ ...defaultArticleDetailResponse })
const relatedArticles = ref<ArticleType[]>([])
const globalStore = useGlobalStore()
const pageLoading = ref(true)

const global = globalStore.global
const articleId = ref(route.params.articleId)

const scrollElement = document.documentElement;

const clicked = ref(false)


const changeClicked = () => {
  clicked.value = !clicked.value
  console.log("clicked: ", clicked.value)
}

provide('loginDialogClicked', changeClicked)

//为了子组件评论时能够无缝更新，重新渲染，提供一个更新函数
const getArticleDetail = (response: ArticleDetailResponse) => {
  Object.assign(articleVo.comments, response.comments)
  console.log(articleVo)
}

provide('updateArticleComment', getArticleDetail)

const mapArticleToSideItem = (article: ArticleType) => ({
  title: article.title,
  name: article.authorName || null,
  url: `/article/detail/${article.articleId}`,
  img: article.cover || null,
  time: article.createTime || null,
  tags: article.tags ? article.tags.map((tag) => Number(tag.tagId)) : null,
  visit: article.count?.readCount ?? null,
})

const sideBarItemsView = computed<SideBarItem[]>(() => {
  const items = articleVo.sideBarItems || []
  if (!relatedArticles.value.length) return items
  return items.map((item) => {
    if (item.style !== 2) return item
    return {
      ...item,
      items: relatedArticles.value.map(mapArticleToSideItem),
    }
  })
})

const relatedSideItem = computed(() => {
  return sideBarItemsView.value.find((item) => item.style === 2) || null
})

const resolveVisit = (item: { visit: number | SideBarVisit | null | undefined }) => {
  if (item.visit === null || item.visit === undefined) {
    return '-'
  }
  if (typeof item.visit === 'number') {
    return item.visit
  }
  return item.visit.visit ?? '-'
}

const extractRelatedArticles = (response: any): ArticleType[] => {
  const result = response?.data?.result
  if (!result) return []
  if (Array.isArray(result)) return result
  if (Array.isArray(result.list)) return result.list
  if (Array.isArray(result.records)) return result.records
  if (Array.isArray(result.articleList)) return result.articleList
  return []
}

// ifUsualArticle 为true时，表示文章正常，为false时，表示文章是专栏文章，需要重定向，引入此变量从而避免在重定向过程中的闪烁情况
const ifUsualArticle = ref(false)
const router = useRouter()
const loadArticleDetail = () => {
  pageLoading.value = true
  ifUsualArticle.value = false
  relatedArticles.value = []
  doGet<CommonResponse>(ARTICLE_DETAIL_URL + `/${articleId.value}`, {})
    .then((response) => {
      console.log(response)
      if (!response.data.redirect) {
        globalStore.setGlobal(response.data.global)
        console.log("global: ", global)
        Object.assign(articleVo, response.data.result)
        setTitle(articleVo.article.title)
        ifUsualArticle.value = true
        pageLoading.value = false
        doGet<CommonResponse>(ARTICLE_RELATED_URL, { articleId: articleId.value })
          .then((recommendResponse) => {
            relatedArticles.value = extractRelatedArticles(recommendResponse)
          })
      } else {
        router.replace("/column/" + response.data.result.columnId + '/' + response.data.result.sectionId)
        pageLoading.value = false
      }
    })
    .catch(() => {
      pageLoading.value = false
    })
}

onMounted(async () => {
  loadArticleDetail()
})

watch(
  () => route.params.articleId,
  (nextId) => {
    if (!nextId) return
    articleId.value = nextId
    loadArticleDetail()
  }
)
</script>

<style scoped>
@media (max-width: 768px) {
  div.layout-side {
    display: none;
  }

  div.layout-main {
    padding: 12px;
  }
}

div#content-menu {
  height: calc(100vh - 70px);
}

.article-loading {
  padding: 24px;
}
</style>
