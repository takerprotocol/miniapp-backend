package com.abmatrix.bool.tg.task.job;

import cn.hutool.core.collection.CollectionUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSettlementFlow;
import com.abmatrix.bool.tg.dao.mapper.BoolUserInviteCountWeekSnapshotMapper;
import com.abmatrix.bool.tg.model.resp.UserInviteRankResp;
import com.abmatrix.bool.tg.service.UserServiceAdaptor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 周榜结算job
 *
 * @author PeterWong
 * @date 2024年8月28日
 */
@Slf4j
public class WeekSnapSettleJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StopWatch watch = StopWatch.createStarted();
        Object app = context.getMergedJobDataMap().get("applicationContext");
        ApplicationContext applicationContext = (ApplicationContext) app;
        RedissonClient redissonClient = applicationContext.getBean(RedissonClient.class);
        UserServiceAdaptor userServiceAdaptor = applicationContext.getBean(UserServiceAdaptor.class);
        BoolUserInviteCountWeekSnapshotMapper boolUserInviteCountWeekSnapshotMapper = applicationContext
                .getBean(BoolUserInviteCountWeekSnapshotMapper.class);

        log.info("周排行榜奖励结算开始,依赖准备耗时{}ms", watch.getTime());
        watch.reset();
        watch.start();
        // 执行结算
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMondayAtEight = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.of(8, 0));
        // 获取上周一早上 8 点，通过减去7天
        LocalDateTime lastMonday8AM = thisMondayAtEight.minusDays(7);
        long lastimestamp = lastMonday8AM.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        Byte verified = 1;

        log.info("周排行榜奖励结算配置项检查耗时{}ms", watch.getTime());
        watch.reset();
        watch.start();
        RLock lock = redissonClient.getFairLock(RedisKeyConstants.USER_WEEKLY_INVITE_RANK_SETTLE_LOCK);
        try {
            if (lock.tryLock(0, 1, TimeUnit.MINUTES)) {
                try {
                    List<UserInviteRankResp> weekList = boolUserInviteCountWeekSnapshotMapper
                            .queryTop500Week(lastimestamp, verified);
                    if (CollectionUtil.isEmpty(weekList)) {
                        return;
                    }
                    log.info("周排行榜奖励结算拽取快照数据{}条,耗时{}ms", CollectionUtil.size(weekList), watch.getTime());
                    watch.reset();
                    watch.start();
                    List<BoolUserInviteCountWeekSettlementFlow> settleList = Lists.newArrayList();
                    for (int i = 0; i < weekList.size(); i++) {
                        int a = i + 1;
                        if (a > NumberConstants.TEN) {
                            break;
                        }
                        UserInviteRankResp resp = weekList.get(i);
                        resp.setRanking(Long.valueOf(a));
                        BoolUserInviteCountWeekSettlementFlow settle = new BoolUserInviteCountWeekSettlementFlow();
                        Long userId = resp.getUserId();
                        settle.setCreateUser(userId);
                        settle.setUpdateUser(userId);
                        settle.setUserId(userId);
                        settle.setAmount(resp.getAmount());
                        settle.setCalculateTimestamp(lastimestamp);
                        settle.setSnapshotId(resp.getSnapId());
                        settle.setAward(NumberConstants.ZERO_STR);

                        settle.setRank(a);
                        settleList.add(settle);
                    }
                    log.info("周排行榜奖励结算交割数据处理{}条,耗时{}ms", CollectionUtil.size(settleList), watch.getTime());
                    watch.reset();
                    watch.start();
                    userServiceAdaptor.batchSaveWeekSettleResult(settleList);
                    log.info("周排行榜奖励结算交割数据入库{}条,耗时{}ms", CollectionUtil.size(settleList), watch.getTime());
                } catch (Exception e) {
                    log.error("本次周排行结算异常", e);
                } finally {
                    if (lock.isLocked()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("结算周排行异常", e);
        }

    }

}
