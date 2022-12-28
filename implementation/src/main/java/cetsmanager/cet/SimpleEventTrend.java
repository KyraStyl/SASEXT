package cetsmanager.cet;

import cetscommon.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleEventTrend extends EventTrend{
  private ArrayList<Event> events;
  private static long lifeTimeBegin;

  public SimpleEventTrend() {
    this(new ArrayList<>());
  }

  public SimpleEventTrend(Event event) {
    this();
    events.add(event);
    lifeTimeBegin = event.lifeBegin;
  }

  private SimpleEventTrend(ArrayList<Event> events) {
    this.events = events;
    if(events.size()>0)
      lifeTimeBegin = events.get(0).lifeBegin;
  }

  @Override
  public List<Event> events() {
    return events;
  }

  public void append(Event event) {
    events.add(event);
    if(events.size() == 1)
      lifeTimeBegin = event.lifeBegin;
  }

  public void append(EventTrend trend) {
    events.addAll(trend.events());
  }

  @Override
  public Event get(int index) {
    return events.get(index);
  }

  @Override
  public long getBeginTime() {
    return lifeTimeBegin;
  }

  @Override
  public int size() {
    return events.size();
  }

  public SimpleEventTrend copy() {
    ArrayList<Event> events = new ArrayList<>(this.events);
    return new SimpleEventTrend(events);
  }

  public String shortString() {
    return "[" + events.stream()
        .map(e->e.timestamp+"")
        .collect(Collectors.joining(", ")) + "]";
  }

  @Override
  public String toString() {
    return "EventTrend{"+shortString()+"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    SimpleEventTrend that = (SimpleEventTrend) o;
    return Objects.equals(events, that.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(events);
  }
}
