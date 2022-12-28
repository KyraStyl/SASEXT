package cetsmanager.graph.construct.dynamic.parallel;

import cetscommon.common.values.NumericValue;
import cetscommon.common.values.Value;
import cetscommon.query.Predicate;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.dynamic.RangeAttributeVertex;
import cetsmanager.graph.construct.dynamic.StaticManager;
import cetsmanager.util.Global;
import cetsmanager.util.MergedIterator;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ParallelStaticDynamicConstructor extends ParallelDynamicConstructor {

  private final EventProcessor[] processors;

  private Future<?>[] futures;

  private ArrayList<AttributeVertex> vertices;


  public ParallelStaticDynamicConstructor(int parallism, Predicate predicate,
      NumericValue start, NumericValue end,
      NumericValue step) {
    super(predicate, start, end, step);

    processors = new EventProcessor[parallism];
    for (int i = 0; i < processors.length; i++) {
      processors[i] = new EventProcessor(predicate, cmp);
    }
    futures = new Future[parallism];

  }

  @Override
  public void parallelLink(ArrayList<Iterator<EventVertex>> iterators) {
    ExecutorService executor = Global.getExecutor();
    for (int i = 0; i < iterators.size(); i++) {
      processors[i].setVertices(iterators.get(i));
    }
    for (int i = 0; i < processors.length; i++) {
      futures[i] = executor.submit(processors[i]);
    }
  }

  @Override
  public void invokeEventsEnd() {
    try {
      for (int i = 0; i < processors.length; i++) {
        futures[i].get();
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void manage() {
    manage2();
  }

  public void manage2() {
    ParallelManager manager = new ParallelManager(Global.getExecutor());

    ArrayList<Iterator<NumericValue>> its = new ArrayList<>(processors.length);
    ArrayList<Iterator<TupleEdge<NumericValue, EventVertex, Object>>> fromtIts = new ArrayList<>(
        processors.length);
    ArrayList<Iterator<TupleEdge<EventVertex, NumericValue, Object>>> toIts = new ArrayList<>(
        processors.length);

    for (EventProcessor processor : processors) {
      its.add(processor.getGaps().iterator());
      fromtIts.add(processor.getFromEdges());
      toIts.add(processor.getToEdges());
    }

    try {
      Global.log("mange ranges");
//      manager.mergeTest(its, start,end,step,predicate.op);
      vertices = manager.mergeGaps(its, start, end, step, predicate.op);
//       vertices = mergeGaps1(its);
      // manage edges
      Global.log("manage from edges");
      countF = manager.reduceFromEdges(fromtIts, vertices);

      Global.log("manage to edges");
      countT = manager.reduceToEdges(toIts, predicate, vertices);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void manage1() {
    // merge gaps
    ArrayList<Iterator<NumericValue>> its = new ArrayList<>(processors.length);
    ArrayList<Iterator<TupleEdge<NumericValue, EventVertex, Object>>> fromtIts = new ArrayList<>(
        processors.length);
    ArrayList<Iterator<TupleEdge<EventVertex, NumericValue, Object>>> toIts = new ArrayList<>(
        processors.length);

    for (EventProcessor processor : processors) {
      its.add(processor.getGaps().iterator());
      fromtIts.add(processor.getFromEdges());
      toIts.add(processor.getToEdges());
    }

    StaticManager manager = new StaticManager(start, end, step, cmp, predicate);

    Global.log("mange ranges");
    MergedIterator<NumericValue> it = new MergedIterator<>(its, cmp);
    vertices = manager.mergeGaps(it);

    Global.log("manage from edges");
    // manage edges
    Iterator<TupleEdge<NumericValue, EventVertex, Object>> fromEdges = new MergedIterator<>(
        fromtIts,
        Ordering.from(cmp).onResultOf(TupleEdge::getSource));
    countF = manager.reduceFromEdges(fromEdges, vertices);

    Global.log("manage to edges");
    Iterator<TupleEdge<EventVertex, NumericValue, Object>> toEdges = new MergedIterator<>(toIts,
        //Comparator.comparing(TupleEdge::getTarget));
        Ordering.from(cmp).onResultOf(TupleEdge::getTarget));
    countT = manager.reduceToEdges(toEdges, vertices);
  }

  private ArrayList<RangeAttributeVertex> mergeGaps1(ArrayList<Iterator<NumericValue>> its) {
    NumericValue lower = this.start;
    NumericValue prevGap = null;
    ArrayList<RangeAttributeVertex> ranges = new ArrayList<>();
    MergedIterator<NumericValue> it = new MergedIterator<>(its, cmp);
    Range<NumericValue> range;
    while (it.hasNext()) {
      NumericValue gap = it.next();
      if (prevGap != null && cmp.compare(gap, prevGap) == 0) {
        continue;
      }
      switch (predicate.op) {
        case gt:
          NumericValue upper = (NumericValue) Value.numeric(
              gap.numericVal() + step.numericVal());
          range = Range.closedOpen(lower, upper);
          lower = upper;
          ranges.add(new RangeAttributeVertex(range));
          break;
        case eq:
          range = Range.singleton(gap);
          ranges.add(new RangeAttributeVertex(range));
          break;
        default:
          break;
      }
      prevGap = gap;
    }
    switch (predicate.op) {
      case gt:
        range = Range.closedOpen(lower, end);
        ranges.add(new RangeAttributeVertex(range));
        break;
      default:
        break;
    }
    return ranges;
  }

  public void addVertex(EventVertex vertex){
    processors[0].processVertex(vertex);
  }

  @Override
  public boolean removeEdge(EventVertex vertex) {
    return processors[0].removeVertex(vertex);
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
