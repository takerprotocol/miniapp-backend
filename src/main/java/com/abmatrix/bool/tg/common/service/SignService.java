package com.abmatrix.bool.tg.common.service;

import com.abmatrix.bool.tg.dao.entity.BoolUserPrivateKeyFragmentInfo;
import com.abmatrix.bool.tg.dao.mapper.UserPrivateKeyFragmentInfoMapper;
import com.abmatrix.bool.tg.middleware.js.JsService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.util.Arrays;

/**
 * 通用签名交易所需接口
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Service
public class SignService {
    /**
     * js接口
     */
    @Autowired
    private JsService jsService;
    /**
     * 用户私钥碎片数据接口
     */
    @Autowired
    private UserPrivateKeyFragmentInfoMapper userPrivateKeyFragmentInfoMapper;

    /**
     * 执行签名并返回data
     *
     * @param transaction
     * @param privateKey1Fragment
     * @param privateKey2Fragment
     * @return
     */
    @SneakyThrows
    public String signTxLocalData(RawTransaction transaction, String privateKey1Fragment, String privateKey2Fragment) {
        byte[] signMessage = TransactionEncoder.encode(transaction);
        String signature = jsService.signTxData(Numeric.toHexString(signMessage), true, privateKey1Fragment,
                privateKey2Fragment);
        Sign.SignatureData signatureData = parseSignatureData(signature);
        byte[] encode = TransactionEncoder.encode(transaction, signatureData);
        signature = Numeric.toHexString(encode);
        return signature;
    }

    /**
     * 解析签名数据
     *
     * @param signature
     * @return
     */
    private Sign.SignatureData parseSignatureData(String signature) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }
        return new Sign.SignatureData(v, Arrays.copyOfRange(signatureBytes, 0, 32),
                Arrays.copyOfRange(signatureBytes, 32, 64));
    }


    /**
     * 查询用户私钥信息
     *
     * @param userId
     * @return
     */
    public BoolUserPrivateKeyFragmentInfo queryUserPrivateKeyFragmentInfo(Long userId) {
        LambdaUpdateWrapper<BoolUserPrivateKeyFragmentInfo> privateKeyFragmentInfoQuery = Wrappers.lambdaUpdate();
        privateKeyFragmentInfoQuery.eq(BoolUserPrivateKeyFragmentInfo::getUserId, userId);
        privateKeyFragmentInfoQuery.orderByDesc(BoolUserPrivateKeyFragmentInfo::getId);
        privateKeyFragmentInfoQuery.last(" LIMIT 1 ");
        BoolUserPrivateKeyFragmentInfo boolUserPrivateKeyFragmentInfo = userPrivateKeyFragmentInfoMapper
                .selectOne(privateKeyFragmentInfoQuery, Boolean.FALSE);
        return boolUserPrivateKeyFragmentInfo;
    }
}
