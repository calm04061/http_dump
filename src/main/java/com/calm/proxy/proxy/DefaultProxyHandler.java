package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.RecordDataTransHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.recode.HandlerRecode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;


public class DefaultProxyHandler implements ProxyHandler, Ordered {
    Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DefaultProxyHandler.class);
    @Resource
    private ObjectProvider<HandlerRecode> handlerRecodes;

    @Override
    public boolean isSupport(FullHttpRequest request) {
        return true;
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        String s = headers.get(AUTH_HEADER);
        LOGGER.info("X-Eng-Auth:{}", s);
        //创建客户端连接目标机器
        ByteBuf content = request.content();
        String requestBody = content.toString(StandardCharsets.UTF_8);
        String uri = request.uri();
        HttpMethod method = request.method();
        ChannelFuture channelFuture = connectToRemote(ctx, URI.create(request.uri()).getHost(), 80, 10000, new RecordDataTransHandler(handlerRecodes, ctx.channel()));
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers, method, uri, requestBody));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
