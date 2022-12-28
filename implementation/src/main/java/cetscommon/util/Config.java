package cetscommon.util;

import cetscommon.common.values.LongValue;
import cetscommon.common.values.NumericValue;

import java.util.Comparator;

public class Config {
  // value configs
  private static boolean isSetValueConfig = false;
  private static double precison;

  private static Comparator<NumericValue> DEFAULT_NUMERIC_COMPARATOR = new NumericValueComparator();

  private static Comparator<LongValue> DEFAULT_LONGVALUE_COMPARATOR = new LongValueComparator();

  public static Comparator<NumericValue> numericValueComparator(double step) {
    return new NumericValueComparator(step);
  }

  public static Comparator<NumericValue> numericValueComparator() {
    return DEFAULT_NUMERIC_COMPARATOR;
  }

  public static class NumericValueComparator implements Comparator<NumericValue> {

    private final double step;

    public NumericValueComparator(double step) {
      this.step = step;
    }

    public NumericValueComparator() {
      this(0.0001);
    }

    @Override
    public int compare(NumericValue o1, NumericValue o2) {
      double diff = o1.numericVal() - o2.numericVal();
      if (diff > step) {
        return 1;
      } else if (diff < -step) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  public static class LongValueComparator implements Comparator<LongValue> {

    public LongValueComparator() {
    }

    @Override
    public int compare(LongValue o1, LongValue o2) {
      double diff = o1.longVal() - o2.longVal();
      if (diff > 0) {
        return 1;
      } else if (diff < 0) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  public static void initValue(double precison) {
    if (isSetValueConfig) {
      throw new DuplicateSetGlobalError("value");
    }
    Config.precison = precison;
    DEFAULT_NUMERIC_COMPARATOR = numericValueComparator(precison / 10000);
    isSetValueConfig = true;
  }

  public static final class DuplicateSetGlobalError extends RuntimeException {

    public DuplicateSetGlobalError() {
    }

    public DuplicateSetGlobalError(String position) {
      super("duplicate set global in " + position);
    }
  }
}
