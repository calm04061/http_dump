package com.calm.proxy.handler;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.listener.AfterRequestCloseListener;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.ObjectProvider;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.calm.proxy.ProxyHandler.*;

public class CalmHttpProxyHandler extends CalmRequestSimpleChannelInboundHandler {
    private final ObjectProvider<ProxyHandler> handlerObjectProvider;
    private final UserPlanInfoRepository planInfoRepository;

    public CalmHttpProxyHandler(UserPlanInfoRepository planInfoRepository,ObjectProvider<ProxyHandler> handlerObjectProvider) {
        this.planInfoRepository = planInfoRepository;
        this.handlerObjectProvider = handlerObjectProvider;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
//        HttpHeaders headers = request.headers();
        //关闭长连接
        request.headers().set(HttpHeaderNames.CONNECTION, "close");
        request.headers().set(HttpHeaderNames.HOST, "recite.gray.perfectlingo.com");
//            String s = headers.get(AUTH_HEADER);
//            System.out.println(s);
        //转发请求到目标服务器
//            LOGGER.info("final uri={}", request.uri());
        URI uri = URI.create(request.uri());
        String query = Optional.of(uri).map(URI::getRawQuery).map(e -> "?" + e).orElse("");
        request.setUri(uri.getPath() + query);
        ChannelFuture channelFuture = connectToRemote(ctx, uri, 10000, new HttpContentDecompressor(), new HttpObjectAggregator(1000 * 1024 * 1024), new DefaultProxyDataHandler(planInfoRepository, request, ctx.channel()));
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (channelFuture.isSuccess()) {
                    //代理服务器连接目标服务器成功
                    //发送消息到目标服务器
                    channelFuture.channel().writeAndFlush(request).addListener(new AfterRequestCloseListener(ctx));
//            LOGGER.info("[{}]count:{}", request.hashCode(), request.refCnt());
                } else {
                    ReferenceCountUtil.retain(request);
                    ctx.writeAndFlush(getResponse(HttpResponseStatus.BAD_REQUEST, "代理服务连接远程服务失败")).addListener(ChannelFutureListener.CLOSE);
                }
            }
        });


//        List<ProxyHandler> collect = handlerObjectProvider.stream().collect(Collectors.toList());
//        Channel channel = ctx.channel();
//
//        for (ProxyHandler handler : collect) {
////            FullHttpRequest copy = request.duplicate();
////            LOGGER.info("[{}]count:{}", request.hashCode(), request.refCnt());
//            channel.attr(UID_ATTR_KAY).set(ProxyHandler.UID);
//            channel.attr(HANDLER_NAME_ATTR_KAY).set(handler.getClass().getName());
//            if (handler.isSupport(request)) {
//                handler.handle(ctx, request);
//            }
//        }
    }
}
