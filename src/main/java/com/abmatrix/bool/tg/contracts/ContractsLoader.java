package com.abmatrix.bool.tg.contracts;

import cn.hutool.core.lang.Assert;
import com.abmatrix.bool.tg.config.configbean.StakeConfigProperties;
import com.abmatrix.bool.tg.config.configbean.StakeConfigProperties.ContractConfig;
import com.abmatrix.bool.tg.config.configbean.StakeConfigProperties.ContractConfigItem;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.util.Map;

/**
 * 合约loader
 * 
 * @author PeterWong
 * @param <T>
 * @param <T>
 * @date 2024年7月31日
 */
@Component
public class ContractsLoader<T extends Contract> {
	/**
	 * 全局web3j
	 */
	@Autowired
	@Qualifier("globalWeb3j")
	private Web3j globalWeb3j;
	/**
	 * 质押配置属性
	 */
	@Autowired
	private StakeConfigProperties stakeConfigProperties;
	/**
	 * 默认signerkey
	 */
	private static final String DEFAULT_SIGNER_KEY = "0x0";
	/**
	 * 默认gas provider
	 */
	private static final ContractGasProvider DEFAULT_GAS_PROVIDER = new StaticGasProvider(
			// 因为是我们自己的链，所以固定
			new BigInteger("25000"), new BigInteger("100000"));

	/**
	 * 合约容器
	 */
	private static final Map<Class<? extends Contract>, Contract> contractMap = Maps.newHashMap();

	/**
	 * 获取合约实例
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T get(Class<? extends Contract> clazz) {
		return (T) contractMap.get(clazz);
	}

	/**
	 * 初始化
	 */
	@PostConstruct
	private void init() {
		loadAndRegistStakeContract();
	}

	/**
	 * 加载并注册stake合约
	 */
	private void loadAndRegistStakeContract() {
		ContractConfig contracts = stakeConfigProperties.getContracts();
		Assert.notNull(contracts);
		ContractConfigItem miningConfig = contracts.getMining();
		Assert.notNull(miningConfig);
		String miningAddress = miningConfig.getAddress();
		Assert.notBlank(miningAddress);
		Boolean isValidAddress = WalletUtils.isValidAddress(miningAddress);
		Assert.isTrue(isValidAddress);
		Credentials credentials = Credentials.create(DEFAULT_SIGNER_KEY);
		Mining mining = Mining.load(miningAddress, globalWeb3j, credentials, DEFAULT_GAS_PROVIDER);
		contractMap.put(Mining.class, mining);
	}

}
