package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PausePlanHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Logger LOGGER = LoggerFactory.getLogger(PausePlanHandler.class);

    private final Channel channel;
    private final UserPlanInfoRepository planInfoRepository;

    public PausePlanHandler(UserPlanInfoRepository planInfoRepository, Channel channel) {
        this.channel = channel;
        this.planInfoRepository = planInfoRepository;
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//
//        LOGGER.info("{} ->  {}", channel, msg);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        String uid = ctx.channel().attr(ProxyHandler.PLAN_ID_ATTR_KAY).get();
        Optional<UserPlanInfo> userPlanInfoByUid = planInfoRepository.getUserPlanInfoByPlanId(uid);
        if (userPlanInfoByUid.isPresent()) {
            UserPlanInfo planInfo = userPlanInfoByUid.get();
            planInfo.setStatus(3);
        }
    }
}
