package com.github.sxh.forum.web.controller.home;

import com.github.sxh.forum.api.model.context.ReqInfoContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.sxh.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.sxh.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.sxh.forum.api.model.vo.recommend.SideBarDTO;
import com.github.sxh.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.sxh.forum.core.async.AsyncUtil;
import com.github.sxh.forum.service.article.service.ArticleReadService;
import com.github.sxh.forum.service.article.service.CategoryService;
import com.github.sxh.forum.service.sidebar.service.SidebarService;
import com.github.sxh.forum.service.user.service.UserService;
import com.github.sxh.forum.web.controller.home.helper.IndexRecommendHelper;
import com.github.sxh.forum.web.controller.home.vo.IndexVo;
import com.github.sxh.forum.web.global.BaseViewController;
import com.github.sxh.forum.web.global.vo.ResultVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


/**
 * @author YiHui
 * @date 2022/7/6
 */
@Controller
public class IndexController extends BaseViewController {
    private static final Executor ASYNC_EXECUTOR = AsyncUtil::execute;

    @Autowired
    private IndexRecommendHelper indexRecommendHelper;
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SidebarService sidebarService;
    @Autowired
    private UserService userService;

    @GetMapping(path = {"/", "", "/login"})
    public String index(Model model, HttpServletRequest request) {
        String activeTab = request.getParameter("category");
        IndexVo vo = buildIndexVo(activeTab, 1, 10);
        model.addAttribute("vo", vo);
        return "views/home/index";
    }

    /**
     * 首页初始化数据
     */
    @GetMapping(path = "/index")
    @ResponseBody
    public ResultVo<IndexVo> index(@RequestParam(name = "category", required = false) String category,
                                   @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ResultVo.ok(buildIndexVo(category, pageNum, pageSize));
    }

    private IndexVo buildIndexVo(String category, Integer pageNum, Integer pageSize) {
        Long userId = ReqInfoContext.getReqInfo() == null ? null : ReqInfoContext.getReqInfo().getUserId();

        CompletableFuture<List<CategoryDTO>> categoriesFuture = CompletableFuture.supplyAsync(categoryService::loadAllCategories, ASYNC_EXECUTOR);
        CompletableFuture<Map<Long, Long>> articleCntFuture = CompletableFuture.supplyAsync(articleReadService::queryArticleCountsByCategory, ASYNC_EXECUTOR);
        CompletableFuture<IPage<ArticleDTO>> articlesFuture = CompletableFuture.supplyAsync(
                () -> articleReadService.queryArticlesByCategoryPagination(pageNum, pageSize, category),
                ASYNC_EXECUTOR);
        CompletableFuture<List<SideBarDTO>> sideBarsFuture = CompletableFuture.supplyAsync(sidebarService::queryHomeSidebarList, ASYNC_EXECUTOR);
        CompletableFuture<UserStatisticInfoDTO> userFuture = userId == null
                ? CompletableFuture.completedFuture(null)
                : CompletableFuture.supplyAsync(() -> userService.queryUserInfoWithStatistic(userId), ASYNC_EXECUTOR);

        CompletableFuture<CategoryDTO> selectedCategoryFuture = categoriesFuture.thenCombine(articleCntFuture, (categories, articleCnt) -> {
            categories.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);
            return categories.stream()
                    .filter(c -> c.getCategory().equals(category))
                    .findFirst()
                    .orElse(CategoryDTO.DEFAULT_CATEGORY);
        });
        CompletableFuture<List<ArticleDTO>> topArticlesFuture = selectedCategoryFuture.thenApplyAsync(indexRecommendHelper::topArticleList, ASYNC_EXECUTOR);

        // 并行访问
        CompletableFuture.allOf(
                categoriesFuture,
                articlesFuture,
                sideBarsFuture,
                topArticlesFuture,
                userFuture
        ).join();

        IndexVo vo = new IndexVo();
        vo.setCategories(categoriesFuture.join());
        CategoryDTO selectedCategory = selectedCategoryFuture.join();
        vo.setCurrentCategory(selectedCategory.getCategory());
        vo.setCategoryId(selectedCategory.getCategoryId());
        vo.setTopArticles(topArticlesFuture.join());
        vo.setArticles(articlesFuture.join());
        vo.setSideBarItems(sideBarsFuture.join());
        vo.setHomeCarouselList(Collections.emptyList());
        vo.setUser(userFuture.join());

        return vo;
    }
}
