/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.index.type;

import com.opengamma.strata.collect.named.ExtendedEnum;

/**
 * Market standard Overnight future conventions.
 */
public final class OvernightFutureConventions {

  /**
   * The extended enum lookup from name to instance.
   */
  static final ExtendedEnum<OvernightFutureConvention> ENUM_LOOKUP = ExtendedEnum.of(OvernightFutureConvention.class);

  /**
   * The 'GBP-SONIA-Quarterly-IMM' convention.
   * <p>
   * The index is based on quarterly IMM dates.
   */
  public static final OvernightFutureConvention GBP_SONIA_QUARTERLY_IMM =
      OvernightFutureConvention.of(StandardOvernightFutureConventions.GBP_SONIA_QUARTERLY_IMM.getName());

  //-------------------------------------------------------------------------
  /**
   * The 'USD-SOFR-Quarterly-IMM' convention.
   * <p>
   * The index is based on quarterly IMM dates.
   */
  public static final OvernightFutureConvention USD_SOFR_QUARTERLY_IMM =
      OvernightFutureConvention.of(StandardOvernightFutureConventions.USD_SOFR_QUARTERLY_IMM.getName());

  /**
   * The 'USD-SOFR-Monthly-1st' convention.
   * <p>
   * The index is based on monthly IMM dates.
   */
  public static final OvernightFutureConvention USD_SOFR_MONTHLY_1ST =
      OvernightFutureConvention.of(StandardOvernightFutureConventions.USD_SOFR_MONTHLY_1ST.getName());

  /**
   * The 'USD-FED-FUND-Monthly-1st' convention.
   * <p>
   * The index is based on monthly IMM dates.
   */
  public static final OvernightFutureConvention USD_FED_FUND_MONTHLY_1ST =
      OvernightFutureConvention.of(StandardOvernightFutureConventions.USD_FED_FUND_MONTHLY_1ST.getName());

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  private OvernightFutureConventions() {
  }

}
