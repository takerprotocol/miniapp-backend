package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.dao.entity.*;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import lombok.SneakyThrows;

import java.util.List;

/**
 * 用户接口适配器，事务专用
 * 
 * @author PeterWong
 * @date 2024年7月19日
 */
public interface UserServiceAdaptor {
	/**
	 * 批量插入用户相关数据
	 */
	void batchSaveUserInfoAndOthers(BoolUser boolUser, BoolUserInvitationRelation relation, BoolUserRewardRecord registerReward, List<BoolUserRewardRecord> inviteRewardList);

	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	void batchUpdateUserReward(List<BoolUserRewardRecord> totalRewardList, Long maxRewardId, Boolean isFull);

	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	void batchSaveUserFullInviteCount(List<BoolUserInviteCountSnapshot> snapList, Long maxInviteId,
									  Boolean isFull);

	void updateUsername(String username, String userId);

	void batchSaveKeyInfoAndUpdateUser(List<BoolUser> boolUserList, List<BoolUserPrivateKeyFragmentInfo> keyFragmentInfoList);

	void batchSaveWeekSettleResult(List<BoolUserInviteCountWeekSettlementFlow> settleList);

	void batchSaveUserWeekInviteCount(List<BoolUserInviteCountWeekSnapshot> snapList);
}
