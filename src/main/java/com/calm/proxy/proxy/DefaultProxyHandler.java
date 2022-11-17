package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.RecordDataTransHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.springframework.core.Ordered;

import java.net.URI;


public class DefaultProxyHandler implements ProxyHandler, Ordered {
    Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DefaultProxyHandler.class);
    @Override
    public boolean isSupport(FullHttpRequest request) {
        return true;
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        String s = headers.get(AUTH_HEADER);
        LOGGER.info("X-Eng-Auth:{}", s);
        //创建客户端连接目标机器
        ChannelFuture channelFuture = connectToRemote(ctx, URI.create(request.uri()).getHost(), 80, 10000, new RecordDataTransHandler(ctx.channel(), request.uri(), request.method()));
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
