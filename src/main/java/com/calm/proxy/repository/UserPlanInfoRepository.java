package com.calm.proxy.repository;

import com.calm.proxy.entity.UserPlanInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPlanInfoRepository extends JpaRepository<UserPlanInfo, Long> {
    Optional<UserPlanInfo> getUserPlanInfoByUid(String uid);

    Optional<UserPlanInfo> getUserPlanInfoByUidAndBelongIdAndBelongType(String uid, String belongId, Long belongType);

    Optional<UserPlanInfo> getUserPlanInfoByPlanId(String planId);
}
