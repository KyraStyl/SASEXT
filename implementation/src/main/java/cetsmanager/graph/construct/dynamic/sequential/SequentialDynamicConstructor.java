package cetsmanager.graph.construct.dynamic.sequential;

import java.util.ArrayList;
import java.util.Iterator;

import cetsmanager.graph.EventVertex;
import cetscommon.common.values.NumericValue;
import cetsmanager.graph.construct.dynamic.DynamicConstructor;
import cetscommon.query.Predicate;

public abstract class SequentialDynamicConstructor extends DynamicConstructor {

  protected SequentialDynamicConstructor(Predicate predicate,
      NumericValue start, NumericValue end,
      NumericValue step) {
    super(predicate, start, end, step);
  }

  @Override
  public void parallelLink(ArrayList<Iterator<EventVertex>> iterators) {
    throw new UnsupportedOperationException("sequential constructor do not use this api");
  }
}
