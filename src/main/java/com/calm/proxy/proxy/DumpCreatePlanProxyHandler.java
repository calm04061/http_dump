package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.handler.EmptyHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.springframework.util.StringUtils;

import java.net.URI;


public class DumpCreatePlanProxyHandler implements ProxyHandler {

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan");
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("handle:{} {}", request.method(), request.uri());

        modifyUser(headers, "123456");
        //创建客户端连接目标机器
        ChannelFuture channelFuture = connectToRemote(ctx, URI.create(request.uri()).getHost(), 80, 10000, new HttpObjectAggregator(1024 * 1024 * 1024), new EmptyHandler());
        channelFuture.addListener(new AfterConnectionListener(ctx, request, headers));
    }


}
