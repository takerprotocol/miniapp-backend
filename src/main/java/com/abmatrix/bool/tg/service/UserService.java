package com.abmatrix.bool.tg.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.common.enuma.PersonalInfoTypeEnum;
import com.abmatrix.bool.tg.common.enuma.RewardType;
import com.abmatrix.bool.tg.common.enuma.RewardTypeEnum;
import com.abmatrix.bool.tg.common.model.vo.UserVo;
import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.abmatrix.bool.tg.dao.entity.BoolUserCustomInfo;
import com.abmatrix.bool.tg.dao.entity.BoolUserInvitationRelation;
import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.abmatrix.bool.tg.dao.mapper.BoolUserCustomInfoMapper;
import com.abmatrix.bool.tg.dao.mapper.UserInvitationRelationMapper;
import com.abmatrix.bool.tg.dao.service.IUserInvitationRelationService;
import com.abmatrix.bool.tg.dao.service.IUserRewardRecordService;
import com.abmatrix.bool.tg.dao.service.IUserService;
import com.abmatrix.bool.tg.middleware.redis.clients.ListRedisClient;
import com.abmatrix.bool.tg.middleware.redis.clients.SetRedisClient;
import com.abmatrix.bool.tg.middleware.redis.clients.SimpleRedisClient;
import com.abmatrix.bool.tg.middleware.telegram.BoolFamilyBot;
import com.abmatrix.bool.tg.model.req.UserInfoReq;
import com.abmatrix.bool.tg.model.req.UserRegisterReq;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.abmatrix.bool.tg.utils.AmountUtil.formatAmount;

/**
 * @author abm
 */
@Slf4j
@Service
public class UserService {

    private static final String SHA256_KEY = "WebAppData";

    @Value("${bool.premium.reward}")
    private Long premiumReward;
    @Value("${bool.og-regex}")
    private Long ogRex;
    @Value("${bool.og.reward}")
    private Long ogReward;
    @Value("${bool.inviter-code.length}")
    private int inviterCodeLength;
    @Value("${bool.inviter-code.source}")
    private String inviterCodeSource;
    @Value("${bool.inviter-reward.l1-ratio}")
    private BigDecimal inviterRewardL1Ratio;
    @Value("${bool.inviter-reward.l2-ratio}")
    private BigDecimal inviterRewardL2Ratio;
    @Value("${bool.family.bot-token}")
    private String token;
    @Value("${bool.create-task.key-set}")
    private String[] taskKeySet;
    @Resource
    private IUserService userDao;
    @Resource
    private IUserRewardRecordService rewardRecordService;
    @Resource
    private IUserInvitationRelationService relationService;
    /**
     * redis客户端
     */
    @Autowired
    private SimpleRedisClient simpleRedisClient;
    /**
     * 用户接口适配器
     */
    @Autowired
    private UserServiceAdaptor userServiceAdaptor;
    /**
     * set 缓存客户端
     */
    @Autowired
    private SetRedisClient setRedisClient;
    /**
     * 列表类型redis客户端
     */
    @Autowired
    private ListRedisClient listRedisClient;

    /**
     * 用户自定义信息mapper
     */
    @Autowired
    private BoolUserCustomInfoMapper boolUserCustomInfoMapper;

    /**
     * redisson客户端
     */
    @Autowired
    private RedissonClient redissonClient;
    @Resource
    private UserInvitationRelationMapper userInvitationRelationMapper;

    @Resource
    private BoolFamilyBot boolFamilyBot;


