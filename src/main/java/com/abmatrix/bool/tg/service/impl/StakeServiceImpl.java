package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.abmatrix.bool.tg.common.model.vo.UserVo;
import com.abmatrix.bool.tg.common.service.SignService;
import com.abmatrix.bool.tg.contracts.ContractsLoader;
import com.abmatrix.bool.tg.contracts.Mining;
import com.abmatrix.bool.tg.dao.entity.BoolStakeApplyRecord;
import com.abmatrix.bool.tg.dao.entity.BoolUserPrivateKeyFragmentInfo;
import com.abmatrix.bool.tg.dao.mapper.BoolStakeApplyRecordMapper;
import com.abmatrix.bool.tg.model.req.StakeReq;
import com.abmatrix.bool.tg.service.StakeService;
import com.abmatrix.bool.tg.service.UserService;
import com.abmatrix.bool.tg.utils.DecimalUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 质押接口实现类
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Service
@Slf4j
public class StakeServiceImpl implements StakeService {
    /**
     * contract loader
     */
    @Autowired
    private ContractsLoader<Mining> contractsLoader;

    /**
     * 全局web3j
     */
    @Autowired
    @Qualifier("globalWeb3j")
    private Web3j golbalWeb3j;

    /**
     * 用户接口
     */
    @Autowired
    private UserService userService;
    /**
     * 通用签名接口
     */
    @Autowired
    private SignService signService;

    @Autowired
    private BoolStakeApplyRecordMapper stakeApplyRecordMapper;

    @Override
    public String stake(StakeReq req) {
        List<String> deviceIds = req.getDeviceId();
        Assert.notEmpty(deviceIds, "Device ID cannot be empty.");
        List<String> amounts = req.getAmount();
        Assert.notEmpty(amounts, "Amount cannot be empty.");
        List<BigDecimal> values = Lists.newArrayList();
        for (String amount : amounts) {
            Assert.notBlank(amount, "Amount cannot be empty.");
            BigDecimal value = null;
            try {
                value = new BigDecimal(amount);
            } catch (Exception e) {
                String errMsg = StrUtil.format("质押数量转换异常[amount={}]", amount);
                log.error(errMsg, e);
                throw e;
            }
            Assert.isTrue(value.compareTo(BigDecimal.ZERO) >= NumberConstants.ZERO, "Amount must be greater than 0.");
            values.add(value);
        }
        String hash = req.getHash();
        String data = req.getData();
        Assert.notBlank(hash, "Identity identifier missing.");
        Assert.notBlank(data, "Identity identifier missing.");
        JSONObject checkUserResult = userService.checkAndParseData(data, hash);
        Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.");
        Long userTgId = checkUserResult.getLong("id");
        Assert.notNull(userTgId, "User identity parsing failed.");
        UserVo user = userService.getUserInfo(userTgId);
        Assert.notNull(user, "Current user does not exist.");
        String evmAddress = user.getEvmAddress();
        Assert.notBlank(evmAddress, "Current user does not have an EVM address.");
        String userIdStr = user.getUserId();
        Long userId = Long.valueOf(userIdStr);
        String signedData = signStakeTx(deviceIds, userId, values, evmAddress);
        saveStakeApplyRecord(userId, amounts, deviceIds, evmAddress);
        return signedData;
    }

    /**
     * 签名质押交易
     *
     * @param deviceIds
     * @param userId
     * @param values
     * @param address
     * @return
     */
    @SneakyThrows
    private String signStakeTx(List<String> deviceIds, Long userId, List<BigDecimal> values, String address) {
        BoolUserPrivateKeyFragmentInfo userPrivateKeyFragmentInfo = signService.queryUserPrivateKeyFragmentInfo(userId);
        Assert.notNull(userPrivateKeyFragmentInfo, "user's pk fragment is null.");
        List<byte[]> ids = Lists.newArrayList();
        for (String deviceId : deviceIds) {
            byte[] id = Numeric.hexStringToByteArray(deviceId);
            ids.add(id);
        }
        List<BigInteger> amounts = Lists.newArrayList();
        for (BigDecimal value : values) {
            BigDecimal stakeValue = DecimalUtil.minUnitQuantity(value, 18);
            BigInteger amount = stakeValue.toBigInteger();
            amounts.add(amount);
        }
        int idSize = CollectionUtil.size(ids);
        int amountsSize = CollectionUtil.size(amounts);
        if (idSize != amountsSize) {
            Assert.isTrue(false, "value not match.");
        }
        Mining wrapper = contractsLoader.get(Mining.class);
        String data = wrapper.updateVotes(ids, amounts).encodeFunctionCall();
        EthGasPrice send = golbalWeb3j.ethGasPrice().send();
        BigInteger gasPrice = send.getGasPrice();
        BigInteger nonce = golbalWeb3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send()
                .getTransactionCount();
        RawTransaction transaction = RawTransaction.createTransaction(nonce, gasPrice,
                // 因为是我们自己的链，所以固定
                new BigInteger("100000"), wrapper.getContractAddress(), data);
        String signedData = signService.signTxLocalData(transaction,
                userPrivateKeyFragmentInfo.getPrivateKey1Fragment(),
                userPrivateKeyFragmentInfo.getPrivateKey2Fragment());
        return signedData;
    }

    /**
     * 保存质押申请data记录
     *
     * @param userId
     * @param amounts
     * @param deviceIds
     * @param address
     */
    @SneakyThrows
    private void saveStakeApplyRecord(Long userId, List<String> amounts, List<String> deviceIds, String address) {
        BoolStakeApplyRecord record = new BoolStakeApplyRecord();
        String amountsStr = StringUtils.join(amounts, StrUtil.COMMA);
        record.setAmounts(amountsStr);
        String deviceIdStr = StringUtils.join(deviceIds, StrUtil.COMMA);
        record.setDeviceids(deviceIdStr);
        record.setCreateUser(userId);
        record.setUpdateUser(userId);
        record.setUserId(userId);
        record.setAddress(address);
        LocalDateTime now = LocalDateTime.now();
        record.setCreateTime(now);
        stakeApplyRecordMapper.insert(record);
    }

}