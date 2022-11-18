package com.calm.proxy.recode;

import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.repository.UserPlanInfoRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Optional;

import static com.calm.proxy.ProxyHandler.parseKV;

@Component
public class DeletePlanRecodeHandler implements HandlerRecode {

    @Resource
    private UserPlanInfoRepository userPlanInfoRepository;

    @Override
    public boolean support(Response response) {
        String uri = response.getUri();
        if (!uri.contains("plan")) {
            return false;
        }
        return uri.contains("delete");

    }

    @Override
    public void handle(Response response) {
        String uri = response.getUri();
        String rawQuery = URI.create(uri).getRawQuery();
        String planId = parseKV(rawQuery).get("plan_id");

        Optional<UserPlanInfo> userPlanInfoByPlanId = userPlanInfoRepository.getUserPlanInfoByPlanId(planId);
        if (userPlanInfoByPlanId.isPresent()){
            userPlanInfoByPlanId.ifPresent(userPlanInfoRepository::delete);
        }
    }
}
