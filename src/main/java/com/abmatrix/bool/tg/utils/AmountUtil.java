package com.abmatrix.bool.tg.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.abmatrix.bool.tg.common.constants.NumberConstants;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author abm
 */
public class AmountUtil {

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat();

    static {
        AMOUNT_FORMAT.applyPattern("0.00");
    }

    public static String formatAmount(BigDecimal amount) {
        return AMOUNT_FORMAT.format(amount.stripTrailingZeros());
    }

    public static void main(String[] args) {
//        LocalDateTime createTime = LocalDateTimeUtil.parse("2024-10-13 22:01:00", "yyyy-MM-dd HH:mm:ss");
//        Duration duration = Duration.between(createTime, DateTime.now().toLocalDateTime());
//        if (duration.compareTo(Duration.ofDays(1)) <= NumberConstants.ZERO) {
//            System.out.println("1");
//        }else {
//            System.out.println("2");
//        }




    }
}
