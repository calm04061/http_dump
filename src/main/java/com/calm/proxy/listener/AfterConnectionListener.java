package com.calm.proxy.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import static com.calm.proxy.ProxyHandler.getResponse;

public class AfterConnectionListener implements ChannelFutureListener{
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final HttpHeaders headers;

    public AfterConnectionListener(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        this.ctx = ctx;
        this.request = request;
        this.headers = headers;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            //代理服务器连接目标服务器成功
            //发送消息到目标服务器
            //关闭长连接
            headers.set(HttpHeaderNames.CONNECTION, "close");

            //转发请求到目标服务器
            channelFuture.channel().writeAndFlush(request).addListener(new AfterRequestCloseListener(ctx));
        } else {
            ReferenceCountUtil.retain(request);
            ctx.writeAndFlush(getResponse(HttpResponseStatus.BAD_REQUEST, "代理服务连接远程服务失败"))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }
}
