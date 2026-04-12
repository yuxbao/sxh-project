package com.github.sxh.forum.web.mq.comsumer;

import com.github.sxh.forum.service.comment.repository.entity.CommentDO;
import com.github.sxh.forum.service.user.repository.entity.UserFootDO;
import com.github.sxh.forum.service.user.repository.entity.UserRelationDO;

public interface MessageQueueUserActivityConsumer {

    void commentAndReply(CommentDO commentDO);

    void collect(UserFootDO footDO);

    void cancelCollect(UserFootDO footDO);

    void praise(UserFootDO footDO);

    void cancelPraise(UserFootDO footDO);

    void follow(UserRelationDO userRelationDO);

    void cancelFollow(UserRelationDO userRelationDO);
}