    @SneakyThrows
    public String register(UserRegisterReq req) {

        JSONObject userInfo = checkAndParseData(req.getData(), req.getHash());
//        if (userInfo.isEmpty()) {
//            return null;
//        }
        Assert.isFalse(userInfo.isEmpty(), "User identity verification failed.req={}",
                JSONObject.toJSONString(req));

        try {

            Long tgId = userInfo.getLong("id");
            String inviterCode = userInfo.getString("start_param");
            String username = userInfo.getString("username");
            Optional<BoolUser> inviterOpt = userDao.lambdaQuery().eq(BoolUser::getInvitationCode, inviterCode).oneOpt();

//            if (inviterOpt.isEmpty()) {
//                // 没有邀请码或者邀请码乱填不让注册
//                log.info("No inviter can not register");
//                return "No inviter can not register";
//            }

            // 1. 获取用户信息 与邀请人注册信息
            if (StringUtils.hasText(username)) {
                // 校验是否已经注册过了
                Optional<BoolUser> userOpt = userDao.lambdaQuery().eq(BoolUser::getUserTgId, tgId).oneOpt();
                if (userOpt.isPresent()) {
                    log.info("Already register");
                    return "Already register";
                }

                long userId = IdUtil.getSnowflakeNextId();

                // 2. 判断用户注册时间并进行分数判断
                var scorePair = calScore(tgId, userInfo.getBoolean("is_premium"));
                Long score = scorePair.getKey();

                // 3. 保存相关内容
                BoolUser boolUser = new BoolUser();
                boolUser.setId(userId);
                boolUser.setUserTgId(tgId);
                boolUser.setUsername(username);
                boolUser.setFirstName(userInfo.getString("first_name"));
                boolUser.setLastName(userInfo.getString("last_name"));
                boolUser.setInvitationCode(generateInvitationCode());

//                Long inviterId = inviterOpt.get().getId();
                Long inviterId = 0L;
                if (inviterOpt.isPresent()) {
                    inviterId = inviterOpt.get().getId();
                }

                // 保存邀请关系
                BoolUserInvitationRelation relation = new BoolUserInvitationRelation();
                relation.setId(IdUtil.getSnowflakeNextId());
                relation.setInviterId(inviterId);
                relation.setInviteeId(userId);
                relation.setInvitationTime(LocalDateTime.now());

                // 注册奖励
                BoolUserRewardRecord registerReward = new BoolUserRewardRecord();
                registerReward.setId(IdUtil.getSnowflakeNextId());
                registerReward.setUserId(userId);
                registerReward.setType(RewardType.REGISTER);
                registerReward.setRewardTime(LocalDateTime.now());
                registerReward.setTxTime(LocalDateTime.now());
                registerReward.setRewardValue(new BigDecimal(score));

                // 邀请奖励
                List<BoolUserRewardRecord> inviteRewardList = getInviteRewardList(inviterId, userId, score);

                boolUser.setAdditionalInfo(scorePair.getValue());

                userServiceAdaptor.batchSaveUserInfoAndOthers(boolUser, relation, registerReward, inviteRewardList);

                // 新人注册用户ID丢入地址生成处理队列
                if (taskKeySet.length > 0) {
                    int target = (int) (userId % taskKeySet.length);
                    String key = taskKeySet[target];
                    boolean success = false;
                    int maxRetry = 3;
                    int retried = 0;
                    while (!success) {
                        if (retried >= maxRetry) {
                            String errMsg = StrUtil.format("用户注册信息userId={}插入地址生成队列异常超过最大次数{},", userId, maxRetry);
                            log.error(errMsg);
                            break;
                        }
                        try {
                            listRedisClient.lpush(key, userId);
                            success = true;
                            log.info("用户注册信息userId={}插入地址生成队列key={}", userId, key);
                        } catch (Exception e) {
                            String errMsg = StrUtil.format("注册用户插入地址生成队列异常userId={}", userId);
                            log.error(errMsg, e);
                            retried++;
                        }
                    }

                }
                return String.valueOf(userId);
            } else {
                log.info("Register user error:[{}]", username);
                return "error username";
            }

        } catch (Exception e) {
            log.info("Register user error:[{}]-[{}]", JSONObject.toJSONString(req), e.getMessage());
            return e.getMessage();
        }
//        return null;
    }

