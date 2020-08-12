/*
 * Copyright (C) 2020 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.calc.runner;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.light.LightMetaBean;

import com.opengamma.strata.data.MarketData;
import com.opengamma.strata.data.MarketDataId;

/**
 * An identifier used to access calculation parameters by name.
 * <p>
 * In most cases, {@link CalculationParameters} should not be stored in {@link MarketData}.
 * However, there can be occasions where it is useful to be able to pass around the
 * parameters with the market data, notably when the market data is only intended to
 * be interpreted in one way.
 */
@BeanDefinition(style = "light", cacheHashCode = true)
public final class CalculationParametersId
    implements MarketDataId<CalculationParameters>, ImmutableBean, Serializable {

  /**
   * The standard instance of this identifier.
   */
  public static final CalculationParametersId STANDARD = new CalculationParametersId("Standard");

  /**
   * The name of the parameters.
   */
  @PropertyDefinition(validate = "notNull")
  private final String name;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance used to obtain calculation parameters by name.
   * <p>
   * In most cases, it is better to use the {@link #STANDARD} constant instead.
   *
   * @param name the name
   * @return the identifier
   */
  public static CalculationParametersId of(String name) {
    return new CalculationParametersId(name);
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<CalculationParameters> getMarketDataType() {
    return CalculationParameters.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code CalculationParametersId}.
   */
  private static final TypedMetaBean<CalculationParametersId> META_BEAN =
      LightMetaBean.of(
          CalculationParametersId.class,
          MethodHandles.lookup(),
          new String[] {
              "name"},
          new Object[0]);

  /**
   * The meta-bean for {@code CalculationParametersId}.
   * @return the meta-bean, not null
   */
  public static TypedMetaBean<CalculationParametersId> meta() {
    return META_BEAN;
  }

  static {
    MetaBean.register(META_BEAN);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The cached hash code, using the racy single-check idiom.
   */
  private transient int cacheHashCode;

  private CalculationParametersId(
      String name) {
    JodaBeanUtils.notNull(name, "name");
    this.name = name;
  }

  @Override
  public TypedMetaBean<CalculationParametersId> metaBean() {
    return META_BEAN;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the parameters.
   * @return the value of the property, not null
   */
  public String getName() {
    return name;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalculationParametersId other = (CalculationParametersId) obj;
      return JodaBeanUtils.equal(name, other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = cacheHashCode;
    if (hash == 0) {
      hash = getClass().hashCode();
      hash = hash * 31 + JodaBeanUtils.hashCode(name);
      cacheHashCode = hash;
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("CalculationParametersId{");
    buf.append("name").append('=').append(JodaBeanUtils.toString(name));
    buf.append('}');
    return buf.toString();
  }

  //-------------------------- AUTOGENERATED END --------------------------
}
