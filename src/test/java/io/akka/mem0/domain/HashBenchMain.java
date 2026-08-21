package io.akka.mem0.domain;

/**
 * Prints hashOf() for a fixed line of texts, one per line, so bench/compare_hash.py can diff
 * it against the identical expression run in Python (the source's own language) on the same
 * inputs -- SPEC-001 rule 1, "same answers first" (PIPELINE.md step e).
 */
public final class HashBenchMain {
  public static void main(String[] args) {
    String[] texts = {
      "User likes tea",
      "  User likes tea ",
      "user likes tea",
      "User has a dog named Poppy",
      "User has a dog named Poppy and their morning walks together are the highlight of their day",
      "",
      "Ünïcödé and emoji 🎉 test",
    };
    for (String t : texts) {
      System.out.println(MemoryStoreState.hashOf(t));
    }
  }
}
