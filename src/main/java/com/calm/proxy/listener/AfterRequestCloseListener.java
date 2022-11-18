package com.calm.proxy.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class AfterRequestCloseListener implements ChannelFutureListener {

    public AfterRequestCloseListener(ChannelHandlerContext ctx) {
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        if (channelFuture.isSuccess()) {
            //移除客户端的http编译码器
//            channelFuture.channel().pipeline().remove(ChannelHandlerDefine.HTTP_CLIENT_CODEC);
//            //移除代理服务和请求端 通道之间的http编译码器和集合器
//            try{
//                ctx.channel().pipeline().remove(ChannelHandlerDefine.HTTP_CODEC);
//            }catch (NoSuchElementException ignore){
//
//            }
//            try{
//                ctx.channel().pipeline().remove(ChannelHandlerDefine.HTTP_AGGREGATOR);
//            }catch (NoSuchElementException ignore){
//
//            }

            //移除后，让通道直接直接变成单纯的ByteBuf传输
        }
    }
}
