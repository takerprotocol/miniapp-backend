package com.abmatrix.bool.tg.config.configbean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * stake配置
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Data
@ConfigurationProperties(prefix = "stake")
@Component
public class StakeConfigProperties implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 合约配置明细
     */
    private ContractConfig contracts;

    @Data
    public static class ContractConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * stake合约配置
         */
        private ContractConfigItem mining;
    }

    /**
     * 合约配置项
     *
     * @author PeterWong
     * @date 2024年7月31日
     */
    @Data
    public static class ContractConfigItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String address;
    }
}