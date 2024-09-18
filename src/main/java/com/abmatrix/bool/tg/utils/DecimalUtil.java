package com.abmatrix.bool.tg.utils;

import cn.hutool.core.util.NumberUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author abm
 */
@Slf4j
public class DecimalUtil {

    private static final int SCALE = 18;

    /**
     * Retain decimal digits
     *
     * @param a a
     * @param b b
     * @return rst
     */
    public static BigDecimal div(Number a, Number b) {
        return NumberUtil.div(a, b, SCALE).stripTrailingZeros();
    }

    public static BigDecimal standardQuantity(@NonNull BigDecimal minUnitQuantity, int decimal) {
        BigDecimal divisor = BigDecimal.TEN.pow(decimal);
        return div(minUnitQuantity, divisor);
    }

    public static BigDecimal standardQuantity(@NonNull BigInteger minUnitQuantity, int decimal) {
        return standardQuantity(new BigDecimal(minUnitQuantity), decimal);
    }

    public static BigDecimal minUnitQuantity(@NonNull BigDecimal standardQuantity, int decimal) {
        BigDecimal multiplier = BigDecimal.TEN.pow(decimal);
        return standardQuantity.multiply(multiplier).stripTrailingZeros();
    }

    public static BigDecimal minUnitQuantity(@NonNull BigInteger standardQuantity, int decimal) {
        return minUnitQuantity(new BigDecimal(standardQuantity), decimal);
    }

}
