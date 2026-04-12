package com.github.sxh.forum.web.controller.rank;

import com.github.sxh.forum.api.model.enums.rank.ActivityRankTimeEnum;
import com.github.sxh.forum.api.model.vo.ResVo;
import com.github.sxh.forum.api.model.vo.rank.dto.RankInfoDTO;
import com.github.sxh.forum.api.model.vo.rank.dto.RankItemDTO;
import com.github.sxh.forum.service.rank.service.UserActivityRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排行榜接口
 *
 * @author XuYifei
 * @date 2026-01-29
 */
@RestController
@RequestMapping(path = "rank")
public class RankRestController {
    @Autowired
    private UserActivityRankService userActivityRankService;

    /**
     * 用户活跃度排行榜
     */
    @GetMapping(path = "{time}")
    public ResVo<RankInfoDTO> activityRank(@PathVariable(name = "time") String time,
                                           @RequestParam(name = "size", required = false, defaultValue = "30") Integer size) {
        ActivityRankTimeEnum rankTime = ActivityRankTimeEnum.nameOf(time);
        if (rankTime == null) {
            rankTime = ActivityRankTimeEnum.MONTH;
        }
        List<RankItemDTO> list = userActivityRankService.queryRankList(rankTime, size);
        RankInfoDTO info = new RankInfoDTO();
        info.setTime(rankTime);
        info.setItems(list);
        return ResVo.ok(info);
    }
}
