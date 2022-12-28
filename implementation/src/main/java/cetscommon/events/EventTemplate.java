package cetscommon.events;

import cetscommon.common.values.Value;
import cetscommon.common.values.Value.ValType;

import java.io.Serializable;
import java.util.ArrayList;

public class EventTemplate implements Serializable {

  private Attribute[] attributes;

  public EventTemplate(Attribute[] attributes) {
    this.attributes = attributes;
  }

  /**
   * the index of attribute in the template,
   * or -1 which means the attribute name is not in this template.
   *
   * @param name the name of attribute
   * @return the index of the given name
   */
  public int indexOf(String name) {
    for (int i = 0; i < attributes.length; i++) {
      if (name.equals(attributes[i].name)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * create event from the string.
   * @param line the string of event
   * @return event
   */
  public Event str2event(String line) {
    String[] sps = line.split(" ");
    long timestamp = Long.parseLong(sps[0]);
    Value[] values = new Value[attributes.length];
    for (int i = 0; i < attributes.length; i++) {
      if (attributes[i].valType == ValType.str) {
        values[i] = Value.str(sps[i + 1]);
      } else {
        values[i] = Value.numeric(Double.parseDouble(sps[i + 1]));
      }
    }
    return new Event(timestamp, values, System.nanoTime(), true);
  }

  public Event str2eventstock(String line){
    String tokens[] = line.split(", ");
    Value[] values = new Value[attributes.length];
    values[0] = Value.str(tokens[0]);
    long timestamp = Integer.parseInt(tokens[1]);
    values[1] = Value.longval(timestamp);
    values[2] = Value.numeric(Integer.parseInt(tokens[2]));
    values[3] = Value.numeric(Integer.parseInt(tokens[3]));
    values[4] = Value.numeric(Integer.parseInt(tokens[4]));
    return new Event(timestamp,values, System.nanoTime(), true);
  }

  public static class Builder {

    private ArrayList<Attribute> attributes;

    public Builder() {
      attributes = new ArrayList<>();
    }

    public Builder addStr(String name) {
      attributes.add(new Attribute(name, ValType.str));
      return this;
    }

    public Builder addNumeric(String name) {
      attributes.add(new Attribute(name, ValType.numeric));
      return this;
    }

    public Builder addLong(String name) {
      attributes.add(new Attribute(name, ValType.longval));
      return this;
    }

    public EventTemplate build() {
      return new EventTemplate(attributes.toArray(new Attribute[]{}));
    }
  }
}
