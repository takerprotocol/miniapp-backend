package com.abmatrix.bool.tg.common.enuma;

import lombok.Getter;

/**
 * @author abm
 */
@Getter
public enum PrivateKeyType {
    /**
     * Mpc钱包类型
     */
    EVM("ECDSA"),
    APTOS(""),
    SOLANA(""),
    BITCOIN(""),
    DOGECOIN(""),
    FILECOIN(""),
    STARKNET(""),
    NEAR(""),
    TRON(""),
    TON("EDDSA"),
    ;

    private final String engine;

    PrivateKeyType(String engine) {
        this.engine = engine;
    }
}
