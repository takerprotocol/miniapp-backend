package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.common.enuma.InviteTimeType;
import com.abmatrix.bool.tg.common.enuma.PersonalInfoTypeEnum;
import com.abmatrix.bool.tg.common.enuma.RewardType;
import com.abmatrix.bool.tg.common.model.vo.InvitationRankVo;
import com.abmatrix.bool.tg.common.model.vo.UserInvitationRelationVo;
import com.abmatrix.bool.tg.common.model.vo.UserRankVo;
import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.abmatrix.bool.tg.dao.entity.BoolUserCustomInfo;
import com.abmatrix.bool.tg.dao.entity.BoolUserInvitationRelation;
import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.abmatrix.bool.tg.dao.mapper.*;
import com.abmatrix.bool.tg.dao.service.IUserInvitationRelationService;
import com.abmatrix.bool.tg.dao.service.IUserRewardRecordService;
import com.abmatrix.bool.tg.dao.service.IUserService;
import com.abmatrix.bool.tg.middleware.redis.clients.SimpleRedisClient;
import com.abmatrix.bool.tg.middleware.redis.clients.ZSetRedisClient;
import com.abmatrix.bool.tg.model.req.UserInvitersRankReq;
import com.abmatrix.bool.tg.model.resp.CustomPage;
import com.abmatrix.bool.tg.model.resp.UserInviteRankResp;
import com.abmatrix.bool.tg.model.resp.UserRankSimpleResp;
import com.abmatrix.bool.tg.service.InviteService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.abmatrix.bool.tg.utils.AmountUtil.formatAmount;

/**
 * 邀请接口
 * 
 * @author PeterWong
 * @date 2024年7月16日
 */
@Service
@Slf4j
public class InviteServiceImpl implements InviteService {
	/**
	 * 用户邀请关系数据接口
	 */
	@Autowired
	private UserInvitationRelationMapper userInvitationRelationMapper;
	/**
	 * 用户表mapper
	 */
	@Autowired
	private UserMapper userMapper;
	/**
	 * 邀请返利表mapper
	 */
	@Autowired
	private UserRewardRecordMapper userRewardRecordMapper;

