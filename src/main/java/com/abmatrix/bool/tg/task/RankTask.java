package com.abmatrix.bool.tg.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.dao.entity.*;
import com.abmatrix.bool.tg.dao.mapper.*;
import com.abmatrix.bool.tg.model.req.UserInviteRankReq;
import com.abmatrix.bool.tg.model.resp.UserInviteRankResp;
import com.abmatrix.bool.tg.model.resp.UserRankSimpleResp;
import com.abmatrix.bool.tg.service.UserServiceAdaptor;
import com.abmatrix.bool.tg.task.job.WeekSnapSettleJob;
import com.alibaba.nacos.shaded.com.google.common.base.Objects;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 排行榜任务
 *
 * @author PeterWong
 * @date 2024年8月3日
 */
@Component
@Slf4j
public class RankTask implements CommandLineRunner {
    /**
     * redission
     */
    @Autowired
    private RedissonClient redissonClient;
    /**
     * 缓存接口
     */
    @Autowired
    private RedisTemplate<String, Object> fastJsonRedisTemplate;
    /**
     * 用户奖励接口
     */
    @Autowired
    private UserRewardRecordMapper userRewardRecordMapper;
    /**
     * 用户mapper
     */
    @Autowired
    private UserMapper userMapper;
    /**
     *
     */
    @Autowired
    private BoolCalculateRankOffsetMapper boolCalculateRankOffsetMapper;
    /**
     * 用户接口适配器
     */
    @Autowired
    private UserServiceAdaptor userServiceAdaptor;
    /**
     * 是否开启task
     */
    @Value("${rank.task.switch:close}")
    private String rankTaskOpen;

    private static final String OPEN = "open";
    /**
     * 用户邀请mapper
     */
    @Autowired
    private UserInvitationRelationMapper userInvitationRelationMapper;
    /**
     * 用户邀请数量快照mapper
     */
    @Autowired
    private BoolUserInviteCountSnapshotMapper boolUserInviteCountSnapshotMapper;

