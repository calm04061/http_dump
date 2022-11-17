package com.calm.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.StringJoiner;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface ProxyHandler {
    Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ProxyHandler.class);
    boolean isSupport(FullHttpRequest request);

    default void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        //从请求头中，获取目标地址
        //该请求头，有发送方和代理服务协商，或者使用常用请求头host
        HttpHeaders headers = request.headers();
        URI uri = URI.create(request.uri());
        String query = Optional.of(uri).map(URI::getRawQuery).map(e -> "?" + e).orElse("");
        request.setUri("http://recite.gray.perfectlingo.com" + uri.getPath() + query);
        //修改目标地址
        headers.set("Host", "recite.gray.perfectlingo.com");
        headers.set("Test", "1");

        ReferenceCountUtil.retain(request);
        doHandle(ctx, request, headers);

    }

    default void modifyUser(HttpHeaders headers, String uid) {
        String auth = headers.get("X-Eng-Auth");
        if (!StringUtils.hasText(auth)) {
            return;
        }
        String decode = URLDecoder.decode(auth, UTF_8);
        String[] split = decode.split("&");
        StringJoiner joiner = new StringJoiner("&");
        for (String row : split) {
            String[] split1 = row.split("=");
            if (split1[0].equals("u")) {
                joiner.add("u=" + uid);
            } else {
                joiner.add(row);
            }
        }
        String s = joiner.toString();
        headers.set("X-Eng-Auth", URLEncoder.encode(s, UTF_8));


    }
    default ChannelFuture connectToRemote(ChannelHandlerContext ctx, String targetHost, int targetPort, int timeout, ChannelInboundHandlerAdapter... next) {
        return new Bootstrap().group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //增加http编码器
                        pipeline.addLast(ChannelHandlerDefine.HTTP_CLIENT_CODEC, new HttpClientCodec());

                        pipeline.addLast(new HttpContentDecompressor());
                        pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 1024));

                        //增加一个传输数据的通道
                        for (ChannelInboundHandlerAdapter adapter:next){
                            pipeline.addLast(adapter);
                        }
                    }
                })
                .connect(targetHost, targetPort);
    }
    static DefaultFullHttpResponse getResponse(HttpResponseStatus statusCode, String message) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
    }

    void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers);
}
