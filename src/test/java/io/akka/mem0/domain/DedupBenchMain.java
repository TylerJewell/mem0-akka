package io.akka.mem0.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Times {@link MemoryStoreState#accept} over a batch of candidates against a set of
 * existing memories, best-of-N -- the Java side of bench/compare_speed.py.
 *
 * <p>Args: nCandidates, nExisting, repeats. Prints the best elapsed time in milliseconds
 * on the last line of stdout.
 */
public final class DedupBenchMain {
  public static void main(String[] args) {
    int nCandidates = Integer.parseInt(args[0]);
    int nExisting = Integer.parseInt(args[1]);
    int repeats = Integer.parseInt(args[2]);

    List<Candidate> candidates = new ArrayList<>(nCandidates);
    for (int i = 0; i < nCandidates; i++) {
      candidates.add(new Candidate("User fact number " + i + " about their day", List.of()));
    }

    List<Memory> existing = new ArrayList<>(nExisting);
    Instant now = Instant.now();
    for (int i = 0; i < nExisting; i++) {
      existing.add(MemoryStoreState.toMemory(new Candidate("User fact number " + i, List.of()), now));
    }
    MemoryStoreState state = new MemoryStoreState(existing);

    double bestMs = Double.MAX_VALUE;
    for (int r = 0; r < repeats; r++) {
      long start = System.nanoTime();
      state.accept(candidates);
      long elapsed = System.nanoTime() - start;
      double ms = elapsed / 1_000_000.0;
      if (ms < bestMs) bestMs = ms;
    }

    System.out.println(bestMs);
  }
}
