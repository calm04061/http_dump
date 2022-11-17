package com.calm.proxy.recode;

import com.alibaba.fastjson.JSONObject;
import com.calm.proxy.ProxyHandler;
import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.listener.AfterConnectionListener;
import com.calm.proxy.repository.UserPlanInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CreatePlanRecodeHandler implements HandlerRecode {
    Logger LOGGER = LoggerFactory.getLogger(CreatePlanRecodeHandler.class);

    @Resource
    private UserPlanInfoRepository userPlanInfoRepository;

    @Override
    public boolean support(Response response) {
        String uri = response.getUri();
        if (!uri.contains("plan")) {
            return false;
        }
        return uri.contains("create");

    }

    @Override
    public void handle(Response response) {
        JSONObject jsonObject = JSONObject.parseObject(response.getResponseBody());
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {
            LOGGER.error("{}", jsonObject.getString("err_msg"));
            return;
        }
        Map<String, List<String>> stringListMap = ProxyHandler.parseKV(response.getRequestBody());
        String belongType = stringListMap.get("belong_type").get(0);
        String belongId = stringListMap.get("belong_id").get(0);
        String planId = jsonObject.getJSONObject("data").getString("plan_id");
        Optional<UserPlanInfo> userPlanInfoByUidAndBelongIdAndBelongType = userPlanInfoRepository.getUserPlanInfoByUidAndBelongIdAndBelongType(response.getUid(), belongId, Long.parseLong(belongType));
        if (userPlanInfoByUidAndBelongIdAndBelongType.isEmpty()) {
            UserPlanInfo userPlanInfo = new UserPlanInfo();
            userPlanInfo.setUid(response.getUid());
            userPlanInfo.setBelongId(belongId);
            userPlanInfo.setBelongType(Long.parseLong(belongType));
            userPlanInfo.setPlanId(planId);
            userPlanInfo.setStatus(3);
            userPlanInfoRepository.save(userPlanInfo);
        }

    }
}