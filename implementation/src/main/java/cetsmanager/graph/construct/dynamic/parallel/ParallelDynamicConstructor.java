package cetsmanager.graph.construct.dynamic.parallel;

import cetsmanager.graph.EventVertex;
import cetscommon.common.values.NumericValue;
import cetsmanager.graph.construct.dynamic.DynamicConstructor;
import cetscommon.query.Predicate;

public abstract class ParallelDynamicConstructor extends DynamicConstructor {

  protected ParallelDynamicConstructor(Predicate predicate, NumericValue start,
      NumericValue end, NumericValue step) {
    super(predicate, start, end, step);
  }

  @Override
  public void link(EventVertex eventVertex) {
    throw new UnsupportedOperationException("parallel constructor do not use this api");
  }
}
