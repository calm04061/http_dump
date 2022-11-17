package com.calm.proxy.handler;

import com.alibaba.fastjson.JSONObject;
import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class DefaultProxyDataHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final Channel channel;
    private final FullHttpRequest request;
    private final UserPlanInfoRepository planInfoRepository;

    public DefaultProxyDataHandler(UserPlanInfoRepository planInfoRepository, FullHttpRequest request, Channel channel) {
        this.planInfoRepository = planInfoRepository;
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
//        LOGGER.info("{}", msg);

        if (!channel.isOpen()) {
            ReferenceCountUtil.release(response);
            return;
        }
        channel.writeAndFlush(response);

    }

}