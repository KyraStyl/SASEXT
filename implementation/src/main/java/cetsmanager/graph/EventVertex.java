package cetsmanager.graph;

import cetscommon.events.Event;

import java.util.*;
import java.util.Map.Entry;

public class EventVertex implements Vertex {
  public final Event event;

  private final Map<Character, ArrayList<AttributeVertex>> edges;
  private int edgeCount = 0;

  public EventVertex(Event event) {
    this.event = event;
    edges = new HashMap<>();
  }

  public <T extends AttributeVertex> void linkToAttr(char c, T vertex) {
    ArrayList<AttributeVertex> egs;
    if (!edges.containsKey(c)) {
      egs = new ArrayList<>();
      edges.put(c, egs);
    } else {
      egs = edges.get(c);
    }
    egs.add(vertex);
//    edgeCount += 1;
//    int p = edgeCount;
//    while (p-->0);
  }

  public long timestamp() {
    return event.timestamp;
  }

  @Override
  public String toString() {
    return "EventVertex{"
        + "event=" + event
        + '}';
  }

  public Map<Character, ArrayList<AttributeVertex>> getEdges() {
    return edges;
  }

  /**
   * for debug, show all edges in string.
   * @return edges in string
   */
  public List<String> edgeStrings() {
    List<String> strings = new ArrayList<>();
    for(Entry<Character, ArrayList<AttributeVertex>> entry: edges.entrySet()) {
      char c = entry.getKey();
      for(AttributeVertex vertex: entry.getValue()) {
        strings.add(c + " " + this.shortString() + "->" + vertex.toString());
      }
    }
    return strings;
  }

  public String shortString() {
    return event.timestamp + "";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EventVertex that = (EventVertex) o;
    return that.event.equals(event);
  }

  @Override
  public int hashCode() {
    return Objects.hash(event, edges, edgeCount);
  }
}
