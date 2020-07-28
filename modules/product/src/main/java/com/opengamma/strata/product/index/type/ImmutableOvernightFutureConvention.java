/*
 * Copyright (C) 2020 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.index.type;

import static com.opengamma.strata.basics.date.BusinessDayConventions.PRECEDING;
import static java.time.temporal.ChronoUnit.MONTHS;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutablePreBuild;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DateSequence;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.index.OvernightIndex;
import com.opengamma.strata.product.SecurityId;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.index.OvernightFuture;
import com.opengamma.strata.product.index.OvernightFutureTrade;
import com.opengamma.strata.product.swap.OvernightAccrualMethod;

/**
 * A market convention for Overnight Future trades.
 * <p>
 * This defines the market convention for a future against a particular index.
 * In most cases, the index contains sufficient information to fully define the convention.
 */
@BeanDefinition
public final class ImmutableOvernightFutureConvention
    implements OvernightFutureConvention, ImmutableBean, Serializable {

  /**
   * The Overnight index.
   * <p>
   * The floating rate to be paid is based on this index
   * It will be a well known market index such as 'GBP-SONIA'.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final OvernightIndex index;
  /**
   * The convention name, such as 'GBP-SONIA-Quarterly-IMM'.
   * <p>
   * This will default to the name of the index suffixed by the name of the date sequence if not specified.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final String name;
  /**
   * The method of accruing Overnight interest.
   */
  @PropertyDefinition(validate = "notNull")
  private final OvernightAccrualMethod accrualMethod;
  /**
   * The sequence of dates that the future is based on.
   * <p>
   * This is used to calculate the reference date of the future that is the start
   * date of the underlying synthetic deposit.
   */
  @PropertyDefinition(validate = "notNull")
  private final DateSequence dateSequence;
  /**
   * The business day adjustment to apply to get the start date.
   * <p>
   * The start date is obtained by applying this adjustment to the reference date from the date sequence.
   * The reference date is often the third Wednesday of the month or the start of the month.
   * This defaults to accepting the date from the sequence without applying a holiday calendar.
   */
  @PropertyDefinition(validate = "notNull")
  private final BusinessDayAdjustment startDateAdjustment;
  /**
   * The days adjustment to apply to get the end date.
   * <p>
   * The end date is obtained by applying this adjustment to the next date in sequence from the start date.
   * This defaults to minus one without applying a holiday calendar.
   */
  @PropertyDefinition
  private final DaysAdjustment endDateAdjustment;
  /**
   * The days adjustment to apply to get the last trade date.
   * <p>
   * The last trade date is obtained by applying this adjustment to the next date in sequence from the start date.
   * This defaults to the previous business day in the fixing calendar (minus one days and preceding).
   */
  @PropertyDefinition(validate = "notNull")
  private final DaysAdjustment lastTradeDateAdjustment;

  //-------------------------------------------------------------------------
  /**
   * Creates a convention based on the specified index, sequence of dates and accrual method.
   * <p>
   * The standard market convention is based on the index.
   * The business day adjustment is set to be 'Following' using the fixing calendar from the index.
   * The convention name will default to the name of the index suffixed by the
   * name of the date sequence.
   * 
   * @param index  the index, the calendar for the adjustment is extracted from the index
   * @param dateSequence  the sequence of dates
   * @param accrualMethod  the accrual method
   * @return the convention
   */
  public static ImmutableOvernightFutureConvention of(
      OvernightIndex index,
      DateSequence dateSequence,
      OvernightAccrualMethod accrualMethod) {

    return ImmutableOvernightFutureConvention.builder()
        .index(index)
        .dateSequence(dateSequence)
        .accrualMethod(accrualMethod)
        .build();
  }

  @ImmutablePreBuild
  private static void preBuild(Builder builder) {
    if (builder.index != null) {
      if (builder.name == null && builder.dateSequence != null) {
        builder.name = builder.index.getName() + "-" + builder.dateSequence.getName();
      }
      if (builder.lastTradeDateAdjustment == null) {
        BusinessDayAdjustment bda = BusinessDayAdjustment.of(PRECEDING, builder.index.getFixingCalendar());
        builder.lastTradeDateAdjustment = DaysAdjustment.ofCalendarDays(-1, bda);
      }
    }
    if (builder.startDateAdjustment == null) {
      builder.startDateAdjustment = BusinessDayAdjustment.NONE;
    }
    if (builder.endDateAdjustment == null) {
      builder.endDateAdjustment = DaysAdjustment.ofCalendarDays(-1);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public OvernightFutureTrade createTrade(
      LocalDate tradeDate,
      SecurityId securityId,
      Period minimumPeriod,
      int sequenceNumber,
      double quantity,
      double notional,
      double price,
      ReferenceData refData) {

    LocalDate earliestDate = tradeDate.plus(minimumPeriod);
    LocalDate referenceDate = dateSequence.nthOrSame(earliestDate, sequenceNumber);
    LocalDate startDate = startDateAdjustment.adjust(referenceDate, refData);
    return createTrade(tradeDate, securityId, quantity, notional, price, startDate, refData);
  }

  @Override
  public OvernightFutureTrade createTrade(
      LocalDate tradeDate,
      SecurityId securityId,
      YearMonth yearMonth,
      double quantity,
      double notional,
      double price,
      ReferenceData refData) {

    LocalDate referenceDate = dateSequence.dateMatching(yearMonth);
    LocalDate startDate = startDateAdjustment.adjust(referenceDate, refData);
    return createTrade(tradeDate, securityId, quantity, notional, price, startDate, refData);
  }

  private OvernightFutureTrade createTrade(
      LocalDate tradeDate,
      SecurityId securityId,
      double quantity,
      double notional,
      double price,
      LocalDate startDate,
      ReferenceData refData) {

    LocalDate nextReferenceDate = dateSequence.next(startDate);
    double accrualFactor = startDate.withDayOfMonth(1).until(nextReferenceDate.withDayOfMonth(1), MONTHS) / 12d;
    LocalDate endDate = endDateAdjustment.adjust(nextReferenceDate, refData);
    LocalDate lastTradeDate = lastTradeDateAdjustment.adjust(nextReferenceDate, refData);
    OvernightFuture product = OvernightFuture.builder()
        .securityId(securityId)
        .index(index)
        .accrualMethod(accrualMethod)
        .accrualFactor(accrualFactor)
        .startDate(startDate)
        .endDate(endDate)
        .lastTradeDate(lastTradeDate)
        .notional(notional)
        .build();
    TradeInfo info = TradeInfo.of(tradeDate);
    return OvernightFutureTrade.builder()
        .info(info)
        .product(product)
        .quantity(quantity)
        .price(price)
        .build();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return name;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code ImmutableOvernightFutureConvention}.
   * @return the meta-bean, not null
   */
  public static ImmutableOvernightFutureConvention.Meta meta() {
    return ImmutableOvernightFutureConvention.Meta.INSTANCE;
  }

  static {
    MetaBean.register(ImmutableOvernightFutureConvention.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ImmutableOvernightFutureConvention.Builder builder() {
    return new ImmutableOvernightFutureConvention.Builder();
  }

  private ImmutableOvernightFutureConvention(
      OvernightIndex index,
      String name,
      OvernightAccrualMethod accrualMethod,
      DateSequence dateSequence,
      BusinessDayAdjustment startDateAdjustment,
      DaysAdjustment endDateAdjustment,
      DaysAdjustment lastTradeDateAdjustment) {
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(name, "name");
    JodaBeanUtils.notNull(accrualMethod, "accrualMethod");
    JodaBeanUtils.notNull(dateSequence, "dateSequence");
    JodaBeanUtils.notNull(startDateAdjustment, "startDateAdjustment");
    JodaBeanUtils.notNull(lastTradeDateAdjustment, "lastTradeDateAdjustment");
    this.index = index;
    this.name = name;
    this.accrualMethod = accrualMethod;
    this.dateSequence = dateSequence;
    this.startDateAdjustment = startDateAdjustment;
    this.endDateAdjustment = endDateAdjustment;
    this.lastTradeDateAdjustment = lastTradeDateAdjustment;
  }

  @Override
  public ImmutableOvernightFutureConvention.Meta metaBean() {
    return ImmutableOvernightFutureConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Overnight index.
   * <p>
   * The floating rate to be paid is based on this index
   * It will be a well known market index such as 'GBP-SONIA'.
   * @return the value of the property, not null
   */
  @Override
  public OvernightIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention name, such as 'GBP-SONIA-Quarterly-IMM'.
   * <p>
   * This will default to the name of the index suffixed by the name of the date sequence if not specified.
   * @return the value of the property, not null
   */
  @Override
  public String getName() {
    return name;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the method of accruing Overnight interest.
   * @return the value of the property, not null
   */
  public OvernightAccrualMethod getAccrualMethod() {
    return accrualMethod;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sequence of dates that the future is based on.
   * <p>
   * This is used to calculate the reference date of the future that is the start
   * date of the underlying synthetic deposit.
   * @return the value of the property, not null
   */
  public DateSequence getDateSequence() {
    return dateSequence;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day adjustment to apply to get the start date.
   * <p>
   * The start date is obtained by applying this adjustment to the reference date from the date sequence.
   * The reference date is often the third Wednesday of the month or the start of the month.
   * This defaults to accepting the date from the sequence without applying a holiday calendar.
   * @return the value of the property, not null
   */
  public BusinessDayAdjustment getStartDateAdjustment() {
    return startDateAdjustment;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the days adjustment to apply to get the end date.
   * <p>
   * The end date is obtained by applying this adjustment to the next date in sequence from the start date.
   * This defaults to minus one without applying a holiday calendar.
   * @return the value of the property
   */
  public DaysAdjustment getEndDateAdjustment() {
    return endDateAdjustment;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the days adjustment to apply to get the last trade date.
   * <p>
   * The last trade date is obtained by applying this adjustment to the next date in sequence from the start date.
   * This defaults to the previous business day in the fixing calendar (minus one days and preceding).
   * @return the value of the property, not null
   */
  public DaysAdjustment getLastTradeDateAdjustment() {
    return lastTradeDateAdjustment;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ImmutableOvernightFutureConvention other = (ImmutableOvernightFutureConvention) obj;
      return JodaBeanUtils.equal(index, other.index) &&
          JodaBeanUtils.equal(name, other.name) &&
          JodaBeanUtils.equal(accrualMethod, other.accrualMethod) &&
          JodaBeanUtils.equal(dateSequence, other.dateSequence) &&
          JodaBeanUtils.equal(startDateAdjustment, other.startDateAdjustment) &&
          JodaBeanUtils.equal(endDateAdjustment, other.endDateAdjustment) &&
          JodaBeanUtils.equal(lastTradeDateAdjustment, other.lastTradeDateAdjustment);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(index);
    hash = hash * 31 + JodaBeanUtils.hashCode(name);
    hash = hash * 31 + JodaBeanUtils.hashCode(accrualMethod);
    hash = hash * 31 + JodaBeanUtils.hashCode(dateSequence);
    hash = hash * 31 + JodaBeanUtils.hashCode(startDateAdjustment);
    hash = hash * 31 + JodaBeanUtils.hashCode(endDateAdjustment);
    hash = hash * 31 + JodaBeanUtils.hashCode(lastTradeDateAdjustment);
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ImmutableOvernightFutureConvention}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<OvernightIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", ImmutableOvernightFutureConvention.class, OvernightIndex.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> name = DirectMetaProperty.ofImmutable(
        this, "name", ImmutableOvernightFutureConvention.class, String.class);
    /**
     * The meta-property for the {@code accrualMethod} property.
     */
    private final MetaProperty<OvernightAccrualMethod> accrualMethod = DirectMetaProperty.ofImmutable(
        this, "accrualMethod", ImmutableOvernightFutureConvention.class, OvernightAccrualMethod.class);
    /**
     * The meta-property for the {@code dateSequence} property.
     */
    private final MetaProperty<DateSequence> dateSequence = DirectMetaProperty.ofImmutable(
        this, "dateSequence", ImmutableOvernightFutureConvention.class, DateSequence.class);
    /**
     * The meta-property for the {@code startDateAdjustment} property.
     */
    private final MetaProperty<BusinessDayAdjustment> startDateAdjustment = DirectMetaProperty.ofImmutable(
        this, "startDateAdjustment", ImmutableOvernightFutureConvention.class, BusinessDayAdjustment.class);
    /**
     * The meta-property for the {@code endDateAdjustment} property.
     */
    private final MetaProperty<DaysAdjustment> endDateAdjustment = DirectMetaProperty.ofImmutable(
        this, "endDateAdjustment", ImmutableOvernightFutureConvention.class, DaysAdjustment.class);
    /**
     * The meta-property for the {@code lastTradeDateAdjustment} property.
     */
    private final MetaProperty<DaysAdjustment> lastTradeDateAdjustment = DirectMetaProperty.ofImmutable(
        this, "lastTradeDateAdjustment", ImmutableOvernightFutureConvention.class, DaysAdjustment.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "index",
        "name",
        "accrualMethod",
        "dateSequence",
        "startDateAdjustment",
        "endDateAdjustment",
        "lastTradeDateAdjustment");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case 3373707:  // name
          return name;
        case -1335729296:  // accrualMethod
          return accrualMethod;
        case -258065009:  // dateSequence
          return dateSequence;
        case -1235962691:  // startDateAdjustment
          return startDateAdjustment;
        case 1599713654:  // endDateAdjustment
          return endDateAdjustment;
        case -1889695799:  // lastTradeDateAdjustment
          return lastTradeDateAdjustment;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ImmutableOvernightFutureConvention.Builder builder() {
      return new ImmutableOvernightFutureConvention.Builder();
    }

    @Override
    public Class<? extends ImmutableOvernightFutureConvention> beanType() {
      return ImmutableOvernightFutureConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<OvernightIndex> index() {
      return index;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> name() {
      return name;
    }

    /**
     * The meta-property for the {@code accrualMethod} property.
     * @return the meta-property, not null
     */
    public MetaProperty<OvernightAccrualMethod> accrualMethod() {
      return accrualMethod;
    }

    /**
     * The meta-property for the {@code dateSequence} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DateSequence> dateSequence() {
      return dateSequence;
    }

    /**
     * The meta-property for the {@code startDateAdjustment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<BusinessDayAdjustment> startDateAdjustment() {
      return startDateAdjustment;
    }

    /**
     * The meta-property for the {@code endDateAdjustment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DaysAdjustment> endDateAdjustment() {
      return endDateAdjustment;
    }

    /**
     * The meta-property for the {@code lastTradeDateAdjustment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DaysAdjustment> lastTradeDateAdjustment() {
      return lastTradeDateAdjustment;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return ((ImmutableOvernightFutureConvention) bean).getIndex();
        case 3373707:  // name
          return ((ImmutableOvernightFutureConvention) bean).getName();
        case -1335729296:  // accrualMethod
          return ((ImmutableOvernightFutureConvention) bean).getAccrualMethod();
        case -258065009:  // dateSequence
          return ((ImmutableOvernightFutureConvention) bean).getDateSequence();
        case -1235962691:  // startDateAdjustment
          return ((ImmutableOvernightFutureConvention) bean).getStartDateAdjustment();
        case 1599713654:  // endDateAdjustment
          return ((ImmutableOvernightFutureConvention) bean).getEndDateAdjustment();
        case -1889695799:  // lastTradeDateAdjustment
          return ((ImmutableOvernightFutureConvention) bean).getLastTradeDateAdjustment();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ImmutableOvernightFutureConvention}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<ImmutableOvernightFutureConvention> {

    private OvernightIndex index;
    private String name;
    private OvernightAccrualMethod accrualMethod;
    private DateSequence dateSequence;
    private BusinessDayAdjustment startDateAdjustment;
    private DaysAdjustment endDateAdjustment;
    private DaysAdjustment lastTradeDateAdjustment;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ImmutableOvernightFutureConvention beanToCopy) {
      this.index = beanToCopy.getIndex();
      this.name = beanToCopy.getName();
      this.accrualMethod = beanToCopy.getAccrualMethod();
      this.dateSequence = beanToCopy.getDateSequence();
      this.startDateAdjustment = beanToCopy.getStartDateAdjustment();
      this.endDateAdjustment = beanToCopy.getEndDateAdjustment();
      this.lastTradeDateAdjustment = beanToCopy.getLastTradeDateAdjustment();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case 3373707:  // name
          return name;
        case -1335729296:  // accrualMethod
          return accrualMethod;
        case -258065009:  // dateSequence
          return dateSequence;
        case -1235962691:  // startDateAdjustment
          return startDateAdjustment;
        case 1599713654:  // endDateAdjustment
          return endDateAdjustment;
        case -1889695799:  // lastTradeDateAdjustment
          return lastTradeDateAdjustment;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          this.index = (OvernightIndex) newValue;
          break;
        case 3373707:  // name
          this.name = (String) newValue;
          break;
        case -1335729296:  // accrualMethod
          this.accrualMethod = (OvernightAccrualMethod) newValue;
          break;
        case -258065009:  // dateSequence
          this.dateSequence = (DateSequence) newValue;
          break;
        case -1235962691:  // startDateAdjustment
          this.startDateAdjustment = (BusinessDayAdjustment) newValue;
          break;
        case 1599713654:  // endDateAdjustment
          this.endDateAdjustment = (DaysAdjustment) newValue;
          break;
        case -1889695799:  // lastTradeDateAdjustment
          this.lastTradeDateAdjustment = (DaysAdjustment) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public ImmutableOvernightFutureConvention build() {
      preBuild(this);
      return new ImmutableOvernightFutureConvention(
          index,
          name,
          accrualMethod,
          dateSequence,
          startDateAdjustment,
          endDateAdjustment,
          lastTradeDateAdjustment);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the Overnight index.
     * <p>
     * The floating rate to be paid is based on this index
     * It will be a well known market index such as 'GBP-SONIA'.
     * @param index  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder index(OvernightIndex index) {
      JodaBeanUtils.notNull(index, "index");
      this.index = index;
      return this;
    }

    /**
     * Sets the convention name, such as 'GBP-SONIA-Quarterly-IMM'.
     * <p>
     * This will default to the name of the index suffixed by the name of the date sequence if not specified.
     * @param name  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder name(String name) {
      JodaBeanUtils.notNull(name, "name");
      this.name = name;
      return this;
    }

    /**
     * Sets the method of accruing Overnight interest.
     * @param accrualMethod  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualMethod(OvernightAccrualMethod accrualMethod) {
      JodaBeanUtils.notNull(accrualMethod, "accrualMethod");
      this.accrualMethod = accrualMethod;
      return this;
    }

    /**
     * Sets the sequence of dates that the future is based on.
     * <p>
     * This is used to calculate the reference date of the future that is the start
     * date of the underlying synthetic deposit.
     * @param dateSequence  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder dateSequence(DateSequence dateSequence) {
      JodaBeanUtils.notNull(dateSequence, "dateSequence");
      this.dateSequence = dateSequence;
      return this;
    }

    /**
     * Sets the business day adjustment to apply to get the start date.
     * <p>
     * The start date is obtained by applying this adjustment to the reference date from the date sequence.
     * The reference date is often the third Wednesday of the month or the start of the month.
     * This defaults to accepting the date from the sequence without applying a holiday calendar.
     * @param startDateAdjustment  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder startDateAdjustment(BusinessDayAdjustment startDateAdjustment) {
      JodaBeanUtils.notNull(startDateAdjustment, "startDateAdjustment");
      this.startDateAdjustment = startDateAdjustment;
      return this;
    }

    /**
     * Sets the days adjustment to apply to get the end date.
     * <p>
     * The end date is obtained by applying this adjustment to the next date in sequence from the start date.
     * This defaults to minus one without applying a holiday calendar.
     * @param endDateAdjustment  the new value
     * @return this, for chaining, not null
     */
    public Builder endDateAdjustment(DaysAdjustment endDateAdjustment) {
      this.endDateAdjustment = endDateAdjustment;
      return this;
    }

    /**
     * Sets the days adjustment to apply to get the last trade date.
     * <p>
     * The last trade date is obtained by applying this adjustment to the next date in sequence from the start date.
     * This defaults to the previous business day in the fixing calendar (minus one days and preceding).
     * @param lastTradeDateAdjustment  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder lastTradeDateAdjustment(DaysAdjustment lastTradeDateAdjustment) {
      JodaBeanUtils.notNull(lastTradeDateAdjustment, "lastTradeDateAdjustment");
      this.lastTradeDateAdjustment = lastTradeDateAdjustment;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("ImmutableOvernightFutureConvention.Builder{");
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
      buf.append("accrualMethod").append('=').append(JodaBeanUtils.toString(accrualMethod)).append(',').append(' ');
      buf.append("dateSequence").append('=').append(JodaBeanUtils.toString(dateSequence)).append(',').append(' ');
      buf.append("startDateAdjustment").append('=').append(JodaBeanUtils.toString(startDateAdjustment)).append(',').append(' ');
      buf.append("endDateAdjustment").append('=').append(JodaBeanUtils.toString(endDateAdjustment)).append(',').append(' ');
      buf.append("lastTradeDateAdjustment").append('=').append(JodaBeanUtils.toString(lastTradeDateAdjustment));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
