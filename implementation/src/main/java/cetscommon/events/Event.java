package cetscommon.events;

import cetscommon.common.values.Value;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Event implements Serializable {
  public final long timestamp;
  protected Value[] values;
  public boolean simple;
  public final long lifeBegin;

  public Event(long timestamp, Value[] values, long lifeBegin, boolean simple) {
    this.timestamp = timestamp;
    this.values = values;
    this.lifeBegin = lifeBegin;
    this.simple = simple;
  }

  public Value get(int i) {
    return values[i];
  }

  public int size() {return values.length;}

  @Override
  public String toString() {
    return "Event{" + timestamp
        + ", " + Arrays.toString(values)
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return timestamp == event.timestamp && Arrays.equals(values, event.values);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(timestamp);
    result = 31 * result + Arrays.hashCode(values);
    return result;
  }
}
