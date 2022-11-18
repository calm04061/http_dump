package com.calm.proxy.service;

import com.calm.proxy.entity.UserPlanInfo;
import com.calm.proxy.repository.UserPlanInfoRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class UserPlanInfoService {
    @Resource
    private UserPlanInfoRepository userPlanInfoRepository;

    public Optional<UserPlanInfo> findByOriginPlanId(String uid, String planId) {
        Optional<UserPlanInfo> userPlanInfoByPlanId = userPlanInfoRepository.getUserPlanInfoByPlanId(planId);
        if (userPlanInfoByPlanId.isPresent()) {
            UserPlanInfo userPlanInfo = userPlanInfoByPlanId.get();
            return userPlanInfoRepository.getUserPlanInfoByUidAndBelongIdAndBelongType(uid, userPlanInfo.getBelongId(), userPlanInfo.getBelongType());
        }
        return userPlanInfoByPlanId;
    }

    public void newPlan(String uid, String belongId, Long belongType, String planId) {
        List<UserPlanInfo> userPlanInfoByUid = userPlanInfoRepository.getUserPlanInfoByUid(uid);
        for (UserPlanInfo userPlanInfo : userPlanInfoByUid) {
            userPlanInfo.setStatus(2);
        }
        if (!userPlanInfoByUid.isEmpty()) {
            userPlanInfoRepository.saveAll(userPlanInfoByUid);
        }
        Optional<UserPlanInfo> userPlanInfoByUidAndBelongIdAndBelongType = userPlanInfoRepository.getUserPlanInfoByUidAndBelongIdAndBelongType(uid, belongId, belongType);
        UserPlanInfo userPlanInfo;
        if (userPlanInfoByUidAndBelongIdAndBelongType.isEmpty()) {
            userPlanInfo = new UserPlanInfo();
            userPlanInfo.setUid(uid);
            userPlanInfo.setBelongId(belongId);
            userPlanInfo.setBelongType(belongType);
            userPlanInfo.setPlanId(planId);
        } else {
            userPlanInfo = userPlanInfoByUidAndBelongIdAndBelongType.get();
        }
        userPlanInfo.setStatus(3);
        userPlanInfoRepository.save(userPlanInfo);
    }

    public void delete(UserPlanInfo userPlanInfo) {
        userPlanInfoRepository.delete(userPlanInfo);

    }
}