    private List<BoolUserRewardRecord> getInviteRewardList(Long inviterId, long userId, Long score) {

        List<BoolUserRewardRecord> inviteRewardList = new ArrayList<>();
        // 生成 1，2 级奖励记录
        LocalDateTime now = LocalDateTime.now();
        BoolUserRewardRecord invite1Reward = new BoolUserRewardRecord();
        invite1Reward.setId(IdUtil.getSnowflakeNextId());
        invite1Reward.setCreateTime(now);
        invite1Reward.setUpdateTime(now);
        invite1Reward.setUserId(inviterId);
        invite1Reward.setType(RewardType.INVITATION);
        invite1Reward.setRewardTime(now);
        invite1Reward.setTxTime(now);
        invite1Reward.setRewardValue(inviterRewardL1Ratio.multiply(new BigDecimal(score)));
        invite1Reward.setAdditionInfo(String.valueOf(userId));
        inviteRewardList.add(invite1Reward);

        var relationQuery =
                Wrappers.<BoolUserInvitationRelation>lambdaQuery()
                        .eq(BoolUserInvitationRelation::getInviteeId, inviterId)
                        .select(BoolUserInvitationRelation::getInviterId)
                        .last(" LIMIT 1 ");
        BoolUserInvitationRelation relation2 = userInvitationRelationMapper.selectOne(relationQuery, false);

        if (relation2 != null) {
            BoolUserRewardRecord invite2Reward = new BoolUserRewardRecord();
            invite2Reward.setCreateTime(now);
            invite2Reward.setUpdateTime(now);
            invite2Reward.setId(IdUtil.getSnowflakeNextId());
            invite2Reward.setUserId(relation2.getInviterId());
            invite2Reward.setType(RewardType.INVITATION2);
            invite2Reward.setRewardTime(LocalDateTime.now());
            invite2Reward.setTxTime(LocalDateTime.now());
            invite2Reward.setRewardValue(inviterRewardL2Ratio.multiply(new BigDecimal(score)));
            invite2Reward.setAdditionInfo(String.valueOf(userId));
            inviteRewardList.add(invite2Reward);
        }

        return inviteRewardList;
    }


    public Pair<Long, String> calScore(Long userTgId, Boolean isPremium) {

        JSONObject scoreJson = new JSONObject();

        String tgId = String.valueOf(userTgId);

        String startId = tgId.length() <= 9 ? "0" + tgId.charAt(0) : tgId.substring(0, 2);
        long tgIdStart = Long.parseLong(startId);

        // 估算 Telegram 账号注册时间
        LocalDateTime registerTime = calRegisterTime(startId);
        long year = LocalDateTimeUtil.between(registerTime, LocalDateTime.now(), ChronoUnit.YEARS);

        boolean premium = isPremium != null && isPremium;
        boolean og = tgIdStart < ogRex;

        // 计算 账号年限奖励、 Telegram 付费会员奖励、OG 老用户奖励
        long ageScore = 20L;
        if (year > 0) {
            if (year == 1) {
                ageScore = 60L;
            } else {
                ageScore = year * 20 + 40;
            }
        }

        Long premiumScore = premium ? premiumReward : 0;
        Long ogScore = og ? ogReward : 0;
        Long score = ageScore + premiumScore + ogScore;

        scoreJson.fluentPut("registerTime", registerTime).fluentPut("age", year).fluentPut("ageScore", ageScore)
                .fluentPut("isPremium", premium).fluentPut("premiumScore", premiumScore).fluentPut("isOG", og)
                .fluentPut("ogScore", ogScore).fluentPut("score", score);

        return new Pair<>(score, scoreJson.toJSONString());
    }

    public LocalDateTime calRegisterTime(String startId) {

        LocalDateTime registerTime = LocalDateTime.now();
        try {

            JSONObject timeMap = JSON.parseObject(ResourceUtil.readUtf8Str("data.json"));
            if (timeMap.containsKey(startId)) {
                Date date = timeMap.getDate(startId);
                registerTime = DateUtil.toLocalDateTime(date);
            } else {
                // 查询离哪个 key 最近
                List<Long> collect = timeMap.keySet().stream().map(Long::valueOf).toList();
                Long key = findClosestValue(collect, Long.parseLong(startId));
                Date date = timeMap.getDate(String.valueOf(key));
                registerTime = DateUtil.toLocalDateTime(date);
            }
        } catch (Exception e) {
            log.info("Cal user register time error:[{}]", e.getMessage());
        }

        return registerTime;
    }

