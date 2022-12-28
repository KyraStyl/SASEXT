package cetsmanager.graph.detect;

import cetscommon.query.Query;
import cetsmanager.cet.EventTrend;
import cetsmanager.cet.SimpleEventTrend;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.Constructor;
import hybridutils.CetManager;
import sasesystem.engine.Profiling;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public abstract class Detector {
  protected final ArrayList<EventVertex> eventVertices;
  protected final HashMap<Character, ArrayList<AttributeVertex>> p2attrVertices;
  protected final Query query;

  protected HashSet<EventVertex> starts;
  protected HashSet<EventVertex> ends;
  protected Consumer<SimpleEventTrend> outputFunc;

  protected String writePath;

  public Detector(ArrayList<EventVertex> eventVertices,
                  ArrayList<Constructor> constructors, Query query, String writePath) {
    this.eventVertices = eventVertices;
    this.query = query;
    p2attrVertices = new HashMap<>();
    for(Constructor constructor:constructors) {
      p2attrVertices.put(constructor.getPredicate().tag, constructor.attributes());
    }
    this.writePath = writePath;
  }

  public abstract void detect();

  protected void prefilter() {
    starts = new HashSet<>(eventVertices);
    ends = new HashSet<>(eventVertices);

    // forward one step
    for(EventVertex vertex: eventVertices) {
      Map<Character, ArrayList<AttributeVertex>> p2edegs = vertex.getEdges();
      Map<Character, HashSet<EventVertex>> p2results = new HashMap<>();
      for(Entry<Character, ArrayList<AttributeVertex>> entry: p2edegs.entrySet()) {
        ArrayList<AttributeVertex> edges = entry.getValue();
        HashSet<EventVertex> outers = new HashSet<>();
        for(AttributeVertex attr: edges) {
          for(EventVertex v: attr.getEdges()) {
            if(v.timestamp() > vertex.timestamp()) outers.add(v);
          }
        }
        p2results.put(entry.getKey(), outers);
      }
      HashSet<EventVertex> results = DetectUtil.syncByQuery(p2results,query);
      if(!results.isEmpty()) {
        starts.removeAll(results);
        ends.remove(vertex);
      }
    }
  }

  /**
   * prefilter do not need to traverse one step, to judge an end vertex, we only need to
   * check if it has a follower. Similarly, to judge an start vertex, we only need to
   * judge if it has a preview vertex. Vertices match by the edges and time. So if we set
   * the time metrics in attr vertices. We don't need to match further.
   */
  protected void fastPrefilter() {
    HashMap<Character, HashSet<EventVertex>> p2starts = new HashMap<>();
    HashMap<Character, HashSet<EventVertex>> p2ends = new HashMap<>();
    for(char c: p2attrVertices.keySet()) {
      HashSet<EventVertex> starts = new HashSet<>(eventVertices);
      HashSet<EventVertex> ends = new HashSet<>(eventVertices);
      HashMap<AttributeVertex, long[]> metas = new HashMap<>();

      // forward one step
      for (EventVertex vertex : eventVertices) {
        ArrayList<AttributeVertex> edges = vertex.getEdges().get(c);
        for (AttributeVertex attr : edges) {
          long[] meta = metas.get(attr);
          if (meta == null) {
            // not cal min and max, compare
            long max = 0;
            for (EventVertex ev : attr.getEdges()) {
              if (ev.timestamp() > max)
                max = ev.timestamp();
            }
            meta = new long[]{max, max + 1};
            metas.put(attr, meta);
          }

          if (vertex.timestamp() < meta[0])
            ends.remove(vertex);
          if (vertex.timestamp() < meta[1])
            meta[1] = vertex.timestamp();
        }
      }
      for (Entry<AttributeVertex, long[]> entry : metas.entrySet()) {
        long[] meta = entry.getValue();
        if (meta[1] < meta[0]) {
          for (EventVertex ev : entry.getKey().getEdges()) {
            if (ev.timestamp() > meta[1])
              starts.remove(ev);
          }
        }
      }
      p2starts.put(c,starts);
      p2ends.put(c,ends);
    }
    starts = DetectUtil.syncByQuery(p2starts, query);
    ends = DetectUtil.syncByQuery(p2ends, query);
  }

  protected void writeTrends(Iterator<EventTrend> trends, boolean inShort, long numCets) {
    //try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writePath,"cets")))) {

//    File file = null;
//    try {
//      file = new File(ConfigFlags.outFile);
//      if (file.createNewFile()) {
//        System.out.println("File created: " + file.getName());
//      }
//      Writer output ;
//      output = new BufferedWriter(new FileWriter(file, true));
      long endW = System.nanoTime();
      Profiling.tempEnd = endW;
      while (trends.hasNext()) {
        EventTrend cet = trends.next();
        if(cet.events().size()>1){
//          try {
//            output.append(cet.shortString()+"\n");
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
          long latency = System.nanoTime() - cet.getBeginTime();
          Profiling.updateLatency(latency);
          Profiling.addCetNum();
          CetManager.cets.add(cet);
          CetManager.rawcets.add(cet);
          CetManager.cetsdetectedtotal +=1;
        }
        //bw.write(cet.shortString() + "\n");
      }
//      output.close();
//    } catch (IOException e) {
//      System.out.println("An error occurred.");
//      e.printStackTrace();
//    }


      //CetManager.allComb();
    //} catch (IOException e) {
    //  e.printStackTrace();
    //}
  }

}
