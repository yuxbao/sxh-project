<template>
  <HeaderBar></HeaderBar>
  <div class="home article-detail flex flex-col">
    <div class="col-body pg-2-article flex" id="article-detail-body-div">
      <div class="com-3-layout">
        <div class="self-info">

          <!-- 正文 -->
          <!-- 文章内容 -->
          <section class="article-info-wrap com-2-panel col-2-article J-articlePanel">
            <!-- 关联推荐 -->
            <h4 class="correlation-article-title">关于作者</h4>
            <el-divider></el-divider>
            <el-card class="mb-2">
              <template #header>基本信息</template>
              <template #default>
                <p class="flex m-2">
                  <span class="mr-3"><el-icon size="13">
                      <CaretRight />
                    </el-icon><b>作者：</b>鲍宇翔</span>
                  <span class="mr-3"><el-icon size="13">
                      <CaretRight />
                    </el-icon><b>性别：</b>♂</span>
                  <span class="mr-3"><el-icon size="13">
                      <CaretRight />
                    </el-icon><b>年龄：</b>22</span>
                </p>
                <p class="flex m-2">
                  <span class="mr-3"><el-icon size="13">
                      <CaretRight />
                    </el-icon><b>邮箱：</b>yux-bao@outlook.com</span>
                  <span class="mr-3"><el-icon size="13">
                      <CaretRight />
                    </el-icon><b>籍贯：</b>上海</span>
                </p>
              </template>
            </el-card>
            <el-card class="mb-2">
              <template #header>教育背景</template>
              <template #default>
                <div>
                  <h2 class="text-blue-400"> 本科：2022.09-2026.06 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>学校：</b>上海大学</span>
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>学院：</b>计算机工程与科学学院</span>
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>专业：</b>计算机科学与技术</span>
                  </p>
                </div>
                <div>
                  <h2 class="text-blue-400"> 研究生：2026.09 - 2027.06 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>学校：</b>香港大学</span>
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>学院：</b>数据与计算科学学院</span>
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>专业：</b>计算机科学</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>获奖/荣誉</template>
              <template #default>
                <div>
                  <!--                  奖项-->
                  <h2 class="text-blue-400"> 所获奖项 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon>2024蓝桥杯大赛<b>省三</b></span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon>2025全国大学生计算机设计大赛国家二等奖</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>技能专长</template>
              <template #default>
                <div>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>语言基础：</b>具备良好编码能力，能熟练应用常见的设计模式；熟悉<b>Java</b>基础知识、熟练使用集合等基础框架；掌握<b>Golang</b>基本使用</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>容器技术：</b>掌握<b>Kubernetes</b>的使用，对<b>Kubernetes存储体系与CSI接口规范</b>有深入了解</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>数据库：</b>熟练掌握<b>MySQL及基本原理</b>【锁机制、索引】；掌握<b>Redis及其底层原理</b>【底层数据结构、数据持久化机制】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>后端框架：</b>会使用SpringMVC、SpringBoot、MyBatis等框架</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>JVM：</b>初步理解JVM原理【类加载机制、内存结构、垃圾回收机制等】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>JUC：</b>熟悉Java多线程与并发底层原理【volatile、synchronized、ReentrantLock】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>MQ：</b>了解消息队列MQ，有使用RabbitMQ经验</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13">
                        <CaretRight />
                      </el-icon><b>其他：</b>有使用前端框架经验【Vue、React】，了解前后端分离开发模式；掌握版本控制工具Git、Docker的基本操作</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>实习经历</template>
              <template #default>
                <el-collapse>
                  <el-collapse-item title="上海寰通商务科技有限公司 后端开发实习生">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>实习时间：</b>2025.06 - 2025.09</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>核心工作：</b>参与 PharmDataLink 药厂数据取数项目与 beisen-sync-integration 人事数据同步平台开发，主要技术栈为 Spring Boot、WebService / REST、Elasticsearch、SQLite、Prometheus、Grafana</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;对接多家药企 ERP，按 SPI（销售 / 进货 / 库存）模型清洗并落地数百万级供应链数据；结合 Elasticsearch 优化销售流向等报表查询路径，将查询耗时从分钟级压缩到 10 秒，并支持一键导出 Excel / CSV 报告</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;基于 BaseCallApiService 封装北森 / 企业微信 API 调用链路，完成员工档案、假勤、加班等数据的周期性增量同步；设计 SQLite 日志表与按天 / 类型查询接口，支持异常恢复与审计追踪</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;引入 Spring Boot Actuator + Micrometer + Prometheus + Grafana 观测方案，将关键接口耗时与调用成功率等指标可视化为 Dashboard，提升运维定位问题的效率</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;在 WebService 与打单报表场景中，将大文件导出从 XSSFWorkbook 迁移为 SXSSFWorkbook 并完成参数调优，在保持导出能力的前提下将 JVM 峰值内存从近 60GB 降到约 4GB，解决 OOM 问题</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;与导师及北森厂商、人力资源部门、供应链业务同事协同梳理需求，参与接口与配置类设计（工厂模式 / 配置驱动），持续调优数据同步与报表生成逻辑</span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                  <el-collapse-item title="上海墨圆方信息科技 软件开发实习生">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>实习时间：</b>2024.06 - 2024.09</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>核心工作：</b>负责工业级 AI-CAD 工具的算法设计与实现，以及客户端更新系统开发</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;完成三维到二维的自动坐标转换，并实现 6 个技术视图下的尺寸标注自动排布，支撑工业图纸场景的自动化出图</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;开发基于 MD5 校验与版本控制的客户端更新系统，实现文件变更检测、批量替换、实时进度跟踪与界面集成，提升版本发布与升级效率</span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                </el-collapse>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>项目经验</template>
              <template #default>
                <el-collapse>
                  <el-collapse-item title="思享汇-知识乐园社区">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>项目时间：</b>2025.06 - 2025.12</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>技术栈：</b>SpringBoot、MySQL、Redis、RabbitMQ、Nginx、Docker</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>项目描述：</b>知识社区项目，完成公网部署、登录认证、消息通知、活跃度统计、缓存优化与异常报警等核心能力建设</span>
                        </p>
                        <p class="m-1">
                          <span><el-icon size="13">
                              <CaretRight />
                            </el-icon><b>核心工作：</b></span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;通过 Nginx + Docker + 自定义域名将项目部署到公网，并基于 Let's Encrypt 配置 HTTPS 证书；结合 SSEEmitter + LoadingCache 实现微信公众号 + 验证码一键登录</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;利用 AOP 切面与 RabbitMQ 构建异步消息通知链路，在评论、点赞等事件发生后将消息投递到队列，并由消费者写入 Redis 等待用户消费</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;使用 CompletableFuture 对首页接口并行化处理，将接口响应时间从 134ms 降至 80ms；通过 WebFilter + ThreadLocal 维护当前用户上下文，减少重复查询数据库</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;基于 Redis zset 实现用户活跃度与文章热度排行榜，通过幂等控制防止重复统计，并结合 Spring EventListener 触发活跃度更新</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11">
                              <InfoFilled />
                            </el-icon>&nbsp;&nbsp;针对文章标签等允许短暂不一致的数据采用 Cache Aside + TTL，结合 Spring Cache 与 Redis 优化加载速度，并整合 Spring Email 实现异常报警</span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                </el-collapse>
              </template>
            </el-card>
          </section>
        </div>

      </div>
      <!--   右侧的图片   -->
      <SideImage></SideImage>
    </div>

    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<style>
.home {
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: center;
  width: 100%;
}

.photos {
  max-width: 200px;
}

@media (max-width: 768px) {
  .photos {
    display: none;
  }
}

.self-info {
  max-width: 900px;
}
</style>
<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import { CaretRight, InfoFilled } from '@element-plus/icons-vue'
import SideImage from '@/components/layout/SideImage.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { onMounted, provide, ref } from 'vue'

import { useGlobalStore } from "@/stores/global";
import { doGet } from "@/http/BackendRequests";
import type { CommonResponse } from "@/http/ResponseTypes/CommonResponseType";
import { GLOBAL_INFO_URL } from "@/http/URL";

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

const globalStore = useGlobalStore()
// 获取登录信息
onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
    })
})
</script>
