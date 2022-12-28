package cetsmanager.graph.construct.dynamic.parallel;

import cetscommon.common.values.NumericValue;
import cetscommon.events.Event;
import cetscommon.query.Predicate;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.dynamic.KeySortedMultimap;
import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class EventProcessor implements Runnable {

  private Iterator<EventVertex> vertices;
  private final Predicate predicate;
  private TreeSet<NumericValue> gaps;
  private KeySortedMultimap<NumericValue, TupleEdge<NumericValue, EventVertex, Object>> fromEdges;
  private KeySortedMultimap<NumericValue, TupleEdge<EventVertex, NumericValue, Object>> toEdges;

  public EventProcessor(Iterator<EventVertex> vertices, Predicate predicate,
      Comparator<NumericValue> cmp) {
    this.vertices = vertices;
    this.predicate = predicate;
    gaps = new TreeSet<>(cmp);
    fromEdges = new KeySortedMultimap<>(cmp);
    toEdges = new KeySortedMultimap<>(cmp);
  }

  public EventProcessor(Predicate predicate,
      Comparator<NumericValue> cmp) {
    this(null, predicate, cmp);
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(vertices);
    while (vertices.hasNext()) {
      processVertex(vertices.next());
    }
  }

  public void setVertices(Iterator<EventVertex> vertices) {
    this.vertices = vertices;
  }

  public void processVertex(EventVertex vertex) {
    System.out.println("processing new vertex");
    Event event = vertex.event;
    NumericValue tv;
    if(event.simple)
      tv = new NumericValue(predicate.rightOperand);
    else
      tv = (NumericValue) predicate.func.apply(event.get(predicate.rightOperand));
    System.out.println("tv == "+tv);
    NumericValue fv = (NumericValue) event.get(predicate.leftOperand);
    gaps.add(tv);

    fromEdges.put(fv, new TupleEdge<>(fv, vertex, null));

    toEdges.put(tv, new TupleEdge<>(vertex, tv, null));
  }

  public boolean removeVertex(EventVertex vertex){
    System.out.println("removing a vertex");
    Event event = vertex.event;
    NumericValue tv = (NumericValue) predicate.func.apply(event.get(predicate.rightOperand));
    NumericValue fv = (NumericValue) event.get(predicate.leftOperand);

    TupleEdge<NumericValue, EventVertex, Object> fe = new TupleEdge<>(fv, vertex, null);
    TupleEdge<EventVertex, NumericValue, Object> te = new TupleEdge<>(vertex, tv, null);

    fromEdges.remove(fv, fe);
    toEdges.remove(tv, te);

    for (Iterator<EventVertex> it = vertices; it.hasNext(); ) {
      EventVertex e = it.next();
      if (e.equals(vertex)){
        System.out.println("successfully removed");
        it.remove();
        return true;
      }
    }

    return false;
  }

  public TreeSet<NumericValue> getGaps() {
    return gaps;
  }

  public Iterator<TupleEdge<NumericValue, EventVertex, Object>> getFromEdges() { return fromEdges.valueIterator();
  }

  public Iterator<TupleEdge<EventVertex, NumericValue, Object>> getToEdges() {
    return toEdges.valueIterator();
  }
}
