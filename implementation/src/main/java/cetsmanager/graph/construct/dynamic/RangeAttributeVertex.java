package cetsmanager.graph.construct.dynamic;

import cetscommon.common.values.NumericValue;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.List;

public class RangeAttributeVertex implements AttributeVertex, Comparable<RangeAttributeVertex> {

  protected final ArrayList<EventVertex> vertices;
  protected Range<NumericValue> range;

  public RangeAttributeVertex(
      Range<NumericValue> range) {
    this.range = range;
    vertices = new ArrayList<>();
  }

  public void linkToEvent(EventVertex eventVertex) {
    vertices.add(eventVertex);
  }
  public void unlinkToEvent(EventVertex eventVertex) {
    int index = -1;
    for (EventVertex e: vertices)
      if(e.equals(eventVertex))
        index = vertices.indexOf(e);
    if(index!=-1)
      vertices.remove(index);
  }

  public Range<NumericValue> getRange() {
    return range;
  }

  public void setRange(Range<NumericValue> range) {
    this.range = range;
  }

  @Override
  public List<EventVertex> getEdges() {
    return vertices;
  }

  @Override
  public String toString() {
    return "RangeAttributeVertex{"
        + "range=" + range
        + '}';
  }

  @Override
  public int compareTo(RangeAttributeVertex o) {
    return range.lowerEndpoint().compareTo(o.range.lowerEndpoint());
  }

  @Override
  public String shortString() {
    return range.toString();
  }
}
