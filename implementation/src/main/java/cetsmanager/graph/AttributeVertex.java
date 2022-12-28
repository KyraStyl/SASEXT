package cetsmanager.graph;

import java.util.ArrayList;
import java.util.List;

public interface AttributeVertex extends Vertex {

  List<EventVertex> getEdges();

  void linkToEvent(EventVertex eventVertex);
  void unlinkToEvent(EventVertex eventVertex);

  String shortString();

  default List<String> edgeStrings() {
    List<String> strings = new ArrayList<>();
    for (EventVertex vertex : getEdges()) {
      //System.out.println(vertex.shortString());
      strings.add(this.shortString() + "->" + vertex.toString());
    }
    return strings;
  }



}
