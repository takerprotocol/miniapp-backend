package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.common.enuma.AssignmentErrorEnum;
import com.abmatrix.bool.tg.common.enuma.AssignmentTypeEnum;
import com.abmatrix.bool.tg.common.enuma.RewardType;
import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import com.abmatrix.bool.tg.common.model.vo.UserVo;
import com.abmatrix.bool.tg.dao.entity.BoolAssignment;
import com.abmatrix.bool.tg.dao.entity.BoolAssignmentUserRelation;
import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.abmatrix.bool.tg.dao.mapper.BoolAssignmentMapper;
import com.abmatrix.bool.tg.dao.mapper.BoolAssignmentUserRelationMapper;
import com.abmatrix.bool.tg.dao.service.IBoolAssignmentService;
import com.abmatrix.bool.tg.middleware.redis.clients.HashRedisClient;
import com.abmatrix.bool.tg.middleware.redis.clients.SimpleRedisClient;
import com.abmatrix.bool.tg.model.req.AssignmentReq;
import com.abmatrix.bool.tg.model.resp.AssignmentResp;
import com.abmatrix.bool.tg.service.AssignmentService;
import com.abmatrix.bool.tg.service.AssignmentServiceAdaptor;
import com.abmatrix.bool.tg.service.UserService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 任务接口
 *
 * @author PeterWong
 * @date 2024年8月3日
 */
