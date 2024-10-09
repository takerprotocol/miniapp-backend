package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.lang.Assert;
import com.abmatrix.bool.tg.common.model.vo.UserVo;
import com.abmatrix.bool.tg.common.service.SignService;
import com.abmatrix.bool.tg.dao.entity.BoolUserPrivateKeyFragmentInfo;
import com.abmatrix.bool.tg.model.req.SignReq;
import com.abmatrix.bool.tg.service.SignServiceAdaptor;
import com.abmatrix.bool.tg.service.UserService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.RawTransaction;

/**
 * 质押接口实现类
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Service
@Slf4j
public class SignServiceAdaptorImpl implements SignServiceAdaptor {
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

    @Override
    public String signTx(SignReq req) {
        String hash = req.getHash();
        String data = req.getData();
        RawTransaction transaction = req.getTransaction();
        Assert.notBlank(hash, "Identity identifier missing.");
        Assert.notBlank(data, "Identity identifier missing.");
        Assert.notNull(transaction, "Transaction data missing.");

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

        BoolUserPrivateKeyFragmentInfo userPrivateKeyFragmentInfo = signService.queryUserPrivateKeyFragmentInfo(userId);
        Assert.notNull(userPrivateKeyFragmentInfo, "user's pk fragment is null.");

        return signService.signTxLocalData(transaction,
                userPrivateKeyFragmentInfo.getPrivateKey1Fragment(),
                userPrivateKeyFragmentInfo.getPrivateKey2Fragment());
    }

}