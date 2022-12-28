package cetsmanager.graph.construct.dynamic.parallel;

import cetscommon.common.values.Value;
import cetscommon.events.Event;
import cetscommon.query.Predicate;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.Constructor;
import cetsmanager.graph.construct.dynamic.ValueAttributeVertex;
import cetsmanager.util.Global;
import cetsmanager.util.Tuple;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ParallelStaticDynamicEqConstructor extends Constructor {

  private final EqEventProcessor[] processors;
  private final ConcurrentHashMap<Value, AttributeVertex> attrs;
  private Future<?>[] futures;

  public ParallelStaticDynamicEqConstructor(Predicate predicate) {
    super(predicate);
    processors = new EqEventProcessor[Global.getParallism()];
    futures = new Future[Global.getParallism()];

    attrs = new ConcurrentHashMap<>();
  }

  @Override
  public void parallelLink(ArrayList<Iterator<EventVertex>> iterators) {
    ExecutorService executor = Global.getExecutor();
    ArrayList<ArrayList<EventVertex>> partitions = new ArrayList<>();
    for (Iterator<EventVertex> iterator : iterators) {
      ArrayList<EventVertex> evs = new ArrayList<>();
      iterator.forEachRemaining(evs::add);
      partitions.add(evs);
    }
    Global.log("after partition");
    for (int i = 0; i < iterators.size(); i++) {
      processors[i] = new EqEventProcessor(partitions.get(i).iterator(), predicate, attrs);
    }
    for (int i = 0; i < processors.length; i++) {
      futures[i] = executor.submit(processors[i]);
    }
  }
  public void addVertex(EventVertex vertex){
    processors[0].processVertex(vertex);
  }

  @Override
  public boolean removeEdge(EventVertex vertex) {
    processors[0].removeVertex(vertex);
    return false;
  }

  @Override
  public void manage() {

  }

  /*
  public void manage1() {
    log("mange attrs");
    HashSet<Value> attrVals = new HashSet<>();
    for (EqEventProcessor processor : processors) {
      attrVals.addAll(processor.getAttrs());
    }
    for (Value v : attrVals) {
      attrs.put(v, new ValueAttributeVertex(v));
    }
    log("manage from edges");
    //from edges
    ArrayList<Value> vals = new ArrayList<>(attrVals);
    List<Runnable> tasks = new ArrayList<>();
    int parallism = Global.getParallism();
    for (int i = 0; i < parallism; i++) {
      final Iterator<Integer> indice = Iters.stepIndices(i, parallism, vals.size());
      tasks.add(() -> {
        while (indice.hasNext()) {
          Value v = vals.get(indice.next());
          AttributeVertex av = attrs.get(v);

          for (EqEventProcessor processor : processors) {
            Collection<EventVertex> col = processor.getFroms().get(v);
            if (col != null) {
              for (EventVertex ev : col) {
                av.linkToEvent(ev);
              }
            }
          }
        }
      });
    }
    try {
      Global.runAndSync(tasks);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    log("manage to edges");
    // to edges
    tasks.clear();
    tasks = Arrays.stream(processors)
        .map(processor -> new Runnable() {
          @Override
          public void run() {
            ArrayList<Tuple<EventVertex, Value>> tos = processor.getTos();
            for (Tuple<EventVertex, Value> edge : tos) {
              edge.left.linkToAttr(predicate.tag, attrs.get(edge.right));
            }
          }
        }).collect(Collectors.toList());
    try {
      Global.runAndSync(tasks);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }
   */

  @Override
  public void invokeEventsEnd() {
    try {
      for (int i = 0; i < processors.length; i++) {
        futures[i].get();
        countF += processors[i].getCountF();
        countT += processors[i].getCountT();
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private ArrayList<AttributeVertex> reserved_attrs = null;

  @Override
  public ArrayList<AttributeVertex> attributes() {
    if(reserved_attrs == null) {
      TreeMap<Value,AttributeVertex> values = new TreeMap<>(
          Ordering.natural().onResultOf(Value::hashCode)
      );
      values.putAll(attrs);
      reserved_attrs = new ArrayList<>(values.values());
    }
    return reserved_attrs;
  }

  @Override
  public int countAttr() {
    return attrs.size();
  }

  private static class EqEventProcessor implements Runnable {
    private final Iterator<EventVertex> source;
    private final Predicate predicate;
    private int countF;
    private int countT;
    private final Multimap<Value, EventVertex> froms;
    private final ArrayList<Tuple<EventVertex, Value>> tos;
    private final ConcurrentHashMap<Value,AttributeVertex> attrs;

    public EqEventProcessor(Iterator<EventVertex> source, Predicate predicate, ConcurrentHashMap<Value,AttributeVertex> attrs) {
      this.source = source;
      this.predicate = predicate;
      countF = countT = 0;
      froms = HashMultimap.create();
      tos = new ArrayList<>();
      this.attrs = attrs;
    }

    @Override
    public void run() {
      while (source.hasNext()) {
        EventVertex vertex = source.next();
        processVertex(vertex);
      }
    }

    public void processVertex(EventVertex vertex){
      Event event = vertex.event;
      Value tv;
      //if(event.simple) {
      //  tv = predicate.func.apply(Value.numeric(predicate.rightOperand));
      //}
      //else
      tv = predicate.func.apply(event.get(predicate.rightOperand));
      Value fv = event.get(predicate.leftOperand);
      System.out.println("fv: "+fv+" ---> tv: "+tv);

      froms.put(fv, vertex);
      tos.add(Tuple.of(vertex, tv));
      attrs.compute(fv,(v,av)->{
        if(av == null) av = new ValueAttributeVertex(v);
        av.linkToEvent(vertex);
        return av;
      });
      attrs.compute(tv,(v,av)->{
        if(av == null) av = new ValueAttributeVertex(v);
        vertex.linkToAttr(predicate.tag,av);
        return av;
      });
      countF++;
      countT++;
    }

    public void removeVertex(EventVertex vertex){
      Event event = vertex.event;
      Value tv = predicate.func.apply(event.get(predicate.rightOperand));
      Value fv = event.get(predicate.leftOperand);

      AttributeVertex v1 = attrs.get(fv);
      v1.unlinkToEvent(vertex);

      AttributeVertex v2 = attrs.get(tv);
      v2.unlinkToEvent(vertex);
    }

    public Multimap<Value, EventVertex> getFroms() {
      return froms;
    }

    public ArrayList<Tuple<EventVertex, Value>> getTos() {
      return tos;
    }

    public int getCountT() {
      return countT;
    }

    public int getCountF() {
      return countF;
    }
  }
}
