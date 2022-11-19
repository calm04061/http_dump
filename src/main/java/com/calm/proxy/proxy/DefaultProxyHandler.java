package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.RecordDataTransHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DefaultProxyHandler extends AbstractProxyHandler implements ProxyHandler, Ordered {


    @Override
    public boolean isSupport(FullHttpRequest request) {
        return true;
    }

    @Override
    public boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        return true;
    }

    @Override
    public void doHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("handle:{} {}", request.method(), request.uri());

        ByteBuf content = request.content();
        String requestBody = content.toString(StandardCharsets.UTF_8);
        String uri = request.uri();
        HttpMethod method = request.method();
        //创建客户端连接目标机器
        ChannelFuture channelFuture = connectToRemote(ctx, request, 10000, new RecordDataTransHandler(getHandlerRecodes(), ctx.channel()));
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers, method, uri, requestBody));
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }


}
