package com.abmatrix.bool.tg.task;

import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.abmatrix.bool.tg.dao.entity.BoolUserPrivateKeyFragmentInfo;
import com.abmatrix.bool.tg.dao.service.IUserService;
import com.abmatrix.bool.tg.middleware.js.JsService;
import com.abmatrix.bool.tg.service.UserServiceAdaptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * @author abm
 */
@Slf4j
@Component
@Profile({"prod", "test"})
public class UserTask {

    @Value("${bool.create-task.enable}")
    private boolean createEnable;

    @Value("${bool.create-task.key-set}")
    private String[] taskKeySet;

    @Resource
    private IUserService userDao;
    @Resource
    private JsService jobService;
    @Resource
    private UserServiceAdaptor userServiceAdaptor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(50);

    @PostConstruct
    private void init() {

        Arrays.stream(taskKeySet)
                .forEach(
                        task -> Executors.newSingleThreadScheduledExecutor()
                                .scheduleAtFixedRate(
                                        // 每 2s 根据对应队列里的 user id 进行地址生成
                                        () -> addressCreate(task), 0, 2, TimeUnit.SECONDS
                                )
                );

    }


    public void addressCreate(String key) {

        if (!createEnable) {
            return;
        }

        log.info("开始进行地址创建, key: [{}]", key);

        try {

            List<String> userIdList = redisTemplate.opsForList().leftPop(key, 200);

            if (null == userIdList || userIdList.isEmpty()) {
                log.info("key: [{}] 没有需要处理的用户地址", key);
                return;
            }

            List<BoolUser> boolUserList = userDao.lambdaQuery()
                    .isNull(BoolUser::getAddress)
                    .in(BoolUser::getId, userIdList)
                    .select(BoolUser::getId, BoolUser::getId)
                    .list();

            if (boolUserList.isEmpty()) {
                log.info("key: [{}] 没有需要创建的用户地址", key);
                return;
            }

            List<BoolUserPrivateKeyFragmentInfo> keyFragmentInfoList = new ArrayList<>(boolUserList.size());

            StopWatch watch = new StopWatch();
            watch.start("create address task");

            forkJoinPool.submit(
                    () -> boolUserList.forEach(user -> {

                                var evmAddressInfo = jobService.genEvmAddressInfo(user.getId());
                                if (StringUtils.hasText(evmAddressInfo.getKey())) {

                                    user.setAddress(evmAddressInfo.getKey());
                                    keyFragmentInfoList.add(evmAddressInfo.getValue());
                                } else {
                                    // 没生成成功重新放回 redis 中
                                    redisTemplate.opsForList().leftPush(key, String.valueOf(user.getId()));
                                }
                            }
                    )).join();

            watch.stop();
            log.info("key: [{}], 创建地址和碎片:[{}]-[{}]",
                    key,
                    boolUserList.size(),
                    keyFragmentInfoList.size()
            );

            if (!keyFragmentInfoList.isEmpty()) {
                watch.start("save address task");
                userServiceAdaptor.batchSaveKeyInfoAndUpdateUser(boolUserList, keyFragmentInfoList);
                watch.stop();
                log.info(
                        "[{}]-[{}] save address",
                        boolUserList.size(),
                        keyFragmentInfoList.size()
                );

                watch.start("expire user cache");
                List<String> keyList = boolUserList.parallelStream().map(x -> {
                    // 清空用户缓存
                    return RedisKeyConstants.USER_TG_KEY + x.getUserTgId();
                }).toList();

                redisTemplate.delete(keyList);
            }

            log.info(watch.prettyPrint());

        } catch (Exception e) {
            log.warn("key: [{}] 创建地址异常: [{}]", key, e.getMessage());
        }
    }



}
