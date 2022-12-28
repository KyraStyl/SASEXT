package cetsmanager.graph.detect.traversal.anchors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import cetsmanager.cet.EventTrend;
import cetsmanager.graph.AttributeVertex;
import cetsmanager.graph.EventVertex;
import cetsmanager.graph.construct.Constructor;
import cetsmanager.graph.detect.DetectUtil;
import cetsmanager.graph.detect.Detector;
import cetsmanager.util.Global;
import cetscommon.query.Predicate;
import cetscommon.query.Query;

public abstract class AnchorBasedDetector extends Detector {

  protected final int numIteration;

  public AnchorBasedDetector(ArrayList<EventVertex> eventVertices,
      ArrayList<Constructor> constructors,
      Query query,
      int numIteration, String writePath) {
    super(eventVertices, constructors, query, writePath);
    this.numIteration = numIteration;
  }

  protected abstract Map<Character, Collection<AttributeVertex>> selectAnchors();

  @Override
  public void detect() {
    Global.log("begin detect");
    Map<Character, Collection<AttributeVertex>> anchors = selectAnchors();
    Global.log("finish select anchors");
    fastPrefilter();
    //prefilter();
    Global.log("finish prefilter");
    detectByAnchors(anchors);
    Global.log("finish detect");
  }

  protected void detectByAnchors(Map<Character, Collection<AttributeVertex>> anchors) {
    HashMap<Character, Collection<EventTrend>> results = new HashMap<>();
    for (Entry<Character, Collection<AttributeVertex>> entry : anchors.entrySet()) {
      Predicate predicate = query.condition.predicates().get(entry.getKey());
      results.put(entry.getKey(), detectOnePredicate(
          entry.getValue(),
          predicate
      ));
    }
    Collection<EventTrend> result = mergeByQuery(results);
  }

  protected abstract Collection<EventTrend> detectOnePredicate(Collection<AttributeVertex> anchors,
      Predicate predicate);

  private Collection<EventTrend> mergeByQuery(HashMap<Character, Collection<EventTrend>> results) {
    return results.values().iterator().next();
  }

  protected void parallelFastPrefilter() {
    try {
      HashMap<Character, HashSet<EventVertex>> p2starts = new HashMap<>();
      HashMap<Character, HashSet<EventVertex>> p2ends = new HashMap<>();
      for (char c : p2attrVertices.keySet()) {
        HashSet<EventVertex> ends = new HashSet<>();
        HashMap<AttributeVertex, PrefilterMeta> metas = new HashMap<>();

        // set meta in advance and parallel
        ArrayList<AttributeVertex> attrs = p2attrVertices.get(c);
        ArrayList<Supplier<HashMap<AttributeVertex, PrefilterMeta>>> tasks = new ArrayList<>();
        int parallism = Global.getParallism();
        int split = Math.max(attrs.size() / parallism, 128);
        for (int i = 0; i < attrs.size(); i += split) {
          final List<AttributeVertex> sub = attrs.subList(i, Math.min(attrs.size(), i + split));
          tasks.add(() -> {
            HashMap<AttributeVertex, PrefilterMeta> subMetas = new HashMap<>();
            for (AttributeVertex attr : sub) {
              long max = 0;
              for (EventVertex ev : attr.getEdges()) {
                if (ev.timestamp() > max) {
                  max = ev.timestamp();
                }
              }
              subMetas.put(attr, new PrefilterMeta(max));
            }
            return subMetas;
          });
        }
        ArrayList<HashMap<AttributeVertex, PrefilterMeta>> subMaps = Global.callAndSync(tasks);
        for (HashMap<AttributeVertex, PrefilterMeta> submap : subMaps) {
          metas.putAll(submap);
        }

        // forward one step
        ArrayList<Supplier<HashSet<EventVertex>>> suppliers = new ArrayList<>();
        split = Math.max(eventVertices.size() / parallism, 256);
        for (int i = 0; i < eventVertices.size(); i += split) {
          final List<EventVertex> sub = eventVertices.subList(i,
              Math.min(eventVertices.size(), i + split));
          suppliers.add(() -> {
            HashSet<EventVertex> localEnds = new HashSet<>(sub);
            for (EventVertex vertex : eventVertices) {
              ArrayList<AttributeVertex> edges = vertex.getEdges().get(c);
              for (AttributeVertex attr : edges) {
                PrefilterMeta meta = metas.get(attr);

                if (vertex.timestamp() < meta.max) {
                  localEnds.remove(vertex);
                }
                meta.compareAndSet(vertex.timestamp());
              }
            }
            return localEnds;
          });
        }
        ArrayList<HashSet<EventVertex>> localEnds = Global.callAndSync(suppliers);
        for (HashSet<EventVertex> lend : localEnds) {
          ends.addAll(lend);
        }

        HashSet<EventVertex> starts = new HashSet<>(eventVertices);
        split = Math.max(attrs.size() / parallism, 128);
        ArrayList<Supplier<HashSet<EventVertex>>> ntasks = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i += split) {
          final List<AttributeVertex> sub = attrs.subList(i, Math.min(attrs.size(), i + split));
          ntasks.add(() -> {
            HashSet<EventVertex> subSet = new HashSet<>();
            for (AttributeVertex attr : sub) {
              for (EventVertex ev : attr.getEdges()) {
                if (ev.timestamp() > metas.get(attr).getAttachMin()) {
                  subSet.add(ev);
                }
              }
            }
            return subSet;
          });
        }
        ArrayList<HashSet<EventVertex>> nonStarts = Global.callAndSync(ntasks);
        for (HashSet<EventVertex> nonStart : nonStarts) {
          starts.removeAll(nonStart);
        }
        p2starts.put(c, starts);
        p2ends.put(c, ends);
      }
      starts = DetectUtil.syncByQuery(p2starts, query);
      ends = DetectUtil.syncByQuery(p2ends, query);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }


  private static class PrefilterMeta {

    final Holder attachMin;
    long max;
    public PrefilterMeta(long max) {
      this.max = max;
      attachMin = new Holder();
      attachMin.value = max + 1;
    }

    public void compareAndSet(long timestamp) {
      synchronized (attachMin) {
        if (attachMin.value > timestamp) {
          attachMin.value = timestamp;
        }
      }
    }

    public long getAttachMin() {
      return attachMin.value;
    }

    private static class Holder {

      long value;
    }
  }
}
