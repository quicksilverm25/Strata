/*
 * Copyright (C) 2020 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.index.type;

import static com.opengamma.strata.basics.date.BusinessDayConventions.FOLLOWING;
import static com.opengamma.strata.basics.date.DateSequences.MONTHLY_1ST;
import static com.opengamma.strata.basics.date.DateSequences.QUARTERLY_IMM;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.GBLO;
import static com.opengamma.strata.basics.index.OvernightIndices.GBP_SONIA;
import static com.opengamma.strata.basics.index.OvernightIndices.USD_FED_FUND;
import static com.opengamma.strata.basics.index.OvernightIndices.USD_SOFR;
import static com.opengamma.strata.product.swap.OvernightAccrualMethod.AVERAGED_DAILY;
import static com.opengamma.strata.product.swap.OvernightAccrualMethod.COMPOUNDED;

import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DaysAdjustment;

/**
 * Market standard Fixed-Overnight swap conventions.
 */
final class StandardOvernightFutureConventions {

  /**
   * The 'GBP-SONIA-Quarterly-IMM' convention.
   */
  public static final OvernightFutureConvention GBP_SONIA_QUARTERLY_IMM =
      ImmutableOvernightFutureConvention.builder()
          .index(GBP_SONIA)
          .dateSequence(QUARTERLY_IMM)
          .accrualMethod(COMPOUNDED)
          .endDateAdjustment(DaysAdjustment.ofCalendarDays(-1))
          .lastTradeDateAdjustment(DaysAdjustment.ofCalendarDays(0, BusinessDayAdjustment.of(FOLLOWING, GBLO)))
          .build();

  //-------------------------------------------------------------------------
  /**
   * The 'USD-SOFR-Quarterly-IMM' convention.
   * The default values for the adjustments are correct.
   */
  public static final OvernightFutureConvention USD_SOFR_QUARTERLY_IMM =
      ImmutableOvernightFutureConvention.of(USD_SOFR, QUARTERLY_IMM, COMPOUNDED);

  /**
   * The 'USD-SOFR-Monthly-1st' convention.
   * The default values for the adjustments are correct.
   */
  public static final OvernightFutureConvention USD_SOFR_MONTHLY_1ST =
      ImmutableOvernightFutureConvention.of(USD_SOFR, MONTHLY_1ST, AVERAGED_DAILY);

  /**
   * The 'USD-FED-FUND-Monthly-1st' convention.
   * The default values for the adjustments are correct.
   */
  public static final OvernightFutureConvention USD_FED_FUND_MONTHLY_1ST =
      ImmutableOvernightFutureConvention.of(USD_FED_FUND, MONTHLY_1ST, AVERAGED_DAILY);

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  private StandardOvernightFutureConventions() {
  }

}
