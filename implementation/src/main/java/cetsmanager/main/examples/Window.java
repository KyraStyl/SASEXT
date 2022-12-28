package cetsmanager.main.examples;

import java.util.ArrayList;
import cetscommon.events.Event;

public class Window {
  final long start;
  final ArrayList<Event> events;

  public Window(long start) {
    this.start = start;
    events = new ArrayList<>();
  }
}
