package cetsmanager.main;

import cetsmanager.main.examples.Example;

import java.util.ArrayList;

public class Main {

  public static void main(String[] args) {
    Example example = Example.getExample(args);

    example.start(new ArrayList<String>());
  }

}
