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

public class CreatePlanHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Logger LOGGER = LoggerFactory.getLogger(CreatePlanHandler.class);

    private final UserPlanInfoRepository planIdRef;

    public CreatePlanHandler(UserPlanInfoRepository planIdRef) {
        this.planIdRef = planIdRef;
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//
//        LOGGER.info("{} ->  {}", channel, msg);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        ByteBuf content = msg.content();
        String body = content.toString(StandardCharsets.UTF_8);
        JSONObject jsonObject = JSONObject.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        String lastPlanId = data.getString("plan_id");
        Optional<UserPlanInfo> userPlanInfoByUid = planIdRef.getUserPlanInfoByUid(ProxyHandler.UID);
        if (userPlanInfoByUid.isPresent()) {
            UserPlanInfo userPlanInfo = userPlanInfoByUid.get();
            userPlanInfo.setPlanId(lastPlanId);
            planIdRef.saveAndFlush(userPlanInfo);
        } else {
            UserPlanInfo planInfo = new UserPlanInfo();
            planInfo.setPlanId(lastPlanId);
            planInfo.setUid(ProxyHandler.UID);
            planIdRef.save(planInfo);
        }
    }
}
