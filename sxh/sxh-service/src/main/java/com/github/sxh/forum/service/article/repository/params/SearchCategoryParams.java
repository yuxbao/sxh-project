package com.github.sxh.forum.service.article.repository.params;

import com.github.sxh.forum.api.model.vo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchCategoryParams extends PageParam {
    // 类目名称
    private String category;
}
