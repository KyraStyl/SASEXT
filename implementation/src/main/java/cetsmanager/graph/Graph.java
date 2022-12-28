package cetsmanager.graph;

import cetscommon.events.Event;
import cetscommon.events.EventTemplate;
import cetscommon.query.Query;
import cetsmanager.graph.construct.Constructor;
import cetsmanager.graph.construct.dynamic.parallel.ParallelDynamicConstructor;
import cetsmanager.graph.construct.dynamic.parallel.ParallelStaticDynamicConstructor;
import cetsmanager.graph.construct.dynamic.parallel.ParallelStaticDynamicEqConstructor;
import cetsmanager.graph.detect.Detector;
import cetsmanager.graph.detect.traversal.anchors.BasicAnchorBasedDetector;
import cetsmanager.util.Global;
import com.google.common.collect.Iterators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Graph {

  protected final EventTemplate eventTemplate;
  protected final Query query;
  protected final ArrayList<EventVertex> eventVertices;
  protected List<Event> events;
  protected ArrayList<Constructor> constructors;


  /**
   * create a new graph.
   *  @param events input events
   * @param eventTemplate event template
   * @param query query
   * @param constructors constructors for graph construction
   */
  public Graph(List<Event> events,
      EventTemplate eventTemplate, Query query,
      ArrayList<Constructor> constructors) {
    this.events = events;
    this.eventTemplate = eventTemplate;
    this.query = query;
    this.constructors = constructors;
    eventVertices = new ArrayList<>(events.size());
  }

  /**
   * construct the graph.
   */
  public void construct() {
    processInputStream(events);
    manageGraph();
  }

  protected void processInputStream(List<Event> events) {
    for (Event event : events) {
      eventVertices.add(new EventVertex(event));
    }

    ArrayList<Constructor> parallels = new ArrayList<>();
    ArrayList<Constructor> sequentials = new ArrayList<>();
    ArrayList<Constructor> statics = new ArrayList<>();
    for (Constructor constructor : constructors) {
      if (constructor instanceof ParallelDynamicConstructor ||
      constructor instanceof ParallelStaticDynamicEqConstructor) {
        parallels.add(constructor);
      }  else {
        sequentials.add(constructor);
      }
    }
    Global.log("begin graph construction, events " + events.size());
    if (!sequentials.isEmpty()) {
      for (EventVertex eventVertex : eventVertices) {
        for (Constructor constructor : sequentials) {
          constructor.link(eventVertex);
        }
      }
    }
    if (!parallels.isEmpty()) {
      int parallism = Global.getParallism();
      for (Constructor constructor : parallels) {
        ArrayList<Iterator<EventVertex>> iterators = new ArrayList<>();
        for (int i = 0; i < parallism; i++) {
          iterators.add(new ParallelInputInterator(eventVertices, i, parallism));
        }
        constructor.parallelLink(iterators);
      }
    }
    if(!statics.isEmpty()) {
      int parallism = Global.getParallism();
      for (Constructor constructor : statics) {
        ArrayList<Iterator<EventVertex>> iterators = new ArrayList<>();
        for (int i = 0; i < parallism; i++) {
          iterators.add(new ParallelInputInterator(eventVertices, i, parallism));
        }
        constructor.parallelLink(iterators);
      }
    }
    for (Constructor constructor : constructors) {
      constructor.invokeEventsEnd();
    }
    Global.log("finish stream");
  }

  protected void manageGraph() {
    for (Constructor constructor : constructors) {
      constructor.manage();
    }
    Global.log("finish manage");
    System.out.println("events: " + eventVertices.size() + "\n"
        + "attrs: " + constructors.stream().mapToInt(Constructor::countAttr).reduce(Integer::sum)
        .getAsInt() + "\n"
        + "from edges: " + constructors.stream().mapToLong(Constructor::countFrom)
        .reduce(Long::sum).getAsLong() + "\n"
        + "to edges: " + constructors.stream().mapToLong(Constructor::countTo).reduce(Long::sum)
        .getAsLong()
    );
  }
  
  public void incrementalChange(Event todelete, Event toadd){
    removeEdge(todelete);
    EventVertex add = new EventVertex(toadd);
    eventVertices.add(add);
    Constructor c = constructors.get(0);
    if(c instanceof ParallelStaticDynamicEqConstructor || c instanceof ParallelStaticDynamicConstructor){
      c.addVertex(add);
    }else{
      c.link(add);
    }
    manageGraph();
  }

  private void removeEdge(Event todelete) {
    if(todelete!=null){
      for(Constructor c: constructors)
        if(c.removeEdge(new EventVertex(todelete))){
          break;
        }

      //constructors.get(0).removeEdge(new EventVertex(todelete));
      int index = -1;
      for(EventVertex v: eventVertices)
        if(v.event.equals(todelete))
          index = eventVertices.indexOf(v);
      if(index!=-1)
        eventVertices.remove(index);
    }
  }


  public void detect(int selectivity, String writePath, int numIteration) {
    Detector detector = new BasicAnchorBasedDetector(eventVertices,
        constructors,query,
        selectivity, numIteration,
        writePath);
//    Detector detector = new DfsBasedDetector(eventVertices,constructors,query,writePath);
    detector.detect();
  }

  public void writeGraph(String path,int num) {
    try {
      File dir = new File(path);
      int tryCount = 3;
      while (!dir.exists() && tryCount-- > 0) {
        dir.mkdir();
      }
      assert dir.exists():"cannot find or create output path:" + dir.getCanonicalPath();

      File eventFile = new File(dir, "event");
      write(eventVertices.stream().map(EventVertex::shortString).iterator(), eventFile);

      write(attributes().stream().map(AttributeVertex::shortString).iterator(),
          new File(dir, "attrs"));

      write(eventVertices.stream().flatMap(vertex -> vertex.edgeStrings().stream()).iterator(),
          new File(dir, "toEdges"));

      write(attributes().stream()
              .flatMap(vertex ->
                  vertex.edgeStrings().stream())
              .iterator(),
          new File(dir, "fromEdges"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void write(Iterator<String> lines, File output) throws IOException {
    BufferedWriter bw = new BufferedWriter(new FileWriter(output));
    while (lines.hasNext()) {
      bw.write(lines.next() + "\n");
    }
    bw.close();
  }

  /**
   * for debug.
   *
   * @return attribute vertices
   */
  public ArrayList<AttributeVertex> attributes() {
    ArrayList<AttributeVertex> nodes = new ArrayList<>();

    Iterator<AttributeVertex> it = Iterators.concat(
        constructors.stream().map(c -> c.attributes().iterator()).iterator()
    );
    Iterators.addAll(nodes, it);
    return nodes;
  }

  public ArrayList<EventVertex> events() {
    return eventVertices;
  }

  private static class ParallelInputInterator implements Iterator<EventVertex> {

    private final ArrayList<EventVertex> source;
    private final int step;
    private int index;

    private ParallelInputInterator(ArrayList<EventVertex> source, int startIndex, int step) {
      this.source = source;
      this.index = startIndex;
      this.step = step;
    }

    @Override
    public boolean hasNext() {
      return index < source.size();
    }

    @Override
    public EventVertex next() {
      EventVertex event = source.get(index);
      index += step;
      return event;
    }
  }
}
