package org.twc.terminator;

public class Triplet<K, U, V> {
  private final K fst;
  private final U snd;
  private final V thd;

  public Triplet(K fst, U snd, V thd) {
    this.fst = fst;
    this.snd = snd;
    this.thd = thd;
  }

  public K getFst() { return this.fst; }
  public U getSnd() { return this.snd; }
  public V getThd() { return this.thd; }
}
