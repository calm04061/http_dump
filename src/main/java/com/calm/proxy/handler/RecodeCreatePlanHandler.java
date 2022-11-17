package com.calm.proxy.handler;

import com.alibaba.fastjson.JSONObject;
import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RecodeCreatePlanHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Logger LOGGER = LoggerFactory.getLogger(RecodeCreatePlanHandler.class);

    private final UserPlanInfoRepository planIdRef;

    public RecodeCreatePlanHandler(UserPlanInfoRepository planIdRef) {
        this.planIdRef = planIdRef;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        ByteBuf content = msg.content();
        String body = content.toString(StandardCharsets.UTF_8);
        JSONObject jsonObject = JSONObject.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        String lastPlanId = data.getString("plan_id");
        String belongId = data.getString("belong_id");
        Long belongType = data.getLong("belong_type");
        String uid = getUid(ctx);
        Optional<UserPlanInfo> userPlanInfoByUid = planIdRef.getUserPlanInfoByUidAndBelongIdAndBelongType(uid, belongType, belongId);
        if (userPlanInfoByUid.isPresent()) {
            UserPlanInfo userPlanInfo = userPlanInfoByUid.get();
            userPlanInfo.setPlanId(lastPlanId);
            userPlanInfo.setStatus(3);
            planIdRef.saveAndFlush(userPlanInfo);
        } else {
            UserPlanInfo planInfo = new UserPlanInfo();
            planInfo.setPlanId(lastPlanId);
            planInfo.setUid(uid);
            planInfo.setBelongId(belongId);
            planInfo.setBelongType(belongType);
            planInfo.setStatus(3);
            planIdRef.save(planInfo);
        }
    }

    public String getUid(ChannelHandlerContext ctx) {
        return ctx.channel().attr(ProxyHandler.ORIGIN_UID_ATTR_KAY).get();
    }
}
