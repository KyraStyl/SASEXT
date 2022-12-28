package cetsmanager.graph.detect;

import java.util.HashSet;
import java.util.Map;
import cetsmanager.graph.EventVertex;
import cetscommon.query.Expression;
import cetscommon.query.LogicalExpression;
import cetscommon.query.LogicalExpression.LogicalOperater;
import cetscommon.query.Predicate;
import cetscommon.query.Query;

public class DetectUtil {
  public static HashSet<EventVertex> syncByQuery(Map<Character, HashSet<EventVertex>> results,
      Query query) {
    Expression condition = query.condition;
    return syncByCondition(results,condition);
  }

  private static HashSet<EventVertex> syncByCondition(Map<Character, HashSet<EventVertex>> results,
      Expression condition) {
    if(condition.isLogical()) {
      LogicalExpression logical = (LogicalExpression) condition;
      HashSet<EventVertex> left = syncByCondition(results, logical.getLeft());
      HashSet<EventVertex> right = syncByCondition(results, logical.getRight());
      if(logical.operater == LogicalOperater.or) {
        left.addAll(right);
        return left;
      }else {
        left.removeAll(right);
        return left;
      }
    }else {
      return results.get(((Predicate)condition).tag);
    }
  }
}
