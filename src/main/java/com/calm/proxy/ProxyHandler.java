package com.calm.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface ProxyHandler {
    AttributeKey<String> UID_KEY = AttributeKey.valueOf("UID_KEY");
    AttributeKey<String> ORIGIN_UID_KEY = AttributeKey.valueOf("ORIGIN_UID_KEY");
    AttributeKey<String> REQUEST_BODY_KEY = AttributeKey.valueOf("REQUEST_BODY_KEY");

    AttributeKey<String> REQUEST_URI_KEY = AttributeKey.valueOf("REQUEST_URI_KEY");

    AttributeKey<HttpMethod> REQUEST_METHOD_KEY = AttributeKey.valueOf("REQUEST_METHOD_KEY");
    String UID = "1234561";
    String AUTH_HEADER = "X-Eng-Auth";

    boolean isSupport(FullHttpRequest request);

    default void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws JsonProcessingException {
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
        if (preHandler(ctx, request, headers)) {
            doHandler(ctx, request, headers);
        }


    }

    void doHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers);


    default ChannelFuture connectToRemote(ChannelHandlerContext ctx, FullHttpRequest request, int timeout, ChannelInboundHandlerAdapter... next) {
        URI uri = URI.create(request.uri());
        int targetPort = uri.getPort();
        if (targetPort == -1) {
            String scheme = uri.getScheme().toLowerCase();
            if (scheme.equals("http")) {
                targetPort = 80;
            } else if (scheme.equals("https")) {
                targetPort = 443;
            } else {
                throw new UnsupportedOperationException("位置scheme:" + scheme);
            }
        }
        return new Bootstrap().group(ctx.channel().eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
//                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
//                InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8890);
//                pipeline.addLast(new HttpProxyHandler(inetSocketAddress));
                //增加http编码器
                pipeline.addLast(ChannelHandlerDefine.HTTP_CLIENT_CODEC, new HttpClientCodec());

                pipeline.addLast(new HttpContentDecompressor());
                pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 1024));

                //增加一个传输数据的通道
                for (ChannelInboundHandlerAdapter adapter : next) {
                    pipeline.addLast(adapter);
                }
            }
        }).connect(uri.getHost(), targetPort);
    }

    static DefaultFullHttpResponse getResponse(HttpResponseStatus statusCode, String message) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
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
        Map<String, String> stringListMap = parseKV(query);
        stringListMap.put(key, value);
        return toString(stringListMap);
    }

    static Map<String, String> parseKV(String kv) {
        Map<String, String> result = new HashMap<>();
        if (kv == null) {
            return result;
        }
        String[] split = kv.split("&");
        for (String row : split) {
            String[] split1 = row.split("=");
            String value = "";
            String key = split1[0];
            if (split1.length == 2) {
                result.put(key, split1[1]);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    static String toString(Map<String, String> valueMap) {
        StringJoiner joiner = new StringJoiner("&");
        List<String> collect = valueMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (String key : collect) {
            String value = valueMap.get(key);
            joiner.add(key + "=" + value);
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

    boolean preHandler(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) throws JsonProcessingException;
}