    /**
     * 邀请排行计算cron表达式
     */
    @Value("${rank.task.invite.settle.cron:0 15 8 ? * MON}")
    private String inviteSettleRankCron;
    /**
     * 周榜邀请mapper
     */
    @Autowired
    private BoolUserInviteCountWeekSnapshotMapper boolUserInviteCountWeekSnapshotMapper;
    /**
     * spring上下文
     */
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        if (StringUtils.equals(OPEN, rankTaskOpen)) {
            // 积分排行榜计算
            ScheduledExecutorService rewardRankSnapExecutorService = Executors
                    .newScheduledThreadPool(NumberConstants.ONE);
            rewardRankSnapExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    calculateUserPointRank();
                } catch (Exception e) {
                    log.info("排行榜任务完成异常: [{}]", e.getMessage());
                }
            }, 0, 1, TimeUnit.MINUTES);
            // 邀请总榜计算
            ScheduledExecutorService inviteRankSnapExecutorService = Executors
                    .newScheduledThreadPool(NumberConstants.ONE);
            inviteRankSnapExecutorService.scheduleWithFixedDelay(() -> {
                RLock lock = redissonClient.getFairLock(RedisKeyConstants.USER_FULL_INVITE_RANK_CALCULATE_LOCK);
                try {
                    if (lock.tryLock(0, 5, TimeUnit.MINUTES)) {
                        try {
                            calculateUserInviteRank();
                            createFullInviteRankIndex();
                        } catch (Exception e) {
                            log.info("邀请排行榜任务完成异常: [{}]", e.getMessage());
                        } finally {
                            if (lock.isLocked()) {
                                lock.unlock();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }, 0, 1, TimeUnit.MINUTES);
            // 邀请周榜计算
            ScheduledExecutorService weekRankSnapExecutorService = Executors
                    .newScheduledThreadPool(NumberConstants.ONE);
            weekRankSnapExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    RLock lock = redissonClient.getFairLock(RedisKeyConstants.USER_WEEKLY_INVITE_RANK_CALCULATE_LOCK);
                    if (lock.tryLock(0, 1, TimeUnit.MINUTES)) {
                        try {
                            weekInvite();
                        } catch (Exception e) {
                            log.info("邀请排行榜周榜计算任务异常: [{}]", e.getMessage());
                        } finally {
                            if (lock.isLocked()) {
                                lock.unlock();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }, 0, 1, TimeUnit.MINUTES);
            // 邀请周榜加载到redis任务
            ScheduledExecutorService weekRankSnapLoadExecutorService = Executors
                    .newScheduledThreadPool(NumberConstants.ONE);
            weekRankSnapLoadExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    RLock lock = redissonClient.getFairLock(RedisKeyConstants.USER_WEEKLY_INVITE_RANK_LOAD_LOCK);
                    if (lock.tryLock(0, 1, TimeUnit.MINUTES)) {
                        try {
                            loadWeekCountToCache();
                        } catch (Exception e) {
                            log.info("邀请排行榜周榜加载任务异常: [{}]", e.getMessage());
                        } finally {
                            if (lock.isLocked()) {
                                lock.unlock();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }, 0, 10, TimeUnit.SECONDS);

            // 初始化周榜结算
            initWeekRankSettleJobExecute();
        }
    }

    /**
     * 计算用户排名
     */
    @SneakyThrows
    private void calculateUserPointRank() {
        RLock lock = redissonClient.getFairLock(RedisKeyConstants.USER_POINT_RANK_CALCULATE_LOCK);
        if (lock.tryLock(0, 1, TimeUnit.MINUTES)) {
            try {
                StopWatch watch = StopWatch.createStarted();
                // 修改对应用户的奖励值
                BoolCalculateRankOffset offsetRecord = boolCalculateRankOffsetMapper.selectById(NumberConstants.ONE);
                Boolean isFull = Boolean.TRUE;
                LambdaQueryWrapper<BoolUserRewardRecord> rewardQuery = Wrappers.lambdaQuery();
                rewardQuery.select(BoolUserRewardRecord::getId, BoolUserRewardRecord::getUserId,
                        BoolUserRewardRecord::getRewardValue);
                if (offsetRecord != null) {
                    Long latestRewardId = offsetRecord.getLatestRewardId();
                    if (latestRewardId != null && latestRewardId > NumberConstants.ZERO) {
                        rewardQuery.gt(BoolUserRewardRecord::getId, latestRewardId);
                        isFull = Boolean.FALSE;
                    }
                }
                if (isFull) {
                    log.warn("开启全量将奖励计算.");
                }
                List<BoolUserRewardRecord> rewardList = userRewardRecordMapper.selectList(rewardQuery);
                if (CollectionUtil.isEmpty(rewardList)) {
                    return;
                }
                Map<Long, List<BoolUserRewardRecord>> rewardMap = rewardList.stream()
                        .collect(Collectors.groupingBy(BoolUserRewardRecord::getUserId));
                if (MapUtil.isEmpty(rewardMap)) {
                    return;
                }
                List<BoolUserRewardRecord> totalRewardList = rewardMap.entrySet().stream().map(entry -> {
                    Long userId = entry.getKey();
                    List<BoolUserRewardRecord> list = entry.getValue();
                    BoolUserRewardRecord tmpRecord = new BoolUserRewardRecord();
                    try {
                        BigDecimal reward = list.stream().map(BoolUserRewardRecord::getRewardValue).reduce(BigDecimal::add)
                                .get();
                        tmpRecord.setUserId(userId);
                        tmpRecord.setRewardValue(reward);
                        return tmpRecord;
                    } catch (Exception e) {
                    }
                    tmpRecord.setUserId(userId);
                    tmpRecord.setRewardValue(BigDecimal.ZERO);
                    return tmpRecord;
                }).collect(Collectors.toList());
                List<Long> rewardIdList = rewardList.stream().map(BoolUserRewardRecord::getId).collect(Collectors.toList());
                Long maxRewardId = CollectionUtil.max(rewardIdList);
                userServiceAdaptor.batchUpdateUserReward(totalRewardList, maxRewardId, isFull);
                log.info("奖励计算完成,总耗时{}ms", watch.getTime());
                watch.reset();
                watch.start();
                // 前500排行榜计算
                LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
                userQuery.select(BoolUser::getUsername, BoolUser::getRewardAmount);
                userQuery.orderByDesc(BoolUser::getRewardAmount);
                userQuery.orderByAsc(BoolUser::getId);
                userQuery.last(" LIMIT 1000 ");
                List<BoolUser> boolUserList = userMapper.selectList(userQuery);
                if (CollectionUtil.isEmpty(boolUserList)) {
                    return;
                }
                int max = 500;
                ZSetOperations<String, Object> zsetOp = fastJsonRedisTemplate.opsForZSet();
                for (int i = 0; i < CollectionUtil.size(boolUserList); i++) {
                    try {
                        if (i >= max) {
                            break;
                        }
                        BoolUser boolUser = boolUserList.get(i);
                        long a = i + 1;
                        Long userId = boolUser.getId();
                        BigDecimal scoreDem = boolUser.getRewardAmount();
                        scoreDem = scoreDem.stripTrailingZeros();
                        UserRankSimpleResp userRankSimpleResp = new UserRankSimpleResp();
                        userRankSimpleResp.setRewardValue(scoreDem.toPlainString());
                        userRankSimpleResp.setUserId(userId);
                        userRankSimpleResp.setUsername(boolUser.getUsername());
                        zsetOp.removeRangeByScore(RedisKeyConstants.USER_POINT_RANK_ZSET_KEY, a, a);
                        zsetOp.add(RedisKeyConstants.USER_POINT_RANK_ZSET_KEY, userRankSimpleResp, a);
                    } catch (Exception e) {
                    }
                }
                log.info("前500名用户排名索引组装完成,总耗时{}ms", watch.getTime());
                watch.reset();
                watch.start();
                // 用户个人排名计算
                fillUserRank();
                log.info("全量用户排名索引组装完成,总耗时{}ms", watch.getTime());
            } catch (Exception e) {
                log.error("计算用户排行榜异常", e);
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        } else {
            log.warn("用户排行计算未获取到锁.");
        }
    }

    private void fillUserRank() {
        Integer start = NumberConstants.ZERO;
        Integer limit = 1000;
        Long count = userMapper.selectCount(null);
        Long needLoop = (long) Math.ceil((double) count / (double) limit);
        log.info("开始处理用户排名，全量数据{}条,{}轮处理", count, needLoop);
        StopWatch watch = StopWatch.createStarted();
        LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
        userQuery.select(BoolUser::getId);
        userQuery.orderByDesc(BoolUser::getRewardAmount);
        userQuery.orderByAsc(BoolUser::getId);
        String limitStrFormat = " LIMIT {},{}";
        userQuery.last(StrUtil.format(limitStrFormat, start, limit));
        List<BoolUser> rankBoolUserList = userMapper.selectList(userQuery);
        Long dealed = NumberConstants.ZERO_LONG;
        Long loop = NumberConstants.ZERO_LONG;
        while (CollectionUtil.isNotEmpty(rankBoolUserList)) {
            Map<String, Long> rankMap = new HashMap<String, Long>();
            int size = CollectionUtil.size(rankBoolUserList);
            for (int i = 0; i < size; i++) {
                BoolUser targetBoolUser = rankBoolUserList.get(i);
                Long userId = targetBoolUser.getId();
                String userIdStr = String.valueOf(userId);
                Long a = Long.valueOf(start + i + NumberConstants.ONE);
                rankMap.put(userIdStr, a);
            }
            fastJsonRedisTemplate.opsForHash().putAll(RedisKeyConstants.USER_POINT_RANK_HASH_KEY, rankMap);
            rankMap.clear();
            loop = loop + 1;
            dealed = dealed + size;
            log.debug("第{}轮处理完成,本轮处理数据{}条,累计处理数据{}条,本轮耗时{}ms", loop, size, dealed, watch.getTime());
            watch.reset();
            watch.start();
            start = start + limit;
            userQuery = Wrappers.lambdaQuery();
            userQuery.select(BoolUser::getId);
            userQuery.orderByDesc(BoolUser::getRewardAmount);
            userQuery.orderByAsc(BoolUser::getId);
            userQuery.last(StrUtil.format(limitStrFormat, start, limit));
            rankBoolUserList = userMapper.selectList(userQuery);
        }
    }

    /**
     * 计算全量用户邀请排行
     */
    @SneakyThrows
    private void calculateUserInviteRank() {
        try {
            StopWatch watch = StopWatch.createStarted();
            // 修改对应用户的奖励值
            BoolCalculateRankOffset offsetRecord = boolCalculateRankOffsetMapper.selectById(NumberConstants.TWO);
            Boolean isFull = Boolean.TRUE;
            LambdaQueryWrapper<BoolUserInvitationRelation> inviteQuery = Wrappers.lambdaQuery();
            inviteQuery.select(BoolUserInvitationRelation::getId, BoolUserInvitationRelation::getInviterId,
                    BoolUserInvitationRelation::getInviteeId);
            if (offsetRecord != null) {
                Long latestRewardId = offsetRecord.getLatestRewardId();
                if (latestRewardId != null && latestRewardId > NumberConstants.ZERO) {
                    inviteQuery.gt(BoolUserInvitationRelation::getId, latestRewardId);
                    isFull = Boolean.FALSE;
                }
            }
            if (isFull) {
                log.warn("开启全量将奖励计算.");
            }
            List<BoolUserInvitationRelation> relationList = userInvitationRelationMapper.selectList(inviteQuery);
            log.info("全量计算邀请排行,查询邀请记录耗时{}ms,数据共有{}条", watch.getTime(), CollectionUtil.size(relationList));
            if (CollectionUtil.isEmpty(relationList)) {
                return;
            }
            watch.reset();
            watch.start();
            Graph<Long, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
            List<Long> relationIds = Lists.newArrayListWithCapacity(CollectionUtil.size(relationList));
            for (BoolUserInvitationRelation relation : relationList) {
                Long inviter = relation.getInviterId();
                Long invitee = relation.getInviteeId();
                graph.addVertex(inviter);
                graph.addVertex(invitee);
                graph.addEdge(inviter, invitee);
                relationIds.add(relation.getId());
            }
            Set<Long> vertexSet = graph.vertexSet();
            if (CollectionUtil.isEmpty(vertexSet)) {
                return;
            }
            log.info("全量计算邀请排行,图数据构建{}ms", watch.getTime());
            watch.reset();
            watch.start();
            BoolUserInviteCountSnapshot snap;
            List<BoolUserInviteCountSnapshot> snapList = Lists.newArrayListWithCapacity(CollectionUtil.size(vertexSet));
            LocalDateTime now = LocalDateTime.now();
            for (Long userId : vertexSet) {
                long levelOneCount = calculateDownlineAmounts(userId, graph, NumberConstants.ONE);
                long levelTwoCount = calculateDownlineAmounts(userId, graph, NumberConstants.TWO);
                snap = new BoolUserInviteCountSnapshot();
                snap.setCreateUser(userId);
                snap.setUpdateUser(userId);
                snap.setUserId(userId);
                snap.setCreateTime(now);
                snap.setUpdateTime(now);
                snap.setAmount(levelOneCount + levelTwoCount);
                snap.setAmount1(levelOneCount);
                snap.setAmount2(levelTwoCount);
                snapList.add(snap);
            }
            log.info("全量计算邀请排行,用户一、二级下线数量计算耗时{}ms", watch.getTime());
            if (CollectionUtil.isEmpty(snapList)) {
                return;
            }
            watch.reset();
            watch.start();
            Long maxRelationId = CollectionUtil.max(relationIds);
            userServiceAdaptor.batchSaveUserFullInviteCount(snapList, maxRelationId, isFull);
            log.info("全量计算邀请排行,用户一、二级数据入库耗时{}ms,数据量{}", watch.getTime(), CollectionUtil.size(snapList));
        } catch (Exception e) {
            log.error("计算邀请排行异常", e);
        }
    }

    /**
     * 计算下线数量，无时间版
     *
     * @param userId
     * @param graph
     * @param level
     * @return
     */
    private Long calculateDownlineAmounts(Long userId, Graph<Long, DefaultEdge> graph, Integer level) {
        return calculateDownlineAmounts(userId, graph, NumberConstants.TWO, null, null);
    }

    /**
     * 计算下线数量
     *
     * @param userId
     * @param graph
     * @param level         级别 只有1和2
     * @param inviteTimeMap
     * @return
     */
    private Long calculateDownlineAmounts(Long userId, Graph<Long, DefaultEdge> graph, Integer level,
                                         final Map<Long, Long> inviteTimeMap, Map<Long, LocalDateTime> relationInviteeTimeMap) {
        if (userId == null || level > NumberConstants.TWO || level <= NumberConstants.ZERO) {
            return NumberConstants.ZERO_LONG;
        }
        if (Objects.equal(NumberConstants.ONE, level)) {
            Set<Long> childUserIdSet = Sets.newHashSet();
            for (DefaultEdge edge : graph.outgoingEdgesOf(userId)) {
                Long child = graph.getEdgeTarget(edge);
                childUserIdSet.add(child);
            }
            if (CollectionUtil.isEmpty(childUserIdSet)) {
                return NumberConstants.ZERO_LONG;
            }
            if (CollectionUtil.size(childUserIdSet) <= 1000) {
                LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
                userQuery.in(BoolUser::getId, childUserIdSet);
                userQuery.select(BoolUser::getId);
                List<BoolUser> boolUserList = userMapper.selectList(userQuery);
                int levelOneCount = CollectionUtil.size(boolUserList);
                if (levelOneCount > NumberConstants.ZERO) {
                    fillInviteTime(userId, boolUserList, inviteTimeMap, relationInviteeTimeMap);
                }
                return Long.valueOf(levelOneCount);
            } else {
                List<Long> childUserIdList = Lists.newArrayList(childUserIdSet);
                List<List<Long>> childUserIdListList = Lists.partition(childUserIdList, 1000);
                int count = 0;
                for (List<Long> innerList : childUserIdListList) {
                    LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
                    userQuery.in(BoolUser::getId, innerList);
                    userQuery.select(BoolUser::getId);
                    List<BoolUser> boolUserList = userMapper.selectList(userQuery);
                    int levelOneCount = CollectionUtil.size(boolUserList);
                    if (levelOneCount > NumberConstants.ZERO) {
                        fillInviteTime(userId, boolUserList, inviteTimeMap, relationInviteeTimeMap);
                    }
                    count += levelOneCount;
                }
                return Long.valueOf(count);
            }
        } else if (Objects.equal(NumberConstants.TWO, level)) {
            int levelTwoCount = 0;
            for (DefaultEdge edge : graph.outgoingEdgesOf(userId)) {
                Long child = graph.getEdgeTarget(edge);
                Set<Long> childChildUserIdSet = Sets.newHashSet();
                for (DefaultEdge childEdge : graph.outgoingEdgesOf(child)) {
                    Long childChild = graph.getEdgeTarget(childEdge);
                    childChildUserIdSet.add(childChild);
                }
                if (CollectionUtil.isEmpty(childChildUserIdSet)) {
                    continue;
                }
                if (CollectionUtil.size(childChildUserIdSet) <= 1000) {
                    LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
                    userQuery.in(BoolUser::getId, childChildUserIdSet);
                    userQuery.select(BoolUser::getId);
                    List<BoolUser> boolUserList = userMapper.selectList(userQuery);
                    int levelCurrentCount = CollectionUtil.size(boolUserList);
                    if (levelCurrentCount > NumberConstants.ZERO) {
                        fillInviteTime(userId, boolUserList, inviteTimeMap, relationInviteeTimeMap);
                    }
                    levelTwoCount += levelCurrentCount;
                } else {
                    List<Long> childUserIdList = Lists.newArrayList(childChildUserIdSet);
                    List<List<Long>> childUserIdListList = Lists.partition(childUserIdList, 1000);
                    for (List<Long> innerList : childUserIdListList) {
                        LambdaQueryWrapper<BoolUser> userQuery = Wrappers.lambdaQuery();
                        userQuery.in(BoolUser::getId, innerList);
                        userQuery.select(BoolUser::getId);
                        List<BoolUser> boolUserList = userMapper.selectList(userQuery);
                        int levelOneCount = CollectionUtil.size(boolUserList);
                        if (levelOneCount > NumberConstants.ZERO) {
                            fillInviteTime(userId, boolUserList, inviteTimeMap, relationInviteeTimeMap);
                        }
                        levelTwoCount += levelOneCount;
                    }
                }
            }
            return Long.valueOf(levelTwoCount);
        }
        return NumberConstants.ZERO_LONG;
    }

    /**
     * 填充inviteTimeMap
     *
     * @param inviter
     * @param boolUserList
     * @param inviteTimeMap
     * @param relationInviteeTimeMap
     */
    private void fillInviteTime(Long inviter, List<BoolUser> boolUserList, final Map<Long, Long> inviteTimeMap,
                                Map<Long, LocalDateTime> relationInviteeTimeMap) {
        if (CollectionUtil.isEmpty(boolUserList) || MapUtil.isEmpty(relationInviteeTimeMap) || inviteTimeMap == null) {
            return;
        }
        for (BoolUser boolUser : boolUserList) {
            Long currentInvitee = boolUser.getId();
            LocalDateTime currentInviteeTime = relationInviteeTimeMap.get(currentInvitee);
            if (currentInviteeTime != null) {
                Long invitationTimestamp = currentInviteeTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
                Long existInvitationTimestamp = inviteTimeMap.get(inviter);
                if (existInvitationTimestamp == null) {
                    inviteTimeMap.put(inviter, invitationTimestamp);
                } else {
                    if (existInvitationTimestamp < invitationTimestamp) {
                        inviteTimeMap.put(inviter, invitationTimestamp);
                    }
                }
            }
        }
    }

    /**
     * 构建全量邀请排名索引
     */
    @SneakyThrows
    private void createFullInviteRankIndex() {
        List<UserInviteRankResp> list = boolUserInviteCountSnapshotMapper.queryTop500Full(null);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        ZSetOperations<String, Object> zsetOp = fastJsonRedisTemplate.opsForZSet();
        for (int i = 0; i < list.size(); i++) {
            int a = i + 1;
            UserInviteRankResp resp = list.get(i);
            resp.setRanking(Long.valueOf(a));
            zsetOp.removeRangeByScore(RedisKeyConstants.USER_FULL_INVITE_RANK_ZSET, a, a);
            zsetOp.add(RedisKeyConstants.USER_FULL_INVITE_RANK_ZSET, resp, a);
        }
    }

    /**
     * 初始化构建周排行计算任务
     */
//	@SneakyThrows
//	private void initWeekInviteCalculateTask() {
//		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
//		Map<String, Object> diMap = Maps.newHashMap();
//		diMap.put("applicationContext", applicationContext);
//		JobDataMap jobDataMap = new JobDataMap(diMap);
//		JobDetail job = JobBuilder.newJob(WeekSnapCalculateJob.class).setJobData(jobDataMap)
//				.withIdentity("weekInviteJob", "bool-tg-interface-job").build();
//		// 定义 Cron Trigger
//		// 0 0 8 ? * 1
//		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("weekInviteTrigger", "bool-tg-interface-job")
//				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10) // 每10秒执行一次
//						.repeatForever())
//				.build();
//		// 开始调度
//		scheduler.start();
//		scheduler.scheduleJob(job, trigger);
//	}

    /**
     * 计算周榜
     */
    @SneakyThrows
    public void weekInvite() {
        StopWatch watch = StopWatch.createStarted();
        StopWatch fullWatch = StopWatch.createStarted();
        // 查询上周一到本周一的邀请记录
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thisMondayAtEight = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .with(LocalTime.of(8, 0));
            long timestamp = thisMondayAtEight.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
            List<BoolUserInvitationRelation> relationList = userInvitationRelationMapper
                    .currentWeekRelations(thisMondayAtEight);
            log.info("计算周邀请排行,查询邀请记录耗时{}ms,数据共有{}条", watch.getTime(), CollectionUtil.size(relationList));
            if (CollectionUtil.isEmpty(relationList)) {
                return;
            }
            Graph<Long, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
            Map<Long, Long> inviteTimeMap = Maps.newHashMap();
            Set<Long> existInviters = Sets.newHashSet();
            for (BoolUserInvitationRelation relation : relationList) {
                Long inviter = relation.getInviterId();
                existInviters.add(inviter);
                Long invitee = relation.getInviteeId();
                graph.addVertex(inviter);
                graph.addVertex(invitee);
                graph.addEdge(inviter, invitee);
            }
            Set<Long> vertexSet = graph.vertexSet();
            if (CollectionUtil.isEmpty(vertexSet)) {
                return;
            }
            log.info("计算周邀请排行,图数据构建{}ms", watch.getTime());
            watch.reset();
            watch.start();
            BoolUserInviteCountWeekSnapshot snap;
            List<BoolUserInviteCountWeekSnapshot> snapList = Lists
                    .newArrayListWithCapacity(CollectionUtil.size(vertexSet));
            Map<Long, LocalDateTime> relationGroup = relationList.stream().collect(
                    Collectors.toMap(BoolUserInvitationRelation::getInviteeId, BoolUserInvitationRelation::getInvitationTime));
            for (Long userId : existInviters) {
                long levelOneCount = calculateDownlineAmounts(userId, graph, NumberConstants.ONE, inviteTimeMap,
                        relationGroup);
                long levelTwoCount = calculateDownlineAmounts(userId, graph, NumberConstants.TWO, inviteTimeMap,
                        relationGroup);
                Long amount = levelOneCount + levelTwoCount;
                if (amount <= NumberConstants.ZERO) {
                    continue;
                }
                snap = new BoolUserInviteCountWeekSnapshot();
                snap.setCreateUser(userId);
                snap.setUpdateUser(userId);
                snap.setUserId(userId);
                snap.setCreateTime(now);
                snap.setUpdateTime(now);
                snap.setAmount(amount);
                snap.setAmount1(levelOneCount);
                snap.setAmount2(levelTwoCount);
                snap.setCalculateTimestamp(timestamp);
                Long latestInviteTime = inviteTimeMap.get(userId);
                snap.setLatestInvitationTimestamp(latestInviteTime);
                snapList.add(snap);
            }
            log.info("计算周邀请排行,用户一、二级下线数量计算耗时{}ms", watch.getTime());
            watch.reset();
            watch.start();
            // 获取当前一级用户的上级用户，补全他的二级邀请数据
            if (CollectionUtil.isNotEmpty(existInviters)) {
                log.info("补充计算上级用户二级邀请数据,共计{}条", CollectionUtil.size(existInviters));
                LambdaQueryWrapper<BoolUserInvitationRelation> inviterRelationQuery = Wrappers.lambdaQuery();
                inviterRelationQuery.in(BoolUserInvitationRelation::getInviteeId, existInviters);
                inviterRelationQuery.select(BoolUserInvitationRelation::getInviterId, BoolUserInvitationRelation::getInviteeId,
                        BoolUserInvitationRelation::getInvitationTime);
                List<BoolUserInvitationRelation> topRelationList = userInvitationRelationMapper
                        .selectList(inviterRelationQuery);
                log.info("补充计算上级用户二级邀请数据,查询上级用户,共计{}条,耗时{}ms", CollectionUtil.size(existInviters), watch.getTime());
                watch.reset();
                watch.start();
                if (CollectionUtil.isNotEmpty(topRelationList)) {
                    int count = 0;
                    for (BoolUserInvitationRelation topRelation : topRelationList) {
                        Long topUserId = topRelation.getInviterId();
                        if (CollectionUtil.contains(existInviters, topUserId)) {
                            continue;
                        }
                        graph.addVertex(topUserId);
                        graph.addVertex(topRelation.getInviteeId());
                        graph.addEdge(topUserId, topRelation.getInviteeId());
                        Map<Long, LocalDateTime> tmpRelationGroup = relationList.stream().collect(Collectors.toMap(
                                BoolUserInvitationRelation::getInviteeId, BoolUserInvitationRelation::getInvitationTime));
                        relationGroup.putAll(tmpRelationGroup);
                        long levelTwoCount = calculateDownlineAmounts(topUserId, graph, NumberConstants.TWO,
                                inviteTimeMap, relationGroup);
                        Long amount = levelTwoCount;
                        if (amount <= NumberConstants.ZERO) {
                            continue;
                        }
                        snap = new BoolUserInviteCountWeekSnapshot();
                        snap.setCreateUser(topUserId);
                        snap.setUpdateUser(topUserId);
                        snap.setUserId(topUserId);
                        snap.setCreateTime(now);
                        snap.setUpdateTime(now);
                        snap.setAmount(amount);
                        snap.setAmount1(NumberConstants.ZERO_LONG);
                        snap.setAmount2(levelTwoCount);
                        snap.setCalculateTimestamp(timestamp);
                        Long latestInviteTime = inviteTimeMap.get(topUserId);
                        snap.setLatestInvitationTimestamp(latestInviteTime);
                        snapList.add(snap);
                        count++;
                    }
                    log.info("补充计算上级用户二级邀请数据,合格用户排行数据,共计{}条,耗时{}ms", count, watch.getTime());
                }
            }
            if (CollectionUtil.isEmpty(snapList)) {
                return;
            }
            watch.reset();
            watch.start();
            userServiceAdaptor.batchSaveUserWeekInviteCount(snapList);
            log.info("计算周邀请排行,用户一、二级数据入库耗时{}ms,数据量{}", watch.getTime(), CollectionUtil.size(snapList));
        } catch (Exception e) {
            log.error("周计算邀请排行错误", e);
        } finally {
            log.info("计算周邀请排行,全量耗时{}s", fullWatch.getTime(TimeUnit.SECONDS));
        }
    }

    public List<UserInviteRankResp> weekInviteHandle(UserInviteRankReq req) {
        StopWatch watch = StopWatch.createStarted();
        StopWatch fullWatch = StopWatch.createStarted();
        long timestamp = req.getTimestamp();
        // 查询上周一到本周一的邀请记录
        try {
            LocalDateTime now = LocalDateTime.now();
            Timestamp lastTimeStamp = new Timestamp(timestamp);
            LocalDateTime calculateMondayAtEight = lastTimeStamp.toLocalDateTime();
            LocalDateTime calculateNextMondayAtEight = calculateMondayAtEight.plusDays(7);
            List<BoolUserInvitationRelation> relationList = userInvitationRelationMapper
                    .weekRelationsByRange(calculateMondayAtEight, calculateNextMondayAtEight);
            log.info("计算周邀请排行,查询邀请记录耗时{}ms,数据共有{}条", watch.getTime(), CollectionUtil.size(relationList));
            if (CollectionUtil.isEmpty(relationList)) {
                return Lists.newArrayList();
            }
            Graph<Long, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
            Map<Long, Long> inviteTimeMap = Maps.newHashMap();
            Set<Long> existInviters = Sets.newHashSet();
            for (BoolUserInvitationRelation relation : relationList) {
                Long inviter = relation.getInviterId();
                existInviters.add(inviter);
                Long invitee = relation.getInviteeId();
                graph.addVertex(inviter);
                graph.addVertex(invitee);
                graph.addEdge(inviter, invitee);
            }
            Set<Long> vertexSet = graph.vertexSet();
            if (CollectionUtil.isEmpty(vertexSet)) {
                return Lists.newArrayList();
            }
            log.info("计算周邀请排行,图数据构建{}ms", watch.getTime());
            watch.reset();
            watch.start();
            BoolUserInviteCountWeekSnapshot snap;
            List<BoolUserInviteCountWeekSnapshot> snapList = Lists
                    .newArrayListWithCapacity(CollectionUtil.size(vertexSet));
            Map<Long, LocalDateTime> relationGroup = relationList.stream().collect(
                    Collectors.toMap(BoolUserInvitationRelation::getInviteeId, BoolUserInvitationRelation::getInvitationTime));
            for (Long userId : existInviters) {
                long levelOneCount = calculateDownlineAmounts(userId, graph, NumberConstants.ONE, inviteTimeMap,
                        relationGroup);
                long levelTwoCount = calculateDownlineAmounts(userId, graph, NumberConstants.TWO, inviteTimeMap,
                        relationGroup);
                Long amount = levelOneCount + levelTwoCount;
                if (amount <= NumberConstants.ZERO) {
                    continue;
                }
                snap = new BoolUserInviteCountWeekSnapshot();
                snap.setCreateUser(userId);
                snap.setUpdateUser(userId);
                snap.setUserId(userId);
                snap.setCreateTime(now);
                snap.setUpdateTime(now);
                snap.setAmount(amount);
                snap.setAmount1(levelOneCount);
                snap.setAmount2(levelTwoCount);
                snap.setCalculateTimestamp(timestamp);
                Long latestInviteTime = inviteTimeMap.get(userId);
                snap.setLatestInvitationTimestamp(latestInviteTime);
                snapList.add(snap);
            }
            log.info("计算周邀请排行,用户一、二级下线数量计算耗时{}ms", watch.getTime());
            watch.reset();
            watch.start();
            // 获取当前一级用户的上级用户，补全他的二级邀请数据
            if (CollectionUtil.isNotEmpty(existInviters)) {
                log.info("补充计算上级用户二级邀请数据,共计{}条", CollectionUtil.size(existInviters));
                LambdaQueryWrapper<BoolUserInvitationRelation> inviterRelationQuery = Wrappers.lambdaQuery();
                inviterRelationQuery.in(BoolUserInvitationRelation::getInviteeId, existInviters);
                inviterRelationQuery.select(BoolUserInvitationRelation::getInviterId, BoolUserInvitationRelation::getInviteeId,
                        BoolUserInvitationRelation::getInvitationTime);
                List<BoolUserInvitationRelation> topRelationList = userInvitationRelationMapper
                        .selectList(inviterRelationQuery);
                log.info("补充计算上级用户二级邀请数据,查询上级用户,共计{}条,耗时{}ms", CollectionUtil.size(existInviters), watch.getTime());
                watch.reset();
                watch.start();
                if (CollectionUtil.isNotEmpty(topRelationList)) {
                    int count = 0;
                    for (BoolUserInvitationRelation topRelation : topRelationList) {
                        Long topUserId = topRelation.getInviterId();
                        if (CollectionUtil.contains(existInviters, topUserId)) {
                            continue;
                        }
                        graph.addVertex(topUserId);
                        graph.addVertex(topRelation.getInviteeId());
                        graph.addEdge(topUserId, topRelation.getInviteeId());
                        Map<Long, LocalDateTime> tmpRelationGroup = relationList.stream().collect(Collectors.toMap(
                                BoolUserInvitationRelation::getInviteeId, BoolUserInvitationRelation::getInvitationTime));
                        relationGroup.putAll(tmpRelationGroup);
                        long levelTwoCount = calculateDownlineAmounts(topUserId, graph, NumberConstants.TWO,
                                inviteTimeMap, relationGroup);
                        Long amount = levelTwoCount;
                        if (amount <= NumberConstants.ZERO) {
                            continue;
                        }
                        snap = new BoolUserInviteCountWeekSnapshot();
                        snap.setCreateUser(topUserId);
                        snap.setUpdateUser(topUserId);
                        snap.setUserId(topUserId);
                        snap.setCreateTime(now);
                        snap.setUpdateTime(now);
                        snap.setAmount(amount);
                        snap.setAmount1(NumberConstants.ZERO_LONG);
                        snap.setAmount2(levelTwoCount);
                        snap.setCalculateTimestamp(timestamp);
                        Long latestInviteTime = inviteTimeMap.get(topUserId);
                        snap.setLatestInvitationTimestamp(latestInviteTime);
                        snapList.add(snap);
                        count++;
                    }
                    log.info("补充计算上级用户二级邀请数据,合格用户排行数据,共计{}条,耗时{}ms", count, watch.getTime());
                }
            }
            if (CollectionUtil.isEmpty(snapList)) {
                return Lists.newArrayList();
            }
            watch.reset();
            watch.start();
            userServiceAdaptor.batchSaveUserWeekInviteCount(snapList);
            log.info("计算周邀请排行,用户一、二级数据入库耗时{}ms,数据量{}", watch.getTime(), CollectionUtil.size(snapList));
        } catch (Exception e) {
            log.error("周计算邀请排行错误", e);
        } finally {
            log.info("计算周邀请排行,全量耗时{}s", fullWatch.getTime(TimeUnit.SECONDS));
        }
        Byte verified = 1;
        List<UserInviteRankResp> list = boolUserInviteCountWeekSnapshotMapper.queryTop500Week(timestamp, verified);
        List<UserInviteRankResp> result = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            int a = i + 1;
            UserInviteRankResp resp = list.get(i);
            resp.setRanking(Long.valueOf(a));
            result.add(resp);
            if (a > 10) {
                break;
            }
        }
        return result;
    }


    /**
     * 加载周榜数据到缓存
     */
    private void loadWeekCountToCache() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMondayAtEight = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.of(8, 0));
        long timestamp = thisMondayAtEight.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        Byte verified = 1;
        List<UserInviteRankResp> list = boolUserInviteCountWeekSnapshotMapper.queryTop500Week(timestamp, verified);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        ZSetOperations<String, Object> zsetOp = fastJsonRedisTemplate.opsForZSet();
        for (int i = 0; i < list.size(); i++) {
            int a = i + 1;
            UserInviteRankResp resp = list.get(i);
            resp.setRanking(Long.valueOf(a));
            zsetOp.removeRangeByScore(RedisKeyConstants.USER_WEEKLY_INVITE_RANK_ZSET, a, a);
            zsetOp.add(RedisKeyConstants.USER_WEEKLY_INVITE_RANK_ZSET, resp, a);
        }
    }

    /**
     * 初始化周排行结算job
     */
    @SneakyThrows
    private void initWeekRankSettleJobExecute() {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        Map<String, Object> diMap = Maps.newHashMap();
        diMap.put("applicationContext", applicationContext);
        JobDataMap jobDataMap = new JobDataMap(diMap);
        JobDetail job = JobBuilder.newJob(WeekSnapSettleJob.class).setJobData(jobDataMap)
                .withIdentity("weekInviteSettleJob", "bool-tg-interface-job").build();
        // 定义 Cron Trigger
        // 0 0 8 ? * 1
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("weekInviteSettleTrigger", "bool-tg-interface-job")
                .withSchedule(CronScheduleBuilder.cronSchedule(inviteSettleRankCron)).build();
        // 开始调度
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }
}
