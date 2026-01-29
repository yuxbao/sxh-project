package com.github.paicoding.forum.web.controller.home;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.sidebar.service.SidebarService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.controller.home.helper.IndexRecommendHelper;
import com.github.paicoding.forum.web.controller.home.vo.IndexVo;
import com.github.paicoding.forum.web.global.BaseViewController;
import com.github.paicoding.forum.web.global.vo.ResultVo;
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


/**
 * @author YiHui
 * @date 2022/7/6
 */
@Controller
public class IndexController extends BaseViewController {
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
        List<CategoryDTO> categories = categoryService.loadAllCategories();
        Map<Long, Long> articleCnt = articleReadService.queryArticleCountsByCategory();
        categories.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);

        CategoryDTO selectedCategory = categories.stream()
                .filter(c -> c.getCategory().equals(category))
                .findFirst()
                .orElse(CategoryDTO.DEFAULT_CATEGORY);

        IPage<ArticleDTO> articles = articleReadService.queryArticlesByCategoryPagination(pageNum, pageSize, category);

        List<ArticleDTO> topArticles = indexRecommendHelper.topArticleList(selectedCategory);
        List<SideBarDTO> sideBars = sidebarService.queryHomeSidebarList();

        IndexVo vo = new IndexVo();
        vo.setCategories(categories);
        vo.setCurrentCategory(selectedCategory.getCategory());
        vo.setCategoryId(selectedCategory.getCategoryId());
        vo.setTopArticles(topArticles);
        vo.setArticles(articles);
        vo.setSideBarItems(sideBars);
        vo.setHomeCarouselList(Collections.emptyList());

        if (ReqInfoContext.getReqInfo() != null && ReqInfoContext.getReqInfo().getUserId() != null) {
            UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(ReqInfoContext.getReqInfo().getUserId());
            vo.setUser(user);
        }

        return vo;
    }
}
