package cetsmanager.graph.construct;

import cetscommon.query.Predicate;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Constructor {

  protected final Predicate predicate;
  protected long countF;
  protected long countT;

  protected Constructor(Predicate predicate) {
    this.predicate = predicate;
  }

  public void parallelLink(ArrayList<Iterator<EventVertex>> iterators) {
    throw new UnsupportedOperationException("");
  }
  public void link(EventVertex eventVertex) {
    throw new UnsupportedOperationException("");
  }

  public void invokeEventsEnd() {
  }

  public abstract void manage();

  public abstract ArrayList<AttributeVertex> attributes();

  public int countAttr() {
    return 0;
  }

  public long countFrom() {
    return countF;
  }

  public long countTo() {
    return countT;
  }

  public Predicate getPredicate() {
    return predicate;
  }

  public void close() {
  }

  public boolean removeEdge(EventVertex vertex) {
    return false;
  }

  public void addVertex(EventVertex add) {
  }
}
