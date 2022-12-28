package cetsmanager.cet;

import cetscommon.events.Event;

import java.util.List;

import static java.util.Objects.hash;

public abstract class EventTrend {

  public abstract List<Event> events();

  public abstract void append(Event event);
  public abstract void append(EventTrend eventTrend);

  public abstract Event get(int index);

  public Event head() {
    return get(0);
  }

  public Event tail() {
    return get(size()-1);
  }

  public abstract long getBeginTime();

  public abstract int size();

  public abstract EventTrend copy();

  public long start() {
    return head().timestamp;
  }

  public long end() {
    return tail().timestamp;
  }

  public abstract String shortString();

  @Override
  public String toString() {
    return "EventTrend{"+shortString()+"}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    if (obj.getClass() == SimpleEventTrend.class){
      SimpleEventTrend that = (SimpleEventTrend) obj;
      SimpleEventTrend thiss = (SimpleEventTrend) this;
      for(int i=0; i < that.events().size();i++){
        for(int j=0; j < thiss.events().size(); j++){
          if(i==j && !that.events().get(i).equals(thiss.events().get(j)))
            return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for(int i=0; i < this.events().size();i++){
      h+= hash(events().get(i));
    }
    return hash(h);
  }
}
