package cetsmanager.graph.construct.dynamic.parallel;

import cetsmanager.graph.AbstractEdge;

public class TupleEdge<S, T, V> extends AbstractEdge<S, T, V> {
  public TupleEdge(S src, T dest, V value) {
    super(src, dest, value);
  }

  public TupleEdge(S src, T dest) {
    super(src, dest);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    TupleEdge<S,T,V> that = (TupleEdge<S,T,V>) obj;
    return that.src.equals(this.src) && that.dest.equals(this.dest);
  }
}
