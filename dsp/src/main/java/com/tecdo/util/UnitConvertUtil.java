package com.tecdo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Zeki on 2023/10/11
 */
public class UnitConvertUtil {

    public static BigDecimal usdToUsc(BigDecimal usd) {
        return usd.multiply(BigDecimal.valueOf(100));
    }

    public static BigDecimal uscToUsd(BigDecimal usc) {
        return usc.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }
}
