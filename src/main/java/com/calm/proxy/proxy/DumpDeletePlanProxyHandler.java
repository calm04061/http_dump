package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.handler.DeletePlanHandler;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.repository.UserPlanInfoRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

//@Component
public class DumpDeletePlanProxyHandler implements ProxyHandler {
    private final UserPlanInfoRepository planInfoRepository;

    public DumpDeletePlanProxyHandler(UserPlanInfoRepository planInfoRepository) {
        this.planInfoRepository = planInfoRepository;
    }

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("plan/delete");
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, FullHttpRequest request, HttpHeaders headers) {
        LOGGER.info("dump request :{} {}", request.method(), request.uri());
        modifyUser(headers, UID);
        modifyRequestParameter(request, "plan_id", planInfoRepository.getUserPlanInfoByUid(UID).map(UserPlanInfo::getPlanId).orElse(""));
        //创建客户端连接目标机器
        ProxyHandler.connectToRemote(ctx, URI.create(request.uri()), 10000, new HttpContentDecompressor(), new HttpObjectAggregator(1000 * 1024 * 1024), new DeletePlanHandler(planInfoRepository, ctx.channel())).addListener(new AfterConnectionListener(ctx, request, headers));
    }

}
