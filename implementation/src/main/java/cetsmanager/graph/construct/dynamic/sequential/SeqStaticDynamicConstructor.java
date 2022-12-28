package cetsmanager.graph.construct.dynamic.sequential;

import java.util.ArrayList;
import java.util.TreeSet;

import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.dynamic.parallel.TupleEdge;
import cetsmanager.util.Global;
import cetscommon.common.values.NumericValue;
import cetscommon.events.Event;
import cetsmanager.graph.construct.dynamic.KeySortedMultimap;
import cetsmanager.graph.construct.dynamic.StaticManager;
import cetscommon.query.Predicate;

public class SeqStaticDynamicConstructor extends SequentialDynamicConstructor {

  private TreeSet<NumericValue> gaps;
  private KeySortedMultimap<NumericValue, TupleEdge<NumericValue, EventVertex, Object>> fromEdges;
  private KeySortedMultimap<NumericValue, TupleEdge<EventVertex, NumericValue, Object>> toEdges;
  private ArrayList<AttributeVertex> vertices;


  public SeqStaticDynamicConstructor(Predicate predicate,
      NumericValue start, NumericValue end,
      NumericValue step) {
    super(predicate, start, end, step);
    gaps = new TreeSet<>(cmp);
    fromEdges = new KeySortedMultimap<>(cmp);
    toEdges = new KeySortedMultimap<>(cmp);
  }

  @Override
  public void link(EventVertex eventVertex) {
    Event event = eventVertex.event;
    NumericValue tv = (NumericValue) predicate.func.apply(event.get(predicate.rightOperand));
    NumericValue fv = (NumericValue) event.get(predicate.leftOperand);
    gaps.add(tv);

    fromEdges.put(fv, new TupleEdge<>(fv, eventVertex, null));

    toEdges.put(tv, new TupleEdge<>(eventVertex, tv, null));
  }

  @Override
  public void manage() {
    StaticManager manager = new StaticManager(start, end, step, cmp, predicate);

    Global.log("mange ranges");
    vertices = manager.mergeGaps(gaps.iterator());

    Global.log("manage from edges");
    // manage edges
    countF = manager.reduceFromEdges(fromEdges.valueIterator(), vertices);

    Global.log("manage to edges");
    countT = manager.reduceToEdges(toEdges.valueIterator(), vertices);
  }

  @Override
  public int countAttr() {
    return vertices.size();
  }

  @Override
  public ArrayList<AttributeVertex> attributes() {
    return vertices;
  }
}
