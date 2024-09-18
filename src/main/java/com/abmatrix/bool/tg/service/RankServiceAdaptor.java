package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountSnapshot;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSettlementFlow;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSnapshot;

import java.util.List;

/**
 * 排行榜接口适配器
 * 
 * @author PeterWong
 * @date 2024年9月3日
 */
public interface RankServiceAdaptor {
	/**
	 * 批量插入用户邀请数量数据
	 * 
	 * @param snapList
	 * @param maxInviteId
	 * @param isFull
	 */
	void batchSaveUserFullInviteCount(List<BoolUserInviteCountSnapshot> snapList, Long maxInviteId, Boolean isFull);

	/**
	 * 批量插入用户周邀请数量数据
	 * 
	 * @param snapList
	 */
	void batchSaveUserWeekInviteCount(List<BoolUserInviteCountWeekSnapshot> snapList);

	/**
	 * 批量插入用户周邀请奖励结算
	 *
	 * @param settleList
	 */
	void batchSaveWeekSettleResult(List<BoolUserInviteCountWeekSettlementFlow> settleList);

	/**
	 * 批量发放奖励
	 * 
	 * @param settleList
	 */
	void batchGrantWeekSettle(Long timeStamp, List<BoolUserInviteCountWeekSettlementFlow> settleList);
}
