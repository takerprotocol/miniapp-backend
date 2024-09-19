package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.dao.entity.*;
import com.abmatrix.bool.tg.dao.mapper.*;
import com.abmatrix.bool.tg.dao.service.IUserInvitationRelationService;
import com.abmatrix.bool.tg.dao.service.IUserPrivateKeyFragmentInfoService;
import com.abmatrix.bool.tg.dao.service.IUserRewardRecordService;
import com.abmatrix.bool.tg.dao.service.IUserService;
import com.abmatrix.bool.tg.service.UserServiceAdaptor;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事务接口适配器
 *
 * @author PeterWong
 * @date 2024年7月19日
 */
@Service
public class UserServiceAdaptorImpl implements UserServiceAdaptor {
	@Resource
	private IUserService userDao;
	@Resource
	private IUserRewardRecordService rewardRecordService;
	@Resource
	private IUserInvitationRelationService relationService;
	@Resource
	private IUserPrivateKeyFragmentInfoService fragmentInfoService;

	/**
	 * 计算用户排名奖励列表偏移量数据接口
	 */
	@Autowired
	private BoolCalculateRankOffsetMapper boolCalculateRankOffsetMapper;
	/**
	 * 用户mapper
	 */
	@Autowired
	private UserMapper userMapper;
	/**
	 * 周排行交割快照数据接口
	 */
	@Autowired
	private BoolUserInviteCountWeekSettlementFlowMapper boolUserInviteCountWeekSettlementFlowMapper;
	/**
	 * 批量插入周用户邀请数量快照
	 */
	@Autowired
	private BoolUserInviteCountWeekSnapshotMapper boolUserInviteCountWeekSnapshotMapper;
	/**
	 * 用户邀请数量快照mapper
	 */
	@Autowired
	private BoolUserInviteCountSnapshotMapper boolUserInviteCountSnapshotMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchSaveUserInfoAndOthers(BoolUser boolUser, BoolUserInvitationRelation relation,
										   BoolUserRewardRecord registerReward, List<BoolUserRewardRecord> inviteRewardList) {

		userDao.save(boolUser);
		relationService.save(relation);
		rewardRecordService.save(registerReward);
		rewardRecordService.saveBatch(inviteRewardList);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchSaveKeyInfoAndUpdateUser(List<BoolUser> boolUserList,
											  List<BoolUserPrivateKeyFragmentInfo> keyFragmentInfoList) {

		// 防止两边数据不一致
		if (boolUserList.size() > keyFragmentInfoList.size()) {
			List<Long> userIdList = keyFragmentInfoList.stream().map(BoolUserPrivateKeyFragmentInfo::getUserId).toList();
			boolUserList = boolUserList.stream().filter(x -> userIdList.contains(x.getId())).collect(Collectors.toList());
		} else if (boolUserList.size() < keyFragmentInfoList.size()) {
			List<Long> userIdList = boolUserList.stream().map(BoolUser::getId).toList();
			keyFragmentInfoList = keyFragmentInfoList.stream().filter(x -> userIdList.contains(x.getUserId())).toList();
		}

		userDao.updateBatchById(boolUserList);
		fragmentInfoService.saveBatch(keyFragmentInfoList);
	}

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchSaveWeekSettleResult(List<BoolUserInviteCountWeekSettlementFlow> settleList) {
		if (CollectionUtil.isEmpty(settleList)) {
			return;
		}
		for (BoolUserInviteCountWeekSettlementFlow settle : settleList) {
			LambdaQueryWrapper<BoolUserInviteCountWeekSettlementFlow> settleQuery = Wrappers.lambdaQuery();
			Long timeStamp = settleList.get(NumberConstants.ZERO).getCalculateTimestamp();
			settleQuery.eq(BoolUserInviteCountWeekSettlementFlow::getCalculateTimestamp, timeStamp);
			settleQuery.eq(BoolUserInviteCountWeekSettlementFlow::getUserId, settle.getUserId());
			settleQuery.select(BoolUserInviteCountWeekSettlementFlow::getId);
			BoolUserInviteCountWeekSettlementFlow existFlow = boolUserInviteCountWeekSettlementFlowMapper
					.selectOne(settleQuery, Boolean.FALSE);
			if (existFlow != null) {
				continue;
			}

			boolUserInviteCountWeekSettlementFlowMapper.insert(settle);
		}
	}

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchUpdateUserReward(List<BoolUserRewardRecord> totalRewardList, Long maxRewardId, Boolean isFull) {
		if (CollectionUtil.isEmpty(totalRewardList) || maxRewardId == null || maxRewardId <= NumberConstants.ZERO) {
			return;
		}
		LambdaUpdateWrapper<BoolCalculateRankOffset> offsetUpdate = Wrappers.lambdaUpdate();
		offsetUpdate.set(BoolCalculateRankOffset::getLatestRewardId, maxRewardId);
		offsetUpdate.eq(BoolCalculateRankOffset::getId, NumberConstants.ONE);
		boolCalculateRankOffsetMapper.update(offsetUpdate);
		List<List<BoolUserRewardRecord>> totalRewardListList = Lists.partition(totalRewardList, NumberConstants.THOUSAND);
		for (List<BoolUserRewardRecord> totalRewardChildList : totalRewardListList) {
			if (isFull) {
				userMapper.batchUpdateUserRewardForFull(totalRewardChildList);
			} else {
				userMapper.batchUpdateUserRewardForIncrease(totalRewardChildList);
			}
		}
	}

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchSaveUserFullInviteCount(List<BoolUserInviteCountSnapshot> snapList, Long maxInviteId,
											 Boolean isFull) {
		if (CollectionUtil.isEmpty(snapList) || maxInviteId == null || maxInviteId <= NumberConstants.ZERO) {
			return;
		}
		LambdaUpdateWrapper<BoolCalculateRankOffset> offsetUpdate = Wrappers.lambdaUpdate();
		offsetUpdate.set(BoolCalculateRankOffset::getLatestRewardId, maxInviteId);
		offsetUpdate.eq(BoolCalculateRankOffset::getId, NumberConstants.TWO);
		boolCalculateRankOffsetMapper.update(offsetUpdate);
		List<List<BoolUserInviteCountSnapshot>> snapListList = Lists.partition(snapList, NumberConstants.THOUSAND);
		for (List<BoolUserInviteCountSnapshot> childSnapList : snapListList) {
			if (isFull) {
				boolUserInviteCountSnapshotMapper.batchSaveUserInviteCountForFull(childSnapList);
			} else {
				boolUserInviteCountSnapshotMapper.batchSaveUserInviteCountForIncrease(childSnapList);
			}
		}
	}

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchSaveUserWeekInviteCount(List<BoolUserInviteCountWeekSnapshot> snapList) {
		if (CollectionUtil.isEmpty(snapList)) {
			return;
		}
		List<List<BoolUserInviteCountWeekSnapshot>> snapListList = Lists.partition(snapList, NumberConstants.THOUSAND);
		for (List<BoolUserInviteCountWeekSnapshot> childSnapList : snapListList) {
			boolUserInviteCountWeekSnapshotMapper.batchSaveInviteCountWeekSnapshotForFull(childSnapList);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUsername(String username, String userId) {
		userDao.update(Wrappers.<BoolUser>lambdaUpdate().eq(BoolUser::getId, userId).set(BoolUser::getUsername, username));
	}

}
