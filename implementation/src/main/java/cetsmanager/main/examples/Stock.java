package cetsmanager.main.examples;

import cetscommon.common.values.NumericValue;
import cetscommon.common.values.Value;
import cetscommon.events.Event;
import cetscommon.events.EventTemplate;
import cetscommon.query.LogicalExpression;
import cetscommon.query.LogicalExpression.LogicalOperater;
import cetscommon.query.Operator;
import cetscommon.query.Predicate;
import cetscommon.query.Query;
import cetsmanager.graph.construct.Constructor;
import cetsmanager.graph.construct.dynamic.parallel.ParallelStaticDynamicConstructor;
import cetsmanager.graph.construct.dynamic.parallel.ParallelStaticDynamicEqConstructor;
import cetsmanager.graph.construct.dynamic.sequential.SeqDynamicConstructor;
import cetsmanager.graph.construct.dynamic.sequential.SeqStaticDynamicConstructor;
import com.beust.jcommander.Parameter;
import sasesystem.engine.ConfigFlags;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;

public class Stock extends Example {

  private final EventTemplate template;
  private final Predicate idPredicate;
  private final Predicate pricePredicate;
  private final Predicate symbolPredicate;
  private final Predicate priceprd1;
  private final boolean isSimple;
  private final NumericValue start;
  private final NumericValue end;
  private final NumericValue step;
  private final String[] predslist;
  private ArrayList<Predicate> predicates = new ArrayList<>();

  public Stock(Argument args, double ratio, String [] predslist) {
    super(args);
    template = new EventTemplate.Builder()
        .addNumeric("id")
        .addNumeric("symbol")
        .addNumeric("price")
        .addNumeric("volume")
        .build();

    this.predslist = predslist;

    idPredicate = new Predicate(Operator.eq, template.indexOf("id"), template.indexOf("id"));
    symbolPredicate = new Predicate(Operator.eq, template.indexOf("symbol"), template.indexOf("symbol"));
    pricePredicate = new Predicate(Operator.gt, template.indexOf("price"),
        template.indexOf("price"),v->Value.numeric(v.numericVal()));
    isSimple = args.isSimple;
    priceprd1 = new Predicate(Operator.eq, template.indexOf("price"), template.indexOf("price"), v -> Value.numeric(v.numericVal()));
    Predicate volumePred = new Predicate(Operator.eq, template.indexOf("volume"), template.indexOf("volume"), v -> Value.numeric(2 * v.numericVal()));

    //this.predicates = createPredicates();

    //System.exit(201);
    //this.predicates.add(pricePredicate);

    this.predicates.add(symbolPredicate);

    //this.predicates.add(priceprd1);
    //this.predicates.add(volumePred);

    String range = args.range.trim();
    range = range.substring(1, range.length() - 1).trim();
    String[] sps = range.split(",");
    start = (NumericValue) Value.numeric(Double.parseDouble(sps[0]));
    end = (NumericValue) Value.numeric(Double.parseDouble(sps[1]));
    step = (NumericValue) Value.numeric(Double.parseDouble(sps[2]));
    setPrecision(step.numericVal());
  }

  private ArrayList<Predicate> createPredicates() {
    ArrayList<Predicate> p = new ArrayList<>();
    for(String s:predslist){
      System.out.println(s);
      String tokens[] = s.split(" ");
      for(String t:tokens)
        System.out.println(t);
      String lop = tokens[0].split("\\.")[0].split("\\[")[0];
      System.out.println(" lop === "+lop);
      int attr1 = template.indexOf(tokens[0].split("\\.")[1]);
      System.out.println("attr1 === "+attr1);
      Operator op = tokens[1].equalsIgnoreCase("=")?Operator.eq:tokens[1].equalsIgnoreCase(">")?Operator.gt:Operator.lt;
      String rop = tokens[2].split("\\.")[0].split("\\[")[0];
      int attr2 = template.indexOf(tokens[2].split("\\.")[1]);
      System.out.println(" rop === "+rop);
      System.out.println("attr2 === "+attr2);

      p.add(new Predicate(op,attr1,attr2));
    }
    return p;
  }


  static Config getArgument() {
    return new Argument();
  }

  @Override
  public String getName() {
    return isSimple ? "simple stock" : "stock";
  }

  private Query simpleStock() {
    return new Query("S", this.predicates.get(0), wl, sl);
  }

  private LogicalExpression createLogicalExpr(ArrayList<Predicate> preds){
    if(preds.size() == 2){
      return new LogicalExpression(preds.get(0),preds.get(1),LogicalOperater.and);
    }
    preds.remove(preds.size()-1);
    return new LogicalExpression(preds.get(preds.size()-1),createLogicalExpr(preds),LogicalOperater.and);
  }

  private Query stock() { //mystock
//    if (predicates.size() == 1)
//      return new Query("S", predicates.get(0), wl, sl);

    LogicalExpression expr = createLogicalExpr(this.predicates);
    return new Query("S", expr, wl, sl);
  }

  private Query cetstock() {
    LogicalExpression expression = new LogicalExpression(idPredicate, pricePredicate,
        LogicalOperater.and);
    return new Query("S", expression, wl, sl);
  }

  @Override
  public Query getQuery() {
    return isSimple ? simpleStock() : stock();
  }

  @Override
  @Nonnull
  public Iterator<Event> readInput(ArrayList<String> eventsStr) {
    return readInputFromFile(path, eventsStr);
  }

  @Override
  public EventTemplate getTemplate() {
    return template;
  }

  @Override
  protected String parameters() {
    return "simple: " + isSimple + "\n"
        + "range: " + "[" + start + ", " + end + ", " + step + ")";
  }

  @Override
  public ArrayList<Constructor> getConstructors() {
    Constructor pc;
    if (parallism > 0) {
      System.out.println("parallelism > 0");
      if(predicates.get(0).op == Operator.eq){
        System.out.println("Operator eq");
        pc = new ParallelStaticDynamicEqConstructor(predicates.get(0));
      }else{
        pc = new ParallelStaticDynamicConstructor(parallism, predicates.get(0), start, end,
                step);
      }

    } else {
      System.out.println("parallelism == 0");
      if(isStatic) pc = new SeqStaticDynamicConstructor(pricePredicate, start, end, step);
      else pc = new SeqDynamicConstructor(pricePredicate,start,end,step);
    }
    ArrayList<Constructor> constructors = new ArrayList<>(2);
    if (!isSimple) {
      System.out.println("!simple");
      constructors.add(null);
    } else {
      System.out.println("simple");
      constructors.add(pc);
    }
    return constructors;
  }

  static class Argument extends Config {

    @Parameter(names = "-simple", description = "simple query or not")
    boolean isSimple;

    @Parameter(names = {"-r",
        "--range"}, description = "range for numeric, in the form of [start,end,step)")
    String range = "[0,1000,1)";

  }
}
