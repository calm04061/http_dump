package com.calm.proxy;


import com.calm.proxy.handler.CalmHttpProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication(scanBasePackages = {"com.calm.proxy", "com.calm.proxy.proxy"})
public class ProxyApplication implements CommandLineRunner {
    private final Logger LOGGER = LoggerFactory.getLogger(ProxyApplication.class);
    @Resource
    private ObjectProvider<ProxyHandler> handlerObjectProvider;
    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        int port = 8880;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();

                //Http编解码器
                pipeline.addLast(ChannelHandlerDefine.HTTP_CODEC, new HttpServerCodec());
                pipeline.addLast(ChannelHandlerDefine.HTTP_AGGREGATOR, new HttpObjectAggregator(100 * 1024 * 1024));
                //Http代理服务
                pipeline.addLast(ChannelHandlerDefine.HTTP_PROXY, new CalmHttpProxyHandler(handlerObjectProvider));
            }
        });
        ChannelFuture bindFuture = serverBootstrap.bind(port).sync();

        try {
            LOGGER.info("start at 0.0.0.0:{}", port);
            bindFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
