package com.calm.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface ProxyHandler {
    AttributeKey<String> UID_ATTR_KAY = AttributeKey.newInstance("UID");
    AttributeKey<String> HANDLER_NAME_ATTR_KAY = AttributeKey.newInstance("HANDLER_NAME");

    AttributeKey<String> ORIGIN_UID_ATTR_KAY = AttributeKey.newInstance("ORIGIN_UID");
    AttributeKey<String> PLAN_ID_ATTR_KAY = AttributeKey.newInstance("PLAN_ID");
    String UID = "123456";
    String AUTH_HEADER = "X-Eng-Auth";
    Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ProxyHandler.class);

    boolean isSupport(FullHttpRequest request);

    default void preHandle(ChannelHandlerContext ctx, FullHttpRequest request) {

    }

    default void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        //从请求头中，获取目标地址
        //该请求头，有发送方和代理服务协商，或者使用常用请求头host
        HttpHeaders headers = request.headers();
        URI uri = URI.create(request.uri());
        String query = Optional.of(uri).map(URI::getRawQuery).map(e -> "?" + e).orElse("");
        request.setUri("http://recite.gray.perfectlingo.com" + uri.getPath() + query);
        //修改目标地址
//        headers.set("Host", "recite.gray.perfectlingo.com");
        headers.set("Test", "1");

//        ReferenceCountUtil.retain(request);
        doHandle(ctx, request, headers);

    }

    default void modifyUser(HttpHeaders headers, String uid) {
        String auth = headers.get(AUTH_HEADER);
        if (!StringUtils.hasText(auth)) {
            return;
        }
        String decode = URLDecoder.decode(auth, UTF_8);
        String result = modifyKV(decode, "u", uid);
        headers.set(AUTH_HEADER, result);
    }

    default String modifyKV(String query, String key, String value) {
        Map<String, List<String>> stringListMap = parseKV(query);
        List<String> strings = stringListMap.computeIfAbsent(key, k -> new ArrayList<>());
        strings.clear();
        strings.add(value);
        return toString(stringListMap);
    }

    static Map<String, List<String>> parseKV(String kv) {
        Map<String, List<String>> result = new HashMap<>();
        String[] split = kv.split("&");
        for (String row : split) {
            String[] split1 = row.split("=");
            String value = "";
            String key = split1[0];
            List<String> values = result.computeIfAbsent(key, k -> new ArrayList<>());
            if (split1.length == 2) {
                values.add(split1[1]);
            } else {
                values.add(value);
            }
        }
        return result;
    }

    static String toString(Map<String, List<String>> valueMap) {
        StringJoiner joiner = new StringJoiner("&");

        for (Map.Entry<String, List<String>> row : valueMap.entrySet()) {
            List<String> values = row.getValue();
            String key = row.getKey();
            for (String value : values) {
                joiner.add(key + "=" + value);
            }
        }
        return joiner.toString();
    }

    default void modifyRequestParameter(FullHttpRequest request, String key, String value) {
        String uri = request.uri();
        URI uri1 = URI.create(uri);
        String rawQuery = Optional.of(uri1).map(URI::getRawQuery).map(e -> modifyKV(e, key, value)).map(e -> "?" + e).orElse("");
        String targetUri = String.format("%s://%s%s%s", uri1.getScheme(), uri1.getHost(), uri1.getPath(), rawQuery);
        request.setUri(targetUri);
    }
    static ChannelFuture connectToRemote(ChannelHandlerContext ctx, URI url, int timeout, ChannelInboundHandlerAdapter... next) {
        String host = url.getHost();
        int port = 80;

        return new Bootstrap().group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
//                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        SocketAddress socketAddress = new InetSocketAddress("10.10.100.53", 8890);
//                        pipeline.addLast(new HttpProxyHandler(socketAddress));
                        //增加http编码器
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(6553600));
                        //增加一个传输数据的通道
                        for (ChannelInboundHandlerAdapter adapter : next) {
                            pipeline.addLast(adapter);
                        }
                    }
                }).connect(host, port);
    }

    static DefaultFullHttpResponse getResponse(HttpResponseStatus statusCode, String message) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
    }

    void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers);
}
