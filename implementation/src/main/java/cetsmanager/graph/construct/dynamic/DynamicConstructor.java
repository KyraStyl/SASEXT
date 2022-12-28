package cetsmanager.graph.construct.dynamic;

import java.util.Comparator;

import cetsmanager.graph.construct.Constructor;
import cetscommon.common.values.NumericValue;
import cetscommon.query.Predicate;
import cetscommon.util.Config;

public abstract class DynamicConstructor extends Constructor {

  protected final NumericValue start;
  protected final NumericValue end;
  protected final NumericValue step;
  protected final Comparator<NumericValue> cmp;

  protected DynamicConstructor(Predicate predicate, NumericValue start,
      NumericValue end, NumericValue step) {
    super(predicate);
    this.start = start;
    this.end = end;
    this.step = step;
    this.cmp = Config.numericValueComparator();
  }
}
