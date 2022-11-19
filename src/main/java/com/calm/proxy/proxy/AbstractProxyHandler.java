package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.ResponseDataHandler;
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

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

public abstract class AbstractProxyHandler implements ProxyHandler {
    public final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(getClass());
    private ObjectProvider<HandlerRecode> handlerRecodes;

    @Override
    public void doHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("handle:{} {}", request.method(), request.uri());

        ByteBuf content = request.content();
        String requestBody = content.toString(StandardCharsets.UTF_8);
        String uri = request.uri();
        HttpMethod method = request.method();
        //创建客户端连接目标机器
        ChannelFuture channelFuture = connectToRemote(ctx, request, 10000, new ResponseDataHandler(handlerRecodes));
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers, method, uri, requestBody));
    }

    @Resource
    public void setHandlerRecodes(ObjectProvider<HandlerRecode> handlerRecodes) {
        this.handlerRecodes = handlerRecodes;
    }

    public ObjectProvider<HandlerRecode> getHandlerRecodes() {
        return handlerRecodes;
    }
}
