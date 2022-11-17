package com.calm.proxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import java.nio.charset.StandardCharsets;

public class DumpCreatePlanHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        //修改http响应体返回至客户端
        msg.headers().add("test1111","from proxy");
        ByteBuf content = msg.content();
        String s = content.toString(StandardCharsets.UTF_8);

        System.out.println(s);
//        clientChannel.writeAndFlush(msg);
    }
}
