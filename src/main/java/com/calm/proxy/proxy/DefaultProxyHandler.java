package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.DataTransHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.core.Ordered;

import java.net.URI;


public class DefaultProxyHandler implements ProxyHandler, Ordered {

    @Override
    public boolean isSupport(FullHttpRequest request) {
        return true;
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        String s = headers.get("X-Eng-Auth");
        LOGGER.info("X-Eng-Auth:{}", s);
        //创建客户端连接目标机器
        connectToRemote(ctx, URI.create(request.uri()).getHost(), 80, 10000, new DataTransHandler(ctx.channel())).addListener(new AfterConnectionListener(ctx, request, headers));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
