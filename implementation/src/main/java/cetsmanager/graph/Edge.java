package cetsmanager.graph;

public interface Edge<S, T, V> {
  S getSource();

  T getTarget();

  V getValue();
}
