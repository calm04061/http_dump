package com.calm.proxy.proxy;

import com.calm.proxy.ProxyHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

@Component
public class DumpCreatePlanProxyHandler extends AbstractDumpProxyHandler implements ProxyHandler {

    @Override
    public boolean isSupport(FullHttpRequest request) {
        String path = URI.create(request.uri()).getPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.contains("/plan") && path.contains("create");
    }

}
