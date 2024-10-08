package com.abmatrix.bool.tg.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * web3j全局配置
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
@Configuration
public class We3jConfig {

    @Value("${bool.web3.rpc}")
    private String rpc;

    /**
     * 全局web3j
     *
     * @return
     */
    @Bean(name = "globalWeb3j")
    public Web3j globalWeb3j() {
        ConnectionPool connectionPool = new ConnectionPool(1500, 5L, TimeUnit.MINUTES);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(Duration.ofSeconds(30));
        builder.readTimeout(Duration.ofSeconds(60));
        builder.writeTimeout(Duration.ofSeconds(60));
        builder.setConnectionPool$okhttp(connectionPool);
        OkHttpClient okhttpClient = builder.build();
        return Web3j.build(new HttpService(rpc, okhttpClient));
    }
}