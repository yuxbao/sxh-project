package com.github.sxh.forum.web.controller.login.wx.helper;

import com.github.sxh.forum.api.model.vo.user.wx.BaseWxMsgResVo;
import com.github.sxh.forum.api.model.vo.user.wx.WxImgTxtItemVo;
import com.github.sxh.forum.api.model.vo.user.wx.WxImgTxtMsgResVo;
import com.github.sxh.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.sxh.forum.core.util.CodeGenerateUtil;
import com.github.sxh.forum.service.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Component
public class WxAckHelper {
    @Autowired
    private LoginService sessionService;
    @Autowired
    private WxLoginHelper qrLoginHelper;

    /**
     * 返回自动响应的文本
     *
     * @return
     */
    public BaseWxMsgResVo buildResponseBody(String eventType, String content, String fromUser) {
        // 返回的文本消息
        String textRes = null;
        // 返回的是图文消息
        List<WxImgTxtItemVo> imgTxtList = null;
        if ("subscribe".equalsIgnoreCase(eventType)) {
            // 订阅
            textRes = "感谢优秀的你关注~~本公众号代码纪元主要记录编程知识分享，希望你能和我一起进步呀！\n"+
            "欢迎关注思享汇网站，这是一个专注于知识分享的社区项目\n" +
                    "链接：https://baoprojects.site";
        }
        // 下面是回复图文消息
        else if ("加群".equalsIgnoreCase(content)) {
            WxImgTxtItemVo imgTxt = new WxImgTxtItemVo();
//            imgTxt.setTitle("扫码加群");
//            imgTxt.setDescription("加入技术派的技术交流群，卷起来！");
//            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLGOyAuBLN76icGMb2LD1a7hBCoialjicOMsicvdsCovZq2ib1utmffHLjVlcyAX2UTmHoslvicK4Mg71Kyw/0?wx_fmt=jpeg");
//            imgTxt.setUrl("https://mp.weixin.qq.com/s/aY5lkyKjLHWSUuEf1UT2yQ");
//            imgTxtList = Arrays.asList(imgTxt);
        } else if ("admin".equalsIgnoreCase(content) || "后台".equals(content) || "002".equals(content)) {
            // admin后台登录，返回对应的用户名 + 密码
            textRes = "思享汇后台游客登录账号\n-----------\n登录用户名: guest\n登录密码: 123456";
        } else if ("商务合作".equalsIgnoreCase(content)) {
            textRes = "商务合作（假的）：添加我qq备注\"小鲍项目\"'";
        }
        // 微信公众号登录
        else if (CodeGenerateUtil.isVerifyCode(content)) {
            sessionService.autoRegisterWxUserInfo(fromUser);
            if (qrLoginHelper.login(content)) {
                textRes = "登录成功，开始愉快的玩耍思享汇吧！";
            } else {
                textRes = "验证码过期了，刷新登录页面重试一下吧";
            }
        } else {
            textRes = "/:? 还向了解更多🐴\n" +
                    "\n" +
                    "[机智] 添加我的qq 522425561，一起交流学习经验";
        }

        if (textRes != null) {
            WxTxtMsgResVo vo = new WxTxtMsgResVo();
            vo.setContent(textRes);
            return vo;
        } else {
            WxImgTxtMsgResVo vo = new WxImgTxtMsgResVo();
            vo.setArticles(imgTxtList);
            vo.setArticleCount(imgTxtList.size());
            return vo;
        }
    }
}
