package cetsmanager.graph.construct.dynamic;

import cetscommon.common.values.Value;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;

import java.util.ArrayList;
import java.util.List;

public class ValueAttributeVertex implements AttributeVertex {
  protected final ArrayList<EventVertex> vertices;
  protected final Value value;

  public ValueAttributeVertex(Value value) {
    this.value = value;
    vertices = new ArrayList<>();
  }


  @Override
  public List<EventVertex> getEdges() {
    return vertices;
  }

  @Override
  public void linkToEvent(EventVertex eventVertex) {
    vertices.add(eventVertex);
  }

  @Override
  public void unlinkToEvent(EventVertex eventVertex) {
    int index = -1;
    for (EventVertex e: vertices)
      if (e.equals(eventVertex))
        index = vertices.indexOf(e);
    if(index!=-1)
      vertices.remove(index);
  }

  @Override
  public String shortString() {
    String s ="";
    for(EventVertex ev:vertices){
      s+=ev.shortString();
      if(vertices.indexOf(ev)!=vertices.size()-1)
        s+=",";
      s+=" ";
    }
    return s;
  }

  @Override
  public String toString() {
    return "ValueAttributeVertex{" +
            "vertices=" + shortString() +
            ", value=" + value +
            '}';
  }
}