	@Resource
	private IUserService userDao;
	@Resource
	private IUserInvitationRelationService userInvitationRelationService;
	@Resource
	private IUserRewardRecordService userRewardRecordService;
	/**
	 * 排行榜mapper
	 */
	@Autowired
	private RankMapper rankMapper;
	/**
	 * redis客户端
	 */
	@Autowired
	private SimpleRedisClient simpleRedisClient;
	/**
	 * 用户自定义信息mapper
	 */
	@Autowired
	private BoolUserCustomInfoMapper boolUserCustomInfoMapper;
	/**
	 * zset 缓存
	 */
	@Autowired
	private ZSetRedisClient zsetRedisClient;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Page<UserInvitationRelationVo> pageInviteRecords(Long userId, Integer pageNo, Integer pageSize,
			Integer level) {
		String key = StringUtils.join(RedisKeyConstants.INVITE_RECORD_KEY, userId, "-", pageNo, "-", pageSize, "-",
				level);
		Object resultObj = null;
		try {
			resultObj = simpleRedisClient.get(key);
		} catch (Exception e) {
		}
		if (resultObj != null) {
			String resultJson = JSONObject.toJSONString(resultObj);
			return JSON.parseObject(resultJson, Page.class);
		}
		Page<BoolUserInvitationRelation> pageResult;
		Page pageQuery = Page.of(pageNo, pageSize);
		if (level == NumberConstants.ONE) {
			LambdaQueryWrapper<BoolUserInvitationRelation> query = Wrappers.lambdaQuery();
			query.eq(BoolUserInvitationRelation::getInviterId, userId);
			query.orderByDesc(BoolUserInvitationRelation::getInvitationTime);
			pageResult = userInvitationRelationMapper.selectPage(pageQuery, query);
			if (pageResult == null || CollectionUtil.isEmpty(pageResult.getRecords())) {
				try {
					simpleRedisClient.set(key, pageQuery, 15, TimeUnit.SECONDS);
				} catch (Exception e) {
				}
				return pageQuery;
			}
		} else {
			Integer offset = PageUtil.getStart(pageNo - 1, pageSize);
			List<BoolUserInvitationRelation> relationList = userInvitationRelationMapper
					.queryLevel2InvitationRelations(userId, offset, pageSize);
			if (CollectionUtil.isEmpty(relationList)) {
				try {
					simpleRedisClient.set(key, pageQuery, 15, TimeUnit.SECONDS);
				} catch (Exception e) {
				}
				return pageQuery;
			}
			Integer count = userInvitationRelationMapper.queryLevel2InvitationRelationsCount(userId);
			pageResult = new Page<BoolUserInvitationRelation>();
			pageResult.setRecords(relationList);
			pageResult.setTotal(count);
			pageResult.setCurrent(pageNo);
			pageResult.setSize(pageSize);
		}
		List<BoolUserInvitationRelation> records = pageResult.getRecords();
		Set<Long> inviteeUserSet = records.stream().filter(record -> record != null && record.getInviteeId() != null)
				.map(BoolUserInvitationRelation::getInviteeId).collect(Collectors.toSet());
		Set<Long> inviterUserSet = records.stream().filter(record -> record != null && record.getInviterId() != null)
				.map(BoolUserInvitationRelation::getInviterId).collect(Collectors.toSet());
		Map<Long, BoolUser> userMap = Maps.newHashMap();
		Map<Long, Set<BoolUserRewardRecord>> rewardMap = Maps.newHashMap();
		if (CollectionUtil.isNotEmpty(inviteeUserSet)) {
			LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
			userQuery.in(BoolUser::getId, inviteeUserSet);
			List<BoolUser> boolUsers = userMapper.selectList(userQuery);
			if (CollectionUtil.isNotEmpty(boolUsers)) {
				for (BoolUser boolUser : boolUsers) {
					userMap.put(boolUser.getId(), boolUser);
				}
			}
			List<BoolUserRewardRecord> rewards = Lists.newArrayList();
			if (level == NumberConstants.ONE) {
				LambdaQueryWrapper<BoolUserRewardRecord> rewardQuery = Wrappers.lambdaQuery();
				rewardQuery.eq(BoolUserRewardRecord::getType, RewardType.INVITATION);
				rewardQuery.select(BoolUserRewardRecord::getId, BoolUserRewardRecord::getRewardValue,
						BoolUserRewardRecord::getUserId, BoolUserRewardRecord::getAdditionInfo);
				rewardQuery.eq(BoolUserRewardRecord::getUserId, userId);
				rewards = userRewardRecordMapper.selectList(rewardQuery);
			} else {
				if (CollectionUtil.isNotEmpty(inviterUserSet)) {
					LambdaQueryWrapper<BoolUserRewardRecord> rewardQuery = Wrappers.lambdaQuery();
					rewardQuery.eq(BoolUserRewardRecord::getType, RewardType.INVITATION);
					rewardQuery.select(BoolUserRewardRecord::getId, BoolUserRewardRecord::getRewardValue,
							BoolUserRewardRecord::getUserId, BoolUserRewardRecord::getAdditionInfo);
					rewardQuery.in(BoolUserRewardRecord::getUserId, inviterUserSet);
					List<BoolUserRewardRecord> tmpRewards = userRewardRecordMapper.selectList(rewardQuery);
					rewards.addAll(tmpRewards);
				}
			}
			if (CollectionUtil.isNotEmpty(rewards)) {
				for (BoolUserRewardRecord reward : rewards) {
					String additionInfo = reward.getAdditionInfo();
					try {
						if (StringUtils.isNotBlank(additionInfo)) {
							try {
								Long inviteeId = Long.valueOf(additionInfo);
								Set<BoolUserRewardRecord> existRewards = rewardMap.get(inviteeId);
								if (CollectionUtil.isEmpty(existRewards)) {
									Set<BoolUserRewardRecord> newRewards = Sets.newHashSet(reward);
									rewardMap.put(inviteeId, newRewards);
								} else {
									existRewards.add(reward);
									rewardMap.put(inviteeId, existRewards);
								}
							} catch (Exception e) {
							}
						}
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		Page<UserInvitationRelationVo> result = new Page<UserInvitationRelationVo>();
		BeanUtil.copyProperties(pageResult, result, "records");
		List<UserInvitationRelationVo> voList = records.stream().map(record -> {
			UserInvitationRelationVo vo = new UserInvitationRelationVo();
			Long inviteeId = record.getInviteeId();
			if (inviteeId != null) {
				BoolUser invitee = userMap.get(inviteeId);
				if (invitee != null) {
					String userName = invitee.getUsername();
					String url = StringUtils.join("https://t.me/", userName);
					userName = maskingUserName(userName);
					vo.setInviteeUsername(userName);
					vo.setPersonalUrl(url);
				}
				if (record.getInvitationTime() != null) {
					vo.setInviteTime(Timestamp.valueOf(record.getInvitationTime()).getTime());
				}
				Set<BoolUserRewardRecord> rewards = rewardMap.get(inviteeId);
				if (CollectionUtil.isNotEmpty(rewards)) {
					BigDecimal rewardValue = rewards.stream()
							.filter(reward -> reward != null && reward.getRewardValue() != null)
							.map(reward -> reward.getRewardValue()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
					vo.setRewardValue(formatAmount(rewardValue));
				}
			}
			return vo;
		}).collect(Collectors.toList());
		result.setRecords(voList);
		try {
			simpleRedisClient.set(key, result, 15, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CustomPage<UserRankVo> rank(Integer pageNo, Integer pageSize) {
		pageNo = 1;
		pageSize = 500;
		String key = StringUtils.join(RedisKeyConstants.RANK_KEY, pageNo, "-", pageSize);
		Object rankResultObj = null;
		try {
			rankResultObj = simpleRedisClient.get(key);
		} catch (Exception e) {
		}
		if (rankResultObj != null) {
			String rankResultJsonStr = JSONObject.toJSONString(rankResultObj);
			CustomPage<UserRankVo> result = JSONObject.parseObject(rankResultJsonStr, CustomPage.class);
			return result;
		}
		long offset = PageUtil.getStart(pageNo - 1, pageSize);
		Set<Object> rankUserSimpleRespSet = zsetRedisClient.range(RedisKeyConstants.USER_POINT_RANK_ZSET_KEY, offset,
				offset + pageSize);
		List<UserRankVo> rankList = Lists.newArrayList();
		if (CollectionUtil.isNotEmpty(rankUserSimpleRespSet)) {
			for (Object rankUserSimpleRespObj : rankUserSimpleRespSet) {
				String simpleRespJson = JSONObject.toJSONString(rankUserSimpleRespObj);
				UserRankSimpleResp simpleResp = JSONObject.parseObject(simpleRespJson, UserRankSimpleResp.class);
				UserRankVo rank = new UserRankVo();
				rank.setUserId(simpleResp.getUserId());
				rank.setRewardValue(simpleResp.getRewardValue());
				rank.setUsername(simpleResp.getUsername());
				rankList.add(rank);
			}
			CollectionUtil.sort(rankList, (r1, r2) -> {
				String rvStr1 = r1.getRewardValue();
				String rvStr2 = r2.getRewardValue();
				if (StringUtils.equalsIgnoreCase(rvStr1, rvStr2)) {
					return NumberConstants.ZERO;
				}
				BigDecimal rv1 = new BigDecimal(rvStr1);
				BigDecimal rv2 = new BigDecimal(rvStr2);
				return rv2.compareTo(rv1);
			});
		} else {
			rankList = rankMapper.pageRank(offset, (long) pageSize);
		}
		AtomicInteger rankAtom = new AtomicInteger(NumberConstants.ZERO);
		Integer pageNoCacl = pageNo;
		Integer pageSizeCacl = pageNo;
		Set<Long> customInfoIdQuerySet = Sets.newHashSet();
		rankList = rankList.stream().map(rank -> {
			Integer rankNo = rankAtom.incrementAndGet();
			Integer rankNoCalculated = (pageNoCacl - 1) * pageSizeCacl + rankNo;
			String rankNoStr = String.valueOf(rankNoCalculated);
			rank.setRank(rankNoStr);
			String value = formatAmount(new BigDecimal(rank.getRewardValue()));
			rank.setRewardValue(value);
			String userName = rank.getUsername();
			userName = maskingUserName(userName);
			rank.setUsername(userName);

			// 添加个人url返回
			if (rankNoCalculated <= NumberConstants.TWEENTY && rankNoCalculated > NumberConstants.ZERO) {
				Long userId = rank.getUserId();
				if (userId != null) {
					customInfoIdQuerySet.add(userId);
				}
			}
			return rank;
		}).collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(customInfoIdQuerySet)) {
			LambdaQueryWrapper<BoolUserCustomInfo> customInfoQuery = Wrappers.lambdaQuery();
			customInfoQuery.in(BoolUserCustomInfo::getUserId, customInfoIdQuerySet);
			customInfoQuery.eq(BoolUserCustomInfo::getPersonalInfoType, PersonalInfoTypeEnum.URL);
			customInfoQuery.select(BoolUserCustomInfo::getUserId, BoolUserCustomInfo::getPersonalInfoValue);
			List<BoolUserCustomInfo> existCustomInfos = boolUserCustomInfoMapper.selectList(customInfoQuery);
			if (CollectionUtil.isNotEmpty(customInfoIdQuerySet)) {
				Map<Long, String> urlMap = existCustomInfos.stream()
						.collect(Collectors.toMap(BoolUserCustomInfo::getUserId, customInfo -> {
							return StringUtils.defaultString(customInfo.getPersonalInfoValue());
						}));
				rankList.stream().forEach(rank -> {
					Long userId = rank.getUserId();
					String url = urlMap.get(userId);
					if (StringUtils.isNotBlank(url)) {
						rank.setPersonalUrl(url);
					}
					rank.setUserId(null);
				});
			}
		}
		CustomPage<UserRankVo> result = new CustomPage<UserRankVo>();
		result.setCurrent(pageNo);
		result.setSize(pageSize);
		Long total = rankMapper.total();
		result.setTotal(total);
		result.setRecords(rankList);
//		int pages;
//		if (500 % pageSize == 0) {
//			pages = 500 / pageSize;
//		} else {
//			pages = 500 / pageSize + 1;
//		}
		result.setPages(1);
		try {
			simpleRedisClient.set(key, result, 30, TimeUnit.SECONDS);
		} catch (Exception e) {
		}
		return result;
	}

	@Override
	public Page<InvitationRankVo> inviterRank(Integer pageNo, Integer pageSize, UserInvitersRankReq query) {
		Page<InvitationRankVo> result = new Page<>();
		result.setCurrent(pageNo);
		result.setSize(pageSize);
		LocalDate startTime = LocalDate.parse("2024-07-18", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate endTime = LocalDate.now().plusDays(1);
		if (query.getType() != null) {
			if (query.getType().equals(InviteTimeType.Week)) {
				startTime = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
				endTime = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
			} else if (query.getType().equals(InviteTimeType.Month)) {
				startTime = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
				endTime = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
			}
		}
		if (query != null && query.getUserName() != null || query.getCode() != null) {
			BoolUser boolUser;
			if (query.getUserName() != null) {
				boolUser = userDao.lambdaQuery().eq(BoolUser::getUsername, query.getUserName()).one();
			} else {
				boolUser = userDao.lambdaQuery().eq(BoolUser::getInvitationCode, query.getCode()).one();
			}

			if (boolUser == null) {
				result.setPages(0);
				result.setTotal(0);
				result.setRecords(null);
				return result;
			}
			List<BoolUserRewardRecord> rewardRecordList = userRewardRecordService.lambdaQuery()
					.between(BoolUserRewardRecord::getRewardTime, startTime, endTime)
					.eq(BoolUserRewardRecord::getUserId, boolUser.getId()).eq(BoolUserRewardRecord::getType, RewardType.INVITATION)
					.list();
			BigDecimal totalReward = BigDecimal.ZERO;
			for (BoolUserRewardRecord boolUserRewardRecord : rewardRecordList) {
				totalReward = totalReward.add(boolUserRewardRecord.getRewardValue());
			}
			InvitationRankVo invitationRankVo = new InvitationRankVo();
			invitationRankVo.setUserId(String.valueOf(boolUser.getId()));
			invitationRankVo.setUserName(getUserName(boolUser));
			invitationRankVo.setInviterIntegral(totalReward);
			invitationRankVo.setInviterNumbers((long) rewardRecordList.size());
			invitationRankVo.setInviterCode(boolUser.getInvitationCode());
			List<InvitationRankVo> invitationRankVos = new ArrayList<>();
			invitationRankVos.add(invitationRankVo);

			result.setPages(1);
			result.setTotal(1);
			if (result.getCurrent() == 1) {
				result.setRecords(invitationRankVos);
			}
			return result;

		}
		List<InvitationRankVo> pageList = userInvitationRelationService.queryRankByInviters(startTime, endTime,
				query.getModel());
		Integer fromIndex = (pageNo - 1) * pageSize;
		Integer toIndex = fromIndex + pageSize;
		if (fromIndex > pageList.size() - 1) {
			fromIndex = pageList.size();
		}
		if (toIndex > pageList.size() - 1) {
			toIndex = pageList.size();
		}
		List<InvitationRankVo> resultList = pageList.subList(fromIndex, toIndex);
		for (InvitationRankVo invitationRankVo : resultList) {
			BoolUser boolUser = userDao.lambdaQuery().eq(BoolUser::getId, invitationRankVo.getUserId()).one();

			if (boolUser == null) {
				continue;
			}
			invitationRankVo.setUserId(invitationRankVo.getUserId());
			invitationRankVo.setUserName(getUserName(boolUser));
			invitationRankVo.setInviterCode(boolUser.getInvitationCode());
		}
		result.setRecords(resultList);

		result.setTotal(pageList.size());

		Long pages = result.getTotal() / pageSize;
		if (result.getTotal() / pageSize != 0L) {
			++pages;
		}
		result.setPages(pages);
		return result;
	}

	private String getUserName(BoolUser boolUser) {
		if (boolUser.getUsername() != null) {
			return boolUser.getUsername();
		} else if (boolUser.getFirstName() != null) {
			return boolUser.getFirstName();
		} else {
			return boolUser.getLastName();
		}
	}


	/**
	 * 用户名脱敏
	 *
	 * @param userName username
	 * @return
	 */
	private String maskingUserName(String userName) {
		try {
            return StrUtil.replaceByCodePoint(userName, 2, 5, '*');
		} catch (Exception e) {
			log.error("用户名脱敏异常", e);
		}
		return userName;
	}

	@Override
	public Page<InvitationRankVo> inviterRankFull(Integer pageNo, Integer pageSize) {
		pageNo = 1;
		pageSize = 500;
		long offset = PageUtil.getStart(pageNo - 1, pageSize);
		Page<InvitationRankVo> pageInfo = Page.of(pageNo, pageSize);
		Set<Object> userInviteRankRespSet = zsetRedisClient.range(RedisKeyConstants.USER_FULL_INVITE_RANK_ZSET, offset,
				offset + pageSize);
		if (CollectionUtil.isEmpty(userInviteRankRespSet)) {
			return pageInfo;
		}
		List<InvitationRankVo> rankList = userInviteRankRespSet.stream().map(rank -> {
			String json = JSONObject.toJSONString(rank);
			UserInviteRankResp resp = JSONObject.parseObject(json, UserInviteRankResp.class);
			InvitationRankVo vo = new InvitationRankVo();
			vo.setInviterNumbers(resp.getAmount());
			vo.setInviterNumbers1(resp.getAmount1());
			vo.setInviterNumbers2(resp.getAmount2());
			vo.setUserName(maskingUserName(resp.getUserName()));
			Long userId = resp.getUserId();
			vo.setUserId(String.valueOf(userId));
			vo.setRanking(resp.getRanking());
			vo.setVerify(resp.getVerify());
			vo.setPersonalUrl(resp.getPersonalUrl());
			return vo;
		}).collect(Collectors.toList());
		if (CollectionUtil.isEmpty(rankList)) {
			return pageInfo;
		}
		CollectionUtil.sort(rankList, (r0, r1) -> (int) (r0.getRanking() - r1.getRanking()));
		pageInfo.setRecords(rankList);
		pageInfo.setTotal(pageSize);
		return pageInfo;
	}

	@Override
	public Page<InvitationRankVo> inviterRankWeek(Integer pageNo, Integer pageSize) {
		pageNo = 1;
		pageSize = 500;
		long offset = PageUtil.getStart(pageNo - 1, pageSize);
		Page<InvitationRankVo> pageInfo = Page.of(pageNo, pageSize);
		LocalDateTime now = LocalDateTime.now();
		now = now.minusHours(1);
		LocalDateTime thisMondayAtEight = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
				.with(LocalTime.of(8, 0));
		long timestamp = thisMondayAtEight.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
		String weekRankKey = RedisKeyConstants.USER_WEEKLY_INVITE_RANK_ZSET + timestamp;
		Set<Object> userInviteRankRespSet = zsetRedisClient.range(weekRankKey, offset, offset + pageSize);
		if (CollectionUtil.isEmpty(userInviteRankRespSet)) {
			return pageInfo;
		}
		List<InvitationRankVo> rankList = userInviteRankRespSet.stream().map(rank -> {
			String json = JSONObject.toJSONString(rank);
			UserInviteRankResp resp = JSONObject.parseObject(json, UserInviteRankResp.class);
			InvitationRankVo vo = new InvitationRankVo();
			vo.setInviterNumbers(resp.getAmount());
			vo.setInviterNumbers1(resp.getAmount1());
			vo.setInviterNumbers2(resp.getAmount2());
			vo.setUserName(maskingUserName(resp.getUserName()));
			Long userId = resp.getUserId();
			vo.setUserId(String.valueOf(userId));
			vo.setRanking(resp.getRanking());
			vo.setVerify(resp.getVerify());
			vo.setPersonalUrl(resp.getPersonalUrl());
			return vo;
		}).filter(rank -> {
			return rank != null && rank.getInviterNumbers() > NumberConstants.ZERO;
		}).collect(Collectors.toList());
		if (CollectionUtil.isEmpty(rankList)) {
			return pageInfo;
		}

		CollectionUtil.sort(rankList, (r0, r1) -> (int) (r0.getRanking() - r1.getRanking()));

		pageInfo.setRecords(rankList);
		pageInfo.setTotal(pageSize);
		return pageInfo;
	}

}
