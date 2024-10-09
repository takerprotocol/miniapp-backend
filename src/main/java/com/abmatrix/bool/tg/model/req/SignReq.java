package com.abmatrix.bool.tg.model.req;

import lombok.Data;
import org.web3j.crypto.RawTransaction;

import java.io.Serializable;
import java.util.List;

/**
 * 质押请求对象
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Data
public class SignReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * raw transaction data
     */
    private RawTransaction transaction;
    /**
     * 用户加密hash
     */
    private String hash;
    /**
     * 校验data
     */
    private String data;
}