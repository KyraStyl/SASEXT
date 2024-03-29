package cetscommon.common.values;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

import static cetscommon.util.Config.numericValueComparator;

public class NumericValue extends Value {

  public NumericValue(double value) {
    super(value);
  }

  @Override
  public int compareTo(@Nonnull Value o) {
    Preconditions.checkNotNull(o);
    if (o instanceof NumericValue) {
      return numericValueComparator().compare(this, (NumericValue) o);
    } else {
      throw new IllegalArgumentException("must be numeric");
    }
  }

  public void add(NumericValue nv) {
    this.value = this.numericVal() + nv.numericVal();
  }

  @Override
  public String strVal() {
    throw new UnsupportedOperationException("not a str value");
  }
}