    public Long findClosestValue(List<Long> keyList, Long target) {
        Long closestValue = keyList.get(0);
        long minDiff = Math.abs(target - keyList.get(0));

        for (int i = 1; i < keyList.size(); i++) {
            long diff = Math.abs(target - keyList.get(i));
            if (diff < minDiff) {
                minDiff = diff;
                closestValue = keyList.get(i);
            }
        }

        return closestValue;
    }


    /**
     * 生成唯一邀请码
     *
     * @return String
     */
    public String generateInvitationCode() {
        String code = RandomUtil.randomString(inviterCodeSource, inviterCodeLength);
        Optional<BoolUser> userDO = userDao.lambdaQuery().eq(BoolUser::getInvitationCode, code).oneOpt();
        if (userDO.isPresent()) {
            return generateInvitationCode();
        }
        return code;
    }


    /**
     * 校验数据 文档地址: <a href=
     * "https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app">...</a>
     * Validating data received via the Mini App
     *
     * @param data data
     * @param hash hash
     * @return boolean
     */
    public JSONObject checkAndParseData(String data, String hash) {

        log.info("Check and parse data, data: [{}],hash: [{}]", JSONObject.toJSONString(data), hash);

        JSONObject result = new JSONObject();
        try {

            // HMAC-SHA-256 encode secret_key
            HMac keyMac = new HMac(HmacAlgorithm.HmacSHA256, SHA256_KEY.getBytes());
            byte[] digest = keyMac.digest(token);

            // HMAC-SHA-256 encode hex hash
            HMac hashMac = new HMac(HmacAlgorithm.HmacSHA256, digest);
            String encode = hashMac.digestHex(data);
            if (!encode.equals(hash)) {
                log.info("Hash is mismatch encode data. data: [{}], hash: [{}]", data, hash);
                return result;
            }

            String[] dataArray = data.split("\n");

            for (String s : dataArray) {
                String[] keyArray = s.split("=");
                if (keyArray.length > 1 && keyArray[1].startsWith("{")) {
                    JSONObject.parseObject(keyArray[1]).forEach(result::fluentPut);
                }
                if ("start_param".equals(keyArray[0])) {
                    result.fluentPut("start_param", keyArray[1]);
                }
            }
        } catch (Exception e) {
            log.info("Compare hash and data error, cause of: [{}]", e.getMessage());
        }

        return result;
    }

