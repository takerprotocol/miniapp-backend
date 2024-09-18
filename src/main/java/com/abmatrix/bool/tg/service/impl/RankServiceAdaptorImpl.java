package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.enuma.WeekSettleStatusEnum;
import com.abmatrix.bool.tg.dao.entity.BoolCaculateRankOffset;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountSnapshot;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSettlementFlow;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSnapshot;
import com.abmatrix.bool.tg.dao.mapper.BoolCaculateRankOffsetMapper;
import com.abmatrix.bool.tg.dao.mapper.BoolUserInviteCountSnapshotMapper;
import com.abmatrix.bool.tg.dao.mapper.BoolUserInviteCountWeekSettlementFlowMapper;
import com.abmatrix.bool.tg.dao.mapper.BoolUserInviteCountWeekSnapshotMapper;
import com.abmatrix.bool.tg.service.RankServiceAdaptor;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 
 * 
 * @author PeterWong
 * @date 2024年9月3日
 */
@Service
@Slf4j
public class RankServiceAdaptorImpl implements RankServiceAdaptor {

	/**
	 * 用户邀请数量快照mapper
	 */
	@Autowired
	private BoolUserInviteCountSnapshotMapper boolUserInviteCountSnapshotMapper;

	/**
	 * 批量插入周用户邀请数量快照
	 */
	@Autowired
	private BoolUserInviteCountWeekSnapshotMapper boolUserInviteCountWeekSnapshotMapper;
	/**
	 * 周排行交割快照数据接口
	 */
	@Autowired
	private BoolUserInviteCountWeekSettlementFlowMapper boolUserInviteCountWeekSettlementFlowMapper;
	/**
	 * 计算用户排名奖励列表偏移量数据接口
	 */
	@Autowired
	private BoolCaculateRankOffsetMapper boolCaculateRankOffsetMapper;

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchSaveUserFullInviteCount(List<BoolUserInviteCountSnapshot> snapList, Long maxInviteId,
			Boolean isFull) {
		if (CollectionUtil.isEmpty(snapList) || maxInviteId == null || maxInviteId <= NumberConstants.ZERO) {
			return;
		}
		LambdaUpdateWrapper<BoolCaculateRankOffset> offsetUpdate = Wrappers.lambdaUpdate();
		offsetUpdate.set(BoolCaculateRankOffset::getLatestRewardId, maxInviteId);
		offsetUpdate.eq(BoolCaculateRankOffset::getId, NumberConstants.TWO);
		boolCaculateRankOffsetMapper.update(offsetUpdate);
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
		List<List<BoolUserInviteCountWeekSnapshot>> snapListList = Lists.partition(snapList,
				NumberConstants.FIVE_THOUSAND);
		for (List<BoolUserInviteCountWeekSnapshot> childSnapList : snapListList) {
			boolUserInviteCountWeekSnapshotMapper.batchSaveInviteCountWeekSnapshotForFull(childSnapList);
		}
	}

	@Override
	@SneakyThrows
	@DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRES_NEW)
	public void batchSaveWeekSettleResult(List<BoolUserInviteCountWeekSettlementFlow> settleList) {
		if (CollectionUtil.isEmpty(settleList)) {
			return;
		}
		Long timeStamp = settleList.get(NumberConstants.ZERO).getCaculateTimestamp();
		LambdaQueryWrapper<BoolUserInviteCountWeekSettlementFlow> settleQuery = Wrappers.lambdaQuery();
		settleQuery.eq(BoolUserInviteCountWeekSettlementFlow::getCaculateTimestamp, timeStamp);
		settleQuery.select(BoolUserInviteCountWeekSettlementFlow::getId);
		List<BoolUserInviteCountWeekSettlementFlow> existFlowList = boolUserInviteCountWeekSettlementFlowMapper
				.selectList(settleQuery);
		if (CollectionUtil.isNotEmpty(existFlowList)) {
			log.warn("指定周已存在交割记录，请手动清理交割数据之后再进行交割清算[timeStamp={}]", timeStamp);
			return;
		}
		for (BoolUserInviteCountWeekSettlementFlow settle : settleList) {
			settleQuery = Wrappers.lambdaQuery();
			settleQuery.eq(BoolUserInviteCountWeekSettlementFlow::getCaculateTimestamp, timeStamp);
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
	public void batchGrantWeekSettle(Long timeStamp, List<BoolUserInviteCountWeekSettlementFlow> settleList) {
		if (CollectionUtil.isEmpty(settleList)) {
			return;
		}
		for (BoolUserInviteCountWeekSettlementFlow settle : settleList) {

			LambdaUpdateWrapper<BoolUserInviteCountWeekSettlementFlow> flowUpdate = Wrappers.lambdaUpdate();
			flowUpdate.set(BoolUserInviteCountWeekSettlementFlow::getStatus, WeekSettleStatusEnum.GRANTED);
			flowUpdate.eq(BoolUserInviteCountWeekSettlementFlow::getId, settle.getId());
			boolUserInviteCountWeekSettlementFlowMapper.update(flowUpdate);
		}
	}

}
