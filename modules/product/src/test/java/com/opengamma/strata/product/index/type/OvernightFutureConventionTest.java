/*
 * Copyright (C) 2020 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.index.type;

import static com.opengamma.strata.basics.date.BusinessDayConventions.FOLLOWING;
import static com.opengamma.strata.basics.date.DateSequences.MONTHLY_IMM;
import static com.opengamma.strata.basics.date.DateSequences.QUARTERLY_IMM;
import static com.opengamma.strata.basics.index.OvernightIndices.GBP_SONIA;
import static com.opengamma.strata.basics.index.OvernightIndices.USD_FED_FUND;
import static com.opengamma.strata.basics.index.OvernightIndices.USD_SOFR;
import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.coverPrivateConstructor;
import static com.opengamma.strata.collect.TestHelper.date;
import static com.opengamma.strata.product.swap.OvernightAccrualMethod.AVERAGED_DAILY;
import static com.opengamma.strata.product.swap.OvernightAccrualMethod.COMPOUNDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.product.SecurityId;
import com.opengamma.strata.product.index.OvernightFutureTrade;

/**
 * Tests {@link OvernightFutureConvention}.
 */
public class OvernightFutureConventionTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  private static final double NOTIONAL_1M = 1_000_000d;
  private static final BusinessDayAdjustment BDA_FOLLOW =
      BusinessDayAdjustment.of(FOLLOWING, USD_SOFR.getFixingCalendar());
  private static final BusinessDayAdjustment BDA_PRECEDE =
      BusinessDayAdjustment.of(BusinessDayConventions.PRECEDING, USD_SOFR.getFixingCalendar());

  //-------------------------------------------------------------------------
  @Test
  public void test_of() {
    ImmutableOvernightFutureConvention test = ImmutableOvernightFutureConvention.of(USD_SOFR, QUARTERLY_IMM, COMPOUNDED);
    assertThat(test.getName()).isEqualTo("USD-SOFR-Quarterly-IMM");
    assertThat(test.getIndex()).isEqualTo(USD_SOFR);
    assertThat(test.getDateSequence()).isEqualTo(QUARTERLY_IMM);
    assertThat(test.getAccrualMethod()).isEqualTo(COMPOUNDED);
    assertThat(test.getStartDateAdjustment()).isEqualTo(BusinessDayAdjustment.NONE);
    assertThat(test.getEndDateAdjustment()).isEqualTo(DaysAdjustment.ofCalendarDays(-1));
    assertThat(test.getLastTradeDateAdjustment()).isEqualTo(DaysAdjustment.ofCalendarDays(-1, BDA_PRECEDE));
  }

  @Test
  public void test_builder() {
    ImmutableOvernightFutureConvention test = ImmutableOvernightFutureConvention.builder()
        .name("USD-IMM")
        .index(USD_SOFR)
        .dateSequence(QUARTERLY_IMM)
        .accrualMethod(AVERAGED_DAILY)
        .build();
    assertThat(test.getName()).isEqualTo("USD-IMM");
    assertThat(test.getIndex()).isEqualTo(USD_SOFR);
    assertThat(test.getDateSequence()).isEqualTo(QUARTERLY_IMM);
    assertThat(test.getAccrualMethod()).isEqualTo(AVERAGED_DAILY);
    assertThat(test.getStartDateAdjustment()).isEqualTo(BusinessDayAdjustment.NONE);
    assertThat(test.getEndDateAdjustment()).isEqualTo(DaysAdjustment.ofCalendarDays(-1));
    assertThat(test.getLastTradeDateAdjustment()).isEqualTo(DaysAdjustment.ofCalendarDays(-1, BDA_PRECEDE));
  }

  @Test
  public void test_builder_incomplete() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> ImmutableOvernightFutureConvention.builder()
            .index(USD_SOFR)
            .build());
    assertThatIllegalArgumentException()
        .isThrownBy(() -> ImmutableOvernightFutureConvention.builder()
            .dateSequence(QUARTERLY_IMM)
            .build());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_createTrade_gbpSoniaImm() {
    OvernightFutureConvention test = OvernightFutureConventions.GBP_SONIA_QUARTERLY_IMM;
    OvernightFutureTrade trade = test.createTrade(
        date(2020, 1, 25),
        SecurityId.of("OG", "1"),
        YearMonth.of(2020, 2),
        20,
        NOTIONAL_1M,
        0.999d,
        REF_DATA);
    assertThat(trade.getCurrency()).isEqualTo(Currency.GBP);
    assertThat(trade.getPrice()).isEqualTo(.999d);
    assertThat(trade.getQuantity()).isEqualTo(20);
    assertThat(trade.getProduct().getIndex()).isEqualTo(GBP_SONIA);
    assertThat(trade.getProduct().getAccrualMethod()).isEqualTo(COMPOUNDED);
    assertThat(trade.getProduct().getAccrualFactor()).isEqualTo(0.25);
    assertThat(trade.getProduct().getStartDate()).isEqualTo(date(2020, 3, 18));
    assertThat(trade.getProduct().getEndDate()).isEqualTo(date(2020, 6, 16));
    assertThat(trade.getProduct().getLastTradeDate()).isEqualTo(date(2020, 6, 17));
  }

  @Test
  public void test_createTrade_usdSofaImm() {
    OvernightFutureConvention test = OvernightFutureConventions.USD_SOFR_QUARTERLY_IMM;
    OvernightFutureTrade trade = test.createTrade(
        date(2020, 1, 25),
        SecurityId.of("OG", "1"),
        YearMonth.of(2020, 2),
        20,
        NOTIONAL_1M,
        0.999d,
        REF_DATA);
    assertThat(trade.getCurrency()).isEqualTo(Currency.USD);
    assertThat(trade.getPrice()).isEqualTo(.999d);
    assertThat(trade.getQuantity()).isEqualTo(20);
    assertThat(trade.getProduct().getIndex()).isEqualTo(USD_SOFR);
    assertThat(trade.getProduct().getAccrualMethod()).isEqualTo(COMPOUNDED);
    assertThat(trade.getProduct().getAccrualFactor()).isEqualTo(0.25);
    assertThat(trade.getProduct().getStartDate()).isEqualTo(date(2020, 3, 18));
    assertThat(trade.getProduct().getEndDate()).isEqualTo(date(2020, 6, 16));
    assertThat(trade.getProduct().getLastTradeDate()).isEqualTo(date(2020, 6, 16));
  }

  @Test
  public void test_createTrade_usdSofaOneMonth() {
    OvernightFutureConvention test = OvernightFutureConventions.USD_SOFR_MONTHLY_1ST;
    OvernightFutureTrade trade = test.createTrade(
        date(2020, 1, 25),
        SecurityId.of("OG", "1"),
        YearMonth.of(2020, 2),
        20,
        NOTIONAL_1M,
        0.999d,
        REF_DATA);
    assertThat(trade.getCurrency()).isEqualTo(Currency.USD);
    assertThat(trade.getPrice()).isEqualTo(.999d);
    assertThat(trade.getQuantity()).isEqualTo(20);
    assertThat(trade.getProduct().getIndex()).isEqualTo(USD_SOFR);
    assertThat(trade.getProduct().getAccrualMethod()).isEqualTo(AVERAGED_DAILY);
    assertThat(trade.getProduct().getAccrualFactor()).isEqualTo(1 / 12d);
    assertThat(trade.getProduct().getStartDate()).isEqualTo(date(2020, 2, 1));
    assertThat(trade.getProduct().getEndDate()).isEqualTo(date(2020, 2, 29));
    assertThat(trade.getProduct().getLastTradeDate()).isEqualTo(date(2020, 2, 28));
  }

  @Test
  public void test_createTrade_usdFedFundOneMonth() {
    OvernightFutureConvention test = OvernightFutureConventions.USD_FED_FUND_MONTHLY_1ST;
    OvernightFutureTrade trade = test.createTrade(
        date(2020, 1, 25),
        SecurityId.of("OG", "1"),
        YearMonth.of(2020, 2),
        20,
        NOTIONAL_1M,
        0.999d,
        REF_DATA);
    assertThat(trade.getCurrency()).isEqualTo(Currency.USD);
    assertThat(trade.getPrice()).isEqualTo(.999d);
    assertThat(trade.getQuantity()).isEqualTo(20);
    assertThat(trade.getProduct().getIndex()).isEqualTo(USD_FED_FUND);
    assertThat(trade.getProduct().getAccrualMethod()).isEqualTo(AVERAGED_DAILY);
    assertThat(trade.getProduct().getAccrualFactor()).isEqualTo(1 / 12d);
    assertThat(trade.getProduct().getStartDate()).isEqualTo(date(2020, 2, 1));
    assertThat(trade.getProduct().getEndDate()).isEqualTo(date(2020, 2, 29));
    assertThat(trade.getProduct().getLastTradeDate()).isEqualTo(date(2020, 2, 28));
  }

  //-------------------------------------------------------------------------
  public static Object[][] data_name() {
    return new Object[][] {
        {OvernightFutureConventions.GBP_SONIA_QUARTERLY_IMM, "GBP-SONIA-Quarterly-IMM"},
        {OvernightFutureConventions.USD_SOFR_QUARTERLY_IMM, "USD-SOFR-Quarterly-IMM"},
        {OvernightFutureConventions.USD_SOFR_MONTHLY_1ST, "USD-SOFR-Monthly-1st"},
        {OvernightFutureConventions.USD_FED_FUND_MONTHLY_1ST, "USD-FED-FUND-Monthly-1st"},
    };
  }

  @ParameterizedTest
  @MethodSource("data_name")
  public void test_name(OvernightFutureConvention convention, String name) {
    assertThat(convention.getName()).isEqualTo(name);
  }

  @ParameterizedTest
  @MethodSource("data_name")
  public void test_toString(OvernightFutureConvention convention, String name) {
    assertThat(convention.toString()).isEqualTo(name);
  }

  @ParameterizedTest
  @MethodSource("data_name")
  public void test_of_lookup(OvernightFutureConvention convention, String name) {
    assertThat(OvernightFutureConvention.of(name)).isEqualTo(convention);
  }

  @ParameterizedTest
  @MethodSource("data_name")
  public void test_extendedEnum(OvernightFutureConvention convention, String name) {
    OvernightFutureConvention.of(name);  // ensures map is populated
    ImmutableMap<String, OvernightFutureConvention> map = OvernightFutureConvention.extendedEnum().lookupAll();
    assertThat(map.get(name)).isEqualTo(convention);
  }

  @Test
  public void test_of_lookup_notFound() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> OvernightFutureConvention.of("Rubbish"));
  }

  @Test
  public void test_of_lookup_null() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> OvernightFutureConvention.of((String) null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void coverage() {
    ImmutableOvernightFutureConvention test = ImmutableOvernightFutureConvention.of(USD_SOFR, QUARTERLY_IMM, COMPOUNDED);
    coverImmutableBean(test);
    ImmutableOvernightFutureConvention test2 = ImmutableOvernightFutureConvention.builder()
        .index(USD_SOFR)
        .accrualMethod(AVERAGED_DAILY)
        .dateSequence(MONTHLY_IMM)
        .startDateAdjustment(BDA_FOLLOW)
        .endDateAdjustment(DaysAdjustment.ofCalendarDays(-2, BDA_FOLLOW))
        .lastTradeDateAdjustment(DaysAdjustment.ofCalendarDays(-2, BDA_FOLLOW))
        .build();
    coverBeanEquals(test, test2);

    coverPrivateConstructor(OvernightFutureConventions.class);
    coverPrivateConstructor(StandardOvernightFutureConventions.class);
  }

  @Test
  public void test_serialization() {
    OvernightFutureConvention test = ImmutableOvernightFutureConvention.of(USD_SOFR, QUARTERLY_IMM, COMPOUNDED);
    assertSerialization(test);
  }

}