    public UserVo getUserInfo(Long tgId) {
        String key = org.apache.commons.lang3.StringUtils.join(RedisKeyConstants.USER_TG_KEY, tgId);
        Object resultObj = null;
        try {
            resultObj = simpleRedisClient.get(key);
        } catch (Exception e) {
        }
        if (resultObj != null) {
            String userJson = JSONObject.toJSONString(resultObj);
            try {
                return JSONObject.parseObject(userJson, UserVo.class);
            } catch (Exception e) {
                String errMsg = StrUtil.format("缓存获取用户信息异常userTgId={}", tgId);
                log.error(errMsg, e);
            }
        }
        Optional<BoolUser> opt = userDao.lambdaQuery().eq(BoolUser::getUserTgId, tgId).oneOpt();
//        if (opt.isEmpty()) {
//            return null;
//        }
        Assert.isFalse(opt.isEmpty(), "opt is Empty.tgId={}",
                JSONObject.toJSONString(tgId));

        BoolUser boolUser = opt.get();
        UserVo userVo = new UserVo();

        Long userId = boolUser.getId();
        userVo.setUserId(String.valueOf(userId));
        userVo.setEvmAddress(boolUser.getAddress());
        userVo.setUsername(boolUser.getUsername());
        userVo.setInviterCode(boolUser.getInvitationCode());
        userVo.setScore(boolUser.getAdditionalInfo());
        userVo.setRewardValue(formatAmount(boolUser.getRewardAmount()));
        Long inviterCount = relationService.lambdaQuery().eq(BoolUserInvitationRelation::getInviterId, userId).count();

        List<BoolUserRewardRecord> recordList = rewardRecordService.lambdaQuery().eq(BoolUserRewardRecord::getUserId, userId)
                .select(BoolUserRewardRecord::getRewardValue, BoolUserRewardRecord::getType).list();

        RewardTypeEnum[] values = RewardTypeEnum.values();

        Map<RewardTypeEnum, String> rewardMap = new HashMap<>(values.length);

        // 计算不同类别的逻辑
        for (RewardTypeEnum value : values) {
            List<RewardType> typeList = Arrays.stream(value.getElements()).toList();
            BigDecimal totalValue = recordList.stream().filter(x -> typeList.contains(x.getType()))
                    .map(BoolUserRewardRecord::getRewardValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

            rewardMap.put(value, formatAmount(totalValue));
        }

        userVo.setRewardMap(rewardMap);
        userVo.setInviterCount(inviterCount);
        String userRank = userDao.getUserRank(userId);
        log.info("userId={},userRank={}", userId, userRank);
        userVo.setRank(userRank);

        // 返回值新增个人链接
        LambdaQueryWrapper<BoolUserCustomInfo> customInfoQuery = Wrappers.lambdaQuery();
        customInfoQuery.eq(BoolUserCustomInfo::getUserId, userId);
        customInfoQuery.eq(BoolUserCustomInfo::getPersonalInfoType, PersonalInfoTypeEnum.URL);
        customInfoQuery.select(BoolUserCustomInfo::getId, BoolUserCustomInfo::getPersonalInfoValue);
        customInfoQuery.orderByDesc(BoolUserCustomInfo::getId);
        customInfoQuery.last(" LIMIT 1 ");
        BoolUserCustomInfo existCustomInfo = boolUserCustomInfoMapper.selectOne(customInfoQuery, Boolean.FALSE);
        if (existCustomInfo != null) {
            userVo.setPersonalUrl(existCustomInfo.getPersonalInfoValue());
        }

//        try {
////            simpleRedisClient.set(key, userVo, 30, TimeUnit.SECONDS);
//        } catch (Exception e) {
////            simpleRedisClient.set(key, userVo, 30, TimeUnit.SECONDS);
//        }
        return userVo;
    }

    /**
     * 查询用户信息之前先校验一下
     *
     * @param req
     * @return
     */
    public UserVo getUserInfoForStrict(UserInfoReq req) {
        String hash = req.getHash();
        Assert.notBlank(hash, "Identity identifier missing.");

        String data = req.getData();
        Assert.notBlank(data, "Identity identifier missing.");

        JSONObject checkUserResult = checkAndParseData(data, hash);
        Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.req={}",
                JSONObject.toJSONString(req));
        Long userTgId = checkUserResult.getLong("id");
        Assert.notNull(userTgId, "User identity parsing failed.");

        try {
            UserVo userInfo = getUserInfo(userTgId);
            updateUserInfo(userInfo, checkUserResult);
            return userInfo;
        } catch (Exception e) {
            log.error("Get user info error:[{}]", e.getMessage());
            return null;
        }
    }

    private void updateUserInfo(UserVo userVo, JSONObject checkUserResult) {

        if (userVo == null || !StringUtils.hasText(userVo.getUsername())) {
            return;
        }

        String username = checkUserResult.getString("username");

        // 代表 username 没变化 不用更新 后续再更新 first name, last name
        if (username.equals(userVo.getUsername())) {
            return;
        }

        userServiceAdaptor.updateUsername(username, userVo.getUserId());
    }

    /**
     * 查询用户 join group channel
     * @param req
     * @return
     */
    public ChatMember getChatMember(UserInfoReq req) {
        String hash = req.getHash();
        Assert.notBlank(hash, "Identity identifier missing.");

        String data = req.getData();
        Assert.notBlank(data, "Identity identifier missing.");

        JSONObject checkUserResult = checkAndParseData(data, hash);
        Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.req={}",
                JSONObject.toJSONString(req));
        Long userTgId = checkUserResult.getLong("id");
        Assert.notNull(userTgId, "User identity parsing failed.");

        ChatMember chatMember = boolFamilyBot.getChatMember(req.getChatId(), userTgId);
        return chatMember;
    }

}