@Service
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {


    @Resource
    @Lazy
    private UserService userService;
    @Resource
    private BoolAssignmentMapper boolAssignmentMapper;
    @Resource
    private BoolAssignmentUserRelationMapper boolAssignmentUserRelationMapper;
    @Resource
    private IBoolAssignmentService assignmentService;
    /**
     * 任务主表map
     */
    @Autowired
    private static final Map<Long, BoolAssignment> assignmmentMap = new ConcurrentHashMap<>();
    /**
     * 声明任务主锁
     */
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    /**
     * 声明任务读锁
     */
    private static final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    /**
     * 声明任务写锁
     */
    private static final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    /**
     * 任务事务接口适配器
     */
    @Resource
    private AssignmentServiceAdaptor assignmentServiceAdaptor;
    /**
     * redisson客户端
     */
    @Resource
    private RedissonClient redissonClient;
    /**
     * redis客户端
     */
    @Resource
    private SimpleRedisClient simpleRedisClient;
    /**
     * hash 缓存处理器
     */
    @Resource
    private HashRedisClient hashRedisClient;

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        try {
            fixAssignmentMap();
        } catch (Exception e) {
            log.error("任务service初始化任务列表异常", e);
        }

        Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(() -> {
                    try {
                        fixAssignmentMap();
                    } catch (Exception e) {
                        log.error("任务service初始化任务列表异常1", e);
                    }
                }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 填充任务map
     */
    private void fixAssignmentMap() {
        LambdaQueryWrapper<BoolAssignment> query = Wrappers.lambdaQuery();
        query.eq(BoolAssignment::getOnline, NumberConstants.ONE);
        List<BoolAssignment> list = boolAssignmentMapper.selectList(query);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        writeLock.lock();
        try {
            assignmmentMap.clear();
            for (BoolAssignment assignment : list) {
                assignmmentMap.put(assignment.getId(), assignment);
            }
        } catch (Exception e) {
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public List<AssignmentResp> queryAssignments(AssignmentReq req) {

        UserVo user = validReq(req.getHash(), req.getData());

        List<BoolAssignment> list = Lists.newArrayList();
        readLock.lock();
        if (MapUtil.isEmpty(assignmmentMap)) {
            readLock.unlock();
            return Lists.newArrayList();
        }
        try {
            assignmmentMap.forEach((key, assignment) -> list.add(assignment));

            CollectionUtil.sort(list, (assignment0, assignment1) -> {
                Boolean top0 = assignment0.getTop();
                Boolean top1 = assignment1.getTop();
                if (top0 && !top1) {
                    return -NumberConstants.ONE;
                } else if (top1 && !top0) {
                    return NumberConstants.ONE;
                } else if (top1 && top0) {
                    LocalDateTime time0 = assignment0.getTopTime();
                    LocalDateTime time1 = assignment1.getTopTime();
                    int timeCompare = time0.compareTo(time1);
                    return -timeCompare;
                } else {
                    Integer sort0 = assignment0.getSortField();
                    Integer sort1 = assignment1.getSortField();
                    return sort0 - sort1;
                }
            });
        } catch (Exception e) {
            log.warn("转换任务执行异常", e);
        } finally {
            readLock.unlock();
        }
        String userIdStr = user.getUserId();
        Long userId = Long.valueOf(userIdStr);
        LambdaQueryWrapper<BoolAssignmentUserRelation> relationQuery = Wrappers.lambdaQuery();
        relationQuery.eq(BoolAssignmentUserRelation::getUserId, userId);
        relationQuery.select(BoolAssignmentUserRelation::getUserId, BoolAssignmentUserRelation::getAssignmentId,
                BoolAssignmentUserRelation::getCreateTime);
        List<BoolAssignmentUserRelation> relationList = boolAssignmentUserRelationMapper.selectList(relationQuery);
        Map<Long, Long> relationMap = relationList.stream().collect(
                Collectors.toMap(BoolAssignmentUserRelation::getAssignmentId, BoolAssignmentUserRelation::getUserId));
        Map<Long, LocalDateTime> compeleteTimeMap = relationList.stream().collect(Collectors
                .toMap(BoolAssignmentUserRelation::getAssignmentId, BoolAssignmentUserRelation::getCreateTime));
        String boolProject = "bool";
        LocalDateTime now = LocalDateTime.now();
        return list.stream().map(assignment -> {
            AssignmentResp resp = new AssignmentResp();
            Long assignmentId = assignment.getId();
            resp.setAssignmentId(assignmentId);
            resp.setTitle(assignment.getTitle());
            resp.setDescribe(assignment.getAssignmentDesc());
            resp.setUrl(assignment.getUrl());
            resp.setSort(assignment.getSortField());
            Long doneUserId = relationMap.get(assignmentId);
            Boolean done = doneUserId != null;
            resp.setDone(done);
            if (done) {
                LocalDateTime compeleteTime = compeleteTimeMap.get(assignmentId);
                resp.setCompleteTime(compeleteTime);
            }
            BigDecimal reward = assignment.getRewardValue();
            if (reward != null) {
                reward = reward.stripTrailingZeros();
                resp.setReward(reward.toPlainString());
            } else {
                resp.setReward(NumberConstants.ZERO_STR);
            }
            resp.setProject(assignment.getProject());
            try {
                Object compelteCountObj = hashRedisClient.hget(RedisKeyConstants.ASSIGNMENT_COMPELTE_COUNT,
                        String.valueOf(assignmentId));
                if (compelteCountObj != null) {
                    if (compelteCountObj instanceof String) {
                        String compelteCountStr = (String) compelteCountObj;
                        resp.setComplete(compelteCountStr);
                    } else if (compelteCountObj instanceof Number) {
                        Number n = (Number) compelteCountObj;
                        String compelteCountStr = String.valueOf(n);
                        resp.setComplete(compelteCountStr);
                    }
                }
            } catch (Exception e) {
            }
            resp.setLogo(assignment.getLogo());
            resp.setTop(assignment.getTop());
            // 获取系统默认的时区ID
            ZoneId systemZoneId = ZoneId.systemDefault();
            // 将系统时区ID转换为当前时间的ZoneOffset
            LocalDateTime localDateTime = LocalDateTime.now();
            ZoneOffset systemZoneOffset = systemZoneId.getRules().getOffset(localDateTime);
            Long timeStamp = assignment.getUpdateTime().toEpochSecond(systemZoneOffset) * 1000;
            resp.setTimestamp(timeStamp);
            return resp;
        }).filter(resp -> {
            String project = resp.getProject();
            if (StringUtils.equalsIgnoreCase(boolProject, project)) {
                return Boolean.TRUE;
            }
            LocalDateTime compelteTime = resp.getCompleteTime();
            if (compelteTime == null) {
                return Boolean.TRUE;
            }
            Boolean done = resp.getDone();
            if (!done) {
                return Boolean.TRUE;
            }
            Duration duration = Duration.between(compelteTime, now);
            if (duration.compareTo(Duration.ofHours(1)) > NumberConstants.ZERO) {
                return Boolean.FALSE;
            }
            resp.setCompleteTime(null);
            return Boolean.TRUE;
        }).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public ResultVo<Boolean> completeAssignment(AssignmentReq req) {

        ResultVo<Boolean> result;
        String hash = req.getHash();
        String data = req.getData();
        try {
            Assert.notBlank(hash, "Identity identifier missing.");
            Assert.notBlank(data, "Identity identifier missing.");
        } catch (Exception e) {
            result = ResultVo.fail(AssignmentErrorEnum.TG_IDENTIFIER_LOST.getCode(),
                    AssignmentErrorEnum.TG_IDENTIFIER_LOST.getMessage(), Boolean.FALSE);
            return result;
        }
        Long assignmentId = req.getAssignmentId();
        Assert.notNull(assignmentId, "Task ID cannot be null.");
        JSONObject checkUserResult = userService.checkAndParseData(data, hash);
        try {
            Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.");
        } catch (Exception e) {
            result = ResultVo.fail(AssignmentErrorEnum.USER_UN_VERIFY.getCode(),
                    AssignmentErrorEnum.USER_UN_VERIFY.getMessage(), Boolean.FALSE);
            return result;
        }
        Long userTgId = checkUserResult.getLong("id");
        try {
            Assert.notNull(userTgId, "User identity parsing failed.");
        } catch (Exception e) {
            result = ResultVo.fail(AssignmentErrorEnum.USER_UN_VERIFY.getCode(),
                    AssignmentErrorEnum.USER_UN_VERIFY.getMessage(), Boolean.FALSE);
            return result;
        }
        UserVo user = userService.getUserInfo(userTgId);
        try {
            Assert.notNull(user, "Current user does not exist.");
        } catch (Exception e) {
            result = ResultVo.fail(AssignmentErrorEnum.USER_INFO_LOST.getCode(),
                    AssignmentErrorEnum.USER_INFO_LOST.getMessage(), Boolean.FALSE);
            return result;
        }

        String userIdStr = user.getUserId();
        Long userId = Long.valueOf(userIdStr);
        String lockKey = StringUtils.join(RedisKeyConstants.USER_ASSIGNMENT_LOCK, userId);
        RLock lock = redissonClient.getFairLock(lockKey);
        if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
            log.warn("未抢占到分布式锁userId={}", userId);
            result = ResultVo.fail(AssignmentErrorEnum.OPERATION_BUSY.getCode(),
                    AssignmentErrorEnum.OPERATION_BUSY.getMessage(), Boolean.FALSE);
            return result;
        }
        try {
            // 组装奖励发放表
            BoolAssignment assignment = boolAssignmentMapper.selectById(assignmentId);
            Assert.notNull(assignment, "The corresponding task does not exist.");

            AssignmentTypeEnum type = assignment.getAssignmentType();

            // 判断是否其实已经完成该任务，分为两种类别的活动，一次性活动 和 每日一次性活动
            BoolAssignmentUserRelation existRelation = null;

            // 当前任务类型为 每日任务
            if (type == AssignmentTypeEnum.DAILY) {
                // 判断是否其实已经完成该任务
                LambdaQueryWrapper<BoolAssignmentUserRelation> relationQuery = Wrappers.lambdaQuery();
                relationQuery.eq(BoolAssignmentUserRelation::getAssignmentId, assignmentId);
                relationQuery.eq(BoolAssignmentUserRelation::getUserId, userId);
                relationQuery.orderByDesc(BoolAssignmentUserRelation::getCreateTime);
                relationQuery.select(BoolAssignmentUserRelation::getCreateTime);
                relationQuery.last(" LIMIT 1 ");
                existRelation = boolAssignmentUserRelationMapper.selectOne(relationQuery,
                        Boolean.FALSE);
                if (existRelation != null) {
                    LocalDateTime createTime = existRelation.getCreateTime();
                    Duration duration = Duration.between(createTime, DateTime.now().toLocalDateTime());
                    if (duration.compareTo(Duration.ofDays(1)) <= NumberConstants.ZERO) {
                        log.warn("当前用户已完成每日活动，该任务已完成userId={},assignmentId={}", userId, assignmentId);
                        result = ResultVo.success(Boolean.TRUE);
                        return result;
                    }
                }
            } else {
                LambdaQueryWrapper<BoolAssignmentUserRelation> relationQuery = Wrappers.lambdaQuery();
                relationQuery.eq(BoolAssignmentUserRelation::getAssignmentId, assignmentId);
                relationQuery.eq(BoolAssignmentUserRelation::getUserId, userId);
                relationQuery.select(BoolAssignmentUserRelation::getId);
                relationQuery.last(" LIMIT 1 ");
                existRelation = boolAssignmentUserRelationMapper.selectOne(relationQuery,
                        Boolean.FALSE);
                if (existRelation != null) {
                    log.warn("当前用户该任务已完成userId={},assignmentId={}", userId, assignmentId);
                    result = ResultVo.success(Boolean.TRUE);
                    return result;
                }
            }

            // 组装完成任务表
            BoolAssignmentUserRelation relation = new BoolAssignmentUserRelation();
            relation.setAssignmentId(req.getAssignmentId());
            relation.setUserId(userId);
            relation.setCreateUser(userId);
            relation.setUpdateUser(userId);
            BoolUserRewardRecord rewardRecord = new BoolUserRewardRecord();
            rewardRecord.setId(IdUtil.getSnowflakeNextId());
            rewardRecord.setUserId(userId);
            switch (type) {
                case TWITTER: {
                    rewardRecord.setType(RewardType.TWITTER);
                    break;
                }
                case COMMUNITY: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case LAUNCH_BOT: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case PLAY_GAME: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case PARTICIPATE_CAMPAIGN: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case VISIT_WEBSITE: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case SUBSCRIBE_YOUTUBE: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case READ_MEDIUM: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case BOOST: {
                    rewardRecord.setType(RewardType.JOIN_COMMUNITY);
                    break;
                }
                case DAILY: {
                    rewardRecord.setType(RewardType.DAILY_TASK);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unexpected task type: " + type);
            }

            rewardRecord.setRewardTime(LocalDateTime.now());
            rewardRecord.setTxTime(LocalDateTime.now());
            rewardRecord.setRewardValue(assignment.getRewardValue());
            String flag = assignment.getAssignmentFlag();
            if (StringUtils.isBlank(flag)) {
                flag = StringUtils.lowerCase(assignment.getName());
            }

            rewardRecord.setAdditionInfo(flag);
            assignmentServiceAdaptor.doAssignmentComplete(userId, assignmentId, relation, rewardRecord);
        } catch (Exception e) {
            String errMsg = StrUtil.format("完成任务异常userId={},assignmentId={}", userId, assignmentId);
            log.error(errMsg, e);
            result = ResultVo.fail(AssignmentErrorEnum.LOGIC_ERR.getCode(), AssignmentErrorEnum.LOGIC_ERR.getMessage(),
                    Boolean.FALSE);
            return result;
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        String key = StringUtils.join(RedisKeyConstants.USER_TG_KEY, userTgId);
        try {
            simpleRedisClient.expire(key, 0);
        } catch (Exception e) {
            simpleRedisClient.expire(key, 0);
        }
        result = ResultVo.success(Boolean.TRUE);
        return result;
    }

    private UserVo validReq(String hash, String data) {

        Assert.notBlank(hash, "Identity identifier missing.");
        Assert.notBlank(data, "Identity identifier missing.");
        JSONObject checkUserResult = userService.checkAndParseData(data, hash);
        Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.");
        Long userTgId = checkUserResult.getLong("id");
        Assert.notNull(userTgId, "User identity parsing failed.");
        UserVo user = userService.getUserInfo(userTgId);
        Assert.notNull(user, "Current user does not exist.");
        return user;
    }

}
