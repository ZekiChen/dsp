package com.tecdo.service.init;

public class Pair<T, S> {
  public T left;
  public S right;

  private Pair(T l, S r) {
    left = l;
    right = r;
  }

  public static <T, S> Pair<T, S> of(T l, S r) {
    return new Pair<>(l, r);
  }
}
