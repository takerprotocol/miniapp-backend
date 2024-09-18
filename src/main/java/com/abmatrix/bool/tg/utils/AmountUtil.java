package com.abmatrix.bool.tg.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

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
}
